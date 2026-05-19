package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.repository.model.InquiryModel

interface InquiryRepository {
    suspend fun getInquiries(): Result<List<InquiryModel>>
    suspend fun createInquiry(content: String): Result<Unit>
}
