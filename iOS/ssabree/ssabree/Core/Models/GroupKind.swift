import Foundation

enum GroupKind: String, CaseIterable {
    case study
    case project

    var displayName: String {
        switch self {
        case .study: return "스터디"
        case .project: return "프로젝트"
        }
    }

    var apiPath: String {
        switch self {
        case .study: return "studies"
        case .project: return "teams"
        }
    }

    static func fromRoute(_ value: String?) -> GroupKind {
        guard let value = value else { return .study }
        return GroupKind(rawValue: value) ?? .study
    }
}

// MARK: - Group Type Mapper

enum GroupTypeMapper {
    static func studyFilterLabels() -> [String] {
        ["전체", "알고리즘", "CS", "자격증", "기타"]
    }

    static func teamFilterLabels() -> [String] {
        ["전체", "싸피", "공모전", "자유"]
    }

    static func studyLabelToApi(_ label: String) -> String {
        switch label {
        case "알고리즘": return "ALGORITHM"
        case "CS": return "CS"
        case "A형": return "SW_TEST_A"
        case "B형": return "SW_TEST_B"
        case "자격증": return "CERTIFICATION"
        case "기타": return "ETC"
        default: return "ETC"
        }
    }

    static func teamLabelToApi(_ label: String) -> String {
        switch label {
        case "싸피": return "SSAFY"
        case "공모전": return "CONTEST"
        case "자유": return "FREE"
        default: return "FREE"
        }
    }

    static func studyTypeToLabel(_ type: String) -> String {
        switch type {
        case "ALGORITHM": return "알고리즘"
        case "CS": return "CS"
        case "SW_TEST_A": return "A형"
        case "SW_TEST_B": return "B형"
        case "CERTIFICATION": return "자격증"
        case "ETC": return "기타"
        default: return type
        }
    }

    static func teamTypeToLabel(_ type: String) -> String {
        switch type {
        case "SSAFY": return "싸피"
        case "CONTEST": return "공모전"
        case "FREE": return "자유"
        default: return type
        }
    }
}
