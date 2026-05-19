package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.repository.model.ProjectModel
import com.ssafy.ssabree.core.repository.model.ProjectCreateInfo
import com.ssafy.ssabree.core.repository.model.ProjectUpdateInfo

interface ProjectRepository {
    suspend fun getProjectsByPortfolio(portfolioId: Long): Result<List<ProjectModel>>
    suspend fun createProject(info: ProjectCreateInfo): Result<Unit>
    suspend fun updateProject(projectId: Long, info: ProjectUpdateInfo): Result<Unit>
    suspend fun deleteProject(projectId: Long): Result<Unit>
}
