import Foundation

// MARK: - Notification Type

enum NotificationType: String {
    case comment = "COMMENT"
    case reply = "REPLY"
    case message = "MESSAGE"
    case hotPost = "HOT_POST"
    case notice = "NOTICE"
    case application = "APPLICATION"
    case etc = "ETC"

    static func fromString(_ value: String) -> NotificationType {
        return NotificationType(rawValue: value) ?? .etc
    }

    var label: String {
        switch self {
        case .comment: return "댓글"
        case .reply: return "답글"
        case .message: return "쪽지"
        case .hotPost: return "인기글"
        case .notice: return "공지"
        case .application: return "지원"
        case .etc: return "알림"
        }
    }
}

// MARK: - Notification Model

struct NotificationModel {
    let id: Int
    let content: String
    let isRead: Bool
    let relatedUrl: String?
    let type: NotificationType
    let createdAt: String

    func copyWith(isRead: Bool? = nil) -> NotificationModel {
        NotificationModel(
            id: id,
            content: content,
            isRead: isRead ?? self.isRead,
            relatedUrl: relatedUrl,
            type: type,
            createdAt: createdAt
        )
    }
}

// MARK: - Repository Protocol

protocol NotificationRepository {
    func getNotifications() async -> Result<[NotificationModel], Error>
    func markAsRead(id: Int) async -> Result<Void, Error>
    func registerFcmToken(token: String) async -> Result<Void, Error>
    func subscribeScheduledNotification(token: String) async -> Result<Void, Error>
    func unsubscribeScheduledNotification(token: String) async -> Result<Void, Error>
}

// MARK: - Repository Implementation

final class NotificationRepositoryImpl: NotificationRepository {
    private let notificationService: NotificationService

    init(notificationService: NotificationService) {
        self.notificationService = notificationService
    }

    func getNotifications() async -> Result<[NotificationModel], Error> {
        do {
            let response = try await notificationService.getNotifications()
            return .success(response.map { $0.toModel() })
        } catch {
            return .failure(error)
        }
    }

    func markAsRead(id: Int) async -> Result<Void, Error> {
        do {
            try await notificationService.markAsRead(id: id)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func registerFcmToken(token: String) async -> Result<Void, Error> {
        do {
            try await notificationService.registerToken(token: token)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func subscribeScheduledNotification(token: String) async -> Result<Void, Error> {
        do {
            try await notificationService.subscribe(token: token)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func unsubscribeScheduledNotification(token: String) async -> Result<Void, Error> {
        do {
            try await notificationService.unsubscribe(token: token)
            return .success(())
        } catch {
            return .failure(error)
        }
    }
}

// MARK: - Fake Repository

final class FakeNotificationRepository: NotificationRepository {
    func getNotifications() async -> Result<[NotificationModel], Error> {
        .success([
            NotificationModel(
                id: 1,
                content: "새로운 댓글이 달렸습니다.",
                isRead: false,
                relatedUrl: "/posts/1",
                type: .comment,
                createdAt: "2025-01-15T12:00:00"
            ),
            NotificationModel(
                id: 2,
                content: "새로운 쪽지가 도착했습니다.",
                isRead: true,
                relatedUrl: "/chats/1",
                type: .message,
                createdAt: "2025-01-14T15:30:00"
            )
        ])
    }

    func markAsRead(id: Int) async -> Result<Void, Error> {
        .success(())
    }

    func registerFcmToken(token: String) async -> Result<Void, Error> {
        .success(())
    }

    func subscribeScheduledNotification(token: String) async -> Result<Void, Error> {
        .success(())
    }

    func unsubscribeScheduledNotification(token: String) async -> Result<Void, Error> {
        .success(())
    }
}

// MARK: - Response Extension

extension NotificationResponse {
    func toModel() -> NotificationModel {
        NotificationModel(
            id: id,
            content: content,
            isRead: isRead,
            relatedUrl: relatedUrl,
            type: NotificationType.fromString(type),
            createdAt: createdAt
        )
    }
}
