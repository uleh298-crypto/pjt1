import Foundation

struct ProjectModel: Identifiable, Equatable {
    let id: Int
    let title: String
    let introduction: String?
    let description: String?
    let techStacks: [String]
    let urls: [String]
    let imageUrls: [String]
    let createdAt: String?
    let updatedAt: String?
    let portfolioId: Int?
}

struct ProjectCreateInfo {
    let portfolioId: Int
    let title: String
    let introduction: String?
    let description: String?
    let techStacks: [String]
    let urls: [String]
    let imageUrls: [String]
}

struct ProjectUpdateInfo {
    let title: String
    let introduction: String?
    let description: String?
    let techStacks: [String]
    let urls: [String]
    let imageUrls: [String]
}

protocol ProjectRepository {
    func getProjects(portfolioId: Int) async -> Result<[ProjectModel], Error>
    func getProjectsByPortfolio(portfolioId: Int) async -> Result<[ProjectModel], Error>
    func createProject(info: ProjectCreateInfo) async -> Result<Void, Error>
    func updateProject(projectId: Int, info: ProjectUpdateInfo) async -> Result<Void, Error>
    func deleteProject(projectId: Int) async -> Result<Void, Error>
}

final class ProjectRepositoryImpl: ProjectRepository {
    private let projectService: ProjectService

    init(projectService: ProjectService) {
        self.projectService = projectService
    }

    func getProjects(portfolioId: Int) async -> Result<[ProjectModel], Error> {
        do {
            let response = try await projectService.getProjects(portfolioId: portfolioId)
            return .success(response.map { $0.toModel() })
        } catch {
            return .failure(error)
        }
    }

    func getProjectsByPortfolio(portfolioId: Int) async -> Result<[ProjectModel], Error> {
        do {
            let response = try await projectService.getProjectsByPortfolio(portfolioId: portfolioId)
            return .success(response.map { $0.toModel() })
        } catch {
            return .failure(error)
        }
    }

    func createProject(info: ProjectCreateInfo) async -> Result<Void, Error> {
        do {
            let request = ProjectCreateRequest(
                portfolioId: info.portfolioId,
                title: info.title,
                introduction: info.introduction,
                description: info.description,
                techStacks: info.techStacks,
                urls: info.urls,
                imageUrls: info.imageUrls
            )
            try await projectService.createProject(request)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func updateProject(projectId: Int, info: ProjectUpdateInfo) async -> Result<Void, Error> {
        do {
            let request = ProjectUpdateRequest(
                title: info.title,
                introduction: info.introduction,
                description: info.description,
                techStacks: info.techStacks,
                urls: info.urls,
                imageUrls: info.imageUrls
            )
            try await projectService.updateProject(projectId: projectId, request: request)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func deleteProject(projectId: Int) async -> Result<Void, Error> {
        do {
            try await projectService.deleteProject(projectId: projectId)
            return .success(())
        } catch {
            return .failure(error)
        }
    }
}

final class FakeProjectRepository: ProjectRepository {
    func getProjects(portfolioId: Int) async -> Result<[ProjectModel], Error> {
        .success([])
    }

    func getProjectsByPortfolio(portfolioId: Int) async -> Result<[ProjectModel], Error> {
        .success([
            ProjectModel(
                id: 1,
                title: "SSABREE",
                introduction: "싸피 커뮤니티 앱",
                description: "싸피 교육생들을 위한 커뮤니티 서비스입니다.",
                techStacks: ["Swift", "SwiftUI", "Kotlin", "Jetpack Compose"],
                urls: ["https://github.com/ssafy/ssabree"],
                imageUrls: [],
                createdAt: "2024-01-01T00:00:00",
                updatedAt: "2024-01-15T00:00:00",
                portfolioId: portfolioId
            )
        ])
    }

    func createProject(info: ProjectCreateInfo) async -> Result<Void, Error> {
        .success(())
    }

    func updateProject(projectId: Int, info: ProjectUpdateInfo) async -> Result<Void, Error> {
        .success(())
    }

    func deleteProject(projectId: Int) async -> Result<Void, Error> {
        .success(())
    }
}

extension ProjectResponse {
    func toModel() -> ProjectModel {
        ProjectModel(
            id: id,
            title: title,
            introduction: introduction,
            description: description,
            techStacks: techStacks ?? [],
            urls: urls ?? [],
            imageUrls: imageUrls ?? [],
            createdAt: createdAt,
            updatedAt: updatedAt,
            portfolioId: portfolioId
        )
    }
}
