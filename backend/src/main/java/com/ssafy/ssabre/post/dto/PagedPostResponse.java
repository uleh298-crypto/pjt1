package com.ssafy.ssabre.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "커서 기반 페이지네이션 게시글 응답")
public record PagedPostResponse(
        @Schema(description = "게시글 목록")
        List<PostResponse> posts,

        @Schema(description = "다음 페이지 커서 (다음 요청 시 cursor 파라미터로 전달)", example = "2026-01-31T10:20:30_123")
        String nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
}
