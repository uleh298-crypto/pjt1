import Foundation

struct GroupModel: Identifiable {
    let id: Int
    let title: String
    let content: String
    let category: String
    let maxMembers: Int
    let currentMembers: Int
    let dDay: String?
    let status: String
    let type: String?
    let leaderId: Int?
    let startDate: String?
    let endDate: String?
}

// MARK: - My Group Item UI Model

struct MyGroupItemUiModel: Identifiable {
    let id: Int
    let title: String
    let role: String
    let category: String
    let currentMembers: Int
    let maxMembers: Int
    let status: String
    let isLeader: Bool
    let isStudy: Bool
    let memberProfileImageUrls: [String]

    init(
        id: Int,
        title: String,
        role: String,
        category: String,
        currentMembers: Int,
        maxMembers: Int,
        status: String,
        isLeader: Bool,
        isStudy: Bool,
        memberProfileImageUrls: [String] = []
    ) {
        self.id = id
        self.title = title
        self.role = role
        self.category = category
        self.currentMembers = currentMembers
        self.maxMembers = maxMembers
        self.status = status
        self.isLeader = isLeader
        self.isStudy = isStudy
        self.memberProfileImageUrls = memberProfileImageUrls
    }
}

// MARK: - GroupSummaryModel -> MyGroupItemUiModel

struct GroupSummaryModel: Identifiable {
    let id: Int
    let title: String
    let type: String
    let capacity: Int
    let currentMembers: Int?
    let status: String?
    let leaderId: Int?
    let startDate: String?
    let endDate: String?

    func toMyGroupUiModel(kind: GroupKind, currentUserId: Int?) -> MyGroupItemUiModel {
        let categoryLabel = kind == .study
            ? GroupTypeMapper.studyTypeToLabel(type)
            : GroupTypeMapper.teamTypeToLabel(type)

        let isUserLeader = currentUserId != nil && leaderId == currentUserId

        let displayStatus: String
        if let apiStatus = status?.uppercased() {
            switch apiStatus {
            case "OPEN": displayStatus = "모집중"
            case "ONGOING": displayStatus = "진행중"
            case "CLOSED": displayStatus = "완료"
            default: displayStatus = calculateStatus(startDate: startDate, endDate: endDate)
            }
        } else {
            displayStatus = calculateStatus(startDate: startDate, endDate: endDate)
        }

        return MyGroupItemUiModel(
            id: id,
            title: title,
            role: isUserLeader ? "팀장" : "팀원",
            category: categoryLabel,
            currentMembers: currentMembers ?? 0,
            maxMembers: capacity,
            status: displayStatus,
            isLeader: isUserLeader,
            isStudy: kind == .study
        )
    }

    private func calculateStatus(startDate: String?, endDate: String?) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        formatter.locale = Locale(identifier: "ko_KR")

        let now = Date()

        guard let startStr = startDate,
              let endStr = endDate,
              let start = formatter.date(from: startStr),
              let end = formatter.date(from: endStr) else {
            return "진행중"
        }

        if now < start {
            return "모집중"
        } else if now <= end {
            return "진행중"
        } else {
            return "완료"
        }
    }

    /// 모집 기간이 마감되었는지 확인
    var isExpired: Bool {
        guard let endStr = endDate else { return false }

        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        formatter.locale = Locale(identifier: "ko_KR")

        guard let end = formatter.date(from: endStr) else { return false }

        // 오늘 날짜의 시작 시간과 비교
        let calendar = Calendar.current
        let today = calendar.startOfDay(for: Date())
        let endDay = calendar.startOfDay(for: end)

        return endDay < today
    }

    /// 인원이 꽉 찼는지 확인
    var isFull: Bool {
        guard let current = currentMembers else { return false }
        return current >= capacity
    }
}

struct AnnouncementModel: Identifiable {
    let id: Int
    let title: String
    let content: String
    let createdAt: String
}

struct ApplicationModel: Identifiable {
    let id: Int
    let groupId: Int
    let groupTitle: String
    let status: String
    let appliedAt: String
}

struct MemberModel: Identifiable {
    let id: Int
    let nickname: String
    let role: String
}

struct ProgressModel: Identifiable {
    let id: Int
    let title: String
    let date: String
    let status: String
}

struct GroupMemberModel: Identifiable {
    let id: Int
    let memberId: Int
    let nickname: String?
    let profileImageUrl: String?
    let role: String?
    let status: String?
    let mattermostId: String?
    let portfolioId: Int?
}

// MARK: - My Application Model

struct MyApplicationModel: Identifiable {
    let id: Int
    let groupId: Int
    let groupTitle: String
    let leaderName: String?
    let status: String
    let position: String
    let createdAt: String?
    let isGroupDeleted: Bool

    var isPending: Bool {
        status == "PENDING" && !isGroupDeleted
    }

    var statusMessage: String {
        switch status {
        case "DELETED": return "그룹이 삭제됐습니다."
        case "APPROVED": return "지원서가 수락됐습니다."
        case "REJECTED": return "지원서가 거절됐습니다."
        default:
            return isGroupDeleted ? "그룹이 삭제됐습니다." : "승인 대기중입니다."
        }
    }

    /// Format createdAt to "yyyy.MM.dd HH:mm"
    var formattedCreatedAt: String? {
        guard let createdAt = createdAt else { return nil }

        // Try parsing various ISO 8601 formats
        let inputFormatters: [DateFormatter] = {
            let formats = [
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss"
            ]
            return formats.map { format in
                let formatter = DateFormatter()
                formatter.dateFormat = format
                formatter.locale = Locale(identifier: "en_US_POSIX")
                // 서버가 KST로 보내므로 timezone 정보 없는 경우 KST로 간주
                formatter.timeZone = TimeZone(identifier: "Asia/Seoul")
                return formatter
            }
        }()

        var date: Date?
        for formatter in inputFormatters {
            if let parsed = formatter.date(from: createdAt) {
                date = parsed
                break
            }
        }

        guard let parsedDate = date else { return nil }

        let outputFormatter = DateFormatter()
        outputFormatter.dateFormat = "yyyy.MM.dd HH:mm"
        outputFormatter.timeZone = TimeZone(identifier: "Asia/Seoul")
        return outputFormatter.string(from: parsedDate)
    }
}

// MARK: - Group Detail Model

struct GroupDetailModel: Identifiable, Equatable {
    let id: Int
    let title: String
    let description: String?
    let type: String
    let capacity: Int
    let currentMembers: Int?
    let status: String?
    let leaderId: Int?
    let leaderName: String?
    let leaderMattermostId: String?
    let leaderProfileImageUrl: String?
    let startDate: String?
    let endDate: String?
    let createdAt: String?
    let updatedAt: String?

    init(
        id: Int,
        title: String,
        description: String? = nil,
        type: String,
        capacity: Int,
        currentMembers: Int? = nil,
        status: String? = nil,
        leaderId: Int? = nil,
        leaderName: String? = nil,
        leaderMattermostId: String? = nil,
        leaderProfileImageUrl: String? = nil,
        startDate: String? = nil,
        endDate: String? = nil,
        createdAt: String? = nil,
        updatedAt: String? = nil
    ) {
        self.id = id
        self.title = title
        self.description = description
        self.type = type
        self.capacity = capacity
        self.currentMembers = currentMembers
        self.status = status
        self.leaderId = leaderId
        self.leaderName = leaderName
        self.leaderMattermostId = leaderMattermostId
        self.leaderProfileImageUrl = leaderProfileImageUrl
        self.startDate = startDate
        self.endDate = endDate
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }

    var displayStatus: String {
        if let apiStatus = status?.uppercased() {
            switch apiStatus {
            case "OPEN": return "모집중"
            case "ONGOING": return "진행중"
            case "CLOSED": return "완료"
            default: return "모집중"
            }
        }
        return "모집중"
    }

    var dDay: String? {
        guard let endStr = endDate else { return nil }

        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        formatter.locale = Locale(identifier: "ko_KR")

        guard let end = formatter.date(from: endStr) else { return nil }

        let calendar = Calendar.current
        let now = Date()
        let components = calendar.dateComponents([.day], from: now, to: end)

        guard let days = components.day else { return nil }

        if days < 0 {
            return "마감"
        } else if days == 0 {
            return "D-Day"
        } else {
            return "D-\(days)"
        }
    }

    var endDateDisplay: String {
        guard let endStr = endDate else { return "-" }

        let inputFormatter = DateFormatter()
        inputFormatter.dateFormat = "yyyy-MM-dd"

        let outputFormatter = DateFormatter()
        outputFormatter.dateFormat = "yyyy.MM.dd"

        if let date = inputFormatter.date(from: endStr) {
            return outputFormatter.string(from: date)
        }
        return endStr
    }
}
