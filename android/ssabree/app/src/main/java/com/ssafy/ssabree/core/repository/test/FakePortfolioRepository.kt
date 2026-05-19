package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.repository.PortfolioRepository
import com.ssafy.ssabree.core.repository.model.PortfolioCreateInfo
import com.ssafy.ssabree.core.repository.model.PortfolioImageModel
import com.ssafy.ssabree.core.repository.model.PortfolioModel
import com.ssafy.ssabree.core.repository.model.PortfolioStackModel
import com.ssafy.ssabree.core.repository.model.PortfolioUpdateInfo
import com.ssafy.ssabree.core.repository.model.PortfolioUrlModel
import com.ssafy.ssabree.core.repository.model.SolvedAcInfoModel
import com.ssafy.ssabree.core.repository.model.SolvedacVerifyInfo

class FakePortfolioRepository : PortfolioRepository {
    private var portfolios = mutableListOf(
        PortfolioModel(
            id = 1L,
            memberId = 1L,
            memberName = "Kim",
            title = "Backend Developer Portfolio",
            description = "I will do my best.",
            introduction = "Hello, I am a developer who keeps growing.",
            bojHandle = "koosaga",
            solvedAcInfo = SolvedAcInfoModel(
                tier = 15,
                tierName = "Gold 1",
                tierImageUrl = "https://static.solved.ac/tier_small/15.svg",
                rating = 1500,
                solvedCount = 500,
                rank = 10000
            ),
            solvedacRank = "gold",
            swTestRank = "A+",
            isVisible = true,
            createdAt = "2026-01-01T10:00:00",
            updatedAt = "2026-01-01T10:00:00",
            stacks = listOf(
                PortfolioStackModel(1L, 1L, "Kotlin", null, "high"),
                PortfolioStackModel(2L, 2L, "Spring", null, "mid")
            ),
            urls = listOf(
                PortfolioUrlModel(1L, "github", "https://github.com/ssafy-user")
            ),
            images = listOf<PortfolioImageModel>()
        )
    )

    override suspend fun getMyPortfolios(): Result<List<PortfolioModel>> {
        return Result.success(portfolios.toList())
    }

    override suspend fun getPortfolio(id: Long): Result<PortfolioModel> {
        val portfolio = portfolios.firstOrNull { it.id == id }
            ?: return Result.failure(IllegalArgumentException("Portfolio not found"))
        return Result.success(portfolio)
    }

    override suspend fun createPortfolio(info: PortfolioCreateInfo): Result<Long> {
        val newId = (portfolios.maxOfOrNull { it.id } ?: 0L) + 1L
        val newPortfolio = PortfolioModel(
            id = newId,
            memberId = 1L,
            memberName = "Kim",
            title = info.title,
            description = info.description,
            introduction = info.introduction,
            bojHandle = info.bojHandle,
            solvedAcInfo = null,
            solvedacRank = info.solvedacRank,
            swTestRank = info.swTestRank,
            isVisible = info.isVisible,
            createdAt = "2026-02-01T00:00:00",
            updatedAt = "2026-02-01T00:00:00",
            stacks = info.stacks.mapIndexed { index, stack ->
                PortfolioStackModel(
                    id = index.toLong() + 1L,
                    stackId = stack.stackId,
                    stackName = "Stack ${stack.stackId}",
                    stackImgUrl = null,
                    expertLevel = stack.expertLevel
                )
            },
            urls = info.urls.mapIndexed { index, url ->
                PortfolioUrlModel(
                    id = index.toLong() + 1L,
                    type = "link",
                    url = url.url
                )
            },
            images = info.images.mapIndexed { index, image ->
                PortfolioImageModel(
                    id = index.toLong() + 1L,
                    imageUrl = image.imageUrl,
                    orders = image.orders
                )
            }
        )
        portfolios.add(newPortfolio)
        return Result.success(newId)
    }

    override suspend fun updatePortfolio(id: Long, info: PortfolioUpdateInfo): Result<Long> {
        portfolios = portfolios.map { portfolio ->
            if (portfolio.id == id) {
                portfolio.copy(
                    title = info.title,
                    description = info.description,
                    introduction = info.introduction,
                    bojHandle = info.bojHandle,
                    solvedAcInfo = null,
                    solvedacRank = info.solvedacRank,
                    swTestRank = info.swTestRank,
                    isVisible = info.isVisible,
                    stacks = info.stacks.mapIndexed { index, stack ->
                        PortfolioStackModel(
                            id = index.toLong() + 1L,
                            stackId = stack.stackId,
                            stackName = "Stack ${stack.stackId}",
                            stackImgUrl = null,
                            expertLevel = stack.expertLevel
                        )
                    },
                    urls = info.urls.mapIndexed { index, url ->
                        PortfolioUrlModel(
                            id = index.toLong() + 1L,
                            type = "link",
                            url = url.url
                        )
                    },
                    images = info.images.mapIndexed { index, image ->
                        PortfolioImageModel(
                            id = index.toLong() + 1L,
                            imageUrl = image.imageUrl,
                            orders = image.orders
                        )
                    }
                )
            } else {
                portfolio
            }
        }.toMutableList()
        return Result.success(id)
    }

    override suspend fun verifySolvedac(handle: String): Result<SolvedacVerifyInfo> {
        if (handle.isBlank()) {
            return Result.failure(IllegalArgumentException("아이디를 입력해주세요."))
        }
        return Result.success(
            SolvedacVerifyInfo(
                handle = handle,
                tier = 15,
                rating = 1500,
                solvedCount = 500,
                classValue = 4,
                rank = 10000
            )
        )
    }
}
