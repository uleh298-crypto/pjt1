package com.ssafy.ssabre.board.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "boards")
@EntityListeners(AuditingEntityListener.class)
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "게시판 ID", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "게시판 이름", example = "공지사항")
    private String name;

    @Schema(description = "게시판 카테고리", example = "GENERAL")
    private String category;

    @Schema(description = "게시판 설명", example = "학교 공지사항 게시판입니다.")
    private String description;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void update(String name, String description) {
        if (name != null)
            this.name = name;
        if (description != null)
            this.description = description;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
