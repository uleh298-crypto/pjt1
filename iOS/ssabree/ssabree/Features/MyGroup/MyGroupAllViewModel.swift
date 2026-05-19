import Foundation
import Observation

struct MyGroupAllUiState {
    let groupKind: GroupKind
    var selectedFilter: String = "전체"
    var groups: [MyGroupItemUiModel] = []
    var isLoading: Bool = false
    var errorMessage: String?

    var filteredGroups: [MyGroupItemUiModel] {
        if selectedFilter == "전체" {
            return groups
        } else {
            return groups.filter { $0.category == selectedFilter }
        }
    }
}

@Observable
final class MyGroupAllViewModel {
    private let groupRepository: GroupRepository
    private let groupKind: GroupKind
    private let authRepository: AuthRepository

    var uiState: MyGroupAllUiState

    init(groupRepository: GroupRepository, groupKind: GroupKind, authRepository: AuthRepository) {
        self.groupRepository = groupRepository
        self.groupKind = groupKind
        self.authRepository = authRepository
        self.uiState = MyGroupAllUiState(groupKind: groupKind)
        Task { await load() }
    }

    @MainActor
    func load() async {
        uiState.isLoading = true
        uiState.errorMessage = nil

        let result: Result<[GroupSummaryModel], Error>
        if groupKind == .study {
            result = await groupRepository.getMyStudies()
        } else {
            result = await groupRepository.getMyTeams()
        }

        let currentUserId = try? await authRepository.getMyMemberId().get()

        switch result {
        case .success(let items):
            var groupUiModels: [MyGroupItemUiModel] = []

            for model in items {
                var uiModel = model.toMyGroupUiModel(kind: groupKind, currentUserId: currentUserId)

                // Fetch members for profile URLs
                let membersResult: Result<[GroupMemberModel], Error>
                if groupKind == .study {
                    membersResult = await groupRepository.getStudyMembers(studyId: model.id)
                } else {
                    membersResult = await groupRepository.getTeamMembers(teamId: model.id)
                }

                if case .success(let members) = membersResult {
                    let profileUrls = members.compactMap { $0.profileImageUrl?.isEmpty == false ? $0.profileImageUrl : nil }
                    uiModel = MyGroupItemUiModel(
                        id: uiModel.id,
                        title: uiModel.title,
                        role: uiModel.role,
                        category: uiModel.category,
                        currentMembers: members.count,
                        maxMembers: uiModel.maxMembers,
                        status: uiModel.status,
                        isLeader: uiModel.isLeader,
                        isStudy: uiModel.isStudy,
                        memberProfileImageUrls: Array(profileUrls.prefix(4))
                    )
                }

                groupUiModels.append(uiModel)
            }

            uiState.groups = groupUiModels
            uiState.isLoading = false

        case .failure(let error):
            uiState.errorMessage = error.localizedDescription
            uiState.isLoading = false
        }
    }

    func onFilterSelected(_ filter: String) {
        uiState.selectedFilter = filter
    }
}
