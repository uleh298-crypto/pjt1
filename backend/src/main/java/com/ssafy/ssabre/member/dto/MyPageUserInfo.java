package com.ssafy.ssabre.member.dto;

public record MyPageUserInfo(
        Long userId,
        String name,
        String mattermostId,
        String campus,
        Integer generation,
        String profileImageUrl
) {
}
