package com.ssafy.ssabre.study.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudyApplyRequest(
                @NotNull(message = "포트폴리오 ID는 필수입니다.") @Schema(description = "제출할 포트폴리오 ID", example = "1") Long portfolioId,

                @NotBlank(message = "제목은 필수입니다.") @Schema(description = "지원 제목", example = "지원합니다.") String title,

                @Schema(description = "지원 메시지", example = "열심히 하겠습니다.") String message,

                @Schema(description = "지원 포지션", example = "BE") String position) {
}
