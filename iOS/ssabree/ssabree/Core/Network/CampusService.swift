import Foundation

protocol CampusService {
    func getCampuses() async throws -> [Campus]
    func getClasses(campusId: Int) async throws -> [Ban]
}

final class CampusServiceImpl: CampusService {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func getCampuses() async throws -> [Campus] {
        // Android: @GET("/api/campuses")
        let endpoint = APIEndpoint(path: "/api/campuses", method: .GET, requiresAuth: false)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getClasses(campusId: Int) async throws -> [Ban] {
        // Android: @GET("/api/campuses/{id}/classes")
        let endpoint = APIEndpoint(path: "/api/campuses/\(campusId)/classes", method: .GET, requiresAuth: false)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }
}
