package com.ssafy.ssabre.portfolio.entity;

import com.ssafy.ssabre.global.entity.BaseEntity;
import com.ssafy.ssabre.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "portfolios")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "포트폴리오 정보")
public class Portfolio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "포트폴리오 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @Schema(description = "회원 정보")
    private Member member;

    @Schema(description = "포트폴리오 제목", example = "백엔드 개발자 포트폴리오")
    private String title;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "포트폴리오 설명", example = "열심히 하겠습니다.")
    private String description;

    @Schema(description = "자기소개", example = "안녕하세요.")
    private String introduction;

    @Column(name = "boj_handle")
    @Schema(description = "백준 아이디", example = "koosaga")
    private String bojHandle;

    @Column(name = "solvedac_rank")
    @Schema(description = "Solved.ac 랭크 (unranked, bronze, silver, gold, platinum, diamond, ruby, master)", example = "gold")
    private String solvedacRank;

    @Column(name = "sw_test_rank")
    @Schema(description = "SW 역량테스트 등급 (N, IM, A, A+, B, C)", example = "A+")
    private String swTestRank;

    @Builder.Default
    @Column(name = "is_visible")
    @Schema(description = "공개 여부", example = "true")
    private Boolean isVisible = true;

    public void update(String title, String description, String introduction, String bojHandle, String swTestRank,
            Boolean isVisible) {
        this.title = title;
        this.description = description;
        this.introduction = introduction;
        this.bojHandle = bojHandle;
        this.swTestRank = swTestRank;
        this.isVisible = isVisible;
    }

    public void updateBojHandle(String bojHandle) {
        this.bojHandle = bojHandle;
    }
}
