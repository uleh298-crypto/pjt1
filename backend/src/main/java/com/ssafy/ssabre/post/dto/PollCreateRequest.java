package com.ssafy.ssabre.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "투표 생성 요청")
public record PollCreateRequest(
        @NotBlank(message = "투표 제목은 필수입니다.")
        @Schema(description = "투표 제목", example = "점심 메뉴 투표")
        String title,

        @NotEmpty(message = "투표 옵션은 최소 2개 이상이어야 합니다.")
        @Size(min = 2, max = 10, message = "투표 옵션은 2개 이상 10개 이하여야 합니다.")
        @Schema(description = "투표 옵션 목록", example = "[\"짜장면\", \"짬뽕\", \"탕수육\"]")
        List<String> options
) {
}
