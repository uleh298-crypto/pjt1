package com.ssafy.ssabre.portfolio.service;

import com.ssafy.ssabre.portfolio.dto.SolvedAcUserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
public class SolvedAcService {

    private static final String SOLVED_AC_API_URL = "https://solved.ac/api/v3/user/show";

    private final RestClient restClient;

    public SolvedAcService() {
        this.restClient = RestClient.builder()
                .baseUrl(SOLVED_AC_API_URL)
                .build();
    }

    public SolvedAcUserResponse getUserInfo(String bojHandle) {
        try {
            return restClient.get()
                    .uri("?handle={handle}", bojHandle)
                    .retrieve()
                    .body(SolvedAcUserResponse.class);
        } catch (RestClientException e) {
            log.error("Failed to fetch solved.ac user info for handle: {}", bojHandle, e);
            return null;
        }
    }

    public String getTierName(int tier) {
        if (tier == 0)
            return "Unrated";
        if (tier == 31)
            return "Master";

        String[] tierNames = { "Bronze", "Silver", "Gold", "Platinum", "Diamond", "Ruby" };
        int tierIndex = (tier - 1) / 5;
        int tierLevel = 5 - ((tier - 1) % 5);

        return tierNames[tierIndex] + " " + tierLevel;
    }

    public String getTierImageUrl(int tier) {
        return "https://static.solved.ac/tier_small/" + tier + ".svg";
    }
}
