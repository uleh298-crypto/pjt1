package com.ssafy.ssabre.notification.event;

import com.ssafy.ssabre.notification.dto.FCMDataType;

import java.time.Instant;

/**
 * 팀/스터디 지원 관련 FCM Data 메시지 발송을 위한 이벤트
 */
public record FCMApplicationEvent(
        Long receiverMemberId,
        FCMDataType type,  // APPLICATION_NEW, APPLICATION_ACCEPTED, APPLICATION_REJECTED
        String groupType,  // "TEAM" or "STUDY"
        Long groupId,
        String groupTitle,
        Instant sentAt
) {
}
