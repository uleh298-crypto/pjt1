package com.ssafy.ssabre.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시판 항목")
public record BoardItem(
        @Schema(description = "게시판 ID", example = "1")
        Long boardId,

        @Schema(description = "게시판 이름", example = "자유게시판")
        String name,

        @Schema(description = "최근 게시글 제목", example = "강사님 잘 지내세요...?")
        String recentPostTitle
) {
}
