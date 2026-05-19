package com.ssafy.ssabre.team.entity;

import com.ssafy.ssabre.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity(name = "TeamNotice")
@Table(name = "team_notices")
@Getter
@NoArgsConstructor
@Schema(description = "팀 공지사항")
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "공지 ID", example = "10")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    @Schema(description = "팀 정보")
    private Team team;

    @Column(nullable = false)
    @Schema(description = "공지 제목", example = "금주 정기 회의 (26-01-23 19:00)")
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    @Schema(description = "공지 내용", example = "내용...")
    private String content;

    @Column(nullable = false)
    @Schema(description = "상단 고정 여부", example = "true")
    private Boolean isPinned;

    public static Notice create(Team team, String title, String content, Boolean isPinned) {
        Notice notice = new Notice();
        notice.team = team;
        notice.title = title;
        notice.content = content;
        notice.isPinned = isPinned != null ? isPinned : false;
        return notice;
    }

    public void update(String title, String content, Boolean isPinned) {
        this.title = title;
        this.content = content;
        this.isPinned = isPinned != null ? isPinned : false;
    }
}
