package com.ssafy.ssabre.chat.controller;

import com.ssafy.ssabre.chat.dto.ChatMessageResponse;
import com.ssafy.ssabre.chat.dto.ChatMessageSendRequest;
import com.ssafy.ssabre.chat.dto.ChatRoomCreateRequest;
import com.ssafy.ssabre.chat.dto.ChatRoomResponse;
import com.ssafy.ssabre.chat.service.ChatService;
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
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "1대1 채팅 API")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    @Operation(summary = "채팅방 생성", description = "1대1 채팅방을 생성합니다.")
    public ResponseEntity<Long> createChatRoom(
            @Valid @RequestBody ChatRoomCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(chatService.createChatRoom(request, userDetails.getUsername()));
    }

    @GetMapping("/rooms")
    @Operation(summary = "내 채팅방 목록 조회", description = "본인이 참여 중인 채팅방 목록을 조회합니다.")
    public ResponseEntity<List<ChatRoomResponse>> getMyChatRooms(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(chatService.getMyChatRooms(userDetails.getUsername()));
    }

    @GetMapping("/rooms/{roomId}")
    @Operation(summary = "채팅방 상세 조회", description = "특정 채팅방의 상세 정보를 조회합니다.")
    public ResponseEntity<ChatRoomResponse> getChatRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(chatService.getChatRoom(roomId, userDetails.getUsername()));
    }

    @PostMapping("/rooms/{roomId}/messages")
    @Operation(summary = "메시지 전송", description = "채팅방에 메시지를 전송합니다.")
    public ResponseEntity<Long> sendMessage(
            @PathVariable Long roomId,
            @Valid @RequestBody ChatMessageSendRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(chatService.sendMessage(roomId, request, userDetails.getUsername()));
    }

    @GetMapping("/rooms/{roomId}/messages")
    @Operation(summary = "메시지 목록 조회", description = "채팅방의 메시지 목록을 조회합니다.")
    public ResponseEntity<List<ChatMessageResponse>> getChatMessages(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(chatService.getChatMessages(roomId, userDetails.getUsername()));
    }

    @DeleteMapping("/rooms/{roomId}")
    @Operation(summary = "채팅방 나가기", description = "채팅방을 나갑니다. 나가면 양쪽 모두 삭제된 채팅방으로 표시됩니다.")
    public ResponseEntity<Void> exitChatRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        chatService.exitChatRoom(roomId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
