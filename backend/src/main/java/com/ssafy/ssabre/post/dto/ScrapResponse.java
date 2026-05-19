package com.ssafy.ssabre.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스크랩 응답")
public record ScrapResponse(
        @Schema(description = "성공 여부", example = "true")
        Boolean success
) {
}
