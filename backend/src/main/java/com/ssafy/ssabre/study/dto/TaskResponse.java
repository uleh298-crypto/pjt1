package com.ssafy.ssabre.study.dto;

import com.ssafy.ssabre.global.entity.TaskStatus;
import com.ssafy.ssabre.study.entity.StudyTask;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "스터디 업무 응답")
public record TaskResponse(
        @Schema(description = "업무 ID", example = "1")
        Long id,

        @Schema(description = "스터디 ID", example = "1")
        Long studyId,

        @Schema(description = "업무 제목", example = "알고리즘 문제 풀이")
        String title,

        @Schema(description = "업무 내용", example = "백준 1000번 풀기")
        String content,

        @Schema(description = "시작일", example = "2024-02-01")
        LocalDate startDate,

        @Schema(description = "종료일", example = "2024-02-05")
        LocalDate endDate,

        @Schema(description = "진행 상태 (TODO, IN_PROGRESS, DONE)", example = "IN_PROGRESS")
        TaskStatus status,

        @Schema(description = "생성자 ID", example = "1")
        Long creatorId,

        @Schema(description = "생성일시")
        LocalDateTime createdAt,

        @Schema(description = "수정일시")
        LocalDateTime updatedAt
) {
    public static TaskResponse from(StudyTask task) {
        return new TaskResponse(
                task.getId(),
                task.getStudy().getId(),
                task.getTitle(),
                task.getContent(),
                task.getStartDate(),
                task.getEndDate(),
                task.getStatus(),
                task.getCreator() != null ? task.getCreator().getId() : null,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
