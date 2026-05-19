import Foundation

struct BoardModel {
    let id: Int
    let title: String  // name을 title로 매핑
    let category: String?
    let description: String?
}

final class BoardRepositoryImpl: BoardRepository {
    private let boardService: BoardService

    init(boardService: BoardService) {
        self.boardService = boardService
    }

    func getBoards() async -> Result<[BoardModel], Error> {
        do {
            let response = try await boardService.getBoards()
            return .success(response.map { $0.toRepositoryModel() })
        } catch {
            print("BoardRepository getBoards error: \(error)")
            return .failure(error)
        }
    }

    func getNotice() async -> Result<String?, Error> {
        do {
            let response = try await boardService.getNotice()
            return .success(response.content)
        } catch {
            print("BoardRepository getNotice error: \(error)")
            return .failure(error)
        }
    }
}

extension BoardResponse {
    func toRepositoryModel() -> BoardModel {
        BoardModel(id: id, title: name, category: category, description: description)
    }
}
