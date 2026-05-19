package com.ssafy.ssabre.chat.repository;

import com.ssafy.ssabre.chat.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    @Query("SELECT crm FROM ChatRoomMember crm LEFT JOIN FETCH crm.chatRoom LEFT JOIN FETCH crm.member WHERE crm.chatRoom.id = :chatRoomId AND crm.member.id = :memberId")
    java.util.Optional<ChatRoomMember> findByChatRoomIdAndMemberId(@Param("chatRoomId") Long chatRoomId, @Param("memberId") Long memberId);

    @Query("SELECT crm FROM ChatRoomMember crm LEFT JOIN FETCH crm.chatRoom LEFT JOIN FETCH crm.member WHERE crm.chatRoom.id = :chatRoomId")
    java.util.List<ChatRoomMember> findByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    long countByChatRoomId(Long chatRoomId);

    java.util.List<ChatRoomMember> findByMemberId(Long memberId);

    void deleteByChatRoomId(Long chatRoomId);
}
