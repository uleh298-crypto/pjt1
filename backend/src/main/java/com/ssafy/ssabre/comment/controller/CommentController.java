package com.ssafy.ssabre.comment.controller;

import com.ssafy.ssabre.comment.dto.CommentLikeResponse;
import com.ssafy.ssabre.comment.service.CommentService;
import com.ssafy.ssabre.post.dto.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "댓글 관리 API")
public class CommentController {

    private final CommentService commentService;

    @PutMapping("/{id}")
    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다.")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long id,
            @RequestBody com.ssafy.ssabre.comment.dto.CommentUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(commentService.update(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        commentService.delete(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{commentId}/like")
    @Operation(summary = "댓글 좋아요", description = "댓글에 좋아요를 추가합니다.")
    public ResponseEntity<CommentLikeResponse> like(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(commentService.like(commentId, userDetails.getUsername()));
    }

    @DeleteMapping("/{commentId}/like")
    @Operation(summary = "댓글 좋아요 취소", description = "댓글 좋아요를 취소합니다.")
    public ResponseEntity<CommentLikeResponse> unlike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(commentService.unlike(commentId, userDetails.getUsername()));
    }
}
