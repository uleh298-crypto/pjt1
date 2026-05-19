package com.ssafy.ssabre.project.entity;

import com.ssafy.ssabre.global.entity.BaseEntity;
import com.ssafy.ssabre.portfolio.entity.Portfolio;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "프로젝트 정보")
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "프로젝트 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    @Schema(description = "포트폴리오 정보")
    private Portfolio portfolio;

    @Schema(description = "프로젝트 제목", example = "유기동물 입양 독려를 위한 플랫폼 제작")
    @Column(nullable = false)
    private String title;

    @Schema(description = "프로젝트 소개", example = "운동 루틴을 공유하고 챌린지 수행하는 앱 프로젝트")
    private String introduction;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "프로젝트 설명", example = "핵심 컨셉: 운동루틴...")
    private String description;

    @ElementCollection
    @CollectionTable(name = "project_tech_stacks", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "tech_stack")
    @Schema(description = "기술 스택 목록")
    @Builder.Default
    private List<String> techStacks = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "project_urls", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "url")
    @Schema(description = "관련 URL 목록")
    @Builder.Default
    private List<String> urls = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "project_image_urls", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "image_url")
    @Schema(description = "이미지 URL 목록")
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @Schema(description = "삭제 시간")
    private LocalDateTime deletedAt;

    public void update(String title, String introduction, String description,
                       List<String> techStacks, List<String> urls, List<String> imageUrls) {
        this.title = title;
        this.introduction = introduction;
        this.description = description;
        this.techStacks = techStacks != null ? techStacks : new ArrayList<>();
        this.urls = urls != null ? urls : new ArrayList<>();
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
