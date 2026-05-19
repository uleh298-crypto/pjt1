package com.ssafy.ssabre.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "투표 요청")
public record VoteRequest(
        @NotNull(message = "옵션 ID는 필수입니다.")
        @Schema(description = "선택한 옵션 ID", example = "2")
        Long optionId
) {
}
