import Foundation

protocol HomeRepository {
    func fetchHome() async -> Result<HomeModel, Error>
}
