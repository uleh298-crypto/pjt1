package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.datasource.remote.model.Ban
import com.ssafy.ssabree.core.datasource.remote.model.Campus
import com.ssafy.ssabree.core.repository.CampusRepository

class FakeCampusRepository : CampusRepository {
    private val campuses = listOf(
        Campus(id = 1, name = "Seoul"),
        Campus(id = 2, name = "Daejeon"),
        Campus(id = 3, name = "Gwangju"),
        Campus(id = 4, name = "Gumi"),
        Campus(id = 5, name = "Busan")
    )

    private val classesByCampus = mapOf(
        1 to listOf(
            Ban(id = 101, name = "Seoul 1", campus = campuses[0], generation = 14, classNo = 1, trackType = "A", createdAt = null, deletedAt = null, updatedAt = null),
            Ban(id = 102, name = "Seoul 2", campus = campuses[0], generation = 14, classNo = 2, trackType = "B", createdAt = null, deletedAt = null, updatedAt = null)
        ),
        2 to listOf(
            Ban(id = 201, name = "Daejeon 1", campus = campuses[1], generation = 14, classNo = 1, trackType = "A", createdAt = null, deletedAt = null, updatedAt = null)
        ),
        3 to listOf(
            Ban(id = 301, name = "Gwangju 1", campus = campuses[2], generation = 14, classNo = 1, trackType = "A", createdAt = null, deletedAt = null, updatedAt = null)
        ),
        4 to listOf(
            Ban(id = 401, name = "Gumi 1", campus = campuses[3], generation = 14, classNo = 1, trackType = "A", createdAt = null, deletedAt = null, updatedAt = null)
        ),
        5 to listOf(
            Ban(id = 501, name = "Busan 1", campus = campuses[4], generation = 14, classNo = 1, trackType = "A", createdAt = null, deletedAt = null, updatedAt = null)
        )
    )

    override suspend fun getCampuses(): Result<List<Campus>> {
        return runCatching { campuses }
    }

    override suspend fun getClasses(campusId: Int): Result<List<Ban>> {
        return runCatching { classesByCampus[campusId].orEmpty() }
    }
}
