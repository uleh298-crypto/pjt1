package com.ssafy.ssabre.comment.entity;

import com.ssafy.ssabre.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "comment_likes")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@IdClass(CommentLike.CommentLikeId.class)
@Schema(description = "댓글 좋아요 정보")
public class CommentLike {

    public CommentLike(Long memberId, Long commentId) {
        this.memberId = memberId;
        this.commentId = commentId;
    }

    @Id
    @Column(name = "member_id")
    @Schema(description = "회원 ID", example = "1")
    private Long memberId;

    @Id
    @Column(name = "comment_id")
    @Schema(description = "댓글 ID", example = "1")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", insertable = false, updatable = false)
    private Comment comment;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class CommentLikeId implements Serializable {
        private Long memberId;
        private Long commentId;
    }
}
