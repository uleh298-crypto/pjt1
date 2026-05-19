package com.ssafy.ssabre.portfolio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "portfolio_urls")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "포트폴리오 링크 정보")
public class PortfolioUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "링크 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    @Schema(description = "포트폴리오 정보")
    private Portfolio portfolio;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Schema(description = "링크 URL", example = "https://github.com/ssafy")
    private String url;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
