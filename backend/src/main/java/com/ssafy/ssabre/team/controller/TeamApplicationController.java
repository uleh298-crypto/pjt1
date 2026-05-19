package com.ssafy.ssabre.team.controller;

import com.ssafy.ssabre.team.dto.TeamApplicationResponse;
import com.ssafy.ssabre.team.dto.TeamApplyRequest;
import com.ssafy.ssabre.team.service.TeamApplicationService;
import com.ssafy.ssabre.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "TeamApplication", description = "팀 지원 API")
public class TeamApplicationController {

    private final TeamService teamService;
    private final TeamApplicationService teamApplicationService;

    @PostMapping("/api/teams/{teamId}/applications")
    @Operation(summary = "팀 지원")
    public ResponseEntity<Void> applyTeam(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long teamId,
            @Valid @RequestBody TeamApplyRequest request) {
        teamService.applyTeam(userDetails.getUsername(), teamId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/teams/{teamId}/applications")
    @Operation(summary = "팀 지원 내역 조회 (팀장용)")
    public ResponseEntity<List<TeamApplicationResponse>> getTeamApplications(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long teamId) {
        return ResponseEntity.ok(teamApplicationService.getApplicationsByTeam(userDetails.getUsername(), teamId));
    }

    @GetMapping("/api/team-applications/{applicationId}")
    @Operation(summary = "지원 상세 조회")
    public ResponseEntity<TeamApplicationResponse> getApplication(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(teamApplicationService.getApplication(userDetails.getUsername(), applicationId));
    }

    @PostMapping("/api/team-applications/{applicationId}/accept")
    @Operation(summary = "지원 수락")
    public ResponseEntity<Void> acceptApplication(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long applicationId) {
        teamApplicationService.acceptApplication(userDetails.getUsername(), applicationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/team-applications/{applicationId}/reject")
    @Operation(summary = "지원 거절")
    public ResponseEntity<Void> rejectApplication(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long applicationId) {
        teamApplicationService.rejectApplication(userDetails.getUsername(), applicationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/team-applications/me")
    @Operation(summary = "내 지원 목록 조회")
    public ResponseEntity<List<TeamApplicationResponse>> getMyApplications(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(teamApplicationService.getMyApplications(userDetails.getUsername()));
    }

    @DeleteMapping("/api/team-applications/{applicationId}")
    @Operation(summary = "지원 취소")
    public ResponseEntity<Void> cancelApplication(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long applicationId) {
        teamApplicationService.cancelApplication(userDetails.getUsername(), applicationId);
        return ResponseEntity.ok().build();
    }
}
