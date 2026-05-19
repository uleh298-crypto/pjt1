package com.ssafy.ssabre.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostUpdateRequest(
        @Schema(description = "게시글 제목", example = "제목 수정") String title,

        @Schema(description = "게시글 내용", example = "내용 수정") String content) {
}
