package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.datasource.remote.ProjectService
import com.ssafy.ssabree.core.datasource.remote.model.toProjectCreateRequest
import com.ssafy.ssabree.core.datasource.remote.model.toProjectUpdateRequest
import com.ssafy.ssabree.core.repository.model.ProjectCreateInfo
import com.ssafy.ssabree.core.repository.model.ProjectModel
import com.ssafy.ssabree.core.repository.model.ProjectUpdateInfo
import com.ssafy.ssabree.core.repository.model.toModels
import com.ssafy.ssabree.core.utils.RetrofitClient

class ProjectRepositoryImpl : ProjectRepository {
    private val projectService = RetrofitClient.instance.create(ProjectService::class.java)

    override suspend fun getProjectsByPortfolio(portfolioId: Long): Result<List<ProjectModel>> {
        return runCatching {
            projectService.getProjects(portfolioId).toModels()
        }
    }

    override suspend fun createProject(info: ProjectCreateInfo): Result<Unit> {
        return runCatching {
            projectService.createProject(info.toProjectCreateRequest())
        }
    }

    override suspend fun updateProject(projectId: Long, info: ProjectUpdateInfo): Result<Unit> {
        return runCatching {
            projectService.updateProject(projectId, info.toProjectUpdateRequest())
        }
    }

    override suspend fun deleteProject(projectId: Long): Result<Unit> {
        return runCatching {
            projectService.deleteProject(projectId)
        }
    }
}
