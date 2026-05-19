package com.ssafy.ssabre.chat.controller;

import com.ssafy.ssabre.chat.dto.ChatMessageSendRequest;
import com.ssafy.ssabre.chat.dto.ChatMessageWebSocketRequest;
import com.ssafy.ssabre.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    /**
     * 채팅방에 메시지 전송
     *
     * 클라이언트 발행: /app/chat/{roomId}/send
     * 구독자 수신:
     *   - /topic/chat/{roomId} (채팅방 내부)
     *   - /topic/user/{memberId}/chat-list (채팅방 목록)
     *
     * ChatService.sendMessage()에서 WebSocket 브로드캐스트 및 푸시 알림까지 처리
     */
    @MessageMapping("/chat/{roomId}/send")
    public void sendMessage(
            @DestinationVariable Long roomId,
            ChatMessageWebSocketRequest request,
            Principal principal) {

        String email = principal.getName();
        log.info("WebSocket 메시지 수신: roomId={}, sender={}", roomId, email);

        // 메시지 저장 + WebSocket 브로드캐스트 + 푸시 알림 (ChatService에서 모두 처리)
        Long messageId = chatService.sendMessage(roomId, new ChatMessageSendRequest(request.content()), email);

        log.info("메시지 처리 완료: roomId={}, messageId={}", roomId, messageId);
    }
}
