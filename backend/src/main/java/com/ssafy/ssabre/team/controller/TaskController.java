package com.ssafy.ssabre.team.controller;

import com.ssafy.ssabre.global.entity.TaskStatus;
import com.ssafy.ssabre.team.dto.TaskResponse;
import com.ssafy.ssabre.team.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("teamTaskController")
@RequiredArgsConstructor
@Tag(name = "Task", description = "팀 업무 API")
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/api/teams/{teamId}/tasks")
    @Operation(summary = "업무 목록 조회")
    public ResponseEntity<List<TaskResponse>> getTasks(@PathVariable Long teamId) {
        return ResponseEntity.ok(taskService.getTasks(teamId));
    }

    @PutMapping("/api/team-tasks/{taskId}/status")
    @Operation(summary = "업무 상태 수정")
    public ResponseEntity<Void> updateTaskStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long taskId,
            @RequestBody TaskStatusRequest request) {
        taskService.updateTaskStatus(userDetails.getUsername(), taskId, request.status());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/team-tasks/{taskId}")
    @Operation(summary = "업무 삭제")
    public ResponseEntity<Void> deleteTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long taskId) {
        taskService.deleteTask(userDetails.getUsername(), taskId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/teams/{teamId}/tasks")
    @Operation(summary = "업무 생성")
    public ResponseEntity<TaskResponse> createTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long teamId,
            @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.createTask(
                userDetails.getUsername(), teamId,
                request.title(), request.content(), request.startDate(), request.endDate(),
                request.status()));
    }

    @PutMapping("/api/team-tasks/{taskId}")
    @Operation(summary = "업무 상세 수정")
    public ResponseEntity<Void> updateTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long taskId,
            @RequestBody TaskRequest request) {
        taskService.updateTask(
                userDetails.getUsername(), taskId,
                request.title(), request.content(), request.startDate(), request.endDate(),
                request.status());
        return ResponseEntity.ok().build();
    }

    public record TaskStatusRequest(TaskStatus status) {
    }

    public record TaskRequest(
            String title,
            String content,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            TaskStatus status) {
    }
}
