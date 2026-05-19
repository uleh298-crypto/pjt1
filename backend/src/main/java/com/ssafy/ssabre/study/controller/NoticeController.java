package com.ssafy.ssabre.study.controller;

import com.ssafy.ssabre.study.dto.NoticeResponse;
import com.ssafy.ssabre.study.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("studyNoticeController")
@RequestMapping("/api/studies")
@RequiredArgsConstructor
@Tag(name = "Study Notice", description = "스터디 공지 API")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/{studyId}/notices")
    @Operation(summary = "스터디 공지사항 목록 조회")
    public ResponseEntity<List<NoticeResponse>> getNotices(@PathVariable Long studyId) {
        return ResponseEntity.ok(noticeService.getNotices(studyId));
    }

    @PostMapping("/{studyId}/notices")
    @Operation(summary = "스터디 공지사항 생성")
    public ResponseEntity<Void> createNotice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studyId,
            @Valid @RequestBody NoticeRequest request) {
        noticeService.createNotice(
                userDetails.getUsername(), studyId, request.title(), request.content(), request.isPinned());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{studyId}/notices/{noticeId}")
    @Operation(summary = "스터디 공지사항 수정")
    public ResponseEntity<Void> updateNotice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studyId,
            @PathVariable Long noticeId,
            @Valid @RequestBody NoticeRequest request) {
        noticeService.updateNotice(userDetails.getUsername(), noticeId, request.title(), request.content(),
                request.isPinned());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{studyId}/notices/{noticeId}")
    @Operation(summary = "스터디 공지사항 삭제")
    public ResponseEntity<Void> deleteNotice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studyId,
            @PathVariable Long noticeId) {
        noticeService.deleteNotice(userDetails.getUsername(), noticeId);
        return ResponseEntity.ok().build();
    }

    public record NoticeRequest(String title, String content, Boolean isPinned, Boolean sendPushNotification) {
    }
}
