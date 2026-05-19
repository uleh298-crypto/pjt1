package com.ssafy.ssabre.board.controller;

import com.ssafy.ssabre.board.dto.NoticeUpdateRequest;
import com.ssafy.ssabre.board.entity.Notice;
import com.ssafy.ssabre.board.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards/notice")
@RequiredArgsConstructor
@Tag(name = "Admin Notice", description = "관리자 공지사항 API (전체 게시판 상단 고정)")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    @Operation(summary = "공지사항 조회", description = "상단에 표시될 공지사항을 조회합니다.")
    public ResponseEntity<Notice> getNotice() {
        return ResponseEntity.ok(noticeService.getNotice());
    }

    @PutMapping
    @Operation(summary = "공지사항 수정", description = "공지사항을 수정합니다. (관리자용)")
    public ResponseEntity<Notice> updateNotice(@RequestBody NoticeUpdateRequest request) {
        return ResponseEntity.ok(noticeService.updateNotice(request));
    }
}
