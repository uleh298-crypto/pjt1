import Foundation

struct HomeModel {
    let dDays: [DDayModel]
    let boards: [HomeBoardModel]
    let team: TeamRecruitModel?
    let study: StudyRecruitModel?
    let lunchMenu: [CampusMealModel]

    static let empty = HomeModel(
        dDays: [],
        boards: [],
        team: nil,
        study: nil,
        lunchMenu: []
    )

    static let sample = HomeModel(
        dDays: [DDayModel(title: "수료", days: 30)],
        boards: [
            HomeBoardModel(id: 1, name: "자유게시판", recentPostTitle: "안녕하세요"),
            HomeBoardModel(id: 2, name: "취업게시판", recentPostTitle: "면접 팁 공유")
        ],
        team: TeamRecruitModel(name: "프로젝트", count: 5),
        study: StudyRecruitModel(name: "알고리즘 스터디", count: 3),
        lunchMenu: []
    )
}

struct DDayModel: Identifiable {
    let id = UUID()
    let title: String
    let days: Int
}

struct HomeBoardModel: Identifiable {
    let id: Int
    let name: String
    let recentPostTitle: String?
}

struct TeamRecruitModel {
    let name: String?
    let count: Int
}

struct StudyRecruitModel {
    let name: String?
    let count: Int
}

struct CampusMealModel: Identifiable {
    let id = UUID()
    let campusId: Int
    let campusName: String
    let imageUrls: [String]
}
