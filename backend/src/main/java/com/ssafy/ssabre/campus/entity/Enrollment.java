package com.ssafy.ssabre.campus.entity;

import com.ssafy.ssabre.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "enrollment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "반 등록 정보")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "등록 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @Schema(description = "회원 정보")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    @Schema(description = "반 정보")
    private Classes classes;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
