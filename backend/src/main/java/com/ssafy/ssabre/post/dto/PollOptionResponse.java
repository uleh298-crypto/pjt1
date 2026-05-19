package com.ssafy.ssabre.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "투표 옵션 응답")
public record PollOptionResponse(
        @Schema(description = "옵션 ID", example = "1")
        Long optionId,

        @Schema(description = "옵션 텍스트", example = "옵션1")
        String text,

        @Schema(description = "투표 수", example = "1")
        Integer voteCount
) {
}
