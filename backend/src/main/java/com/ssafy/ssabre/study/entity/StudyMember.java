package com.ssafy.ssabre.study.entity;

import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.global.entity.BaseEntity;
import com.ssafy.ssabre.global.entity.GroupMemberStatus;
import com.ssafy.ssabre.global.entity.MemberRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity(name = "StudyMember")
@Table(name = "study_members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"study_id", "member_id"})
})
@Getter
@NoArgsConstructor
@Schema(description = "스터디원 정보")
public class StudyMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "스터디원 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    @Schema(description = "스터디 정보")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @Schema(description = "회원 정보")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "스터디원 상태 (ACTIVE, INACTIVE, QUIT)", example = "ACTIVE")
    private GroupMemberStatus status = GroupMemberStatus.ACTIVE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "역할 (LEADER, MEMBER)")
    private MemberRole role = MemberRole.MEMBER;

    public static StudyMember create(Study study, Member member, MemberRole role) {
        StudyMember studyMember = new StudyMember();
        studyMember.study = study;
        studyMember.member = member;
        studyMember.role = role;
        studyMember.status = GroupMemberStatus.ACTIVE;
        return studyMember;
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
