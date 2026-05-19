package com.ssafy.ssabre.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ChatMessageSendRequest(
                @NotBlank(message = "메시지 내용은 필수입니다.") @Schema(description = "메세지 내용", example = "안녕하세요!") String content) {
}
