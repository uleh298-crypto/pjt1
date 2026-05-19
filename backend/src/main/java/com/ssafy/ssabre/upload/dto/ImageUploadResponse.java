package com.ssafy.ssabre.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ImageUploadResponse(
    @Schema(description = "성공 여부", example = "true")
    boolean success,

    @Schema(description = "업로드된 이미지 URL", example = "https://your-domain.com/static/uploads/project/2026/01/abcd1234.webp")
    String url
) {
    public static ImageUploadResponse of(String url) {
        return new ImageUploadResponse(true, url);
    }
}
