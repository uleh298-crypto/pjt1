package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.repository.model.PortfolioModel
import com.ssafy.ssabree.core.repository.model.PortfolioCreateInfo
import com.ssafy.ssabree.core.repository.model.PortfolioUpdateInfo
import com.ssafy.ssabree.core.repository.model.SolvedacVerifyInfo

interface PortfolioRepository {
    suspend fun getMyPortfolios(): Result<List<PortfolioModel>>
    suspend fun getPortfolio(id: Long): Result<PortfolioModel>
    suspend fun createPortfolio(info: PortfolioCreateInfo): Result<Long>
    suspend fun updatePortfolio(id: Long, info: PortfolioUpdateInfo): Result<Long>
    suspend fun verifySolvedac(handle: String): Result<SolvedacVerifyInfo>
}
