import Foundation

struct DdayListResponse: Codable {
    let items: [DdayResponse]
}

struct DdayResponse: Codable {
    let id: Int
    let title: String
    let targetDate: String
    let iconKey: String?

    enum CodingKeys: String, CodingKey {
        case id = "ddayId"
        case title
        case targetDate = "date"
        case iconKey = "icon"
    }
}
