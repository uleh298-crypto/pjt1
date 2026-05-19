package com.ssafy.ssabre.admin.controller;

import com.ssafy.ssabre.admin.service.DataInitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Admin - 데이터 초기화", description = "초기 데이터 설정 API")
@RestController
@RequestMapping("/api/admin/init")
@RequiredArgsConstructor
public class DataInitController {

    private final DataInitService dataInitService;

    @Operation(summary = "게시판 초기화", description = "게시판 데이터를 초기화합니다. 이미 데이터가 있으면 건너뜁니다.")
    @PostMapping("/boards")
    public ResponseEntity<Map<String, Object>> initBoards() {
        int count = dataInitService.initBoards();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", count > 0 ? count + "건의 게시판 데이터가 초기화되었습니다." : "이미 데이터가 존재하여 건너뛰었습니다.",
                "count", count
        ));
    }

    @Operation(summary = "캠퍼스 초기화", description = "캠퍼스 데이터를 초기화합니다. 이미 데이터가 있으면 건너뜁니다.")
    @PostMapping("/campuses")
    public ResponseEntity<Map<String, Object>> initCampuses() {
        int count = dataInitService.initCampuses();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", count > 0 ? count + "건의 캠퍼스 데이터가 초기화되었습니다." : "이미 데이터가 존재하여 건너뛰었습니다.",
                "count", count
        ));
    }

    @Operation(summary = "기술 스택 초기화", description = "기술 스택 데이터를 초기화합니다. 이미 데이터가 있으면 건너뜁니다.")
    @PostMapping("/stacks")
    public ResponseEntity<Map<String, Object>> initStacks() {
        int count = dataInitService.initStacks();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", count > 0 ? count + "건의 스택 데이터가 초기화되었습니다." : "이미 데이터가 존재하여 건너뛰었습니다.",
                "count", count
        ));
    }

    @Operation(summary = "반(Class) 초기화", description = "반 데이터를 초기화합니다. 이미 데이터가 있으면 건너뜁니다.")
    @PostMapping("/classes")
    public ResponseEntity<Map<String, Object>> initClasses() {
        int count = dataInitService.initClasses();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", count > 0 ? count + "건의 반 데이터가 초기화되었습니다." : "이미 데이터가 존재하여 건너뛰었습니다.",
                "count", count
        ));
    }
}
