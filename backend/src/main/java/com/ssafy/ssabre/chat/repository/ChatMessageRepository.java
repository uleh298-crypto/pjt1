package com.ssafy.ssabre.chat.repository;

import com.ssafy.ssabre.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT cm FROM ChatMessage cm LEFT JOIN FETCH cm.chatRoom LEFT JOIN FETCH cm.sender WHERE cm.chatRoom.id = :chatRoomId ORDER BY cm.sentAt ASC")
    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(@Param("chatRoomId") Long chatRoomId);

    @Query("SELECT cm FROM ChatMessage cm LEFT JOIN FETCH cm.chatRoom LEFT JOIN FETCH cm.sender WHERE cm.chatRoom.id = :chatRoomId ORDER BY cm.sentAt DESC LIMIT 1")
    Optional<ChatMessage> findTopByChatRoomIdOrderBySentAtDesc(@Param("chatRoomId") Long chatRoomId);

    void deleteByChatRoomId(Long chatRoomId);
}
