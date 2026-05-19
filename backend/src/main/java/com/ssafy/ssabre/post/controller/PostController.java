package com.ssafy.ssabre.post.controller;

import com.ssafy.ssabre.post.dto.CommentCreateRequest;
import com.ssafy.ssabre.post.dto.CommentResponse;
import com.ssafy.ssabre.post.dto.DeleteResponse;
import com.ssafy.ssabre.post.dto.PagedCommentResponse;
import com.ssafy.ssabre.post.dto.PagedPostResponse;
import com.ssafy.ssabre.post.dto.PollResponse;
import com.ssafy.ssabre.post.dto.PopularKeywordResponse;
import com.ssafy.ssabre.post.dto.PostDetailResponse;
import com.ssafy.ssabre.post.dto.PostLikeResponse;
import com.ssafy.ssabre.post.dto.PostResponse;
import com.ssafy.ssabre.post.dto.ScrapResponse;
import com.ssafy.ssabre.post.dto.ReplyCreateRequest;
import com.ssafy.ssabre.post.dto.ReplyResponse;
import com.ssafy.ssabre.post.dto.SearchHistoryResponse;
import com.ssafy.ssabre.post.dto.VoteRequest;
import com.ssafy.ssabre.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "게시글 관리 API")
public class PostController {

    private final PostService postService;

    @GetMapping
    @Operation(summary = "게시글 목록 조회/검색 (커서 기반 페이지네이션)", description = "게시글을 커서 기반으로 조회합니다. boardId로 게시판별 조회, keyword로 검색이 가능합니다. 무한 스크롤에 최적화되어 있습니다.")
    public ResponseEntity<PagedPostResponse> getPosts(
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (keyword != null && !keyword.isBlank()) {
            return ResponseEntity.ok(postService.searchByKeywordPaged(keyword, userDetails.getUsername(), cursor, limit));
        }
        if (boardId != null) {
            return ResponseEntity.ok(postService.findByBoardIdPaged(boardId, cursor, limit));
        }
        return ResponseEntity.ok(postService.findAllPaged(cursor, limit));
    }

    @GetMapping("/search-history")
    @Operation(summary = "검색 기록 조회", description = "내 검색 기록 목록을 조회합니다. 최근 20개까지 저장됩니다.")
    public ResponseEntity<List<SearchHistoryResponse>> getSearchHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(postService.getSearchHistory(userDetails.getUsername()));
    }

    @DeleteMapping("/search-history/{id}")
    @Operation(summary = "검색 기록 삭제", description = "특정 검색 기록을 삭제합니다.")
    public ResponseEntity<DeleteResponse> deleteSearchHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        postService.deleteSearchHistory(id, userDetails.getUsername());
        return ResponseEntity.ok(new DeleteResponse(true));
    }

    @DeleteMapping("/search-history")
    @Operation(summary = "검색 기록 전체 삭제", description = "모든 검색 기록을 삭제합니다.")
    public ResponseEntity<DeleteResponse> deleteAllSearchHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        postService.deleteAllSearchHistory(userDetails.getUsername());
        return ResponseEntity.ok(new DeleteResponse(true));
    }

    @GetMapping("/popular-keywords")
    @Operation(summary = "인기 검색어 조회", description = "전체 사용자 인기 검색어 TOP 10을 조회합니다.")
    public ResponseEntity<List<PopularKeywordResponse>> getPopularKeywords() {
        return ResponseEntity.ok(postService.getPopularKeywords());
    }

    @GetMapping("/hot")
    @Operation(summary = "Hot 게시글 목록 조회", description = "좋아요 10개 이상인 인기 게시글을 조회합니다. 커서 기반 페이지네이션을 지원합니다.")
    public ResponseEntity<PagedPostResponse> getHotPosts(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(postService.findHotPostsPaged(cursor, limit));
    }

    @GetMapping("/{id}")
    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 내용을 조회합니다.")
    public ResponseEntity<PostDetailResponse> getPost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(postService.findDetailById(id, userDetails.getUsername()));
    }

    @PostMapping
    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    public ResponseEntity<PostResponse> createPost(
            @RequestBody @jakarta.validation.Valid com.ssafy.ssabre.post.dto.PostCreateRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        return ResponseEntity.ok(postService.save(request, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "게시글 수정", description = "본인이 작성한 게시글을 수정합니다.")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @RequestBody com.ssafy.ssabre.post.dto.PostUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(postService.update(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "게시글 삭제", description = "본인이 작성한 게시글을 삭제합니다.")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        postService.delete(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{postId}/comments")
    @Operation(summary = "댓글 목록 조회 (커서 기반 페이지네이션)", description = "게시글의 댓글을 커서 기반으로 조회합니다. 무한 스크롤에 최적화되어 있습니다.")
    public ResponseEntity<PagedCommentResponse> getComments(
            @PathVariable Long postId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(postService.getCommentsPaged(postId, userDetails.getUsername(), cursor, limit));
    }

    @PostMapping("/{postId}/comments")
    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다.")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @RequestBody @Valid CommentCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(postService.createComment(postId, request, userDetails.getUsername()));
    }

    @PostMapping("/{postId}/comments/{commentId}/replies")
    @Operation(summary = "대댓글 작성", description = "댓글에 대댓글을 작성합니다.")
    public ResponseEntity<ReplyResponse> createReply(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody @Valid ReplyCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(postService.createReply(postId, commentId, request, userDetails.getUsername()));
    }

    @PostMapping("/{postId}/poll/vote")
    @Operation(summary = "투표하기", description = "게시글의 투표에 참여합니다.")
    public ResponseEntity<PollResponse> vote(
            @PathVariable Long postId,
            @RequestBody @Valid VoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(postService.vote(postId, request, userDetails.getUsername()));
    }

    @PostMapping("/{postId}/like")
    @Operation(summary = "게시글 좋아요", description = "게시글에 좋아요를 추가합니다.")
    public ResponseEntity<PostLikeResponse> like(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(postService.like(postId, userDetails.getUsername()));
    }

    @DeleteMapping("/{postId}/like")
    @Operation(summary = "게시글 좋아요 취소", description = "게시글 좋아요를 취소합니다.")
    public ResponseEntity<PostLikeResponse> unlike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(postService.unlike(postId, userDetails.getUsername()));
    }

    @PostMapping("/{postId}/scrap")
    @Operation(summary = "게시글 스크랩", description = "게시글을 스크랩합니다.")
    public ResponseEntity<ScrapResponse> scrap(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(postService.scrap(postId, userDetails.getUsername()));
    }

    @DeleteMapping("/{postId}/scrap")
    @Operation(summary = "게시글 스크랩 취소", description = "게시글 스크랩을 취소합니다.")
    public ResponseEntity<ScrapResponse> unscrap(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(postService.unscrap(postId, userDetails.getUsername()));
    }
}
