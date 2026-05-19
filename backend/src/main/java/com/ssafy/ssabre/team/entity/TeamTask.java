package com.ssafy.ssabre.team.entity;

import com.ssafy.ssabre.global.entity.BaseEntity;
import com.ssafy.ssabre.global.entity.TaskStatus;
import com.ssafy.ssabre.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Entity(name = "TeamTask")
@Table(name = "team_tasks")
@Getter
@NoArgsConstructor
@Schema(description = "팀 업무")
public class TeamTask extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "업무 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    @Schema(description = "팀 정보")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    @Schema(description = "생성자 정보")
    private Member creator;

    @Column(nullable = false)
    @Schema(description = "업무 제목", example = "API 명세서 작성")
    private String title;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "업무 내용", example = "노션에 작성해주세요.")
    private String content;

    @Schema(description = "시작일", example = "2024-02-01")
    private LocalDate startDate;

    @Schema(description = "종료일", example = "2024-02-05")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Schema(description = "진행 상태 (TODO, IN_PROGRESS, DONE)", example = "IN_PROGRESS")
    private TaskStatus status;

    public static TeamTask create(Team team, Member creator, String title, String content, LocalDate startDate, LocalDate endDate,
            TaskStatus status) {
        TeamTask task = new TeamTask();
        task.team = team;
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
