import SwiftUI

struct NotificationItemData: Hashable {
    enum NotificationType {
        case comment
    }
    
    let title: String
    let content: String
    let type: NotificationType
}

struct NotificationDetailView: View {
    let id: Int
    
    var body: some View {
        Text("Notification Detail ID: \(id)")
    }
}