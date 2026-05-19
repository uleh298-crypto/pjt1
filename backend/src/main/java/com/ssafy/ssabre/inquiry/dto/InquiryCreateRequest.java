package com.ssafy.ssabre.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "문의사항 생성 요청")
public record InquiryCreateRequest(
        @NotBlank(message = "문의 내용은 필수입니다.")
        @Schema(description = "문의 내용", example = "스터디 기능에서 관리자 바꾸기는 안되나요?")
        String content
) {
}
