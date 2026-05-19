package com.ssafy.ssabre.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record NoticeUpdateRequest(
        @Schema(description = "공지 내용", example = "2024년 2월 1일 서비스 점검이 예정되어 있습니다.") String content
) {
}
