package com.ssafy.ssabre.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "채팅방 목록 실시간 업데이트 응답")
public record ChatListUpdateResponse(
        @Schema(description = "채팅방 ID")
        Long roomId,

        @Schema(description = "마지막 메시지 내용")
        String lastMessage,

        @Schema(description = "마지막 메시지 시간")
        LocalDateTime lastMessageAt
) {
}
