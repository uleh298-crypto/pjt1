package com.ssafy.ssabre.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ChatRoomCreateRequest(
        @NotNull(message = "게시글 ID는 필수입니다.")
        @Schema(description = "채팅을 시작할 게시글 ID", example = "1")
        Long postId
) {
}
