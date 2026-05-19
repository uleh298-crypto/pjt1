package com.ssafy.ssabre.post.entity;

import com.ssafy.ssabre.board.entity.Board;
import com.ssafy.ssabre.global.entity.BaseEntity;
import com.ssafy.ssabre.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "posts")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    @Schema(description = "게시판 정보")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @Schema(description = "작성자 정보")
    private Member member;

    @Schema(description = "게시글 제목", example = "오늘 점심 뭐 먹지?")
    private String title;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "게시글 내용 (HTML/Markdown)", example = "돈까스 vs 제육볶음 추천 좀")
    private String content;

    @Column(name = "view_count")
    @Schema(description = "조회수", example = "0")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "like_count")
    @Schema(description = "좋아요 수", example = "0")
    @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "comment_count")
    @Schema(description = "댓글 수", example = "0")
    @Builder.Default
    private Integer commentCount = 0;

    @Column(name = "scrap_count")
    @Schema(description = "스크랩 수", example = "0")
    @Builder.Default
    private Integer scrapCount = 0;

    @Column(name = "is_blinded")
    @Schema(description = "블라인드 여부", example = "false")
    @Builder.Default
    private Boolean isBlinded = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void update(String title, String content) {
        if (title != null)
            this.title = title;
        if (content != null)
            this.content = content;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void clearMember() {
        this.member = null;
    }

    public void incrementLikeCount() {
        this.likeCount = (this.likeCount == null ? 0 : this.likeCount) + 1;
    }

    public void decrementLikeCount() {
        this.likeCount = Math.max(0, (this.likeCount == null ? 0 : this.likeCount) - 1);
    }

    public void incrementScrapCount() {
        this.scrapCount = (this.scrapCount == null ? 0 : this.scrapCount) + 1;
    }

    public void decrementScrapCount() {
        this.scrapCount = Math.max(0, (this.scrapCount == null ? 0 : this.scrapCount) - 1);
    }

    public void incrementCommentCount() {
        this.commentCount = (this.commentCount == null ? 0 : this.commentCount) + 1;
    }

    public void decrementCommentCount() {
        this.commentCount = Math.max(0, (this.commentCount == null ? 0 : this.commentCount) - 1);
    }

    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    public void setBlinded(boolean isBlinded) {
        this.isBlinded = isBlinded;
    }
}
