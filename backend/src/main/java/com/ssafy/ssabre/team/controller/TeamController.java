package com.ssafy.ssabre.team.controller;

import com.ssafy.ssabre.team.dto.TeamCreateRequest;
import com.ssafy.ssabre.team.dto.TeamMemberResponse;
import com.ssafy.ssabre.team.dto.TeamUpdateRequest;
import com.ssafy.ssabre.team.entity.Team;
import com.ssafy.ssabre.team.entity.TeamType;
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
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "Team", description = "팀 관련 API")
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @Operation(summary = "팀 생성")
    public ResponseEntity<Team> createTeam(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TeamCreateRequest request) {
        return ResponseEntity.ok(teamService.createTeam(userDetails.getUsername(), request));
    }

    @GetMapping
    @Operation(summary = "팀 목록 조회")
    public ResponseEntity<List<Team>> getTeams(
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) TeamType type) {
        return ResponseEntity.ok(teamService.findAll(campusId, type));
    }

    @GetMapping("/{teamId}")
    @Operation(summary = "팀 상세 조회")
    public ResponseEntity<Team> getTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getTeam(teamId));
    }

    @PutMapping("/{teamId}")
    @Operation(summary = "팀 정보 수정")
    public ResponseEntity<Void> updateTeam(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long teamId,
            @Valid @RequestBody TeamUpdateRequest request) {
        teamService.updateTeam(userDetails.getUsername(), teamId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{teamId}")
    @Operation(summary = "팀 삭제")
    public ResponseEntity<Void> deleteTeam(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long teamId) {
        teamService.deleteTeam(userDetails.getUsername(), teamId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @Operation(summary = "내가 속한 팀 조회")
    public ResponseEntity<List<Team>> getMyTeams(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(teamService.getMyTeams(userDetails.getUsername()));
    }

    @GetMapping("/{teamId}/members")
    @Operation(summary = "팀 멤버 목록 조회")
    public ResponseEntity<List<TeamMemberResponse>> getTeamMembers(@PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getTeamMembers(teamId));
    }

    @DeleteMapping("/{teamId}/members/{memberId}")
    @Operation(summary = "팀 멤버 추방 (리더 전용)")
    public ResponseEntity<Void> kickMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long teamId,
            @PathVariable Long memberId) {
        teamService.kickMember(userDetails.getUsername(), teamId, memberId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{teamId}/leave")
    @Operation(summary = "팀 자진 탈퇴")
    public ResponseEntity<Void> leaveTeam(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long teamId) {
        teamService.leaveTeam(userDetails.getUsername(), teamId);
        return ResponseEntity.ok().build();
    }
}
