package com.ssafy.ssabre.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "대댓글 응답")
public record ReplyResponse(
        @Schema(description = "댓글 ID", example = "502")
        Long id,

        @Schema(description = "생성일시")
        LocalDateTime createdAt,

        @Schema(description = "댓글 내용")
        String content,

        @Schema(description = "좋아요 수", example = "3")
        Integer likeCount,

        @Schema(description = "좋아요 여부")
        Boolean isLiked,

        @Schema(description = "블라인드 여부")
        Boolean isBlinded,

        @Schema(description = "삭제 여부")
        Boolean isDeleted,

        @Schema(description = "익명 정보")
        AnonResponse anon
) {
}
