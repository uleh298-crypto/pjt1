package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.datasource.remote.StackService
import com.ssafy.ssabree.core.repository.model.StackModel
import com.ssafy.ssabree.core.repository.model.toModel
import com.ssafy.ssabree.core.utils.RetrofitClient

class StackRepositoryImpl : StackRepository {
    private val stackService = RetrofitClient.instance.create(StackService::class.java)

    override suspend fun getStacks(): Result<List<StackModel>> {
        return runCatching {
            stackService.getStacks().map { it.toModel() }
        }
    }
}
