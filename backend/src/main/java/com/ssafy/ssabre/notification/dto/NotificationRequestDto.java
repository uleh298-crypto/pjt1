package com.ssafy.ssabre.notification.dto;

import com.ssafy.ssabre.notification.entity.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class NotificationRequestDto {

    @Getter
    @NoArgsConstructor
    public static class TokenRequest {
        @Schema(description = "FCM 토큰", example = "fcm_token_value_...")
        private String token;
    }

    @Getter
    @NoArgsConstructor
    public static class SettingRequest {
        @Schema(description = "알림 타입", example = "COMMENT")
        private NotificationType type;

        @Schema(description = "활성화 여부", example = "true")
        private boolean enabled;
    }
}
