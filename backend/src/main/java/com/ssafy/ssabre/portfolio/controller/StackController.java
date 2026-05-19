package com.ssafy.ssabre.portfolio.controller;

import com.ssafy.ssabre.portfolio.entity.Stack;
import com.ssafy.ssabre.portfolio.repository.StackRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/stacks")
@RequiredArgsConstructor
@Tag(name = "Stacks", description = "기술 스택 관리 API")
public class StackController {

    private final StackRepository stackRepository;

    @GetMapping
    @Operation(summary = "기술 스택 목록 조회", description = "등록된 모든 기술 스택을 조회합니다.")
    public ResponseEntity<List<Stack>> getAllStacks() {
        return ResponseEntity.ok(stackRepository.findAll());
    }
}
