package com.ssafy.ssabre.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "D-Day 항목")
public record DDayItem(
        @Schema(description = "제목", example = "취업")
        String title,

        @Schema(description = "남은 일수", example = "30")
        long days
) {
}
