package com.ssafy.ssabre.notification.service;

import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.MemberRepository;
import com.ssafy.ssabre.notification.entity.Notification;
import com.ssafy.ssabre.notification.entity.NotificationSetting;
import com.ssafy.ssabre.notification.entity.NotificationType;
import com.ssafy.ssabre.notification.repository.NotificationRepository;
import com.ssafy.ssabre.notification.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository settingRepository;
    private final MemberRepository memberRepository;
    private final NotificationTokenService tokenService;
    private final StringRedisTemplate redisTemplate;

    private static final String SETTING_CACHE_KEY_PREFIX = "fcm:setting:";

    /**
     * 알림 생성 (DB 저장만 수행)
     * FCM 발송은 FCMEventListener에서 처리
     *
     * @param receiver   알림 받을 회원
     * @param type       알림 타입
     * @param title      제목 (게시글 제목, 채팅방 이름 등)
     * @param content    알림 내용
     * @param relatedUrl 관련 URL
     */
    @Transactional
    public Long send(Member receiver, NotificationType type, String title, String content, String relatedUrl) {
        // DB 저장만 수행 (FCM 발송은 FCMEventListener에서 처리)
        Notification notification = Notification.builder()
                .member(receiver)
                .type(type)
                .content(content)
                .relatedUrl(relatedUrl)
                .build();
        Notification saved = notificationRepository.save(notification);

        return saved.getId();
    }

    // overloading for convenience (memberId 버전)
    @Transactional
    public Long send(Long memberId, NotificationType type, String title, String content, String relatedUrl) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        return send(member, type, title, content, relatedUrl);
    }

    /**
     * 알림 설정 변경
     */
    @Transactional
    public void toggleNotificationSetting(Long memberId, NotificationType type, boolean enabled) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        NotificationSetting setting = settingRepository.findByMemberIdAndNotificationType(memberId, type)
                .orElseGet(() -> {
                    // Create new setting if not exists
                    NotificationSetting newSetting = NotificationSetting.builder()
                            .member(member)
                            .notificationType(type)
                            .enabled(enabled)
                            .build();
                    return settingRepository.save(newSetting);
                });

        // Update existing setting
        if (setting.getId() != null && settingRepository.existsById(setting.getId())) {
            setting.update(enabled);
        }

        // Cache Update (Write-Through)
        String key = SETTING_CACHE_KEY_PREFIX + memberId;
        redisTemplate.opsForHash().put(key, type.name(), String.valueOf(enabled));
    }

    /**
     * FCM 토큰 저장
     */
    @Transactional
    public void registerToken(Long memberId, String token) {
        tokenService.saveToken(memberId, token);
    }

    public List<Notification> getMyNotifications(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        return notificationRepository.findByMemberIdOrderByCreatedAtDesc(member.getId());
    }

    @Transactional
    public void readNotification(Long notificationId, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        // 본인 알림인지 검증
        if (!notification.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("본인의 알림만 읽음 처리할 수 있습니다.");
        }

        notification.markAsRead();
    }
}
