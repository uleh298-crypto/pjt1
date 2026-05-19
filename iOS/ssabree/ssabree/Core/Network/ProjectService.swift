import Foundation

protocol ProjectService {
    func getProjects(portfolioId: Int) async throws -> [ProjectResponse]
    func getProjectsByPortfolio(portfolioId: Int) async throws -> [ProjectResponse]
    func createProject(_ request: ProjectCreateRequest) async throws
    func updateProject(projectId: Int, request: ProjectUpdateRequest) async throws
    func deleteProject(projectId: Int) async throws
}

// 프로젝트 목록 응답 wrapper
struct ProjectListResponse: Decodable {
    let projects: [ProjectResponse]
}

struct ProjectResponse: Decodable {
    let id: Int
    let title: String
    let introduction: String?
    let description: String?
    let techStacks: [String]?
    let urls: [String]?
    let imageUrls: [String]?
    let createdAt: String?
    let updatedAt: String?
    let portfolioId: Int?
}

struct ProjectCreateRequest: Encodable {
    let portfolioId: Int
    let title: String
    let introduction: String?
    let description: String?
    let techStacks: [String]
    let urls: [String]
    let imageUrls: [String]
}

struct ProjectUpdateRequest: Encodable {
    let title: String
    let introduction: String?
    let description: String?
    let techStacks: [String]
    let urls: [String]
    let imageUrls: [String]
}

final class ProjectServiceImpl: ProjectService {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func getProjects(portfolioId: Int) async throws -> [ProjectResponse] {
        let endpoint = APIEndpoint(path: "/api/projects/portfolio/\(portfolioId)", method: .GET, requiresAuth: true)
        let response: ProjectListResponse = try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
        return response.projects
    }

    func getProjectsByPortfolio(portfolioId: Int) async throws -> [ProjectResponse] {
        // 올바른 경로: /api/projects/portfolio/{portfolioId}
        let endpoint = APIEndpoint(path: "/api/projects/portfolio/\(portfolioId)", method: .GET, requiresAuth: true)
        let response: ProjectListResponse = try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
        return response.projects
    }

    func createProject(_ request: ProjectCreateRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/projects", method: .POST, requiresAuth: true)
        let _: ProjectSuccessResponse = try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }

    func updateProject(projectId: Int, request: ProjectUpdateRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/projects/\(projectId)", method: .PUT, requiresAuth: true)
        let _: ProjectSuccessResponse = try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }

    func deleteProject(projectId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/projects/\(projectId)", method: .DELETE, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }
}

private struct ProjectDeleteResponse: Decodable {}
private struct ProjectSuccessResponse: Decodable {
    let success: Bool?
}
