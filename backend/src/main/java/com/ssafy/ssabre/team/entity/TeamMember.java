package com.ssafy.ssabre.team.entity;

import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.global.entity.BaseEntity;
import com.ssafy.ssabre.global.entity.GroupMemberStatus;
import com.ssafy.ssabre.global.entity.MemberRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity(name = "TeamMember")
@Table(name = "team_members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"team_id", "member_id"})
})
@Getter
@NoArgsConstructor
@Schema(description = "팀원 정보")
public class TeamMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "팀원 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    @Schema(description = "팀 정보")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @Schema(description = "회원 정보")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "팀원 상태 (ACTIVE, INACTIVE, QUIT)", example = "ACTIVE")
    private GroupMemberStatus status = GroupMemberStatus.ACTIVE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "역할 (LEADER, MEMBER)")
    private MemberRole role = MemberRole.MEMBER;

    public static TeamMember create(Team team, Member member, MemberRole role) {
        TeamMember teamMember = new TeamMember();
        teamMember.team = team;
        teamMember.member = member;
        teamMember.role = role;
        teamMember.status = GroupMemberStatus.ACTIVE;
        return teamMember;
    }

    public void quit() {
        this.status = GroupMemberStatus.QUIT;
        this.deletedAt = LocalDateTime.now();
    }

    public void reactivate() {
        this.status = GroupMemberStatus.ACTIVE;
        this.role = MemberRole.MEMBER;
        this.deletedAt = null;
    }

    public boolean isActive() {
        return this.status == GroupMemberStatus.ACTIVE && this.deletedAt == null;
    }
}
