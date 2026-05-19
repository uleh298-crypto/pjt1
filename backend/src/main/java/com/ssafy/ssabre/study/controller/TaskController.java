package com.ssafy.ssabre.study.controller;

import com.ssafy.ssabre.global.entity.TaskStatus;
import com.ssafy.ssabre.study.dto.TaskResponse;
import com.ssafy.ssabre.study.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("studyTaskController")
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Study Task", description = "스터디 업무 API")
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/studies/{studyId}/tasks")
    @Operation(summary = "스터디 업무 목록 조회")
    public ResponseEntity<List<TaskResponse>> getTasks(@PathVariable Long studyId) {
        return ResponseEntity.ok(taskService.getTasks(studyId));
    }

    @PutMapping("/study-tasks/{taskId}/status")
    @Operation(summary = "업무 상태 수정")
    public ResponseEntity<Void> updateTaskStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long taskId,
            @RequestBody TaskStatusRequest request) {
        taskService.updateTaskStatus(userDetails.getUsername(), taskId, request.status());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/study-tasks/{taskId}")
    @Operation(summary = "업무 삭제")
    public ResponseEntity<Void> deleteTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long taskId) {
        taskService.deleteTask(userDetails.getUsername(), taskId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/studies/{studyId}/tasks")
    @Operation(summary = "업무 생성")
    public ResponseEntity<TaskResponse> createTask(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long studyId,
            @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.createTask(
                userDetails.getUsername(), studyId,
                request.title(), request.content(), request.startDate(), request.endDate(),
                request.status()));
    }

    @PutMapping("/study-tasks/{taskId}")
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
