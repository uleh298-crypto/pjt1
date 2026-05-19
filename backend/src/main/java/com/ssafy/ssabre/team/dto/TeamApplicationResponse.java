package com.ssafy.ssabre.team.dto;

import com.ssafy.ssabre.global.entity.ApplicationStatus;
import com.ssafy.ssabre.team.entity.TeamApplication;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "팀 지원 응답")
public record TeamApplicationResponse(
        @Schema(description = "지원 ID", example = "1")
        Long id,

        @Schema(description = "팀 정보")
        TeamInfo team,

        @Schema(description = "포트폴리오 정보")
        PortfolioInfo portfolio,

        @Schema(description = "지원 제목", example = "지원합니다.")
        String title,

        @Schema(description = "지원 메시지", example = "열심히 하겠습니다.")
        String message,

        @Schema(description = "지원 포지션 (BE, FE 등)", example = "BE")
        String position,

        @Schema(description = "지원 상태 (PENDING, APPROVED, REJECTED)", example = "PENDING")
        ApplicationStatus status,

        @Schema(description = "생성일시")
        LocalDateTime createdAt,

        @Schema(description = "수정일시")
        LocalDateTime updatedAt
) {
    public static TeamApplicationResponse from(TeamApplication application) {
        return new TeamApplicationResponse(
                application.getId(),
                TeamInfo.from(application.getTeam()),
                PortfolioInfo.from(application.getPortfolio()),
                application.getTitle(),
                application.getMessage(),
                application.getPosition(),
                application.getStatus(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }

    @Schema(description = "팀 요약 정보")
    public record TeamInfo(
            @Schema(description = "팀 ID", example = "1")
            Long id,

            @Schema(description = "팀 이름", example = "알고리즘 스터디 팀")
            String title,

            @Schema(description = "팀장 ID", example = "1")
            Long leaderId,

            @Schema(description = "팀장 이름", example = "홍길동")
            String leaderName
    ) {
        public static TeamInfo from(com.ssafy.ssabre.team.entity.Team team) {
            return new TeamInfo(
                    team.getId(),
                    team.getTitle(),
                    team.getLeader() != null ? team.getLeader().getId() : null,
                    team.getLeader() != null ? team.getLeader().getName() : "탈퇴한 사용자"
            );
        }
    }

    @Schema(description = "포트폴리오 요약 정보")
    public record PortfolioInfo(
            @Schema(description = "포트폴리오 ID", example = "1")
            Long id,

            @Schema(description = "포트폴리오 제목", example = "백엔드 개발자 포트폴리오")
            String title,

            @Schema(description = "회원 ID", example = "1")
            Long memberId,

            @Schema(description = "회원 이름", example = "홍길동")
            String memberName,

            @Schema(description = "회원 이메일", example = "hong@example.com")
            String memberEmail,

            @Schema(description = "회원 프로필 이미지 URL")
            String memberProfileImageUrl,

            @Schema(description = "자기소개", example = "안녕하세요.")
            String introduction,

            @Schema(description = "백준 아이디", example = "koosaga")
            String bojHandle,

            @Schema(description = "Solved.ac 랭크", example = "gold")
            String solvedacRank,

            @Schema(description = "SW 역량테스트 등급", example = "A+")
            String swTestRank
    ) {
        public static PortfolioInfo from(com.ssafy.ssabre.portfolio.entity.Portfolio portfolio) {
            com.ssafy.ssabre.member.entity.Member member = portfolio.getMember();
            return new PortfolioInfo(
                    portfolio.getId(),
                    portfolio.getTitle(),
                    member != null ? member.getId() : null,
                    member != null ? member.getName() : "탈퇴한 사용자",
                    member != null ? member.getEmail() : null,
                    member != null ? member.getProfileImageUrl() : null,
                    portfolio.getIntroduction(),
                    portfolio.getBojHandle(),
                    portfolio.getSolvedacRank(),
                    portfolio.getSwTestRank()
            );
        }
    }
}
