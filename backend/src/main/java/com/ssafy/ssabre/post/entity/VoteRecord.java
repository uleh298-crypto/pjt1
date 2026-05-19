package com.ssafy.ssabre.post.entity;

import com.ssafy.ssabre.member.entity.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "vote_records")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "투표 참여 기록")
public class VoteRecord {

    @Builder
    public VoteRecord(Vote vote, Member member, VoteItem item) {
        this.vote = vote;
        this.member = member;
        this.item = item;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "기록 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    @Schema(description = "투표 정보")
    private Vote vote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @Schema(description = "참여자 정보")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    @Schema(description = "선택한 항목")
    private VoteItem item;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void updateItem(VoteItem item) {
        this.item = item;
    }
}
