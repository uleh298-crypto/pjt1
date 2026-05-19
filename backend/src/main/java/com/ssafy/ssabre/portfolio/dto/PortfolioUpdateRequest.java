package com.ssafy.ssabre.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record PortfolioUpdateRequest(
        @Schema(description = "포트폴리오 제목", example = "수정된 포트폴리오") String title,

        @Schema(description = "포트폴리오 설명", example = "수정된 설명") String description,

        @Schema(description = "자기소개", example = "수정된 자기소개") String introduction,

        @Schema(description = "백준 아이디", example = "koosaga") String bojHandle,

        @Schema(description = "SW 역량테스트 등급", example = "IM") String swTestRank,

        @Schema(description = "공개 여부", example = "false") Boolean isVisible,

        @Schema(description = "기술 스택 목록 (전체 교체)") List<PortfolioCreateRequest.PortfolioStackDto> stacks,

        @Schema(description = "관련 링크 목록 (전체 교체)") List<PortfolioCreateRequest.PortfolioUrlDto> urls,

        @Schema(description = "이미지 목록 (전체 교체)") List<PortfolioCreateRequest.PortfolioImageDto> images) {
}
