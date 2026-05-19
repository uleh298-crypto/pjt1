package com.ssafy.ssabre.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 좋아요 응답")
public record CommentLikeResponse(
        @Schema(description = "좋아요 여부", example = "true")
        Boolean liked,

        @Schema(description = "좋아요 수", example = "5")
        Integer likeCount
) {
}
