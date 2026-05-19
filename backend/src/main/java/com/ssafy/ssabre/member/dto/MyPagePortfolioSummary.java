package com.ssafy.ssabre.member.dto;

import java.util.List;
import java.util.Map;

public record MyPagePortfolioSummary(
        Map<String, String> techStack,
        String ssafySwRating,
        String solvedAcRank,
        List<String> links,
        List<String> projects
) {
}
