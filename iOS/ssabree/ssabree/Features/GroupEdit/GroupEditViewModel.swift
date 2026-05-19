import Foundation
import Observation

// MARK: - Group Update Info

struct GroupUpdateInfo {
    let title: String
    let type: String
    let capacity: Int
    let startDate: String
    let endDate: String
    let description: String
}

// MARK: - Group Edit UI State

struct GroupEditUiState {
    var detail: GroupDetailModel? = nil
    var isLoading: Bool = false
    var isSubmitting: Bool = false
    var isSuccess: Bool = false
    var errorMessage: String? = nil
    var campusId: Int? = nil
}

// MARK: - Group Edit ViewModel

@Observable
final class GroupEditViewModel {
    private let groupRepository: GroupRepository
    private let groupService: GroupService
    private let myPageRepository: MyPageRepository
    private let campusRepository: CampusRepository
    private let groupKind: GroupKind
    private let groupId: Int

    var uiState = GroupEditUiState()

    init(
        groupRepository: GroupRepository,
        groupService: GroupService,
        myPageRepository: MyPageRepository,
        campusRepository: CampusRepository,
        groupKind: GroupKind,
        groupId: Int
    ) {
        self.groupRepository = groupRepository
        self.groupService = groupService
        self.myPageRepository = myPageRepository
        self.campusRepository = campusRepository
        self.groupKind = groupKind
        self.groupId = groupId

        Task { await loadCampusId() }
    }

    @MainActor
    private func loadCampusId() async {
        // Get user's campus name
        let myPageResult = await myPageRepository.getMyPage()
        guard case .success(let myPage) = myPageResult,
              let campusName = myPage.user?.campus?.trimmingCharacters(in: .whitespaces),
              !campusName.isEmpty else {
            return
        }

        // Get campus list and find matching ID
        let campusesResult = await campusRepository.getCampuses()
        guard case .success(let campuses) = campusesResult else {
            return
        }

        let normalized = normalizeCampusName(campusName)
        let matchingCampus = campuses.first { campus in
            let campusNormalized = normalizeCampusName(campus.name)
            return campusNormalized == normalized ||
                   campusNormalized.contains(normalized) ||
                   normalized.contains(campusNormalized)
        }

        uiState.campusId = matchingCampus?.id
    }

    private func normalizeCampusName(_ name: String) -> String {
        name.replacingOccurrences(of: "캠퍼스", with: "")
            .replacingOccurrences(of: " ", with: "")
            .lowercased()
    }

    @MainActor
    func loadDetail() async {
        uiState.isLoading = true
        uiState.errorMessage = nil

        let result: Result<GroupDetailModel, Error>
        if groupKind == .study {
            result = await groupRepository.getStudyDetail(studyId: groupId)
        } else {
            result = await groupRepository.getTeamDetail(teamId: groupId)
        }

        switch result {
        case .success(let detail):
            uiState.detail = detail
            uiState.isLoading = false
        case .failure(let error):
            uiState.errorMessage = error.localizedDescription
            uiState.isLoading = false
        }
    }

    @MainActor
    func submit(info: GroupUpdateInfo) async {
        uiState.isSubmitting = true
        uiState.errorMessage = nil

        do {
            let request = GroupUpdateRequest(
                title: info.title,
                type: info.type,
                capacity: info.capacity,
                startDate: info.startDate,
                endDate: info.endDate,
                campusId: uiState.campusId,
                description: info.description,
                status: nil
            )

            if groupKind == .study {
                try await groupService.updateStudy(studyId: groupId, request: request)
            } else {
                try await groupService.updateTeam(teamId: groupId, request: request)
            }

            uiState.isSubmitting = false
            uiState.isSuccess = true
        } catch {
            uiState.isSubmitting = false
            uiState.errorMessage = error.localizedDescription
        }
    }

    func resetResult() {
        uiState.isSuccess = false
        uiState.errorMessage = nil
    }
}
