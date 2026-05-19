package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.repository.InquiryRepository
import com.ssafy.ssabree.core.repository.model.InquiryModel
import java.time.OffsetDateTime

class FakeInquiryRepository : InquiryRepository {
    private val inquiries = mutableListOf(
        InquiryModel(
            id = 1L,
            content = "Can we preview monthly cafeteria menus?\nIt is hard to check daily. Please upload monthly menus.",
            answer = "Thanks for the feedback. We will update next month.",
            createdAt = OffsetDateTime.now().minusDays(1).toString()
        ),
        InquiryModel(
            id = 2L,
            content = "Can we change the study admin?\nThe owner is inactive.",
            answer = null,
            createdAt = OffsetDateTime.now().minusHours(2).toString()
        )
    )

    override suspend fun getInquiries(): Result<List<InquiryModel>> {
        return Result.success(inquiries.toList())
    }

    override suspend fun createInquiry(content: String): Result<Unit> {
        inquiries.add(
            0,
            InquiryModel(
                id = (inquiries.maxOfOrNull { it.id } ?: 0L) + 1L,
                content = content,
                answer = null,
                createdAt = OffsetDateTime.now().toString()
            )
        )
        return Result.success(Unit)
    }
}
