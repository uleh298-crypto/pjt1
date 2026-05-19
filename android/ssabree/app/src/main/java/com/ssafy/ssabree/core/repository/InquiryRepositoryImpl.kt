package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.datasource.remote.InquiryService
import com.ssafy.ssabree.core.datasource.remote.model.InquiryCreateRequest
import com.ssafy.ssabree.core.repository.model.InquiryModel
import com.ssafy.ssabree.core.repository.model.toModel
import com.ssafy.ssabree.core.utils.RetrofitClient

class InquiryRepositoryImpl : InquiryRepository {
    private val inquiryService = RetrofitClient.instance.create(InquiryService::class.java)

    override suspend fun getInquiries(): Result<List<InquiryModel>> {
        return runCatching {
            inquiryService.getInquiries().items.map { it.toModel() }
        }
    }

    override suspend fun createInquiry(content: String): Result<Unit> {
        return runCatching {
            val response = inquiryService.createInquiry(InquiryCreateRequest(content = content))
            if (!response.success) {
                error("Inquiry creation failed")
            }
        }
    }
}
