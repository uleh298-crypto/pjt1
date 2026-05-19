package com.ssafy.ssabre.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "신고 요청")
public record ReportCreateRequest(
        @NotBlank(message = "신고 대상 타입은 필수입니다.")
        @Schema(description = "신고 대상 타입", example = "POST", allowableValues = {"POST", "COMMENT"})
        String targetType,

        @NotNull(message = "신고 대상 ID는 필수입니다.")
        @Schema(description = "신고 대상 ID", example = "1")
        Long targetId,

        @NotBlank(message = "신고 사유는 필수입니다.")
        @Schema(description = "신고 사유", example = "ABUSE", allowableValues = {"ABUSE", "SPAM", "INAPPROPRIATE", "OTHER"})
        String reason,

        @Schema(description = "상세 내용", example = "욕설이 포함되어 있습니다")
        String detail
) {
}
