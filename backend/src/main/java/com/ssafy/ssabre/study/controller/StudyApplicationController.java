package com.ssafy.ssabre.study.controller;

import com.ssafy.ssabre.study.dto.StudyApplyRequest;
import com.ssafy.ssabre.study.dto.StudyApplicationResponse;
import com.ssafy.ssabre.study.service.StudyApplicationService;
import com.ssafy.ssabre.study.service.StudyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "StudyApplication", description = "스터디 지원 API")
public class StudyApplicationController {

    private final StudyService studyService;
    private final StudyApplicationService studyApplicationService;

    @PostMapping("/api/studies/{studyId}/applications")
    @Operation(summary = "스터디 지원")
    public ResponseEntity<Void> applyStudy(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studyId,
            @Valid @RequestBody StudyApplyRequest request) {
        studyService.applyStudy(userDetails.getUsername(), studyId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/studies/{studyId}/applications")
    @Operation(summary = "스터디 지원 내역 조회 (스터디장용)")
    public ResponseEntity<List<StudyApplicationResponse>> getStudyApplications(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studyId) {
        return ResponseEntity.ok(studyApplicationService.getApplicationsByStudy(userDetails.getUsername(), studyId));
    }

    @GetMapping("/api/study-applications/{applicationId}")
    @Operation(summary = "지원 상세 조회")
    public ResponseEntity<StudyApplicationResponse> getApplication(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(studyApplicationService.getApplication(userDetails.getUsername(), applicationId));
    }

    @PostMapping("/api/study-applications/{applicationId}/accept")
    @Operation(summary = "지원 수락")
    public ResponseEntity<Void> acceptApplication(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long applicationId) {
        studyApplicationService.acceptApplication(userDetails.getUsername(), applicationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/study-applications/{applicationId}/reject")
    @Operation(summary = "지원 거절")
    public ResponseEntity<Void> rejectApplication(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long applicationId) {
        studyApplicationService.rejectApplication(userDetails.getUsername(), applicationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/study-applications/me")
    @Operation(summary = "내 지원 목록 조회")
    public ResponseEntity<List<StudyApplicationResponse>> getMyApplications(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studyApplicationService.getMyApplications(userDetails.getUsername()));
    }

    @DeleteMapping("/api/study-applications/{applicationId}")
    @Operation(summary = "지원 취소")
    public ResponseEntity<Void> cancelApplication(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long applicationId) {
        studyApplicationService.cancelApplication(userDetails.getUsername(), applicationId);
        return ResponseEntity.ok().build();
    }
}
