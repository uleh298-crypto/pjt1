package com.ssafy.ssabre.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 채팅방 접속 상태 관리 서비스 (인메모리)
 * 사용자가 특정 채팅방에 웹소켓으로 접속 중인지 추적
 */
@Service
@Slf4j
public class ChatPresenceService {

    // memberId -> roomId (현재 접속 중인 채팅방)
    private final ConcurrentHashMap<Long, Long> presenceMap = new ConcurrentHashMap<>();

    /**
     * 사용자가 채팅방에 접속했음을 기록
     */
    public void enterRoom(Long memberId, Long roomId) {
        presenceMap.put(memberId, roomId);
        log.debug("Member {} entered chat room {}", memberId, roomId);
    }

    /**
     * 사용자가 채팅방에서 나갔음을 기록
     */
    public void leaveRoom(Long memberId) {
        presenceMap.remove(memberId);
        log.debug("Member {} left chat room", memberId);
    }

    /**
     * 사용자가 특정 채팅방에 접속 중인지 확인
     */
    public boolean isInRoom(Long memberId, Long roomId) {
        Long currentRoomId = presenceMap.get(memberId);
        return currentRoomId != null && currentRoomId.equals(roomId);
    }
}
