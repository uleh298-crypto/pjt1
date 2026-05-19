package com.ssafy.ssabre.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record MattermostRequest(
                @Schema(description = "메시지 받을 대상 ID (Mattermost ID)", example = "kim_ssafy") String targetUserId,
                @Schema(description = "기수 (14 또는 15)", example = "14") Integer generation,
                @Schema(description = "학생 이름", example = "김싸피") String name) {
}