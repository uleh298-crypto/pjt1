package com.ssafy.ssabre.home.controller;

import com.ssafy.ssabre.home.dto.HomeResponse;
import com.ssafy.ssabre.home.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@Tag(name = "Home", description = "홈 화면 API")
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    @Operation(summary = "홈 화면 조회", description = "홈 화면에 필요한 정보를 조회합니다.")
    public ResponseEntity<HomeResponse> getHome(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(homeService.getHome(userDetails.getUsername()));
    }
}
