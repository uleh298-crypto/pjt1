package com.ssafy.ssabre.study.dto;

import com.ssafy.ssabre.study.entity.StudyType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record StudyUpdateRequest(
                @Schema(description = "스터디 이름", example = "알고리즘 스터디 (수정)") String title,

                @Schema(description = "스터디 종류", example = "ALGORITHM") StudyType type,

                @Schema(description = "모집 인원", example = "5") Integer capacity,

                @Schema(description = "시작일", example = "2024-02-01") LocalDate startDate,

                @Schema(description = "종료일", example = "2024-03-01") LocalDate endDate,

                @Schema(description = "캠퍼스 ID", example = "1") Long campusId,

                @Schema(description = "스터디 설명", example = "수정된 설명입니다.") String description,

                @Schema(description = "모집 상태 (open, ongoing, closed)", example = "closed") String status) {
}
