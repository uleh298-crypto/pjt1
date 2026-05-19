package com.ssafy.ssabre.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "성공 응답")
public record SuccessResponse(
        @Schema(description = "성공 여부", example = "true")
        boolean success
) {
    public static SuccessResponse ok() {
        return new SuccessResponse(true);
    }
}
