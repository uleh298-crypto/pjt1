package com.ssafy.ssabree.core.datasource.remote.model

data class PortfolioUpdateRequest(
    val title: String,
    val description: String,
    val introduction: String,
    val bojHandle: String?,
    val solvedacRank: String?,
    val swTestRank: String?,
    val isVisible: Boolean,
    val stacks: List<PortfolioStackRequest>,
    val urls: List<PortfolioUrlRequest>,
    val images: List<PortfolioImageRequest>
)

data class PortfolioCreateRequest(
    val title: String,
    val description: String,
    val introduction: String,
    val bojHandle: String?,
    val solvedacRank: String?,
    val swTestRank: String?,
    val isVisible: Boolean,
    val stacks: List<PortfolioStackRequest>,
    val urls: List<PortfolioUrlRequest>,
    val images: List<PortfolioImageRequest>
)

data class PortfolioStackRequest(
    val stackId: Long,
    val expertLevel: String
)

data class PortfolioUrlRequest(
    val url: String
)

data class PortfolioImageRequest(
    val imageUrl: String,
    val orders: Int
)
