package com.ssafy.ssabre.campus.controller;

import com.ssafy.ssabre.campus.dto.ClassesResponseDto;
import com.ssafy.ssabre.campus.entity.Campus;
import com.ssafy.ssabre.campus.service.CampusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/campuses")
@RequiredArgsConstructor
@Tag(name = "Campuses", description = "캠퍼스 관리 API")
public class CampusController {

    private final CampusService campusService;

    @GetMapping
    @Operation(summary = "캠퍼스 목록 조회", description = "모든 캠퍼스 목록을 조회합니다.")
    public ResponseEntity<List<Campus>> getCampuses() {
        return ResponseEntity.ok(campusService.findAll());
    }

    @GetMapping("/{id}/classes")
    @Operation(summary = "반 목록 조회", description = "특정 캠퍼스의 반 목록을 조회합니다.")
    public ResponseEntity<List<ClassesResponseDto>> getClasses(@PathVariable Long id) {
        return ResponseEntity.ok(campusService.findClassesByCampusId(id).stream()
                .map(ClassesResponseDto::new)
                .collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "캠퍼스 생성", description = "새로운 캠퍼스를 생성합니다.")
    public ResponseEntity<Long> createCampus(@RequestParam String name) {
        return ResponseEntity.ok(campusService.save(name));
    }
}
