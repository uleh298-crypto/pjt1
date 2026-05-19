package com.ssafy.ssabree.core.repository.model

data class PortfolioModel(
    val id: Long,
    val memberId: Long,
    val memberName: String,
    val title: String,
    val description: String,
    val introduction: String,
    val bojHandle: String?,
    val solvedAcInfo: SolvedAcInfoModel?,
    val solvedacRank: String?,
    val swTestRank: String?,
    val isVisible: Boolean,
    val createdAt: String?,
    val updatedAt: String?,
    val stacks: List<PortfolioStackModel>,
    val urls: List<PortfolioUrlModel>,
    val images: List<PortfolioImageModel>
)

data class SolvedAcInfoModel(
    val tier: Int?,
    val tierName: String?,
    val tierImageUrl: String?,
    val rating: Int?,
    val solvedCount: Int?,
    val rank: Int?
)

data class PortfolioStackModel(
    val id: Long,
    val stackId: Long,
    val stackName: String,
    val stackImgUrl: String?,
    val expertLevel: String?
)

data class PortfolioUrlModel(
    val id: Long,
    val type: String?,
    val url: String
)

data class PortfolioImageModel(
    val id: Long,
    val imageUrl: String,
    val orders: Int
)

data class PortfolioUpdateInfo(
    val title: String,
    val description: String,
    val introduction: String,
    val bojHandle: String?,
    val solvedacRank: String?,
    val swTestRank: String?,
    val isVisible: Boolean,
    val stacks: List<PortfolioStackUpdateInfo>,
    val urls: List<PortfolioUrlUpdateInfo>,
    val images: List<PortfolioImageUpdateInfo>
)

data class PortfolioCreateInfo(
    val title: String,
    val description: String,
    val introduction: String,
    val bojHandle: String?,
    val solvedacRank: String?,
    val swTestRank: String?,
    val isVisible: Boolean,
    val stacks: List<PortfolioStackUpdateInfo>,
    val urls: List<PortfolioUrlUpdateInfo>,
    val images: List<PortfolioImageUpdateInfo>
)

data class PortfolioStackUpdateInfo(
    val stackId: Long,
    val expertLevel: String
)

data class PortfolioUrlUpdateInfo(
    val url: String
)

data class PortfolioImageUpdateInfo(
    val imageUrl: String,
    val orders: Int
)
