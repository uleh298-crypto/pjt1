package com.ssafy.ssabre.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "채팅 메시지 응답")
public record ChatMessageResponse(
        @Schema(description = "메시지 ID", example = "1")
        Long messageId,

        @Schema(description = "본인 메시지 여부")
        Boolean isMine,

        @Schema(description = "발신자 익명 이름", example = "싸용자123")
        String senderName,

        @Schema(description = "메시지 내용", example = "안녕하세요!")
        String content,

        @Schema(description = "전송 시간")
        LocalDateTime sentAt
) {
}
