package com.ssafy.ssabre.project.controller;

import com.ssafy.ssabre.project.dto.ProjectCreateRequest;
import com.ssafy.ssabre.project.dto.ProjectListResponse;
import com.ssafy.ssabre.project.dto.ProjectUpdateRequest;
import com.ssafy.ssabre.project.dto.ProjectResponse;
import com.ssafy.ssabre.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "프로젝트 관리 API")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "프로젝트 생성", description = "새로운 프로젝트를 생성합니다.")
    public ResponseEntity<ProjectResponse> createProject(
            @RequestBody @Valid ProjectCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        projectService.create(request, userDetails.getUsername());
        return ResponseEntity.ok(ProjectResponse.of());
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "프로젝트 수정", description = "기존 프로젝트를 수정합니다.")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long projectId,
            @RequestBody @Valid ProjectUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        projectService.update(projectId, request, userDetails.getUsername());
        return ResponseEntity.ok(ProjectResponse.of());
    }

    @GetMapping("/portfolio/{portfolioId}")
    @Operation(summary = "포트폴리오별 프로젝트 목록 조회", description = "특정 포트폴리오에 속한 프로젝트 목록을 조회합니다.")
    public ResponseEntity<ProjectListResponse> getProjectsByPortfolio(
            @PathVariable Long portfolioId) {
        return ResponseEntity.ok(projectService.findByPortfolioId(portfolioId));
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "프로젝트 삭제", description = "프로젝트를 삭제합니다.")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {

        projectService.delete(projectId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
