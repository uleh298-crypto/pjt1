package com.ssafy.ssabre.notification.controller;

import com.ssafy.ssabre.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "알림 관리 API")
public class NotificationController {

    private final NotificationService notificationService;
    private final com.ssafy.ssabre.member.repository.MemberRepository memberRepository;
    private final com.ssafy.ssabre.notification.service.FCMService fcmService;

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "나의 알림 목록을 조회합니다.")
    public ResponseEntity<java.util.List<com.ssafy.ssabre.notification.entity.Notification>> getNotifications(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        return ResponseEntity.ok(notificationService.getMyNotifications(userDetails.getUsername()));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}/read")
    @Operation(summary = "알림 읽음 처리", description = "알림을 읽음 상태로 변경합니다.")
    public ResponseEntity<Void> readNotification(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        notificationService.readNotification(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @org.springframework.web.bind.annotation.PostMapping("/token")
    @Operation(summary = "FCM 토큰 등록", description = "로그인 시 FCM 토큰을 등록합니다.")
    public ResponseEntity<Void> registerToken(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            @org.springframework.web.bind.annotation.RequestBody com.ssafy.ssabre.notification.dto.NotificationRequestDto.TokenRequest request) {
        com.ssafy.ssabre.member.entity.Member member = memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        notificationService.registerToken(member.getId(), request.getToken());
        return ResponseEntity.ok().build();
    }

    @org.springframework.web.bind.annotation.PostMapping("/settings")
    @Operation(summary = "알림 설정 변경", description = "알림 수신 여부를 변경합니다.")
    public ResponseEntity<Void> updateSetting(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            @org.springframework.web.bind.annotation.RequestBody com.ssafy.ssabre.notification.dto.NotificationRequestDto.SettingRequest request) {
        com.ssafy.ssabre.member.entity.Member member = memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        notificationService.toggleNotificationSetting(member.getId(), request.getType(), request.isEnabled());
        return ResponseEntity.ok().build();
    }

    @org.springframework.web.bind.annotation.PostMapping("/subscribe")
    @Operation(summary = "알림 토픽 구독", description = "FCM 토큰을 daily_reminder 토픽에 구독시킵니다.")
    public ResponseEntity<Void> subscribe(
            @org.springframework.web.bind.annotation.RequestBody com.ssafy.ssabre.notification.dto.NotificationRequestDto.TokenRequest request) {
        fcmService.subscribeToTopic(request.getToken(), "daily_reminder");
        return ResponseEntity.ok().build();
    }

    @org.springframework.web.bind.annotation.PostMapping("/unsubscribe")
    @Operation(summary = "알림 토픽 구독 해지", description = "FCM 토큰을 daily_reminder 토픽에서 구독 해지합니다.")
    public ResponseEntity<Void> unsubscribe(
            @org.springframework.web.bind.annotation.RequestBody com.ssafy.ssabre.notification.dto.NotificationRequestDto.TokenRequest request) {
        fcmService.unsubscribeFromTopic(request.getToken(), "daily_reminder");
        return ResponseEntity.ok().build();
    }
}
