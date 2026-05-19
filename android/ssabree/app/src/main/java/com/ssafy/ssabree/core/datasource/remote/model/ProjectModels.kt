package com.ssafy.ssabree.core.datasource.remote.model

data class ProjectListResponse(
    val projects: List<ProjectItemResponse>
)

data class ProjectItemResponse(
    val id: Long,
    val title: String,
    val introduction: String?,
    val description: String?,
    val techStacks: List<String>?,
    val urls: List<String>?,
    val imageUrls: List<String>?,
    val createdAt: String?,
    val updatedAt: String?
)
