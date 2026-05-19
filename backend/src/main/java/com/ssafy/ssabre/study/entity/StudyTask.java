package com.ssafy.ssabre.study.entity;

import com.ssafy.ssabre.global.entity.BaseEntity;
import com.ssafy.ssabre.global.entity.TaskStatus;
import com.ssafy.ssabre.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Entity(name = "StudyTask")
@Table(name = "study_tasks")
@Getter
@NoArgsConstructor
@Schema(description = "스터디 업무")
public class StudyTask extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "업무 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    @Schema(description = "스터디 정보")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    @Schema(description = "생성자 정보")
    private Member creator;

    @Column(nullable = false)
    @Schema(description = "업무 제목", example = "알고리즘 문제 풀이")
    private String title;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "업무 내용", example = "백준 1000번 풀기")
    private String content;

    @Schema(description = "시작일", example = "2024-02-01")
    private LocalDate startDate;

    @Schema(description = "종료일", example = "2024-02-05")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Schema(description = "진행 상태 (TODO, IN_PROGRESS, DONE)", example = "IN_PROGRESS")
    private TaskStatus status;

    public static StudyTask create(Study study, Member creator, String title, String content, LocalDate startDate, LocalDate endDate,
            TaskStatus status) {
        StudyTask task = new StudyTask();
        task.study = study;
        task.creator = creator;
        task.title = title;
        task.content = content;
        task.startDate = startDate;
        task.endDate = endDate;
        task.status = status != null ? status : TaskStatus.TODO;
        return task;
    }

    public void update(String title, String content, LocalDate startDate, LocalDate endDate, TaskStatus status) {
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        if (status != null) {
            this.status = status;
        }
    }

    public void updateStatus(TaskStatus status) {
        this.status = status;
    }

    public void clearCreator() {
        this.creator = null;
    }
}
