package com.ssafy.ssabre.portfolio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "portfolio_images")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "포트폴리오 이미지 정보")
public class PortfolioImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "이미지 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    @Schema(description = "포트폴리오 정보")
    private Portfolio portfolio;

    @Builder.Default
    @Schema(description = "이미지 종류 (general, representative)", example = "general")
    private String type = "general";

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Column(name = "orders")
    @Schema(description = "순서", example = "1")
    private Integer orders;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
