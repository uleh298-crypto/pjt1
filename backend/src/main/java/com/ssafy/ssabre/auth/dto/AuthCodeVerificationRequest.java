package com.ssafy.ssabre.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthCodeVerificationRequest(
                @Schema(description = "Mattermost ID", example = "kim_ssafy") String targetUserId, // mattermost ID

                @Schema(description = "인증번호", example = "123456") String authCode // 사용자가 입력한 코드
) {
}