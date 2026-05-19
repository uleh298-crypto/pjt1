package com.ssafy.ssabre.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberUpdateRequest(
                @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg") String profileImageUrl) {
}
