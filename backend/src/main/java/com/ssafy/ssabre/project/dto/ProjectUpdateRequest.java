package com.ssafy.ssabre.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ProjectUpdateRequest(
    @NotBlank(message = "제목은 필수입니다.")
    @Schema(description = "프로젝트 제목", example = "유기동물 입양 독려를 위한 플랫폼 제작")
    String title,

    @Schema(description = "프로젝트 소개", example = "운동 루틴을 공유하고 챌린지 수행하는 앱 프로젝트")
    String introduction,

    @Schema(description = "프로젝트 설명", example = "핵심 컨셉: 운동 루틴들 /n Python 모델 설계: 이런저런 모델 사용/n")
    String description,

    @Schema(description = "기술 스택 목록", example = "[\"python\", \"kotlin\", \"docker\"]")
    List<String> techStacks,

    @Schema(description = "관련 URL 목록", example = "[\"github.com/simonjiho/recoraddic\", \"notion.so/asdf/asdfasad\"]")
    List<String> urls,

    @Schema(description = "이미지 URL 목록", example = "[\"https://your-domain.com/static/uploads/project/2026/01/abcd1234.jpg\"]")
    List<String> imageUrls
) { }
