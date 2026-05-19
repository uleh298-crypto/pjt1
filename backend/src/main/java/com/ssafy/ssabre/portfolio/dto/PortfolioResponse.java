package com.ssafy.ssabre.portfolio.dto;

import com.ssafy.ssabre.portfolio.entity.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record PortfolioResponse(
        @Schema(description = "포트폴리오 ID") Long id,

        @Schema(description = "작성자 ID") Long memberId,

        @Schema(description = "작성자 이름") String memberName,

        @Schema(description = "제목") String title,

        @Schema(description = "설명") String description,

        @Schema(description = "자기소개") String introduction,

        @Schema(description = "백준 아이디") String bojHandle,

        @Schema(description = "solved.ac 정보 (실시간)") SolvedAcInfo solvedAcInfo,

        @Schema(description = "SW 역량테스트 등급") String swTestRank,

        @Schema(description = "공개 여부") Boolean isVisible,

        @Schema(description = "생성일") LocalDateTime createdAt,

        @Schema(description = "수정일") LocalDateTime updatedAt,

        @Schema(description = "기술 스택 목록") List<StackResponse> stacks,

        @Schema(description = "관련 링크 목록") List<UrlResponse> urls,

        @Schema(description = "이미지 목록") List<ImageResponse> images) {
    public static PortfolioResponse from(Portfolio portfolio, List<PortfolioStack> stacks, List<PortfolioUrl> urls,
            List<PortfolioImage> images, SolvedAcInfo solvedAcInfo) {
        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getMember().getId(),
                portfolio.getMember().getName(),
                portfolio.getTitle(),
                portfolio.getDescription(),
                portfolio.getIntroduction(),
                portfolio.getBojHandle(),
                solvedAcInfo,
                portfolio.getSwTestRank(),
                portfolio.getIsVisible(),
                portfolio.getCreatedAt(),
                portfolio.getUpdatedAt(),
                stacks.stream().map(StackResponse::from).collect(Collectors.toList()),
                urls.stream().map(UrlResponse::from).collect(Collectors.toList()),
                images.stream().map(ImageResponse::from).collect(Collectors.toList()));
    }

    public record SolvedAcInfo(
            @Schema(description = "티어 숫자 (0-31)") Integer tier,
            @Schema(description = "티어 이름") String tierName,
            @Schema(description = "티어 이미지 URL") String tierImageUrl,
            @Schema(description = "레이팅") Integer rating,
            @Schema(description = "푼 문제 수") Integer solvedCount,
            @Schema(description = "랭킹") Integer rank
    ) {
        public static SolvedAcInfo from(SolvedAcUserResponse response) {
            if (response == null) return null;
            return new SolvedAcInfo(
                    response.tier(),
                    response.getTierName(),
                    response.getTierImageUrl(),
                    response.rating(),
                    response.solvedCount(),
                    response.rank()
            );
        }
    }

    public record StackResponse(
            Long id,
            Long stackId,
            String stackName,
            String stackImgUrl,
            String expertLevel) {
        public static StackResponse from(PortfolioStack ps) {
            return new StackResponse(
                    ps.getId(),
                    ps.getStack().getId(),
                    ps.getStack().getName(),
                    ps.getStack().getImgUrl(),
                    ps.getExpertLevel());
        }
    }

    public record UrlResponse(
            Long id,
            String url) {
        public static UrlResponse from(PortfolioUrl pu) {
            return new UrlResponse(pu.getId(), pu.getUrl());
        }
    }

    public record ImageResponse(
            Long id,
            String imageUrl,
            Integer orders) {
        public static ImageResponse from(PortfolioImage pi) {
            return new ImageResponse(pi.getId(), pi.getImageUrl(), pi.getOrders());
        }
    }
}
