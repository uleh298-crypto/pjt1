import Foundation

final class UploadRepositoryImpl: UploadRepository {
    private let uploadService: UploadService

    init(uploadService: UploadService) {
        self.uploadService = uploadService
    }

    func uploadImage(image: Data) async -> Result<String, Error> {
        do {
            let response = try await uploadService.uploadImage(image: image)
            return .success(response.url)
        } catch {
            return .failure(error)
        }
    }
}
