package com.ssafy.ssabre.campus.entity;

import com.ssafy.ssabre.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "menus")
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "식단 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id", nullable = false)
    private Campus campus;

    @Column(nullable = false)
    @Schema(description = "날짜", example = "2026-01-27")
    private LocalDate date;

    @Column(name = "image_url", columnDefinition = "TEXT", nullable = false)
    @Schema(description = "이미지 URL", example = "/uploads/meal/1/2026-01-27/a1b2c3d4.jpg")
    private String imageUrl;

    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;

    @Builder
    public Menu(Campus campus, LocalDate date, String imageUrl) {
        this.campus = campus;
        this.date = date;
        this.imageUrl = imageUrl;
    }
}
