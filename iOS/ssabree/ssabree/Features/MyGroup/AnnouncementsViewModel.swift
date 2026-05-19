import Foundation
import Observation

struct AnnouncementsUiState {
    var announcements: [NoticeUiModel] = []
    var isLoading: Bool = false
    var error: String? = nil
    var isDeleteSuccess: Bool = false
}

@Observable
@MainActor
final class AnnouncementsViewModel {
    let groupService: GroupService
    var uiState = AnnouncementsUiState()

    init(groupService: GroupService) {
        self.groupService = groupService
    }

    func loadAnnouncements(groupId: Int, groupKind: GroupKind) async {
        uiState.isLoading = true
        do {
            let responses: [NoticeResponse]
            if groupKind == .study {
                responses = try await groupService.getStudyNotices(studyId: groupId)
            } else {
                responses = try await groupService.getTeamNotices(teamId: groupId)
            }
            uiState.announcements = responses.map { response in
                NoticeUiModel(
                    id: response.id,
                    title: response.title,
                    content: response.content,
                    isPinned: response.isPinned ?? false,
                    createdAt: response.createdAt
                )
            }
            uiState.error = nil
        } catch {
            uiState.error = error.localizedDescription
        }
        uiState.isLoading = false
    }

    func deleteAnnouncement(groupId: Int, groupKind: GroupKind, announcementId: Int) async {
        do {
            if groupKind == .study {
                try await groupService.deleteStudyNotice(studyId: groupId, noticeId: announcementId)
            } else {
                try await groupService.deleteTeamNotice(teamId: groupId, noticeId: announcementId)
            }
            uiState.isDeleteSuccess = true
            await loadAnnouncements(groupId: groupId, groupKind: groupKind)
        } catch {
            uiState.error = error.localizedDescription
        }
    }

    func resetDeleteSuccess() {
        uiState.isDeleteSuccess = false
    }
}
