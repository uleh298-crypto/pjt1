package com.ssafy.ssabre.dday.dto;

import com.ssafy.ssabre.member.entity.DDay;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "D-Day 응답")
public record DDayResponse(
        @Schema(description = "D-Day ID", example = "301")
        Long ddayId,

        @Schema(description = "제목", example = "중간고사 시작")
        String title,

        @Schema(description = "목표 날짜", example = "2023-10-25")
        LocalDate date,

        @Schema(description = "아이콘", example = "CAP")
        String icon
) {
    public static DDayResponse from(DDay dDay) {
        return new DDayResponse(
                dDay.getId(),
                dDay.getTitle(),
                dDay.getTargetDate(),
                dDay.getIcon()
        );
    }
}
