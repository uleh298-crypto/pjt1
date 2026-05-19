package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.repository.DdayRepository
import com.ssafy.ssabree.core.repository.model.DDayModel
import com.ssafy.ssabree.core.repository.model.DdayItemModel

class FakeDdayRepository : DdayRepository {
    override suspend fun fetchDdays(): Result<List<DdayItemModel>> {
        return runCatching {
            listOf(
                DdayItemModel(id = 1, title = "SSAFY 수료", targetDate = "2024-06-01", dDay = 30, iconKey = null),
                DdayItemModel(id = 2, title = "프로젝트 마감", targetDate = "2024-05-10", dDay = 8, iconKey = null)
            )
        }
    }

    override suspend fun getAllDdays(): Result<List<DDayModel>> {
        return runCatching {
            listOf(
                DDayModel(title = "SSAFY 수료", days = 30),
                DDayModel(title = "프로젝트 마감", days = 8),
                DDayModel(title = "월급날", days = 5)
            )
        }
    }
}
