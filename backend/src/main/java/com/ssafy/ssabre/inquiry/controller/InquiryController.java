package com.ssafy.ssabre.inquiry.controller;

import com.ssafy.ssabre.inquiry.dto.InquiryCreateRequest;
import com.ssafy.ssabre.inquiry.dto.InquiryListResponse;
import com.ssafy.ssabre.inquiry.dto.SuccessResponse;
import com.ssafy.ssabre.inquiry.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
@Tag(name = "Inquiries", description = "문의사항 관리 API")
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    @Operation(summary = "문의사항 작성", description = "새로운 문의사항을 작성합니다.")
    public ResponseEntity<SuccessResponse> createInquiry(
            @RequestBody @Valid InquiryCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        inquiryService.createInquiry(request, userDetails.getUsername());
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @GetMapping
    @Operation(summary = "문의사항 목록 조회", description = "본인의 문의사항 목록을 조회합니다.")
    public ResponseEntity<InquiryListResponse> getInquiries(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(inquiryService.getInquiries(userDetails.getUsername()));
    }
}
