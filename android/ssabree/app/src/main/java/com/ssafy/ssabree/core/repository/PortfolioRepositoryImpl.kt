package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.datasource.remote.PortfolioService
import com.ssafy.ssabree.core.datasource.remote.model.toPortfolioCreateRequest
import com.ssafy.ssabree.core.datasource.remote.model.toPortfolioUpdateRequest
import com.ssafy.ssabree.core.repository.model.PortfolioModel
import com.ssafy.ssabree.core.repository.model.PortfolioCreateInfo
import com.ssafy.ssabree.core.repository.model.PortfolioUpdateInfo
import com.ssafy.ssabree.core.repository.model.SolvedacVerifyInfo
import com.ssafy.ssabree.core.repository.model.toModel
import com.ssafy.ssabree.core.utils.RetrofitClient

class PortfolioRepositoryImpl : PortfolioRepository {
    private val portfolioService = RetrofitClient.instance.create(PortfolioService::class.java)

    override suspend fun getMyPortfolios(): Result<List<PortfolioModel>> {
        return runCatching {
            portfolioService.getMyPortfolios().map { it.toModel() }
        }
    }

    override suspend fun getPortfolio(id: Long): Result<PortfolioModel> {
        return runCatching {
            portfolioService.getPortfolio(id).toModel()
        }
    }

    override suspend fun createPortfolio(info: PortfolioCreateInfo): Result<Long> {
        return runCatching {
            portfolioService.createPortfolio(info.toPortfolioCreateRequest())
        }
    }

    override suspend fun updatePortfolio(id: Long, info: PortfolioUpdateInfo): Result<Long> {
        return runCatching {
            portfolioService.updatePortfolio(id, info.toPortfolioUpdateRequest())
        }
    }

    override suspend fun verifySolvedac(handle: String): Result<SolvedacVerifyInfo> {
        return runCatching {
            val response = portfolioService.verifySolvedac(handle)
            SolvedacVerifyInfo(
                handle = response.handle,
                tier = response.tier,
                rating = response.rating,
                solvedCount = response.solvedCount,
                classValue = response.classValue,
                rank = response.rank
            )
        }
    }
}
