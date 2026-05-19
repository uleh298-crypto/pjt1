package com.ssafy.ssabre.study.controller;

import com.ssafy.ssabre.study.dto.StudyCreateRequest;
import com.ssafy.ssabre.study.dto.StudyMemberResponse;
import com.ssafy.ssabre.study.dto.StudyUpdateRequest;
import com.ssafy.ssabre.study.entity.Study;
import com.ssafy.ssabre.study.entity.StudyType;
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
@RequestMapping("/api/studies")
@RequiredArgsConstructor
@Tag(name = "Study", description = "스터디 관련 API")
public class StudyController {

    private final StudyService studyService;

    @PostMapping
    @Operation(summary = "스터디 생성")
    public ResponseEntity<Study> createStudy(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StudyCreateRequest request) {
        return ResponseEntity.ok(studyService.createStudy(userDetails.getUsername(), request));
    }

    @GetMapping
    @Operation(summary = "스터디 목록 조회")
    public ResponseEntity<List<Study>> getStudies(
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) StudyType type) {
        return ResponseEntity.ok(studyService.findAll(campusId, type));
    }

    @GetMapping("/{studyId}")
    @Operation(summary = "스터디 상세 조회")
    public ResponseEntity<Study> getStudy(@PathVariable Long studyId) {
        return ResponseEntity.ok(studyService.getStudy(studyId));
    }

    @PutMapping("/{studyId}")
    @Operation(summary = "스터디 정보 수정")
    public ResponseEntity<Void> updateStudy(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studyId,
            @Valid @RequestBody StudyUpdateRequest request) {
        studyService.updateStudy(userDetails.getUsername(), studyId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{studyId}")
    @Operation(summary = "스터디 삭제")
    public ResponseEntity<Void> deleteStudy(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studyId) {
        studyService.deleteStudy(userDetails.getUsername(), studyId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @Operation(summary = "내가 속한 스터디 조회")
    public ResponseEntity<List<Study>> getMyStudies(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studyService.getMyStudies(userDetails.getUsername()));
    }

    @GetMapping("/{studyId}/members")
    @Operation(summary = "스터디 멤버 목록 조회")
    public ResponseEntity<List<StudyMemberResponse>> getStudyMembers(@PathVariable Long studyId) {
        return ResponseEntity.ok(studyService.getStudyMembers(studyId));
    }

    @DeleteMapping("/{studyId}/members/{memberId}")
    @Operation(summary = "스터디 멤버 추방")
    public ResponseEntity<Void> kickMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studyId,
            @PathVariable Long memberId) {
        studyService.kickMember(userDetails.getUsername(), studyId, memberId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{studyId}/leave")
    @Operation(summary = "스터디 자진 탈퇴")
    public ResponseEntity<Void> leaveStudy(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studyId) {
        studyService.leaveStudy(userDetails.getUsername(), studyId);
        return ResponseEntity.ok().build();
    }
}
