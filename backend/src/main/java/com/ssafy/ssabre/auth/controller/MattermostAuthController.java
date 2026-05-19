package com.ssafy.ssabre.auth.controller;

import com.ssafy.ssabre.auth.dto.AuthCodeVerificationRequest;
import com.ssafy.ssabre.auth.dto.AuthVerifyResponse;
import com.ssafy.ssabre.auth.dto.FindIdResponse;
import com.ssafy.ssabre.auth.dto.MattermostRequest;
import com.ssafy.ssabre.auth.dto.ResetPasswordRequest;
import com.ssafy.ssabre.auth.service.MattermostAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Mattermost Auth", description = "Mattermost 인증 API")
public class MattermostAuthController {

    private final MattermostAuthService mattermostAuthService;

    @PostMapping("/send")
    @Operation(summary = "인증번호 발송", description = "Mattermost ID로 인증번호를 전송합니다. generation에 따라 해당 기수의 Mattermost로 전송됩니다. 학생 정보(기수, 이름, Mattermost ID)가 일치해야 발송됩니다.")
    public ResponseEntity<String> sendAuth(@RequestBody MattermostRequest request) {
        mattermostAuthService.sendAuthMessage(request.targetUserId(), request.generation(), request.name());
        return ResponseEntity.ok("인증번호가 발송되었습니다.");
    }

    @PostMapping("/verify")
    @Operation(summary = "인증번호 검증", description = "입력한 인증번호를 검증합니다.")
    public ResponseEntity<AuthVerifyResponse> verifyAuth(@RequestBody AuthCodeVerificationRequest request) {
        boolean isVerified = mattermostAuthService.verifyCode(request.targetUserId(), request.authCode());

        if (isVerified) {
            return ResponseEntity.ok(new AuthVerifyResponse(true));
        } else {
            return ResponseEntity.badRequest().body(new AuthVerifyResponse(false));
        }
    }

    @GetMapping("/findId")
    @Operation(summary = "아이디 찾기", description = "Mattermost ID로 사용자 이메일을 조회합니다.")
    public ResponseEntity<FindIdResponse> findId(@RequestParam String mattermostId) {
        String email = mattermostAuthService.findEmailByMattermostId(mattermostId)
                .orElseThrow(() -> new IllegalArgumentException("해당 Mattermost ID로 등록된 이메일을 찾을 수 없습니다."));
        return ResponseEntity.ok(new FindIdResponse(email));
    }

    @PostMapping("/resetPassword")
    @Operation(summary = "비밀번호 재설정", description = "Mattermost 인증 후 비밀번호를 재설정합니다. 먼저 /send로 인증번호를 받고 /verify로 검증을 완료해야 합니다.")
    public ResponseEntity<AuthVerifyResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        boolean success = mattermostAuthService.resetPassword(request.mattermostId(), request.newPassword());

        if (success) {
            return ResponseEntity.ok(new AuthVerifyResponse(true));
        } else {
            throw new IllegalArgumentException("비밀번호 재설정에 실패했습니다.");
        }
    }
}