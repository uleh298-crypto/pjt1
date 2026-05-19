import Foundation

final class KeywordRepositoryImpl: KeywordRepository {
    private let userDefaults: UserDefaults

    init(userDefaults: UserDefaults = .standard) {
        self.userDefaults = userDefaults
    }

    func getPopularKeywords() -> [String] {
        [
            "사브리",
            "사피",
            "자바",
            "코틀린",
            "안드로이드",
            "스프링",
            "취업",
            "알고리즘",
            "코딩테스트",
        ]
    }

    func getRecentKeywords() -> [String] {
        userDefaults.stringArray(forKey: "recent_keywords") ?? []
    }

    func addRecentKeyword(keyword: String) {
        var keywords = getRecentKeywords()
        if let index = keywords.firstIndex(of: keyword) {
            keywords.remove(at: index)
        }
        keywords.insert(keyword, at: 0)
        userDefaults.set(Array(keywords.prefix(10)), forKey: "recent_keywords")
    }

    func deleteRecentKeyword(keyword: String) {
        var keywords = getRecentKeywords()
        keywords.removeAll { $0 == keyword }
        userDefaults.set(keywords, forKey: "recent_keywords")
    }
}
