package com.ssafy.ssabre.study.dto;

import com.ssafy.ssabre.global.entity.GroupMemberStatus;
import com.ssafy.ssabre.global.entity.MemberRole;
import com.ssafy.ssabre.study.entity.StudyMember;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "스터디 멤버 응답")
public record StudyMemberResponse(
        @Schema(description = "스터디 멤버 ID", example = "1")
        Long id,

        @Schema(description = "스터디 ID", example = "1")
        Long studyId,

        @Schema(description = "회원 ID", example = "1")
        Long memberId,

        @Schema(description = "회원 이름", example = "홍길동")
        String memberName,

        @Schema(description = "회원 이메일", example = "hong@example.com")
        String memberEmail,

        @Schema(description = "회원 프로필 이미지 URL")
        String memberProfileImageUrl,

        @Schema(description = "포트폴리오 ID", example = "1")
        Long portfolioId,

        @Schema(description = "역할 (LEADER, MEMBER)")
        MemberRole role,

        @Schema(description = "상태 (ACTIVE, INACTIVE, QUIT)")
        GroupMemberStatus status,

        @Schema(description = "가입일시")
        LocalDateTime createdAt
) {
    public static StudyMemberResponse from(StudyMember studyMember, Long portfolioId) {
        return new StudyMemberResponse(
                studyMember.getId(),
                studyMember.getStudy().getId(),
                studyMember.getMember().getId(),
                studyMember.getMember().getName(),
                studyMember.getMember().getEmail(),
                studyMember.getMember().getProfileImageUrl(),
                portfolioId,
                studyMember.getRole(),
                studyMember.getStatus(),
                studyMember.getCreatedAt()
        );
    }
}
