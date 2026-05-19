import Foundation

protocol ReportService {
    func createReport(_ request: ReportCreateRequest) async throws
}

struct ReportCreateRequest: Encodable {
    let targetType: String  // POST, COMMENT, USER
    let targetId: Int
    let reason: String
    let detail: String?
}

final class ReportServiceImpl: ReportService {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func createReport(_ request: ReportCreateRequest) async throws {
        print("[ReportService] Creating report: \(request)")
        let endpoint = APIEndpoint(path: "/api/reports", method: .POST, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
        print("[ReportService] Report created successfully")
    }
}
