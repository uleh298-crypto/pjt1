package com.ssafy.ssabre.inquiry.dto;

import com.ssafy.ssabre.inquiry.entity.Inquiry;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Schema(description = "문의사항 응답")
public record InquiryResponse(
        @Schema(description = "문의사항 ID", example = "219")
        Long inquiryId,

        @Schema(description = "문의 내용", example = "학식 한달치 미리보기 안되나요?")
        String content,

        @Schema(description = "답변", example = "안됩니다.")
        String answer,

        @Schema(description = "생성일시", example = "2026-01-21T12:44:00+09:00")
        OffsetDateTime createdAt
) {
    public static InquiryResponse from(Inquiry inquiry) {
        return new InquiryResponse(
                inquiry.getId(),
                inquiry.getContent(),
                inquiry.getAnswer(),
                inquiry.getCreatedAt() != null
                        ? inquiry.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toOffsetDateTime()
                        : null
        );
    }
}
