import Foundation

// MARK: - Domain Model

struct InquiryModel: Identifiable {
    let id: Int
    let content: String
    let answer: String?
    let createdAt: String?
}

// MARK: - Protocol

protocol InquiryRepository {
    func getInquiries() async -> Result<[InquiryModel], Error>
    func createInquiry(content: String) async -> Result<Void, Error>
}

// MARK: - Implementation

final class InquiryRepositoryImpl: InquiryRepository {
    private let inquiryService: InquiryService

    init(inquiryService: InquiryService) {
        self.inquiryService = inquiryService
    }

    func getInquiries() async -> Result<[InquiryModel], Error> {
        do {
            let response = try await inquiryService.getInquiries()
            let models = response.items.map { dto in
                InquiryModel(
                    id: dto.inquiryId,
                    content: dto.content,
                    answer: dto.answer,
                    createdAt: dto.createdAt
                )
            }
            return .success(models)
        } catch {
            return .failure(error)
        }
    }

    func createInquiry(content: String) async -> Result<Void, Error> {
        do {
            try await inquiryService.createInquiry(content: content)
            return .success(())
        } catch {
            return .failure(error)
        }
    }
}

// MARK: - Fake

final class FakeInquiryRepository: InquiryRepository {
    func getInquiries() async -> Result<[InquiryModel], Error> {
        .success([])
    }

    func createInquiry(content: String) async -> Result<Void, Error> {
        .success(())
    }
}
