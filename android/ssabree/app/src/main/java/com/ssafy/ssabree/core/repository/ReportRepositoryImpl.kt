package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.datasource.remote.ReportService
import com.ssafy.ssabree.core.datasource.remote.model.ReportCreateRequest
import com.ssafy.ssabree.core.repository.model.ReportResult
import com.ssafy.ssabree.core.utils.RetrofitClient

class ReportRepositoryImpl : ReportRepository {

    private val reportService = RetrofitClient.instance.create(ReportService::class.java)

    override suspend fun reportPost(postId: Long, reason: String, detail: String?): Result<ReportResult> {
        return runCatching {
            val response = reportService.createReport(
                ReportCreateRequest(
                    targetType = "POST",
                    targetId = postId,
                    reason = reason,
                    detail = detail
                )
            )
            ReportResult(reportId = response.reportId, success = true)
        }
    }

    override suspend fun reportComment(commentId: Long, reason: String, detail: String?): Result<ReportResult> {
        return runCatching {
            val response = reportService.createReport(
                ReportCreateRequest(
                    targetType = "COMMENT",
                    targetId = commentId,
                    reason = reason,
                    detail = detail
                )
            )
            ReportResult(reportId = response.reportId, success = true)
        }
    }
}
