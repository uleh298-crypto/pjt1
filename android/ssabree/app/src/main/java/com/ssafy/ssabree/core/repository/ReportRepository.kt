package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.repository.model.ReportResult

interface ReportRepository {
    suspend fun reportPost(postId: Long, reason: String, detail: String?): Result<ReportResult>
    suspend fun reportComment(commentId: Long, reason: String, detail: String?): Result<ReportResult>
}
