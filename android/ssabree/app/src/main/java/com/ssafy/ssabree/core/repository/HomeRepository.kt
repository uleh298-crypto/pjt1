package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.repository.model.HomeModel

interface HomeRepository {
    suspend fun fetchHome(): Result<HomeModel>
}