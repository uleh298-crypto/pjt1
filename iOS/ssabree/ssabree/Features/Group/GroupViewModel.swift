import Foundation
import Observation

struct GroupUiState {
    let groupKind: GroupKind
    var selectedFilter: String = "전체"
    var groups: [GroupListItemUiModel] = []
    var recentKeywords: [String] = []
    var campusId: Int? = nil
    var showMinLengthError: Bool = false
    var isLoading: Bool = false
    var errorMessage: String?

    var filteredGroups: [GroupListItemUiModel] {
        if selectedFilter == "전체" {
            return groups
        } else {
            return groups.filter { $0.categoryLabel == selectedFilter }
        }
    }
}

struct GroupListItemUiModel: Identifiable {
    let id: Int
    let title: String
    let categoryLabel: String
    let dDay: String
    let currentMembers: Int
    let maxMembers: Int
    let status: String
    let isExpired: Bool
    let isFull: Bool
    let isClosed: Bool
    let closedMessage: String?
}

@Observable
final class GroupViewModel {
    private let groupRepository: GroupRepository
    private let keywordRepository: KeywordRepository
    private let myPageRepository: MyPageRepository
    private let campusRepository: CampusRepository
    private let groupKind: GroupKind

    var uiState: GroupUiState

    init(
        groupRepository: GroupRepository,
        keywordRepository: KeywordRepository? = nil,
        myPageRepository: MyPageRepository? = nil,
        campusRepository: CampusRepository? = nil,
        initialKind: GroupKind = .study
    ) {
        self.groupRepository = groupRepository
        self.keywordRepository = keywordRepository ?? KeywordRepositoryImpl()
        self.myPageRepository = myPageRepository ?? FakeMyPageRepository()
        self.campusRepository = campusRepository ?? FakeCampusRepository()
        self.groupKind = initialKind
        self.uiState = GroupUiState(groupKind: initialKind)
    }

    @MainActor
    func load() async {
        uiState.isLoading = true
        uiState.errorMessage = nil

        // Resolve campus ID if not already set
        if uiState.campusId == nil {
            uiState.campusId = await resolveCampusId()
        }

        let result: Result<[GroupSummaryModel], Error>
        if groupKind == .study {
            result = await groupRepository.getStudies(campusId: uiState.campusId, type: nil)
        } else {
            result = await groupRepository.getTeams(campusId: uiState.campusId, type: nil)
        }

        switch result {
        case .success(let items):
            // Android와 동일하게 각 그룹의 실제 멤버 수를 별도 API로 조회 (병렬)
            let kind = groupKind
            let repo = groupRepository
            let uiItems = await withTaskGroup(of: (Int, Int).self) { group in
                for model in items {
                    group.addTask {
                        let membersResult: Result<[GroupMemberModel], Error>
                        if kind == .study {
                            membersResult = await repo.getStudyMembers(studyId: model.id)
                        } else {
                            membersResult = await repo.getTeamMembers(teamId: model.id)
                        }
                        let count = (try? membersResult.get())?.count ?? (model.currentMembers ?? 0)
                        return (model.id, count)
                    }
                }

                var memberCounts: [Int: Int] = [:]
                for await (id, count) in group {
                    memberCounts[id] = count
                }
                return memberCounts
            }

            // 활성 그룹 먼저, 비활성(마감/만석) 그룹 나중에 정렬
            let allGroups = items.map { model in
                model.toListItemUiModel(kind: groupKind, currentMembersOverride: uiItems[model.id])
            }
            uiState.groups = allGroups.sorted { a, b in
                if a.isClosed != b.isClosed { return !a.isClosed }
                return false
            }
            uiState.isLoading = false
        case .failure(let error):
            uiState.errorMessage = error.localizedDescription
            uiState.isLoading = false
        }
    }

    func loadRecentKeywords() {
        uiState.recentKeywords = keywordRepository.getRecentKeywords()
    }

    func onFilterSelected(_ filter: String) {
        uiState.selectedFilter = filter
    }

    func onSearchSubmit(_ query: String) {
        let keyword = query.trimmingCharacters(in: .whitespaces)
        if keyword.count < 2 {
            uiState.showMinLengthError = true
            return
        }
        keywordRepository.addRecentKeyword(keyword: keyword)
        loadRecentKeywords()
        uiState.showMinLengthError = false
    }

    func deleteRecentKeyword(_ keyword: String) {
        keywordRepository.deleteRecentKeyword(keyword: keyword)
        loadRecentKeywords()
    }

    func clearMinLengthError() {
        uiState.showMinLengthError = false
    }

    private func resolveCampusId() async -> Int? {
        let myPageResult = await myPageRepository.getMyPage()
        guard case .success(let myPage) = myPageResult,
              let campusName = myPage.user?.campus?.trimmingCharacters(in: .whitespaces),
              !campusName.isEmpty else {
            return nil
        }

        let campusesResult = await campusRepository.getCampuses()
        guard case .success(let campuses) = campusesResult else {
            return nil
        }

        let normalized = normalizeCampusName(campusName)
        return campuses.first { campus in
            let campusNormalized = normalizeCampusName(campus.name)
            return campusNormalized == normalized ||
                   campusNormalized.contains(normalized) ||
                   normalized.contains(campusNormalized)
        }?.id
    }

    private func normalizeCampusName(_ name: String) -> String {
        return name.replacingOccurrences(of: "캠퍼스", with: "")
            .replacingOccurrences(of: " ", with: "")
            .lowercased()
    }
}

// MARK: - GroupSummaryModel Extension

extension GroupSummaryModel {
    func toListItemUiModel(kind: GroupKind, currentMembersOverride: Int? = nil) -> GroupListItemUiModel {
        let categoryLabel = kind == .study
            ? GroupTypeMapper.studyTypeToLabel(type)
            : GroupTypeMapper.teamTypeToLabel(type)

        let dDayText = calculateDDay(endDate: endDate)

        let current = currentMembersOverride ?? currentMembers ?? 0
        let expired = isExpired
        let full = current >= capacity
        let closed = expired || full
        let closedMsg: String? = if expired {
            "모집 기간이 지났습니다."
        } else if full {
            "그룹 인원이 꽉 찼습니다."
        } else {
            nil
        }

        let displayStatus: String
        if let apiStatus = status?.uppercased() {
            switch apiStatus {
            case "OPEN": displayStatus = "모집중"
            case "ONGOING": displayStatus = "진행중"
            case "CLOSED": displayStatus = "완료"
            default: displayStatus = "모집중"
            }
        } else {
            displayStatus = "모집중"
        }

        return GroupListItemUiModel(
            id: id,
            title: title,
            categoryLabel: categoryLabel,
            dDay: dDayText,
            currentMembers: current,
            maxMembers: capacity,
            status: displayStatus,
            isExpired: expired,
            isFull: full,
            isClosed: closed,
            closedMessage: closedMsg
        )
    }

    private func calculateDDay(endDate: String?) -> String {
        guard let endStr = endDate else { return "" }

        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        formatter.locale = Locale(identifier: "ko_KR")

        guard let end = formatter.date(from: endStr) else { return "" }

        let calendar = Calendar.current
        let now = Date()
        let components = calendar.dateComponents([.day], from: now, to: end)

        guard let days = components.day else { return "" }

        if days < 0 {
            return "마감"
        } else if days == 0 {
            return "D-Day"
        } else {
            return "D-\(days)"
        }
    }
}
