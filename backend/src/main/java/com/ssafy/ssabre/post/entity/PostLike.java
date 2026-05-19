package com.ssafy.ssabre.post.entity;

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
@Table(name = "post_likes")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@IdClass(PostLike.PostLikeId.class)
@Schema(description = "게시글 좋아요 정보")
public class PostLike {

    public PostLike(Long postId, Long memberId) {
        this.postId = postId;
        this.memberId = memberId;
    }

    @Id
    @Column(name = "post_id")
    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Id
    @Column(name = "member_id")
    @Schema(description = "회원 ID", example = "1")
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    private Member member;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PostLikeId implements Serializable {
        private Long postId;
        private Long memberId;
    }
}
