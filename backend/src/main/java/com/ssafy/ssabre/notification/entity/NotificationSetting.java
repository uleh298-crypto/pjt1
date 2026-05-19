package com.ssafy.ssabre.notification.entity;

import com.ssafy.ssabre.global.entity.BaseEntity;
import com.ssafy.ssabre.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification_settings", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "member_id", "notification_type" })
})
public class NotificationSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Column(nullable = false)
    private boolean enabled;

    @Builder
    public NotificationSetting(Member member, NotificationType notificationType, boolean enabled) {
        this.member = member;
        this.notificationType = notificationType;
        this.enabled = enabled;
    }

    public void update(boolean enabled) {
        this.enabled = enabled;
    }
}
