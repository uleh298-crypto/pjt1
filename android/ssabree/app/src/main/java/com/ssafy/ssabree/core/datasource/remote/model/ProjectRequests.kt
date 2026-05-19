package com.ssafy.ssabree.core.datasource.remote.model

data class ProjectCreateRequest(
    val portfolioId: Long,
    val title: String,
    val introduction: String?,
    val description: String?,
    val techStacks: List<String>,
    val urls: List<String>,
    val imageUrls: List<String>
)

data class ProjectUpdateRequest(
    val title: String,
    val introduction: String?,
    val description: String?,
    val techStacks: List<String>,
    val urls: List<String>,
    val imageUrls: List<String>
)
