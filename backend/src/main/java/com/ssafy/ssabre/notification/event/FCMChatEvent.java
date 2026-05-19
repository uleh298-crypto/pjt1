package com.ssafy.ssabre.notification.event;

import java.time.Instant;

/**
 * 채팅 메시지 생성 시 FCM Data 메시지 발송을 위한 이벤트
 */
public record FCMChatEvent(
        Long receiverMemberId,
        Long roomId,
        Instant sentAt,
        String preview,
        String senderName
) {
}
