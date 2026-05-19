import Foundation

// MARK: - DTOs

struct InquiryCreateRequest: Encodable {
    let content: String
}

struct InquiryResponseDTO: Decodable {
    let inquiryId: Int
    let content: String
    let answer: String?
    let createdAt: String?
}

struct InquiryListResponseDTO: Decodable {
    let items: [InquiryResponseDTO]
}

// MARK: - Service Protocol

protocol InquiryService {
    func getInquiries() async throws -> InquiryListResponseDTO
    func createInquiry(content: String) async throws
}

// MARK: - Service Implementation

final class InquiryServiceImpl: InquiryService {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func getInquiries() async throws -> InquiryListResponseDTO {
        let endpoint = APIEndpoint(path: "/api/inquiries", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func createInquiry(content: String) async throws {
        let endpoint = APIEndpoint(path: "/api/inquiries", method: .POST, requiresAuth: true)
        let request = InquiryCreateRequest(content: content)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }
}
