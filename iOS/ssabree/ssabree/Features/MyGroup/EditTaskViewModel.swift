import Foundation
import Observation

struct EditTaskUiState {
    var title: String = ""
    var content: String = ""
    var startDate: Date = Date()
    var endDate: Date = Date().addingTimeInterval(7 * 24 * 60 * 60)
    var startDateString: String = ""
    var endDateString: String = ""
    var status: String = "TODO"
    var isLoading: Bool = false
    var isSaving: Bool = false
    var error: String? = nil
    var isSuccess: Bool = false
}

@Observable
@MainActor
final class EditTaskViewModel {
    private let groupService: GroupService
    private let taskId: Int
    var uiState = EditTaskUiState()

    init(groupService: GroupService, taskId: Int) {
        self.groupService = groupService
        self.taskId = taskId
    }

    func loadTask(groupId: Int, groupKind: GroupKind) async {
        uiState.isLoading = true
        uiState.error = nil

        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"

        do {
            let responses: [TaskResponse]
            if groupKind == .study {
                responses = try await groupService.getStudyTasks(studyId: groupId)
            } else {
                responses = try await groupService.getTeamTasks(teamId: groupId)
            }

            guard let task = responses.first(where: { $0.id == taskId }) else {
                uiState.error = "일정 정보를 찾을 수 없습니다."
                uiState.isLoading = false
                return
            }

            uiState.title = task.title
            uiState.content = task.content
            uiState.status = task.status

            if let startDate = task.startDate, let date = dateFormatter.date(from: startDate) {
                uiState.startDate = date
                uiState.startDateString = startDate
            }
            if let endDate = task.endDate, let date = dateFormatter.date(from: endDate) {
                uiState.endDate = date
                uiState.endDateString = endDate
            }
        } catch {
            uiState.error = error.localizedDescription
        }
        uiState.isLoading = false
    }

    func updateTask(groupKind: GroupKind) async {
        uiState.isSaving = true
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
                try await groupService.updateStudyTask(taskId: taskId, request: request)
            } else {
                try await groupService.updateTeamTask(taskId: taskId, request: request)
            }
            uiState.isSuccess = true
        } catch {
            uiState.error = error.localizedDescription
        }
        uiState.isSaving = false
    }
}
