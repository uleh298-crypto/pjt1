package com.ssafy.ssabre.project.dto;

import com.ssafy.ssabre.project.entity.Project;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectListResponse(
    @Schema(description = "프로젝트 목록")
    List<ProjectItem> projects
) {
    public static ProjectListResponse from(List<Project> projects) {
        List<ProjectItem> items = projects.stream()
                .map(ProjectItem::from)
                .toList();
        return new ProjectListResponse(items);
    }

    public record ProjectItem(
        @Schema(description = "프로젝트 ID", example = "1")
        Long id,

        @Schema(description = "프로젝트 제목", example = "유기동물 입양 독려를 위한 플랫폼 제작")
        String title,

        @Schema(description = "프로젝트 소개", example = "운동 루틴을 공유하고 챌린지 수행하는 앱 프로젝트")
        String introduction,

        @Schema(description = "프로젝트 설명", example = "핵심 컨셉: 운동 루틴들...")
        String description,

        @Schema(description = "기술 스택 목록")
        List<String> techStacks,

        @Schema(description = "관련 URL 목록")
        List<String> urls,

        @Schema(description = "이미지 URL 목록")
        List<String> imageUrls,

        @Schema(description = "생성일시")
        LocalDateTime createdAt,

        @Schema(description = "수정일시")
        LocalDateTime updatedAt
    ) {
        public static ProjectItem from(Project project) {
            return new ProjectItem(
                    project.getId(),
                    project.getTitle(),
                    project.getIntroduction(),
                    project.getDescription(),
                    project.getTechStacks() != null ? List.copyOf(project.getTechStacks()) : List.of(),
                    project.getUrls() != null ? List.copyOf(project.getUrls()) : List.of(),
                    project.getImageUrls() != null ? List.copyOf(project.getImageUrls()) : List.of(),
                    project.getCreatedAt(),
                    project.getUpdatedAt()
            );
        }
    }
}
