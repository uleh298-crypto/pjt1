package com.ssafy.ssabre.chat.repository;

import com.ssafy.ssabre.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT DISTINCT cr FROM ChatRoom cr " +
           "LEFT JOIN FETCH cr.post " +
           "JOIN ChatRoomMember crm ON cr.id = crm.chatRoom.id " +
           "WHERE crm.member.id = :memberId " +
           "AND crm.deletedAt IS NULL")
    List<ChatRoom> findByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT cr FROM ChatRoom cr " +
           "LEFT JOIN FETCH cr.post " +
           "WHERE cr.post.id = :postId " +
           "AND EXISTS (SELECT 1 FROM ChatRoomMember crm1 WHERE crm1.chatRoom = cr AND crm1.member.id = :memberId1) " +
           "AND EXISTS (SELECT 1 FROM ChatRoomMember crm2 WHERE crm2.chatRoom = cr AND crm2.member.id = :memberId2)")
    Optional<ChatRoom> findByPostIdAndMembers(@Param("postId") Long postId,
                                              @Param("memberId1") Long memberId1,
                                              @Param("memberId2") Long memberId2);

    @Query("SELECT cr FROM ChatRoom cr LEFT JOIN FETCH cr.post WHERE cr.id = :id")
    Optional<ChatRoom> findByIdWithPost(@Param("id") Long id);
}
