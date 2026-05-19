package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.datasource.remote.CampusService
import com.ssafy.ssabree.core.datasource.remote.model.Ban
import com.ssafy.ssabree.core.datasource.remote.model.Campus
import com.ssafy.ssabree.core.utils.RetrofitClient

class CampusRepositoryImpl : CampusRepository {

    private val campusService = RetrofitClient.instance.create(CampusService::class.java)

    override suspend fun getCampuses(): Result<List<Campus>> {
        return runCatching {
            campusService.getCampuses()
        }
    }

    override suspend fun getClasses(campusId: Int): Result<List<Ban>> {
        return runCatching {
            campusService.getClasses(campusId)
        }
    }
}
