package com.ssafy.ssabre.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProjectResponse(
    @Schema(description = "성공 여부", example = "true")
    boolean success
) {
    public static ProjectResponse of() {
        return new ProjectResponse(true);
    }
}
