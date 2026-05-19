import Foundation

protocol UploadRepository {
    func uploadImage(image: Data) async -> Result<String, Error>
}
