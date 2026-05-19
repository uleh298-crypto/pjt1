package com.ssafy.ssabre.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "홈 화면 응답")
public record HomeResponse(
        @Schema(description = "D-Day 목록")
        List<DDayItem> dDays,

        @Schema(description = "팀 썸네일")
        ThumbnailItem teamThumbnail,

        @Schema(description = "스터디 썸네일")
        ThumbnailItem studyThumbnail,

        @Schema(description = "캠퍼스별 학식 이미지 목록")
        List<CampusMealItem> campusMeals,

        @Schema(description = "게시판 목록")
        List<BoardItem> boardsList
) {
}
