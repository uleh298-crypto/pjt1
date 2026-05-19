package com.ssafy.ssabre.auth.service;

import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MattermostAuthService {

    @Value("${mattermost.webhook_14.url}")
    private String webhook14Url;

    @Value("${mattermost.webhook_15.url}")
    private String webhook15Url;

    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentMattermostLoader studentMattermostLoader;

    private static final SecureRandom random = new SecureRandom();
    private static final String CHARACTERS = "0123456789";

    @org.springframework.scheduling.annotation.Async
    public void sendAuthMessage(String targetUsername, Integer generation, String name) {
        // 학생 데이터 매칭 검증
        if (!validateStudentMattermost(generation, targetUsername, name)) {
            throw new IllegalArgumentException("학생 정보가 일치하지 않습니다. 기수, 이름, Mattermost ID를 확인해주세요.");
        }

        // 인증 코드 생성 (6자리)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        String authCode = sb.toString();

        // generation에 따른 webhook URL 선택
        String webhookUrl = getWebhookUrl(generation);

        // Mattermost 메시지 전송 로직
        Map<String, Object> payload = new HashMap<>();
        payload.put("channel", "@" + targetUsername);
        payload.put("username", "싸브리타임 인증 봇");
        String message = """
                ---
                ### 🔐 싸브리타임 인증코드
                | 항목 | 내용 |
                |:---:|:---:|
                | 📬 **인증코드** | **`%s`** |
                | ⏱️ **유효시간** | 3분 |
                | 📞 **문의** | 서비스 내 문의사항 기능을 이용해주세요. |
                ---
                """.formatted(authCode);
        payload.put("text", message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForEntity(webhookUrl, entity, String.class);

            redisTemplate.opsForValue().set("AUTH:" + targetUsername, authCode, Duration.ofMinutes(3));
            log.info("✅ Mattermost 발송 성공: {} (기수: {})", targetUsername, generation);

        } catch (Exception e) {
            log.error("❌ Mattermost 발송 실패", e);
            throw new RuntimeException("메시지 발송 실패");
        }
    }

    private String getWebhookUrl(Integer generation) {
        if (generation == null) {
            throw new IllegalArgumentException("기수 정보가 필요합니다.");
        }
        return switch (generation) {
            case 14 -> webhook14Url;
            case 15 -> webhook15Url;
            default -> throw new IllegalArgumentException("지원하지 않는 기수입니다: " + generation);
        };
    }

    public boolean validateStudentMattermost(Integer generation, String mattermostId, String name) {
        return studentMattermostLoader.validate(generation, name, mattermostId);
    }

    public boolean verifyCode(String targetUsername, String inputCode) {
        String redisCode = redisTemplate.opsForValue().get("AUTH:" + targetUsername);

        if (redisCode != null && redisCode.equals(inputCode)) {
            // 인증 성공 시: "AUTH:..." 삭제 후 "VERIFIED:..." 저장 (회원가입 진행용, 30분 유효)
            redisTemplate.delete("AUTH:" + targetUsername);
            redisTemplate.opsForValue().set("VERIFIED:" + targetUsername, "true", Duration.ofMinutes(30));
            return true;
        }
        return false;
    }

    public boolean isVerified(String targetUsername) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("VERIFIED:" + targetUsername));
    }

    public Optional<String> findEmailByMattermostId(String mattermostId) {
        return memberRepository.findByMattermostId(mattermostId)
                .map(Member::getEmail);
    }

    @Transactional
    public boolean resetPassword(String mattermostId, String newPassword) {
        // Mattermost 인증 여부 확인
        if (!isVerified(mattermostId)) {
            throw new IllegalArgumentException("Mattermost 인증이 완료되지 않았습니다. 인증을 먼저 진행해주세요.");
        }

        Optional<Member> memberOpt = memberRepository.findByMattermostId(mattermostId);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            String encodedPassword = passwordEncoder.encode(newPassword);
            member.updatePassword(encodedPassword);

            // 인증 정보 삭제 (재사용 방지)
            redisTemplate.delete("VERIFIED:" + mattermostId);
            return true;
        }
        return false;
    }
}
