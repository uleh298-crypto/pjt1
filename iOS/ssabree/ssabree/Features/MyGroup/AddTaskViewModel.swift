import Foundation
import Observation

struct AddTaskUiState {
    var title: String = ""
    var content: String = ""
    var startDate: Date = Date()
    var endDate: Date = Date().addingTimeInterval(7 * 24 * 60 * 60)
    var startDateString: String = ""
    var endDateString: String = ""
    var status: String = "TODO"
    var isPosting: Bool = false
    var error: String? = nil
    var isSuccess: Bool = false
}

@Observable
@MainActor
final class AddTaskViewModel {
    private let groupService: GroupService
    var uiState = AddTaskUiState()

    init(groupService: GroupService) {
        self.groupService = groupService
    }

    func createTask(groupId: Int, groupKind: GroupKind) async {
        guard !uiState.startDateString.isEmpty && !uiState.endDateString.isEmpty else {
            uiState.error = "시작일과 종료일을 선택해주세요."
            return
        }

        uiState.isPosting = true
        uiState.error = nil

        let request = TaskCreateRequest(
            title: uiState.title,
            content: uiState.content,
            startDate: uiState.startDateString,
            endDate: uiState.endDateString,
            status: uiState.status
        )

        do {
            if groupKind == .study {
                _ = try await groupService.createStudyTask(studyId: groupId, request: request)
            } else {
                _ = try await groupService.createTeamTask(teamId: groupId, request: request)
            }
            uiState.isSuccess = true
        } catch {
            uiState.error = error.localizedDescription
        }

        uiState.isPosting = false
    }
}
