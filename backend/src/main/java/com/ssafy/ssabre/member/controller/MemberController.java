package com.ssafy.ssabre.member.controller;

import com.ssafy.ssabre.member.dto.CheckEmailResponse;
import com.ssafy.ssabre.member.dto.MemberResponse;
import com.ssafy.ssabre.member.dto.MemberSignUpRequest;
import com.ssafy.ssabre.member.dto.MyCommentResponse;
import com.ssafy.ssabre.member.dto.MyPageResponse;
import com.ssafy.ssabre.member.dto.SignUpResponse;
import com.ssafy.ssabre.post.dto.PostResponse;
import com.ssafy.ssabre.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "회원 관리 API")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody MemberSignUpRequest request) {
        boolean success = memberService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SignUpResponse(success));
    }

    @org.springframework.web.bind.annotation.GetMapping
    @Operation(summary = "회원 목록 조회", description = "모든 회원 목록을 조회합니다.")
    public ResponseEntity<java.util.List<MemberResponse>> getMembers() {
        return ResponseEntity.ok(memberService.findAll().stream()
                .map(memberService::getMemberInfo)
                .toList());
    }

    @org.springframework.web.bind.annotation.GetMapping("/{id}")
    @Operation(summary = "회원 상세 조회", description = "특정 회원의 상세 정보를 조회합니다.")
    public ResponseEntity<MemberResponse> getMember(
            @org.springframework.web.bind.annotation.PathVariable Long id) {
        com.ssafy.ssabre.member.entity.Member member = memberService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다. id=" + id));
        return ResponseEntity.ok(memberService.getMemberInfo(member));
    }

    @org.springframework.web.bind.annotation.GetMapping("/check-email")
    @Operation(summary = "이메일 중복 체크", description = "이메일 중복 여부를 확인합니다. (unique: true - 사용 가능, unique: false - 중복)")
    public ResponseEntity<CheckEmailResponse> checkEmail(
            @org.springframework.web.bind.annotation.RequestParam String email) {
        boolean isDuplicate = memberService.checkEmailDuplicate(email);
        return ResponseEntity.ok(new CheckEmailResponse(!isDuplicate));
    }

    @org.springframework.web.bind.annotation.GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "로그인한 회원의 상세 정보를 조회합니다.")
    public ResponseEntity<MemberResponse> getMyInfo(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        com.ssafy.ssabre.member.entity.Member member = memberService.findByEmail(userDetails.getUsername())
                .orElseThrow(
                        () -> new IllegalArgumentException("해당 회원이 존재하지 않습니다. email=" + userDetails.getUsername()));
        return ResponseEntity.ok(memberService.getMemberInfo(member));
    }

    @org.springframework.web.bind.annotation.PutMapping("/me")
    @Operation(summary = "내 정보 수정", description = "로그인한 회원의 정보를 수정합니다.")
    public ResponseEntity<MemberResponse> updateMyInfo(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            @RequestBody com.ssafy.ssabre.member.dto.MemberUpdateRequest request) {
        com.ssafy.ssabre.member.entity.Member member = memberService.update(userDetails.getUsername(), request);
        return ResponseEntity.ok(memberService.getMemberInfo(member));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴", description = "로그인한 회원을 탈퇴 처리합니다.")
    public ResponseEntity<Void> withdraw(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        memberService.delete(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @org.springframework.web.bind.annotation.GetMapping("/mypage")
    @Operation(summary = "마이페이지 조회", description = "로그인한 회원의 마이페이지 정보를 조회합니다.")
    public ResponseEntity<MyPageResponse> getMyPage(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        return ResponseEntity.ok(memberService.getMyPage(userDetails.getUsername()));
    }

    @org.springframework.web.bind.annotation.GetMapping("/mypage/posts")
    @Operation(summary = "내가 작성한 글 목록", description = "로그인한 회원이 작성한 게시글 목록을 조회합니다.")
    public ResponseEntity<java.util.List<PostResponse>> getMyPosts(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        return ResponseEntity.ok(memberService.getMyPosts(userDetails.getUsername()));
    }

    @org.springframework.web.bind.annotation.GetMapping("/mypage/comments")
    @Operation(summary = "내가 작성한 댓글 목록", description = "로그인한 회원이 작성한 댓글 목록을 조회합니다. (대댓글 포함)")
    public ResponseEntity<java.util.List<MyCommentResponse>> getMyComments(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        return ResponseEntity.ok(memberService.getMyComments(userDetails.getUsername()));
    }

    @org.springframework.web.bind.annotation.GetMapping("/mypage/scraps")
    @Operation(summary = "내가 스크랩한 글 목록", description = "로그인한 회원이 스크랩한 게시글 목록을 조회합니다.")
    public ResponseEntity<java.util.List<PostResponse>> getMyScraps(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        return ResponseEntity.ok(memberService.getMyScraps(userDetails.getUsername()));
    }
}