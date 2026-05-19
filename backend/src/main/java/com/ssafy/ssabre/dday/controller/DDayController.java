package com.ssafy.ssabre.dday.controller;

import com.ssafy.ssabre.dday.dto.DDayListResponse;
import com.ssafy.ssabre.dday.service.DDayService;
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
@RequestMapping("/api/ddays")
@RequiredArgsConstructor
@Tag(name = "D-Days", description = "D-Day 관리 API")
public class DDayController {

    private final DDayService dDayService;

    @GetMapping
    @Operation(summary = "D-Day 목록 조회", description = "본인의 D-Day 목록을 조회합니다.")
    public ResponseEntity<DDayListResponse> getDDays(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dDayService.getDDays(userDetails.getUsername()));
    }
}
