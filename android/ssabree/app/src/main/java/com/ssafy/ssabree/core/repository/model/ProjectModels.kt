package com.ssafy.ssabree.core.repository.model

data class ProjectModel(
    val id: Long,
    val title: String,
    val introduction: String?,
    val description: String?,
    val techStacks: List<String>,
    val urls: List<String>,
    val imageUrls: List<String>,
    val createdAt: String?,
    val updatedAt: String?
)

data class ProjectCreateInfo(
    val portfolioId: Long,
    val title: String,
    val introduction: String?,
    val description: String?,
    val techStacks: List<String>,
    val urls: List<String>,
    val imageUrls: List<String>
)

data class ProjectUpdateInfo(
    val title: String,
    val introduction: String?,
    val description: String?,
    val techStacks: List<String>,
    val urls: List<String>,
    val imageUrls: List<String>
)
