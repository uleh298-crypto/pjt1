package com.ssafy.ssabre.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PostCreateRequest(
                @NotBlank(message = "제목은 필수입니다.") @Schema(description = "게시글 제목", example = "게시글 제목입니다.") String title,

                @NotBlank(message = "내용은 필수입니다.") @Schema(description = "게시글 내용", example = "게시글 내용입니다.") String content,

                @NotNull(message = "게시판 ID는 필수입니다.") @Schema(description = "게시판 ID", example = "1") Long boardId,

                @Schema(description = "이미지 URL 목록 (temp 폴더에 업로드된 URL)", example = "[\"https://domain.com/uploads/temp/a1b2c3d4.jpg\"]") List<String> imageUrls,

                @Valid @Schema(description = "투표 정보 (선택사항)") PollCreateRequest poll) {
}
