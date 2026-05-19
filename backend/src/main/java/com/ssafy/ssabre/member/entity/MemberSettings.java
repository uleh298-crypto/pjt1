package com.ssafy.ssabre.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "member_settings")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "회원 설정 정보")
public class MemberSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "설정 ID", example = "1")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @Schema(description = "회원 정보")
    private Member member;

    @Column(name = "is_entry_exit_noti")
    @Schema(description = "입퇴실 알림 수신 여부", example = "true")
    private Boolean isEntryExitNoti = true;

    @Column(name = "is_menu_noti")
    @Schema(description = "식단 알림 수신 여부", example = "true")
    private Boolean isMenuNoti = true;

    @Column(name = "is_comment_noti")
    @Schema(description = "댓글 알림 수신 여부", example = "true")
    private Boolean isCommentNoti = true;

    @Column(name = "is_team_noti")
    @Schema(description = "팀 모집 알림 수신 여부", example = "true")
    private Boolean isTeamNoti = true;

    @Column(name = "is_dark_mode")
    @Schema(description = "다크모드 활성화 여부", example = "false")
    private Boolean isDarkMode = false;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
