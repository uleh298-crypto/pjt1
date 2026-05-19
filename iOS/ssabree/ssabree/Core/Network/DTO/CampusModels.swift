import Foundation

struct Campus: Codable, Hashable, Identifiable {
    let id: Int
    let name: String
}

// API 응답에 맞춘 Ban 모델
// /api/campuses/{id}/classes 응답: Classes 엔티티 직접 반환
// campus 객체가 포함될 수 있어서 두 가지 형태 모두 지원
struct Ban: Codable, Hashable, Identifiable {
    let id: Int
    let name: String
    let generation: Int?
    let classNo: Int?
    let trackType: String?

    // campusId가 직접 올 수도 있고, campus 객체로 올 수도 있음
    let campusId: Int?
    let campus: Campus?

    // 실제 campusId 값을 가져오는 computed property
    var resolvedCampusId: Int {
        campusId ?? campus?.id ?? 0
    }

    // 커스텀 디코딩으로 유연하게 처리
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decode(Int.self, forKey: .id)
        name = try container.decode(String.self, forKey: .name)
        generation = try container.decodeIfPresent(Int.self, forKey: .generation)
        classNo = try container.decodeIfPresent(Int.self, forKey: .classNo)
        trackType = try container.decodeIfPresent(String.self, forKey: .trackType)
        campusId = try container.decodeIfPresent(Int.self, forKey: .campusId)
        campus = try container.decodeIfPresent(Campus.self, forKey: .campus)
    }

    enum CodingKeys: String, CodingKey {
        case id, name, generation, classNo, trackType, campusId, campus
    }

    // 테스트/프리뷰용 이니셜라이저
    init(id: Int, name: String, campusId: Int, generation: Int?, classNo: Int?, trackType: String?) {
        self.id = id
        self.name = name
        self.campusId = campusId
        self.campus = nil
        self.generation = generation
        self.classNo = classNo
        self.trackType = trackType
    }
}

struct CampusMealResponse: Codable {
    let type: String
    let menu: String
    let date: String
}
