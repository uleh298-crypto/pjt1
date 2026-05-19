package com.ssafy.ssabre.notification.event;

import com.ssafy.ssabre.notification.dto.FCMDataType;

import java.time.Instant;

/**
 * 댓글/대댓글 생성 시 FCM Data 메시지 발송을 위한 이벤트
 */
public record FCMCommentEvent(
        Long receiverMemberId,
        FCMDataType type,  // POST_COMMENT or COMMENT_REPLY
        Long postId,
        Long commentId,
        Instant sentAt
) {
}
