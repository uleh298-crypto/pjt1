package com.ssafy.ssabre.report.dto;

import com.ssafy.ssabre.report.entity.Report;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record ReportResponse(
        @Schema(description = "신고 ID") Long id,

        @Schema(description = "신고자 ID") Long reporterId,

        @Schema(description = "신고자 이름") String reporterName,

        @Schema(description = "게시글 ID") Long postId,

        @Schema(description = "댓글 ID") Long commentId,

        @Schema(description = "신고 사유") String reason,

        @Schema(description = "생성일") LocalDateTime createdAt) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getReporter() != null ? report.getReporter().getId() : null,
                report.getReporter() != null ? report.getReporter().getName() : "탈퇴한 사용자",
                report.getPost() != null ? report.getPost().getId() : null,
                report.getComment() != null ? report.getComment().getId() : null,
                report.getReason(),
                report.getCreatedAt());
    }
}
