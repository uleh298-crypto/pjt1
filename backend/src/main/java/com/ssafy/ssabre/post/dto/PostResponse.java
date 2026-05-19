package com.ssafy.ssabre.post.dto;

import com.ssafy.ssabre.post.entity.Post;
import com.ssafy.ssabre.post.entity.PostImage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
                @Schema(description = "게시글 ID") Long id,
                @Schema(description = "게시판 ID") Long boardId,
                @Schema(description = "게시판 이름") String boardName,
                @Schema(description = "본인 작성 여부") Boolean isMine,
                @Schema(description = "게시글 제목") String title,
                @Schema(description = "게시글 내용") String content,
                @Schema(description = "조회수") Integer viewCount,
                @Schema(description = "좋아요 수") Integer likeCount,
                @Schema(description = "댓글 수") Integer commentCount,
                @Schema(description = "생성일") LocalDateTime createdAt,
                @Schema(description = "수정일") LocalDateTime updatedAt,
                @Schema(description = "이미지 목록") List<String> imageUrls,
                @Schema(description = "블라인드 여부") Boolean isBlinded) {
        public static PostResponse from(Post post, List<PostImage> images, Long currentMemberId) {
                List<String> imageUrls = images.stream()
                                .map(PostImage::getImageUrl)
                                .toList();

                boolean isMine = currentMemberId != null && post.getMember() != null && post.getMember().getId().equals(currentMemberId);

                return new PostResponse(
                                post.getId(),
                                post.getBoard().getId(),
                                post.getBoard().getName(),
                                isMine,
                                post.getTitle(),
                                post.getContent(),
                                post.getViewCount(),
                                post.getLikeCount(),
                                post.getCommentCount(),
                                post.getCreatedAt(),
                                post.getUpdatedAt(),
                                imageUrls,
                                post.getIsBlinded());
        }

        /**
         * currentMemberId 없이 호출 시 isMine = false
         */
        public static PostResponse from(Post post, List<PostImage> images) {
                return from(post, images, null);
        }
}
