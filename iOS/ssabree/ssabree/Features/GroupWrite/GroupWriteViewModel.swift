import Foundation
import Observation

struct GroupWriteUiState {
    var isSubmitting: Bool = false
    var isSuccess: Bool = false
    var errorMessage: String? = nil
    var campusId: Int? = nil
}

@Observable
final class GroupWriteViewModel {
    private let groupRepository: GroupRepository
    private let myPageRepository: MyPageRepository
    private let campusRepository: CampusRepository

    var uiState = GroupWriteUiState()

    init(
        groupRepository: GroupRepository,
        myPageRepository: MyPageRepository,
        campusRepository: CampusRepository
    ) {
        self.groupRepository = groupRepository
        self.myPageRepository = myPageRepository
        self.campusRepository = campusRepository
    }

    @MainActor
    func loadCampusId() async {
        let myPageResult = await myPageRepository.getMyPage()
        guard case .success(let myPage) = myPageResult,
              let campusName = myPage.user?.campus?.trimmingCharacters(in: .whitespaces),
              !campusName.isEmpty else {
            uiState.campusId = nil
            return
        }

        let campusesResult = await campusRepository.getCampuses()
        guard case .success(let campuses) = campusesResult else {
            uiState.campusId = nil
            return
        }

        let normalized = normalizeCampusName(campusName)
        uiState.campusId = campuses.first { campus in
            let campusNormalized = normalizeCampusName(campus.name)
            return campusNormalized == normalized ||
                   campusNormalized.contains(normalized) ||
                   normalized.contains(campusNormalized)
        }?.id
    }

    @MainActor
    func submit(kind: GroupKind, info: GroupCreateInfo) async {
        uiState.isSubmitting = true
        uiState.errorMessage = nil

        let result: Result<GroupSummaryModel, Error>
        switch kind {
        case .study:
            result = await groupRepository.createStudy(info: info)
        case .project:
            result = await groupRepository.createTeam(info: info)
        }

        uiState.isSubmitting = false

        switch result {
        case .success:
            uiState.isSuccess = true
        case .failure(let error):
            uiState.errorMessage = error.localizedDescription
        }
    }

    func resetResult() {
        uiState.isSuccess = false
        uiState.errorMessage = nil
    }

    private func normalizeCampusName(_ name: String) -> String {
        return name.replacingOccurrences(of: "캠퍼스", with: "")
            .replacingOccurrences(of: " ", with: "")
            .lowercased()
    }
}
