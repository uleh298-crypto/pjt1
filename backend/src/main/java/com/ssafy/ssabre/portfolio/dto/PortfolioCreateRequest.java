package com.ssafy.ssabre.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record PortfolioCreateRequest(
        @NotBlank(message = "제목은 필수입니다.") @Schema(description = "포트폴리오 제목", example = "백엔드 개발자 포트폴리오") String title,

        @Schema(description = "포트폴리오 설명", example = "열심히 하겠습니다.") String description,

        @Schema(description = "자기소개", example = "안녕하세요.") String introduction,

        @Schema(description = "백준 아이디", example = "koosaga") String bojHandle,

        @Schema(description = "SW 역량테스트 등급", example = "A+") String swTestRank,

        @Schema(description = "공개 여부", example = "true") Boolean isVisible,

        @Schema(description = "기술 스택 목록") List<PortfolioStackDto> stacks,

        @Schema(description = "관련 링크 목록") List<PortfolioUrlDto> urls,

        @Schema(description = "이미지 목록") List<PortfolioImageDto> images) {
    public record PortfolioStackDto(
            @Schema(description = "스택 ID", example = "1") Long stackId,

            @Schema(description = "숙련도 (high, mid, low)", example = "mid") String expertLevel) {
    }

    public record PortfolioUrlDto(
            @Schema(description = "링크 URL", example = "https://github.com") String url) {
    }

    public record PortfolioImageDto(
            @Schema(description = "이미지 URL", example = "https://example.com/image.jpg") String imageUrl,

            @Schema(description = "순서", example = "1") Integer orders) {
    }
}
