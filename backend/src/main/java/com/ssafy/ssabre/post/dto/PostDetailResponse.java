package com.ssafy.ssabre.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "게시글 상세 응답")
public record PostDetailResponse(
        @Schema(description = "생성일시")
        LocalDateTime createdAt,

        @Schema(description = "수정일시")
        LocalDateTime updatedAt,

        @Schema(description = "게시글 ID")
        Long id,

        @Schema(description = "게시판 ID")
        Long boardId,

        @Schema(description = "본인 작성 여부")
        Boolean isMine,

        @Schema(description = "게시글 제목")
        String title,

        @Schema(description = "게시글 내용")
        String content,

        @Schema(description = "블라인드 여부")
        Boolean isBlinded,

        @Schema(description = "이미지 URL 목록 (nullable)")
        List<String> imageUrls,

        @Schema(description = "투표 정보 (nullable)")
        PollResponse poll,

        @Schema(description = "좋아요 수")
        Integer likeCount,

        @Schema(description = "좋아요 여부")
        Boolean isLiked,

        @Schema(description = "댓글 수")
        Integer commentCount,

        @Schema(description = "스크랩 수")
        Integer scrapCount,

        @Schema(description = "스크랩 여부")
        Boolean isScraped,

        @Schema(description = "댓글 목록")
        List<CommentResponse> comments
) {
}
