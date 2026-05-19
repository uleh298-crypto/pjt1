import Foundation

// MARK: - UI State

struct NotificationUiState {
    var isLoading: Bool = false
    var notifications: [NotificationModel] = []
    var error: String? = nil
}

// MARK: - ViewModel

@Observable
@MainActor
final class NotificationViewModel {
    private let notificationRepository: NotificationRepository

    private(set) var uiState = NotificationUiState()

    init(notificationRepository: NotificationRepository) {
        self.notificationRepository = notificationRepository
    }

    func loadNotifications() async {
        uiState.isLoading = true
        uiState.error = nil

        let result = await notificationRepository.getNotifications()

        switch result {
        case .success(let notifications):
            uiState.notifications = notifications
            uiState.isLoading = false
        case .failure(let error):
            uiState.error = error.localizedDescription
            uiState.isLoading = false
        }
    }

    func markAsRead(id: Int) async {
        let result = await notificationRepository.markAsRead(id: id)

        if case .success = result {
            uiState.notifications = uiState.notifications.map { notification in
                if notification.id == id {
                    return notification.copyWith(isRead: true)
                }
                return notification
            }
        }
    }
}
