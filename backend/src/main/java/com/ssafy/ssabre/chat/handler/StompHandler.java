package com.ssafy.ssabre.chat.handler;

import com.ssafy.ssabre.auth.jwt.JwtTokenProvider;
import com.ssafy.ssabre.chat.service.ChatPresenceService;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final ChatPresenceService chatPresenceService;
    private final MemberRepository memberRepository;

    private static final Pattern CHAT_ROOM_PATTERN = Pattern.compile("/topic/chat/(\\d+)");

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            handleConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            handleSubscribe(accessor);
        } else if (StompCommand.UNSUBSCRIBE.equals(command) || StompCommand.DISCONNECT.equals(command)) {
            handleLeave(accessor);
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtTokenProvider.validateToken(token)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                accessor.setUser(authentication);
                log.info("WebSocket 연결 성공: {}", authentication.getName());
            } else {
                log.warn("WebSocket 연결 실패: 유효하지 않은 토큰");
                throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
            }
        } else {
            log.warn("WebSocket 연결 실패: 토큰 없음");
            throw new IllegalArgumentException("인증 토큰이 필요합니다.");
        }
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }

        Matcher matcher = CHAT_ROOM_PATTERN.matcher(destination);
        if (matcher.matches()) {
            Long roomId = Long.parseLong(matcher.group(1));
            Long memberId = getMemberId(accessor.getUser());
            if (memberId != null) {
                chatPresenceService.enterRoom(memberId, roomId);
            }
        }
    }

    private void handleLeave(StompHeaderAccessor accessor) {
        StompCommand command = accessor.getCommand();

        // DISCONNECT: 연결 종료 시 무조건 퇴장 처리
        if (StompCommand.DISCONNECT.equals(command)) {
            Long memberId = getMemberId(accessor.getUser());
            if (memberId != null) {
                chatPresenceService.leaveRoom(memberId);
            }
            return;
        }

        // UNSUBSCRIBE: 채팅방 구독 해제 시에만 퇴장 처리
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }

        Matcher matcher = CHAT_ROOM_PATTERN.matcher(destination);
        if (matcher.matches()) {
            Long memberId = getMemberId(accessor.getUser());
            if (memberId != null) {
                chatPresenceService.leaveRoom(memberId);
            }
        }
    }

    private Long getMemberId(Principal principal) {
        if (principal == null) {
            return null;
        }
        String email = principal.getName();
        return memberRepository.findByEmail(email)
                .map(Member::getId)
                .orElse(null);
    }
}
