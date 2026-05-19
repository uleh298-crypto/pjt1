package com.ssafy.ssabre.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "blocked_members")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "차단된 회원 정보")
public class BlockedMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "차단 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    @Schema(description = "차단한 회원")
    private Member blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    @Schema(description = "차단된 회원")
    private Member blocked;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
