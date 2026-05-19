package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.repository.model.DdayItemModel
import com.ssafy.ssabree.core.repository.model.DDayModel

interface DdayRepository {
    suspend fun fetchDdays(): Result<List<DdayItemModel>>

    /**
     * 모든 D-day 소스를 통합하여 반환 (원격 + 로컬 + 월급날)
     * HomeScreen에서 사용
     */
    suspend fun getAllDdays(): Result<List<DDayModel>>
}
