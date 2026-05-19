package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.repository.model.StackModel

interface StackRepository {
    suspend fun getStacks(): Result<List<StackModel>>
}
