package com.ssafy.ssabre.member.dto;

public record MyPageResponse(
        MyPageUserInfo user,
        MyPageCounts counts,
        MyPagePortfolioSummary portfolioSummary
) {
}
