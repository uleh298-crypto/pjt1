package com.ssafy.ssabre.portfolio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "portfolio_stacks")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "포트폴리오 기술 스택 정보")
public class PortfolioStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    @Schema(description = "포트폴리오 정보")
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stack_id", nullable = false)
    @Schema(description = "기술 스택 정보")
    private Stack stack;

    @Builder.Default
    @Column(name = "expert_level")
    @Schema(description = "숙련도 (high, mid, low)", example = "mid")
    private String expertLevel = "mid";

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
