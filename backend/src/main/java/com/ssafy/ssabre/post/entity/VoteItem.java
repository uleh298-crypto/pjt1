package com.ssafy.ssabre.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "vote_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "투표 항목 정보")
public class VoteItem {

    @Builder
    public VoteItem(Vote vote, String content, Integer itemOrder) {
        this.vote = vote;
        this.content = content;
        this.itemOrder = itemOrder != null ? itemOrder : 0;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "항목 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    @Schema(description = "투표 정보")
    private Vote vote;

    @Column(nullable = false)
    @Schema(description = "항목 내용", example = "짜장면")
    private String content;

    @Column(name = "item_order", nullable = false)
    @Schema(description = "항목 순서", example = "1")
    private Integer itemOrder = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
