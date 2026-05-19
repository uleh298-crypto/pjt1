package com.ssafy.ssabre.report.entity;

import com.ssafy.ssabre.comment.entity.Comment;
import com.ssafy.ssabre.global.entity.BaseEntity;
import com.ssafy.ssabre.member.entity.Member;
import com.ssafy.ssabre.post.entity.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor
@lombok.AllArgsConstructor
@lombok.Builder
@Schema(description = "신고 정보")
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "신고 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    @Schema(description = "신고자 정보")
    private Member reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    @Schema(description = "신고된 게시글 (선택)")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    @Schema(description = "신고된 댓글 (선택)")
    private Comment comment;

    @Column(nullable = false)
    @Schema(description = "신고 사유", example = "ABUSE")
    private String reason;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "상세 내용", example = "욕설이 포함되어 있습니다")
    private String detail;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
