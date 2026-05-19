package com.ssafy.ssabre.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "캠퍼스별 학식 이미지")
public record CampusMealItem(
        @Schema(description = "캠퍼스 ID", example = "1")
        Long campusId,

        @Schema(description = "캠퍼스 이름", example = "서울")
        String campusName,

        @Schema(description = "학식 이미지 URL 목록")
        List<String> imageUrls
) {
}
