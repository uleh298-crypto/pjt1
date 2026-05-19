package com.ssafy.ssabre.comment.entity;

import com.ssafy.ssabre.global.entity.BaseEntity;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.post.entity.Post;
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
@Table(name = "comments")
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "댓글 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @Schema(description = "게시글 정보")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @Schema(description = "작성자 정보")
    private Member member;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "댓글 내용", example = "좋은 정보 감사합니다.")
    private String content;

    @Column(name = "parent_id")
    @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "null")
    private Long parentId;

    @Builder.Default
    @Column(name = "like_count")
    @Schema(description = "좋아요 수", example = "0")
    private Integer likeCount = 0;

    @Builder.Default
    @Column(name = "is_blinded")
    @Schema(description = "블라인드 여부", example = "false")
    private Boolean isBlinded = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void update(String content) {
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

    public void setBlinded(boolean isBlinded) {
        this.isBlinded = isBlinded;
    }
}
