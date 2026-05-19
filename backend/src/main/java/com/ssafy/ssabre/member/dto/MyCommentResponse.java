package com.ssafy.ssabre.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "내가 작성한 댓글 응답")
public record MyCommentResponse(
        @Schema(description = "댓글 ID")
        Long id,

        @Schema(description = "댓글 내용")
        String content,

        @Schema(description = "생성일시")
        LocalDateTime createdAt,

        @Schema(description = "대댓글 여부")
        Boolean isReply,

        @Schema(description = "게시글 ID")
        Long postId,

        @Schema(description = "게시글 제목")
        String postTitle,

        @Schema(description = "게시판 ID")
        Long boardId,

        @Schema(description = "게시판 이름")
        String boardName
) {
}
