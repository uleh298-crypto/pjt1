import Foundation

struct HomeResponse: Codable {
    let dDays: [HomeDDayResponse]
    let teamThumbnail: HomeRecruitThumbResponse?
    let studyThumbnail: HomeRecruitThumbResponse?
    let campusMeals: [HomeCampusMealResponse]
    let boardsList: [HomeBoardThumbResponse]
    // 백엔드가 camelCase로 반환하므로 CodingKeys 불필요
}

struct HomeDDayResponse: Codable {
    let title: String
    let days: Int
}

struct HomeRecruitThumbResponse: Codable {
    let name: String?
    let count: Int
}

struct HomeBoardThumbResponse: Codable {
    let boardId: Int
    let name: String
    let recentPostTitle: String?
    // 백엔드가 camelCase로 반환하므로 CodingKeys 불필요
}

struct HomeCampusMealResponse: Codable {
    let campusId: Int
    let campusName: String
    let imageUrls: [String]
    // 백엔드가 camelCase로 반환하므로 CodingKeys 불필요
}
