package com.ssafy.ssabre.notification.entity;

import com.ssafy.ssabre.global.entity.BaseEntity;
import com.ssafy.ssabre.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "알림 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    @Schema(description = "알림 내용", example = "새로운 댓글이 달렸습니다.")
    private String content;

    @Column(nullable = false)
    @Schema(description = "알림 읽음 여부", example = "false")
    private boolean isRead;

    @Column
    @Schema(description = "관련 URL", example = "/posts/1")
    private String relatedUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Schema(description = "알림 타입", example = "COMMENT")
    private NotificationType type;

    @Builder
    public Notification(Member member, String content, String relatedUrl, NotificationType type) {
        this.member = member;
        this.content = content;
        this.relatedUrl = relatedUrl;
        this.type = type;
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
