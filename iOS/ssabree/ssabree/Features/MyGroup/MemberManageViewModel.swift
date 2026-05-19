import Foundation
import Observation

struct MemberManageUiState {
    var members: [GroupMemberModel] = []
    var applicants: [GroupMemberModel] = []
    var leaderId: Int? = nil
    var capacity: Int = 0
    var currentMembers: Int = 0
    var isLoading: Bool = false
    var error: String? = nil
    var successMessage: String? = nil

    var isFull: Bool {
        currentMembers >= capacity && capacity > 0
    }
}

@Observable
@MainActor
final class MemberManageViewModel {
    private let groupRepository: GroupRepository
    private let groupService: GroupService
    var uiState = MemberManageUiState()

    init(groupRepository: GroupRepository, groupService: GroupService) {
        self.groupRepository = groupRepository
        self.groupService = groupService
    }

    func loadMembers(groupId: Int, groupKind: GroupKind) async {
        uiState.isLoading = true
        uiState.error = nil

        async let detailResult = loadGroupDetail(groupId: groupId, groupKind: groupKind)
        async let membersResult = loadGroupMembers(groupId: groupId, groupKind: groupKind)
        async let applicantsResult = loadApplications(groupId: groupId, groupKind: groupKind)

        let (detail, membersData, applicantsData) = await (detailResult, membersResult, applicantsResult)

        if let detail = detail {
            uiState.leaderId = detail.leaderId
            uiState.capacity = detail.capacity
            uiState.currentMembers = detail.currentMembers ?? 0
        }

        if let members = membersData {
            uiState.members = members
        }

        uiState.applicants = applicantsData

        uiState.isLoading = false
    }

    private func loadGroupDetail(groupId: Int, groupKind: GroupKind) async -> GroupDetailModel? {
        let result: Result<GroupDetailModel, Error>
        if groupKind == .study {
            result = await groupRepository.getStudyDetail(studyId: groupId)
        } else {
            result = await groupRepository.getTeamDetail(teamId: groupId)
        }
        return try? result.get()
    }

    private func loadGroupMembers(groupId: Int, groupKind: GroupKind) async -> [GroupMemberModel]? {
        let result: Result<[GroupMemberModel], Error>
        if groupKind == .study {
            result = await groupRepository.getStudyMembers(studyId: groupId)
        } else {
            result = await groupRepository.getTeamMembers(teamId: groupId)
        }

        switch result {
        case .success(let data):
            return data
        case .failure(let error):
            uiState.error = error.localizedDescription
            return nil
        }
    }

    private func loadApplications(groupId: Int, groupKind: GroupKind) async -> [GroupMemberModel] {
        do {
            let responses: [GroupApplicationDetailResponse]
            if groupKind == .study {
                responses = try await groupService.getStudyApplications(studyId: groupId)
            } else {
                responses = try await groupService.getTeamApplications(teamId: groupId)
            }
            return responses
                .filter { $0.status.uppercased() == "PENDING" }
                .map { $0.toApplicantModel() }
        } catch {
            // Applications may fail if not leader - silently return empty
            return []
        }
    }

    func acceptApplication(groupId: Int, groupKind: GroupKind, applicationId: Int) async {
        do {
            if groupKind == .study {
                try await groupService.acceptStudyApplication(applicationId: applicationId)
            } else {
                try await groupService.acceptTeamApplication(applicationId: applicationId)
            }
            uiState.successMessage = "멤버를 수락했습니다."
            await loadMembers(groupId: groupId, groupKind: groupKind)
        } catch {
            uiState.error = error.localizedDescription
        }
    }

    func rejectApplication(groupId: Int, groupKind: GroupKind, applicationId: Int) async {
        do {
            if groupKind == .study {
                try await groupService.rejectStudyApplication(applicationId: applicationId)
            } else {
                try await groupService.rejectTeamApplication(applicationId: applicationId)
            }
            uiState.successMessage = "지원을 거절했습니다."
            await loadMembers(groupId: groupId, groupKind: groupKind)
        } catch {
            uiState.error = error.localizedDescription
        }
    }

    func removeMember(groupId: Int, groupKind: GroupKind, memberId: Int) async {
        do {
            if groupKind == .study {
                try await groupService.removeStudyMember(studyId: groupId, memberId: memberId)
            } else {
                try await groupService.removeTeamMember(teamId: groupId, memberId: memberId)
            }
            uiState.successMessage = "멤버를 내보냈습니다."
            await loadMembers(groupId: groupId, groupKind: groupKind)
        } catch {
            uiState.error = error.localizedDescription
        }
    }

    func clearMessages() {
        uiState.error = nil
        uiState.successMessage = nil
    }
}
