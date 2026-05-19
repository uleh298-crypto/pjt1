package com.ssafy.ssabre.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record BoardCreateRequest(
        @NotBlank(message = "게시판 이름은 필수입니다.") @Schema(description = "게시판 이름", example = "자유게시판") String name,

        @Schema(description = "게시판 카테고리", example = "GENERAL") String category,

        @Schema(description = "게시판 설명", example = "자유롭게 이야기를 나누는 공간입니다.") String description) {
}
