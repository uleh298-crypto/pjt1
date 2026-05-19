package com.ssafy.ssabre.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CommentUpdateRequest(
        @Schema(description = "댓글 내용", example = "댓글 수정") String content) {
}
