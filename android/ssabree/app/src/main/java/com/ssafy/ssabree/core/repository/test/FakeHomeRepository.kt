package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.repository.HomeRepository
import com.ssafy.ssabree.core.repository.model.HomeModel

class FakeHomeRepository: HomeRepository {
    override suspend fun fetchHome(): Result<HomeModel> {
        return runCatching {
            HomeModel.sample
        }
    }
}
