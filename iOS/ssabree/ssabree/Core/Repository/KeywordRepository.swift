import Foundation

protocol KeywordRepository {
    func getPopularKeywords() -> [String]
    func getRecentKeywords() -> [String]
    func addRecentKeyword(keyword: String)
    func deleteRecentKeyword(keyword: String)
}
