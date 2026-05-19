package com.ssafy.ssabre.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "썸네일 항목")
public record ThumbnailItem(
        @Schema(description = "이름", example = "신한 해커톤 프론트엔드 모집")
        String name,

        @Schema(description = "개수", example = "1")
        int count
) {
}
