package com.ssafy.ssabree.core.datasource.remote.model

data class PortfolioResponse(
    val id: Long,
    val memberId: Long,
    val memberName: String,
    val title: String,
    val description: String,
    val introduction: String,
    val bojHandle: String?,
    val solvedAcInfo: SolvedAcInfoResponse?,
    val solvedacRank: String?,
    val swTestRank: String?,
    val isVisible: Boolean,
    val createdAt: String?,
    val updatedAt: String?,
    val stacks: List<PortfolioStackResponse>,
    val urls: List<PortfolioUrlResponse>,
    val images: List<PortfolioImageResponse>
)

data class SolvedAcInfoResponse(
    val tier: Int?,
    val tierName: String?,
    val tierImageUrl: String?,
    val rating: Int?,
    val solvedCount: Int?,
    val rank: Int?
)

data class PortfolioStackResponse(
    val id: Long,
    val stackId: Long,
    val stackName: String,
    val stackImgUrl: String?,
    val expertLevel: String?
)

data class PortfolioUrlResponse(
    val id: Long,
    val type: String?,
    val url: String
)

data class PortfolioImageResponse(
    val id: Long,
    val imageUrl: String,
    val orders: Int
)
