package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.repository.StackRepository
import com.ssafy.ssabree.core.repository.model.StackModel

class FakeStackRepository : StackRepository {
    override suspend fun getStacks(): Result<List<StackModel>> {
        return Result.success(
            listOf(
                StackModel(1L, "Kotlin", null),
                StackModel(2L, "Spring", null),
                StackModel(3L, "React", null)
            )
        )
    }
}
