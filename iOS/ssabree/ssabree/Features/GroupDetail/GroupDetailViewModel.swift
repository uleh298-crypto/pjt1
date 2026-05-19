import Foundation
import Observation

struct GroupDetailUiState {
    var detail: GroupDetailModel? = nil
    var members: [GroupMemberModel] = []
    var isLoading: Bool = false
    var error: String? = nil
    var isLeader: Bool = false
    var isMember: Bool = false
    var hasApplied: Bool = false  // 이미 지원했는지 여부
}

@Observable
final class GroupDetailViewModel {
    private let groupRepository: GroupRepository
    private let authRepository: AuthRepository
    private let groupKind: GroupKind
    private let groupId: Int

    var uiState = GroupDetailUiState()

    init(
        groupRepository: GroupRepository,
        authRepository: AuthRepository,
        groupKind: GroupKind,
        groupId: Int
    ) {
        self.groupRepository = groupRepository
        self.authRepository = authRepository
        self.groupKind = groupKind
        self.groupId = groupId
    }

    @MainActor
    func loadDetail() async {
        uiState.isLoading = true
        uiState.error = nil

        // Load group detail based on kind
        let detailResult: Result<GroupDetailModel, Error>
        if groupKind == .study {
            detailResult = await groupRepository.getStudyDetail(studyId: groupId)
        } else {
            detailResult = await groupRepository.getTeamDetail(teamId: groupId)
        }

        // Get current user ID
        let currentUserId = try? await authRepository.getMyMemberId().get()

        // Check if user is a member
        let myGroupsResult: Result<[GroupSummaryModel], Error>
        if groupKind == .study {
            myGroupsResult = await groupRepository.getMyStudies()
        } else {
            myGroupsResult = await groupRepository.getMyTeams()
        }
        let myGroups = (try? myGroupsResult.get()) ?? []
        let isMember = myGroups.contains { $0.id == groupId }

        // Check if user has already applied
        let myApplicationsResult: Result<[MyApplicationModel], Error>
        if groupKind == .study {
            myApplicationsResult = await groupRepository.getMyStudyApplications()
        } else {
            myApplicationsResult = await groupRepository.getMyTeamApplications()
        }
        let myApplications = (try? myApplicationsResult.get()) ?? []
        let hasApplied = myApplications.contains { $0.groupId == groupId && $0.status == "PENDING" }

        switch detailResult {
        case .success(let detail):
            let isLeader = currentUserId != nil && detail.leaderId == currentUserId
            uiState.detail = detail
            uiState.isLeader = isLeader
            uiState.isMember = isMember
            uiState.hasApplied = hasApplied
            uiState.isLoading = false

            // Load members
            await loadMembers()

        case .failure(let error):
            uiState.error = error.localizedDescription
            uiState.isLoading = false
        }
    }

    @MainActor
    private func loadMembers() async {
        let membersResult: Result<[GroupMemberModel], Error>
        if groupKind == .study {
            membersResult = await groupRepository.getStudyMembers(studyId: groupId)
        } else {
            membersResult = await groupRepository.getTeamMembers(teamId: groupId)
        }

        if case .success(let members) = membersResult {
            uiState.members = members
        }
    }

    func categoryLabel(for type: String) -> String {
        if groupKind == .study {
            return GroupTypeMapper.studyTypeToLabel(type)
        } else {
            return GroupTypeMapper.teamTypeToLabel(type)
        }
    }

    @MainActor
    func deleteGroup() async {
        uiState.isLoading = true
        uiState.error = nil

        let result: Result<Void, Error>
        if groupKind == .study {
            result = await groupRepository.deleteStudy(studyId: groupId)
        } else {
            result = await groupRepository.deleteTeam(teamId: groupId)
        }

        switch result {
        case .success:
            uiState.isLoading = false
        case .failure(let error):
            uiState.error = error.localizedDescription
            uiState.isLoading = false
        }
    }

    @MainActor
    func clearError() {
        uiState.error = nil
    }
}
