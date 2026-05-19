package com.ssafy.ssabre.notification.event;

import com.ssafy.ssabre.chat.service.ChatPresenceService;
import com.ssafy.ssabre.notification.dto.FCMDataType;
import com.ssafy.ssabre.notification.entity.NotificationType;
import com.ssafy.ssabre.notification.repository.NotificationSettingRepository;
import com.ssafy.ssabre.notification.service.FCMService;
import com.ssafy.ssabre.notification.service.NotificationTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * FCM Data 메시지 이벤트 리스너
 * 트랜잭션 커밋 후에 FCM 발송을 수행하여 데이터 정합성 보장
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FCMEventListener {

    private final FCMService fcmService;
    private final NotificationTokenService tokenService;
    private final NotificationSettingRepository settingRepository;
    private final StringRedisTemplate redisTemplate;
    private final ChatPresenceService chatPresenceService;

    private static final String SETTING_CACHE_KEY_PREFIX = "fcm:setting:";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    /**
     * 채팅 메시지 생성 후 FCM Data 메시지 발송
     * 단, 수신자가 해당 채팅방에 웹소켓으로 접속 중이면 발송하지 않음
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatEvent(FCMChatEvent event) {
        // 수신자가 해당 채팅방에 접속 중이면 FCM 발송 스킵
        if (chatPresenceService.isInRoom(event.receiverMemberId(), event.roomId())) {
            log.debug("Skipping FCM: member {} is currently in chat room {}",
                    event.receiverMemberId(), event.roomId());
            return;
        }

        if (!isNotificationEnabled(event.receiverMemberId(), NotificationType.MESSAGE)) {
            log.debug("FCM chat notification disabled for member: {}", event.receiverMemberId());
            return;
        }

        String token = tokenService.getToken(event.receiverMemberId());
        if (token == null) {
            log.debug("FCM token not found for member: {}", event.receiverMemberId());
            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("roomId", String.valueOf(event.roomId()));
        data.put("sentAt", ISO_FORMATTER.format(event.sentAt()));

        if (event.preview() != null) {
            data.put("preview", event.preview());
        }
        if (event.senderName() != null) {
            data.put("senderName", event.senderName());
        }

        fcmService.sendDataMessage(token, FCMDataType.CHAT_NEW, data);
    }

    /**
     * 댓글/대댓글 생성 후 FCM Data 메시지 발송
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentEvent(FCMCommentEvent event) {
        NotificationType notificationType = event.type() == FCMDataType.POST_COMMENT
                ? NotificationType.COMMENT
                : NotificationType.REPLY;

        if (!isNotificationEnabled(event.receiverMemberId(), notificationType)) {
            log.debug("FCM comment notification disabled for member: {}", event.receiverMemberId());
            return;
        }

        String token = tokenService.getToken(event.receiverMemberId());
        if (token == null) {
            log.debug("FCM token not found for member: {}", event.receiverMemberId());
            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("postId", String.valueOf(event.postId()));
        data.put("sentAt", ISO_FORMATTER.format(event.sentAt()));

        if (event.commentId() != null) {
            data.put("commentId", String.valueOf(event.commentId()));
        }

        fcmService.sendDataMessage(token, event.type(), data);
    }

    /**
     * 팀/스터디 지원 관련 FCM Data 메시지 발송
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApplicationEvent(FCMApplicationEvent event) {
        if (!isNotificationEnabled(event.receiverMemberId(), NotificationType.APPLICATION)) {
            log.debug("FCM application notification disabled for member: {}", event.receiverMemberId());
            return;
        }

        String token = tokenService.getToken(event.receiverMemberId());
        if (token == null) {
            log.debug("FCM token not found for member: {}", event.receiverMemberId());
            return;
        }

        Map<String, String> data = new HashMap<>();
        data.put("groupType", event.groupType());
        data.put("groupId", String.valueOf(event.groupId()));
        data.put("groupTitle", event.groupTitle());
        data.put("sentAt", ISO_FORMATTER.format(event.sentAt()));

        fcmService.sendDataMessage(token, event.type(), data);
    }

    /**
     * 알림 설정 확인 (Cache-Aside Pattern)
     */
    private boolean isNotificationEnabled(Long memberId, NotificationType type) {
        String key = SETTING_CACHE_KEY_PREFIX + memberId;
        Object cachedValue = redisTemplate.opsForHash().get(key, type.name());

        if (cachedValue != null) {
            return Boolean.parseBoolean(cachedValue.toString());
        }

        // Cache Miss -> DB 조회
        var setting = settingRepository.findByMemberIdAndNotificationType(memberId, type)
                .orElse(null);

        boolean enabled = (setting != null) ? setting.isEnabled() : true; // 기본값 TRUE

        // Cache Warming
        redisTemplate.opsForHash().put(key, type.name(), String.valueOf(enabled));
        redisTemplate.expire(key, Duration.ofDays(30));

        return enabled;
    }
}
