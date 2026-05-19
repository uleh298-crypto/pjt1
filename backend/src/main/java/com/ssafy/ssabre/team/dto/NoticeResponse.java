package com.ssafy.ssabre.team.dto;

import com.ssafy.ssabre.team.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "팀 공지사항 응답")
public record NoticeResponse(
        @Schema(description = "공지 ID", example = "10")
        Long id,

        @Schema(description = "팀 ID", example = "1")
        Long teamId,

        @Schema(description = "공지 제목", example = "금주 정기 회의 (26-01-23 19:00)")
        String title,

        @Schema(description = "공지 내용", example = "내용...")
        String content,

        @Schema(description = "상단 고정 여부", example = "true")
        Boolean isPinned,

        @Schema(description = "생성일시")
        LocalDateTime createdAt,

        @Schema(description = "수정일시")
        LocalDateTime updatedAt
) {
    public static NoticeResponse from(Notice notice) {
        return new NoticeResponse(
                notice.getId(),
                notice.getTeam().getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getIsPinned(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
