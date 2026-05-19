package com.ssafy.ssabre.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "문의사항 목록 응답")
public record InquiryListResponse(
        @Schema(description = "문의사항 목록")
        List<InquiryResponse> items
) {
    public static InquiryListResponse of(List<InquiryResponse> items) {
        return new InquiryListResponse(items);
    }
}
