package com.ssafy.ssabre.study.dto;

import com.ssafy.ssabre.study.entity.StudyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record StudyCreateRequest(
                @NotBlank(message = "제목은 필수입니다.") @Schema(description = "스터디 이름", example = "알고리즘 스터디") String title,

                @NotNull(message = "종류는 필수입니다.") @Schema(description = "스터디 종류 (CS, ALGORITHM, SW_TEST_A, SW_TEST_B, CERTIFICATION, ETC)", example = "ALGORITHM") StudyType type,

                @NotNull(message = "인원은 필수입니다.") @Schema(description = "모집 인원", example = "4") Integer capacity,

                @NotNull(message = "시작일은 필수입니다.") @Schema(description = "스터디 시작일", example = "2024-02-01") LocalDate startDate,

                @NotNull(message = "종료일은 필수입니다.") @Schema(description = "스터디 종료일", example = "2024-03-01") LocalDate endDate,

                @NotNull(message = "캠퍼스 ID는 필수입니다.") @Schema(description = "캠퍼스 ID", example = "1") Long campusId,

                @Schema(description = "스터디 설명", example = "매주 알고리즘 문제 풀이 진행합니다.") String description) {
}
