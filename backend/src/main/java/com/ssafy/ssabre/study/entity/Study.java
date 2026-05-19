package com.ssafy.ssabre.study.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ssafy.ssabre.campus.entity.Campus;
import com.ssafy.ssabre.global.entity.BaseEntity;
import com.ssafy.ssabre.global.entity.GroupStatus;
import com.ssafy.ssabre.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(name = "Study")
@Table(name = "studies")
@Getter
@NoArgsConstructor
@Schema(description = "스터디 정보")
public class Study extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "스터디 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Schema(description = "스터디장 정보")
    private Member leader;

    @Schema(description = "스터디 이름", example = "알고리즘 스터디")
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Schema(description = "캠퍼스 정보")
    private Campus campus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Schema(description = "스터디 종류 (CS, ALGORITHM, SW_TEST_A, SW_TEST_B, CERTIFICATION, ETC)", example = "ALGORITHM")
    private StudyType type;

    @Schema(description = "모집 인원", example = "4")
    private Integer capacity;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "스터디 설명", example = "매주 알고리즘 문제 풀이 진행합니다.")
    private String description;

    @Schema(description = "시작일", example = "2024-02-01")
    private LocalDate startDate;

    @Schema(description = "종료일", example = "2024-03-01")
    private LocalDate endDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Schema(description = "모집 상태 (OPEN, ONGOING, CLOSED)", example = "OPEN")
    private GroupStatus status = GroupStatus.OPEN;

    @Column(name = "deleted_at")
    @Schema(description = "삭제 일시")
    private LocalDateTime deletedAt;

    public static Study create(Member leader, String title, StudyType type, Integer capacity, String description,
            Campus campus, LocalDate startDate, LocalDate endDate) {
        Study study = new Study();
        study.leader = leader;
        study.title = title;
        study.type = type;
        study.capacity = capacity;
        study.description = description;
        study.campus = campus;
        study.startDate = startDate;
        study.endDate = endDate;
        study.status = GroupStatus.OPEN;
        return study;
    }

    public void update(String title, StudyType type, Integer capacity, String description, GroupStatus status,
            Campus campus, LocalDate startDate, LocalDate endDate) {
        if (title != null)
            this.title = title;
        if (type != null)
            this.type = type;
        if (capacity != null)
            this.capacity = capacity;
        if (description != null)
            this.description = description;
        if (status != null)
            this.status = status;
        if (campus != null)
            this.campus = campus;
        if (startDate != null)
            this.startDate = startDate;
        if (endDate != null)
            this.endDate = endDate;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isOpen() {
        return this.status == GroupStatus.OPEN && !isDeleted();
    }

    public void clearLeader() {
        this.leader = null;
    }
}
