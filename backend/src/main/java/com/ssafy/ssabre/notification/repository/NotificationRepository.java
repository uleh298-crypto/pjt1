package com.ssafy.ssabre.notification.repository;

import com.ssafy.ssabre.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.member WHERE n.member.id = :memberId ORDER BY n.createdAt DESC")
    List<Notification> findByMemberIdOrderByCreatedAtDesc(@Param("memberId") Long memberId);

    void deleteByMemberId(Long memberId);
}
