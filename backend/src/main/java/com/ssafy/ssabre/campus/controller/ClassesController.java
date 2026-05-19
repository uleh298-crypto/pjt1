package com.ssafy.ssabre.campus.controller;

import com.ssafy.ssabre.campus.dto.ClassesRequestDto;
import com.ssafy.ssabre.campus.dto.ClassesResponseDto;
import com.ssafy.ssabre.campus.service.ClassesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@Tag(name = "Classes", description = "반 관리 API")
public class ClassesController {

    private final ClassesService classesService;

    @PostMapping
    @Operation(summary = "반 생성", description = "새로운 반을 생성합니다.")
    public ResponseEntity<Long> createClasses(@RequestBody ClassesRequestDto requestDto) {
        return ResponseEntity.ok(classesService.save(requestDto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "반 조회", description = "특정 반을 조회합니다.")
    public ResponseEntity<ClassesResponseDto> getClasses(@PathVariable Long id) {
        return ResponseEntity.ok(classesService.findById(id));
    }

    @GetMapping
    @Operation(summary = "모든 반 조회", description = "모든 반 목록을 조회합니다.")
    public ResponseEntity<List<ClassesResponseDto>> getAllClasses() {
        return ResponseEntity.ok(classesService.findAll());
    }

    @PutMapping("/{id}")
    @Operation(summary = "반 수정", description = "특정 반 정보를 수정합니다.")
    public ResponseEntity<Void> updateClasses(@PathVariable Long id, @RequestBody ClassesRequestDto requestDto) {
        classesService.update(id, requestDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "반 삭제", description = "특정 반을 삭제합니다.")
    public ResponseEntity<Void> deleteClasses(@PathVariable Long id) {
        classesService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/enroll")
    @Operation(summary = "반 등록", description = "특정 회원을 반에 등록합니다.")
    public ResponseEntity<Void> enrollMember(@PathVariable Long id, @RequestParam Long memberId) {
        classesService.enroll(id, memberId);
        return ResponseEntity.ok().build();
    }
}
