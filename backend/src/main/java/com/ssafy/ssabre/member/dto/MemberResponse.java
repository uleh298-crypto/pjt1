package com.ssafy.ssabre.member.dto;

import com.ssafy.ssabre.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record MemberResponse(
        @Schema(description = "회원 ID") Long id,
        @Schema(description = "이메일") String email,
        @Schema(description = "이름") String name,
        @Schema(description = "학번") Integer studentNo,
        @Schema(description = "캠퍼스") String campus,
        @Schema(description = "기수") Integer generation,
        @Schema(description = "반") Integer classNo,
        @Schema(description = "Mattermost ID") String mattermostId,
        @Schema(description = "프로필 이미지 URL") String profileImageUrl,
        @Schema(description = "탈퇴일") LocalDateTime deletedAt,
        @Schema(description = "생성일") LocalDateTime createdAt,
        @Schema(description = "수정일") LocalDateTime updatedAt
) {
    public static MemberResponse from(Member member, String campus, Integer generation, Integer classNo) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getStudentNo(),
                campus,
                generation,
                classNo,
                member.getMattermostId(),
                member.getProfileImageUrl(),
                member.getDeletedAt(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }
}
