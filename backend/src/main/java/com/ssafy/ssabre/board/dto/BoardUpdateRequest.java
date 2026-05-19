package com.ssafy.ssabre.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record BoardUpdateRequest(
        @Schema(description = "게시판 이름", example = "자유게시판 (수정)") String name,

        @Schema(description = "게시판 설명", example = "수정된 설명입니다.") String description) {
}
