package com.ssafy.ssabre.post.entity;

import com.ssafy.ssabre.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 게시글별 익명 번호 관리
 * - 각 게시글에서 사용자에게 순차적으로 익명 번호 부여
 * - 같은 게시글에서는 항상 같은 익명 번호 사용
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post_anonymous_numbers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "member_id"}))
public class PostAnonymousNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "anonymous_number", nullable = false)
    private Integer anonymousNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    public PostAnonymousNumber(Post post, Member member, Integer anonymousNumber) {
        this.post = post;
        this.member = member;
        this.anonymousNumber = anonymousNumber;
        this.createdAt = LocalDateTime.now();
    }
}
