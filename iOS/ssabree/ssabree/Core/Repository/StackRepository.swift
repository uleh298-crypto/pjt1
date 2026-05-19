import Foundation

struct StackModel {
    let id: Int
    let name: String
    let imgUrl: String?
}

protocol StackRepository {
    func getStacks() async -> Result<[StackModel], Error>
}

final class StackRepositoryImpl: StackRepository {
    private let stackService: StackService

    init(stackService: StackService) {
        self.stackService = stackService
    }

    func getStacks() async -> Result<[StackModel], Error> {
        do {
            let response = try await stackService.getStacks()
            return .success(response.map { $0.toModel() })
        } catch {
            return .failure(error)
        }
    }
}

final class FakeStackRepository: StackRepository {
    func getStacks() async -> Result<[StackModel], Error> {
        .success([])
    }
}

extension StackResponse {
    func toModel() -> StackModel {
        StackModel(id: id, name: name, imgUrl: imgUrl)
    }
}
