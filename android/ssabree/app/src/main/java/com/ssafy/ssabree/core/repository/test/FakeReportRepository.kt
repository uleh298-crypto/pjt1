package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.repository.ReportRepository
import com.ssafy.ssabree.core.repository.model.ReportResult

class FakeReportRepository : ReportRepository {

    override suspend fun reportPost(postId: Long, reason: String, detail: String?): Result<ReportResult> {
        return Result.success(ReportResult(reportId = 1L, success = true))
    }

    override suspend fun reportComment(commentId: Long, reason: String, detail: String?): Result<ReportResult> {
        return Result.success(ReportResult(reportId = 1L, success = true))
    }
}
