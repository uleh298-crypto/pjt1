package com.ssafy.ssabre.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "solved.ac 사용자 정보 응답")
public record SolvedAcUserResponse(
        @Schema(description = "백준 아이디") String handle,
        @Schema(description = "티어 (0: Unrated, 1-5: Bronze, 6-10: Silver, 11-15: Gold, 16-20: Platinum, 21-25: Diamond, 26-30: Ruby, 31: Master)") int tier,
        @Schema(description = "레이팅") int rating,
        @Schema(description = "푼 문제 수") int solvedCount,
        @JsonProperty("class") @Schema(description = "클래스") int classValue,
        @Schema(description = "랭킹") int rank) {
    public String getTierName() {
        if (tier == 0)
            return "Unrated";
        if (tier == 31)
            return "Master";

        String[] tierNames = { "Bronze", "Silver", "Gold", "Platinum", "Diamond", "Ruby" };
        int tierIndex = (tier - 1) / 5;
        int tierLevel = 5 - ((tier - 1) % 5);

        return tierNames[tierIndex] + " " + tierLevel;
    }

    public String getTierImageUrl() {
        return "https://static.solved.ac/tier_small/" + tier + ".svg";
    }
}
