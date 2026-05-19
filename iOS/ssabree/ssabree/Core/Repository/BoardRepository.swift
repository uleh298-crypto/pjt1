import Foundation

protocol BoardRepository {
    func getBoards() async -> Result<[BoardModel], Error>
    func getNotice() async -> Result<String?, Error>
}
