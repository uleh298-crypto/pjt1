package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.datasource.remote.model.Ban
import com.ssafy.ssabree.core.datasource.remote.model.Campus

interface CampusRepository {
    suspend fun getCampuses(): Result<List<Campus>>
    suspend fun getClasses(campusId: Int): Result<List<Ban>>
}
