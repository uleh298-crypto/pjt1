package com.ssafy.ssabre.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "신고 생성 응답")
public record ReportCreateResponse(
        @Schema(description = "신고 ID", example = "9001")
        Long reportId,

        @Schema(description = "생성일시")
        LocalDateTime createdAt
) {
}
