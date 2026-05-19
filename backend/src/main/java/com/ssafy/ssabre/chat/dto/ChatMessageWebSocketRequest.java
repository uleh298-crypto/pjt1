package com.ssafy.ssabre.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "WebSocket 채팅 메시지 요청")
public record ChatMessageWebSocketRequest(
        @Schema(description = "메시지 내용", example = "안녕하세요!")
        String content
) {
}
