import Foundation

protocol UploadService {
    func uploadImage(image: Data) async throws -> ImageUploadResponse
}

final class UploadServiceImpl: UploadService {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func uploadImage(image: Data) async throws -> ImageUploadResponse {
        let endpoint = APIEndpoint(path: "/api/uploads/images", method: .POST, requiresAuth: true)
        return try await apiClient.upload(
            endpoint: endpoint,
            fileData: image,
            fileName: "image.jpg",
            mimeType: "image/jpeg",
            fieldName: "file"
        )
    }
}
