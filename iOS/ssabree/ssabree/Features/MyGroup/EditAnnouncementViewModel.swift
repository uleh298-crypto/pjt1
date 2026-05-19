import Foundation
import Observation

struct EditAnnouncementUiState {
    var title: String = ""
    var content: String = ""
    var isPinned: Bool = false
    var isLoading: Bool = false
    var isPosting: Bool = false
    var error: String? = nil
    var isSuccess: Bool = false
    var isDeleted: Bool = false
}

@Observable
@MainActor
final class EditAnnouncementViewModel {
    private let groupService: GroupService
    var uiState = EditAnnouncementUiState()

    init(groupService: GroupService) {
        self.groupService = groupService
    }

    func loadAnnouncement(groupId: Int, groupKind: GroupKind, announcementId: Int) async {
        uiState.isLoading = true
        do {
            let notices: [NoticeResponse]
            if groupKind == .study {
                notices = try await groupService.getStudyNotices(studyId: groupId)
            } else {
                notices = try await groupService.getTeamNotices(teamId: groupId)
            }
            if let notice = notices.first(where: { $0.id == announcementId }) {
                uiState.title = notice.title
                uiState.content = notice.content
                uiState.isPinned = notice.isPinned ?? false
            }
        } catch {
            uiState.error = error.localizedDescription
        }
        uiState.isLoading = false
    }

    func updateAnnouncement(groupId: Int, groupKind: GroupKind, announcementId: Int) async {
        uiState.isPosting = true
        uiState.error = nil

        let request = NoticeCreateRequest(title: uiState.title, content: uiState.content, isPinned: uiState.isPinned)

        do {
            if groupKind == .study {
                try await groupService.updateStudyNotice(studyId: groupId, noticeId: announcementId, request: request)
            } else {
                try await groupService.updateTeamNotice(teamId: groupId, noticeId: announcementId, request: request)
            }
            uiState.isSuccess = true
        } catch {
            uiState.error = error.localizedDescription
        }

        uiState.isPosting = false
    }

    func deleteAnnouncement(groupId: Int, groupKind: GroupKind, announcementId: Int) async {
        uiState.isPosting = true
        uiState.error = nil

        do {
            if groupKind == .study {
                try await groupService.deleteStudyNotice(studyId: groupId, noticeId: announcementId)
            } else {
                try await groupService.deleteTeamNotice(teamId: groupId, noticeId: announcementId)
            }
            uiState.isDeleted = true
        } catch {
            uiState.error = error.localizedDescription
        }

        uiState.isPosting = false
    }
}
