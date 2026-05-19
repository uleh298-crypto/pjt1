package com.ssafy.ssabre.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "d_days")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "D-Day 정보")
public class DDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "D-Day ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @Schema(description = "회원 정보")
    private Member member;

    @Column(nullable = false, length = 100)
    @Schema(description = "제목", example = "프로젝트 마감일")
    private String title;

    @Column(name = "target_date")
    @Schema(description = "목표 날짜", example = "2026-12-31")
    private LocalDate targetDate;

    @Column(name = "is_fixed")
    @Schema(description = "상단 고정 여부", example = "false")
    private Boolean isFixed = false;

    @Column(length = 50)
    @Schema(description = "아이콘", example = "CALENDAR")
    private String icon;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
