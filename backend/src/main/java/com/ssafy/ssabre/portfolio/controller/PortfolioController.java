package com.ssafy.ssabre.portfolio.controller;

import com.ssafy.ssabre.portfolio.dto.PortfolioCreateRequest;
import com.ssafy.ssabre.portfolio.dto.PortfolioUpdateRequest;
import com.ssafy.ssabre.portfolio.dto.PortfolioResponse;
import com.ssafy.ssabre.portfolio.dto.SolvedAcUserResponse;
import com.ssafy.ssabre.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
@Tag(name = "Portfolios", description = "포트폴리오 관리 API")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping
    @Operation(summary = "포트폴리오 생성", description = "새로운 포트폴리오를 작성합니다.")
    public ResponseEntity<Long> createPortfolio(
            @RequestBody @jakarta.validation.Valid PortfolioCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(portfolioService.createPortfolio(request, userDetails.getUsername()));
    }

    @GetMapping
    @Operation(summary = "전체 포트폴리오 목록 조회", description = "공개된 모든 포트폴리오를 조회합니다.")
    public ResponseEntity<List<PortfolioResponse>> getAllPortfolios() {
        return ResponseEntity.ok(portfolioService.getAllPortfolios());
    }

    @GetMapping("/me")
    @Operation(summary = "내 포트폴리오 조회", description = "내가 작성한 포트폴리오 목록을 조회합니다.")
    public ResponseEntity<List<PortfolioResponse>> getMyPortfolios(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(portfolioService.getMyPortfolios(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "포트폴리오 상세 조회", description = "특정 포트폴리오의 상세 정보를 조회합니다.")
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable Long id) {
        return ResponseEntity.ok(portfolioService.getPortfolio(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "포트폴리오 수정", description = "포트폴리오 내용을 수정합니다.")
    public ResponseEntity<Long> updatePortfolio(
            @PathVariable Long id,
            @RequestBody @jakarta.validation.Valid PortfolioUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(portfolioService.updatePortfolio(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "포트폴리오 삭제", description = "포트폴리오를 삭제합니다.")
    public ResponseEntity<Void> deletePortfolio(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        portfolioService.deletePortfolio(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/solvedac/verify")
    @Operation(summary = "백준 아이디 확인", description = "백준 아이디로 solved.ac 정보를 조회합니다. 연동 전 아이디 확인용입니다.")
    public ResponseEntity<SolvedAcUserResponse> verifySolvedAcHandle(
            @RequestParam String handle) {
        SolvedAcUserResponse response = portfolioService.verifySolvedAcHandle(handle);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}
