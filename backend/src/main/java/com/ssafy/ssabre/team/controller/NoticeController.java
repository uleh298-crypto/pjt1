package com.ssafy.ssabre.team.controller;

import com.ssafy.ssabre.team.dto.NoticeResponse;
import com.ssafy.ssabre.team.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("teamNoticeController")
@RequestMapping("/api/teams/{teamId}/notices")
@RequiredArgsConstructor
@Tag(name = "Notice", description = "팀 공지사항 API")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    @Operation(summary = "공지사항 목록 조회")
    public ResponseEntity<List<NoticeResponse>> getNotices(@PathVariable Long teamId) {
        return ResponseEntity.ok(noticeService.getNotices(teamId));
    }

    @PostMapping
    @Operation(summary = "공지사항 생성")
    public ResponseEntity<Void> createNotice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long teamId,
            @RequestBody NoticeRequest request) {
        noticeService.createNotice(userDetails.getUsername(), teamId, request.title(), request.content(),
                request.isPinned());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{noticeId}")
    @Operation(summary = "공지사항 수정")
    public ResponseEntity<Void> updateNotice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long teamId,
            @PathVariable Long noticeId,
            @RequestBody NoticeRequest request) {
        noticeService.updateNotice(userDetails.getUsername(), noticeId, request.title(), request.content(),
                request.isPinned());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{noticeId}")
    @Operation(summary = "공지사항 삭제")
    public ResponseEntity<Void> deleteNotice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long teamId,
            @PathVariable Long noticeId) {
        noticeService.deleteNotice(userDetails.getUsername(), noticeId);
        return ResponseEntity.ok().build();
    }

    public record NoticeRequest(String title, String content, Boolean isPinned, Boolean sendPushNotification) {
    }
}
