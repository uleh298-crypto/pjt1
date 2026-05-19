package com.ssafy.ssabre.post.dto;

import com.ssafy.ssabre.post.entity.SearchHistory;

import java.time.LocalDateTime;

public record SearchHistoryResponse(
        Long id,
        String keyword,
        LocalDateTime createdAt
) {
    public static SearchHistoryResponse from(SearchHistory history) {
        return new SearchHistoryResponse(
                history.getId(),
                history.getKeyword(),
                history.getCreatedAt()
        );
    }
}
