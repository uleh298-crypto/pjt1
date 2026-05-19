package com.ssafy.ssabre.team.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ssafy.ssabre.global.entity.ApplicationStatus;
import com.ssafy.ssabre.global.entity.BaseEntity;
import com.ssafy.ssabre.global.error.GlobalErrorCode;
import com.ssafy.ssabre.global.error.exception.BusinessException;
import com.ssafy.ssabre.portfolio.entity.Portfolio;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity(name = "TeamApplication")
@Table(name = "team_applications")
@Getter
@NoArgsConstructor
@Schema(description = "팀 지원 정보")
public class TeamApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "지원 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Schema(description = "팀 정보")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Schema(description = "포트폴리오 정보")
    private Portfolio portfolio;

    @Schema(description = "지원 제목", example = "지원합니다.")
    private String title;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "지원 메시지", example = "열심히 하겠습니다.")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "지원 상태 (PENDING, APPROVED, REJECTED)", example = "PENDING")
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(length = 100)
    @Schema(description = "지원 포지션 (BE, FE 등)", example = "BE")
    private String position;

    public static TeamApplication create(Team team, Portfolio portfolio, String title, String message,
            String position) {
        TeamApplication application = new TeamApplication();
        application.team = team;
        application.portfolio = portfolio;
        application.title = title;
        application.message = message;
        application.position = position;
        application.status = ApplicationStatus.PENDING;
        return application;
    }

    public void approve() {
        validateStatusTransition(ApplicationStatus.APPROVED);
        this.status = ApplicationStatus.APPROVED;
    }

    public void reject() {
        validateStatusTransition(ApplicationStatus.REJECTED);
        this.status = ApplicationStatus.REJECTED;
    }

    private void validateStatusTransition(ApplicationStatus newStatus) {
        if (this.status != ApplicationStatus.PENDING) {
            throw new BusinessException(GlobalErrorCode.INVALID_APPLICATION_STATUS);
        }
    }

    public boolean isPending() {
        return this.status == ApplicationStatus.PENDING;
    }

    public boolean isApproved() {
        return this.status == ApplicationStatus.APPROVED;
    }
}
