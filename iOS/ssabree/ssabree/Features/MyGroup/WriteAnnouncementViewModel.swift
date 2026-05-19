import Foundation
import Observation

struct WriteAnnouncementUiState {
    var isPosting: Bool = false
    var error: String? = nil
    var isSuccess: Bool = false
}

@Observable
@MainActor
final class WriteAnnouncementViewModel {
    private let groupService: GroupService
    var uiState = WriteAnnouncementUiState()

    init(groupService: GroupService) {
        self.groupService = groupService
    }

    func createAnnouncement(groupId: Int, groupKind: GroupKind, title: String, content: String, isPinned: Bool = false) async {
        uiState.isPosting = true
        uiState.error = nil

        let request = NoticeCreateRequest(title: title, content: content, isPinned: isPinned)

        do {
            if groupKind == .study {
                try await groupService.createStudyNotice(studyId: groupId, request: request)
            } else {
                try await groupService.createTeamNotice(teamId: groupId, request: request)
            }
            uiState.isSuccess = true
        } catch {
            uiState.error = error.localizedDescription
        }

        uiState.isPosting = false
    }
}
