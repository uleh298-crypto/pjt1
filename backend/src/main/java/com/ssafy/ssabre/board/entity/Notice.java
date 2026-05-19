package com.ssafy.ssabre.board.entity;

import com.ssafy.ssabre.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "admin_notice")
@Getter
@NoArgsConstructor
@Schema(description = "관리자 공지사항")
public class Notice extends BaseEntity {

    @Id
    private Long id = 1L;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "공지 내용")
    private String content;

    public void update(String content) {
        if (content != null) this.content = content;
    }

    public static Notice createDefault() {
        Notice notice = new Notice();
        notice.id = 1L;
        notice.content = "";
        return notice;
    }
}
