package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.repository.ProjectRepository
import com.ssafy.ssabree.core.repository.model.ProjectCreateInfo
import com.ssafy.ssabree.core.repository.model.ProjectModel
import com.ssafy.ssabree.core.repository.model.ProjectUpdateInfo

class FakeProjectRepository : ProjectRepository {
    private val projects = mutableListOf(
        ProjectModel(
            id = 1L,
            title = "Autonomous Driving Simulator",
            introduction = "Sensor-data-based autonomous simulator",
            description = "Resolved data transmission latency issues.",
            techStacks = listOf("React", "Python", "ROS2"),
            urls = listOf("https://github.com/ssafy-user/autodrive"),
            imageUrls = emptyList(),
            createdAt = "2025-01-01T10:00:00",
            updatedAt = "2025-02-01T10:00:00"
        )
    )

    override suspend fun getProjectsByPortfolio(portfolioId: Long): Result<List<ProjectModel>> {
        return Result.success(projects.toList())
    }

    override suspend fun createProject(info: ProjectCreateInfo): Result<Unit> {
        val newId = (projects.maxOfOrNull { it.id } ?: 0L) + 1L
        projects.add(
            ProjectModel(
                id = newId,
                title = info.title,
                introduction = info.introduction,
                description = info.description,
                techStacks = info.techStacks,
                urls = info.urls,
                imageUrls = info.imageUrls,
                createdAt = "2026-02-01T00:00:00",
                updatedAt = "2026-02-01T00:00:00"
            )
        )
        return Result.success(Unit)
    }

    override suspend fun updateProject(projectId: Long, info: ProjectUpdateInfo): Result<Unit> {
        val index = projects.indexOfFirst { it.id == projectId }
        if (index < 0) return Result.failure(IllegalArgumentException("Project not found"))
        projects[index] = projects[index].copy(
            title = info.title,
            introduction = info.introduction,
            description = info.description,
            techStacks = info.techStacks,
            urls = info.urls,
            imageUrls = info.imageUrls,
            updatedAt = "2026-02-01T00:00:00"
        )
        return Result.success(Unit)
    }

    override suspend fun deleteProject(projectId: Long): Result<Unit> {
        projects.removeIf { it.id == projectId }
        return Result.success(Unit)
    }
}
