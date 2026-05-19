import Foundation
import Observation

// MARK: - UI Models

struct NoticeUiModel: Identifiable, Equatable {
    let id: Int
    let title: String
    let content: String
    let isPinned: Bool
    let createdAt: String?
}

struct TaskUiModel: Identifiable, Equatable {
    let id: Int
    let title: String
    let content: String
    let startDate: String
    let endDate: String
    let status: String
    let creatorId: Int?
    var authorName: String?
    var authorProfileImageUrl: String?

    var statusLabel: String {
        switch status {
        case "TODO": return "예정"
        case "IN_PROGRESS": return "진행"
        case "DONE": return "완료"
        default: return status
        }
    }
}

struct MemberUiModel: Identifiable, Equatable {
    let id: Int
    let memberId: Int
    let name: String
    let mattermostId: String
    let profileImageUrl: String?
    let portfolioId: Int?
}

// MARK: - UI State

struct MyGroupDetailUiState {
    var title: String = ""
    var memberCountText: String = "-"
    var dDayText: String = "D-?"
    var notices: [NoticeUiModel] = []
    var tasks: [TaskUiModel] = []
    var members: [MemberUiModel] = []
    var leaderId: Int? = nil
    var leaderName: String? = nil
    var leaderProfileImageUrl: String? = nil
    var isLoading: Bool = false
    var errorMessage: String? = nil
    var deleteSuccess: Bool = false
    var leaveSuccess: Bool = false
}

// MARK: - ViewModel

@Observable
@MainActor
final class MyGroupDetailViewModel {
    private let groupRepository: GroupRepository
    private let groupService: GroupService
    private let groupKind: GroupKind
    private let groupId: Int
    let isLeader: Bool

    var uiState = MyGroupDetailUiState()

    init(
        groupRepository: GroupRepository,
        groupService: GroupService,
        groupKind: GroupKind,
        groupId: Int,
        isLeader: Bool
    ) {
        self.groupRepository = groupRepository
        self.groupService = groupService
        self.groupKind = groupKind
        self.groupId = groupId
        self.isLeader = isLeader
    }

    func load() async {
        uiState.isLoading = true
        uiState.errorMessage = nil

        // Load detail, notices, tasks, members in parallel
        async let detailResult = loadDetail()
        async let noticesResult = loadNotices()
        async let tasksResult = loadTasks()
        async let membersResult = loadMembers()

        let (detail, notices, tasks, members) = await (detailResult, noticesResult, tasksResult, membersResult)

        // Build member map for task author enrichment
        let memberMap = Dictionary(uniqueKeysWithValues: members.map { ($0.memberId, $0) })

        // Enrich tasks with author info
        let enrichedTasks = tasks.map { task -> TaskUiModel in
            var enriched = task
            if let creatorId = task.creatorId, let author = memberMap[creatorId] {
                enriched.authorName = author.name
                enriched.authorProfileImageUrl = author.profileImageUrl
            }
            return enriched
        }.sorted { ($0.startDate) < ($1.startDate) }

        // Order members with leader first
        let orderedMembers: [MemberUiModel]
        if let leaderId = detail?.leaderId {
            let leader = members.first { $0.memberId == leaderId }
            let others = members.filter { $0.memberId != leaderId }
            orderedMembers = (leader.map { [$0] } ?? []) + others
        } else {
            orderedMembers = members
        }

        uiState.title = detail?.title ?? ""
        uiState.memberCountText = "\(detail?.currentMembers ?? members.count)/\(detail?.capacity ?? 0)명"
        uiState.dDayText = detail?.dDay ?? "D-?"
        uiState.notices = notices
        uiState.tasks = enrichedTasks
        uiState.members = orderedMembers
        uiState.leaderId = detail?.leaderId
        uiState.leaderName = detail?.leaderName
        uiState.leaderProfileImageUrl = detail?.leaderProfileImageUrl
        uiState.isLoading = false
    }

    private func loadDetail() async -> GroupDetailModel? {
        let result: Result<GroupDetailModel, Error>
        if groupKind == .study {
            result = await groupRepository.getStudyDetail(studyId: groupId)
        } else {
            result = await groupRepository.getTeamDetail(teamId: groupId)
        }
        return try? result.get()
    }

    private func loadNotices() async -> [NoticeUiModel] {
        do {
            let responses: [NoticeResponse]
            if groupKind == .study {
                responses = try await groupService.getStudyNotices(studyId: groupId)
            } else {
                responses = try await groupService.getTeamNotices(teamId: groupId)
            }
            return responses.map { response in
                NoticeUiModel(
                    id: response.id,
                    title: response.title,
                    content: response.content,
                    isPinned: response.isPinned ?? false,
                    createdAt: response.createdAt
                )
            }
        } catch {
            print("Failed to load notices: \(error)")
            return []
        }
    }

    private func loadTasks() async -> [TaskUiModel] {
        do {
            let responses: [TaskResponse]
            if groupKind == .study {
                responses = try await groupService.getStudyTasks(studyId: groupId)
            } else {
                responses = try await groupService.getTeamTasks(teamId: groupId)
            }
            return responses.map { response in
                TaskUiModel(
                    id: response.id,
                    title: response.title,
                    content: response.content,
                    startDate: response.startDate ?? "",
                    endDate: response.endDate ?? "",
                    status: response.status,
                    creatorId: response.creatorId
                )
            }
        } catch {
            print("Failed to load tasks: \(error)")
            return []
        }
    }

    private func loadMembers() async -> [MemberUiModel] {
        let result: Result<[GroupMemberModel], Error>
        if groupKind == .study {
            result = await groupRepository.getStudyMembers(studyId: groupId)
        } else {
            result = await groupRepository.getTeamMembers(teamId: groupId)
        }

        guard let members = try? result.get() else { return [] }

        let uiMembers = members.map { member in
            MemberUiModel(
                id: member.id,
                memberId: member.memberId,
                name: member.nickname ?? "-",
                mattermostId: member.mattermostId ?? "-",
                profileImageUrl: member.profileImageUrl,
                portfolioId: member.portfolioId
            )
        }

        // Android와 동일: 멤버별 프로필 API로 mattermostId 보강
        return await enrichMattermostIds(uiMembers)
    }

    private func enrichMattermostIds(_ members: [MemberUiModel]) async -> [MemberUiModel] {
        await withTaskGroup(of: MemberUiModel.self) { group in
            for member in members {
                group.addTask {
                    if !member.mattermostId.isEmpty && member.mattermostId != "-" {
                        return member
                    }
                    do {
                        let profile = try await self.groupService.getMemberProfile(memberId: member.memberId)
                        if let mmId = profile.mattermostId, !mmId.isEmpty {
                            return MemberUiModel(
                                id: member.id,
                                memberId: member.memberId,
                                name: member.name,
                                mattermostId: mmId,
                                profileImageUrl: member.profileImageUrl,
                                portfolioId: member.portfolioId
                            )
                        }
                    } catch {
                        print("[MyGroupDetailVM] Failed to fetch mattermostId for member \(member.memberId): \(error)")
                    }
                    return member
                }
            }

            var enriched: [MemberUiModel] = []
            for await member in group {
                enriched.append(member)
            }
            // 원래 순서 유지
            return members.map { original in
                enriched.first { $0.memberId == original.memberId } ?? original
            }
        }
    }

    func deleteGroup() async {
        uiState.isLoading = true
        let result: Result<Void, Error>
        if groupKind == .study {
            result = await groupRepository.deleteStudy(studyId: groupId)
        } else {
            result = await groupRepository.deleteTeam(teamId: groupId)
        }

        switch result {
        case .success:
            uiState.deleteSuccess = true
        case .failure(let error):
            uiState.errorMessage = error.localizedDescription
        }
        uiState.isLoading = false
    }

    func leaveGroup() async {
        uiState.isLoading = true
        do {
            if groupKind == .study {
                try await groupService.leaveStudy(studyId: groupId)
            } else {
                try await groupService.leaveTeam(teamId: groupId)
            }
            uiState.leaveSuccess = true
        } catch {
            uiState.errorMessage = error.localizedDescription
        }
        uiState.isLoading = false
    }

    func clearError() {
        uiState.errorMessage = nil
    }
}
