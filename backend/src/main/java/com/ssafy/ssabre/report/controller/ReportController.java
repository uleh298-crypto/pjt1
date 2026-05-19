package com.ssafy.ssabre.report.controller;

import com.ssafy.ssabre.report.dto.ReportCreateRequest;
import com.ssafy.ssabre.report.dto.ReportCreateResponse;
import com.ssafy.ssabre.report.dto.ReportResponse;
import com.ssafy.ssabre.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "신고 관리 API")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "신고하기", description = "게시글 또는 댓글을 신고합니다.")
    public ResponseEntity<ReportCreateResponse> createReport(
            @RequestBody @jakarta.validation.Valid ReportCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reportService.createReport(request, userDetails.getUsername()));
    }

    @GetMapping
    @Operation(summary = "신고 내역 조회 (전체)", description = "모든 신고 내역을 조회합니다.")
    public ResponseEntity<List<ReportResponse>> getReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/paged")
    @Operation(summary = "신고 내역 조회 (페이징)", description = "신고 내역을 페이징하여 조회합니다.")
    public ResponseEntity<org.springframework.data.domain.Page<ReportResponse>> getReportsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reportService.getAllReportsPaged(page, size));
    }
}
