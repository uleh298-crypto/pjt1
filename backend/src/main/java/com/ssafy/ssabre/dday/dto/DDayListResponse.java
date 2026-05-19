package com.ssafy.ssabre.dday.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "D-Day 목록 응답")
public record DDayListResponse(
        @Schema(description = "D-Day 목록")
        List<DDayResponse> items
) {
    public static DDayListResponse of(List<DDayResponse> items) {
        return new DDayListResponse(items);
    }
}
