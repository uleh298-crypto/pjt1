import SwiftUI

// MARK: - Date Formatting Helper

private func formatDateText(_ createdAt: String) -> String {
    guard createdAt.contains("T") else { return createdAt }

    let datePart = String(createdAt.prefix(while: { $0 != "T" }))
    let timePart = createdAt.components(separatedBy: "T").last?.prefix(5) ?? ""

    return "\(datePart) \(timePart)"
}

// MARK: - Notification View

struct NotificationView: View {
    @Environment(\.dismiss) private var dismiss
    @State var viewModel: NotificationViewModel

    var onPostClick: ((Int) -> Void)?
    var onChatClick: ((Int) -> Void)?
    var onGroupApplicationClick: ((Int, GroupKind) -> Void)?
    var onApplicationAcceptedClick: ((Int, GroupKind) -> Void)?
    var onSettingsTap: (() -> Void)?

    init(
        viewModel: NotificationViewModel,
        onPostClick: ((Int) -> Void)? = nil,
        onChatClick: ((Int) -> Void)? = nil,
        onGroupApplicationClick: ((Int, GroupKind) -> Void)? = nil,
        onApplicationAcceptedClick: ((Int, GroupKind) -> Void)? = nil,
        onSettingsTap: (() -> Void)? = nil
    ) {
        self._viewModel = State(initialValue: viewModel)
        self.onPostClick = onPostClick
        self.onChatClick = onChatClick
        self.onGroupApplicationClick = onGroupApplicationClick
        self.onApplicationAcceptedClick = onApplicationAcceptedClick
        self.onSettingsTap = onSettingsTap
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Button(action: { dismiss() }) {
                    Image(systemName: "chevron.left")
                        .font(.title3)
                        .foregroundStyle(AppColors.onBackground)
                }
                Spacer()
                Text("알림")
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundStyle(AppColors.onBackground)
                Spacer()
                // Settings button
                Button(action: { onSettingsTap?() }) {
                    Image(systemName: "gearshape")
                        .font(.title3)
                        .foregroundStyle(AppColors.onBackground)
                }
            }
            .padding()
            .background(Color.clear)

            notificationContent
        }
        .background(AppColors.background)
        .navigationBarBackButtonHidden(true)
        .task {
            await viewModel.loadNotifications()
        }
    }

    @ViewBuilder
    private var notificationContent: some View {
        if viewModel.uiState.isLoading {
            loadingView
        } else if viewModel.uiState.error != nil {
            errorView
        } else if viewModel.uiState.notifications.isEmpty {
            emptyView
        } else {
            notificationList
        }
    }

    private var loadingView: some View {
        VStack {
            Spacer()
            ProgressView()
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    private var errorView: some View {
        VStack {
            Spacer()
            Text("알림을 불러오는데 실패했습니다.")
                .foregroundStyle(AppColors.onSurface.opacity(0.6))
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    private var emptyView: some View {
        VStack {
            Spacer()
            Text("알림이 없습니다.")
                .foregroundStyle(AppColors.onSurface.opacity(0.6))
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    @ViewBuilder
    private var notificationList: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                ForEach(viewModel.uiState.notifications, id: \.id) { notification in
                    NotificationListItem(notification: notification) {
                        Task {
                            await viewModel.markAsRead(id: notification.id)
                        }
                        handleNotificationClick(notification: notification)
                    }
                    Divider()
                        .background(AppColors.onSurface.opacity(0.08))
                }
            }
        }
    }

    private func handleNotificationClick(notification: NotificationModel) {
        guard let relatedUrl = notification.relatedUrl else { return }

        if relatedUrl.hasPrefix("/posts/") {
            let postIdString = relatedUrl.replacingOccurrences(of: "/posts/", with: "")
            if let postId = Int(postIdString) {
                onPostClick?(postId)
            }
        } else if relatedUrl.hasPrefix("/chats/") {
            let roomIdString = relatedUrl.replacingOccurrences(of: "/chats/", with: "")
            if let roomId = Int(roomIdString) {
                onChatClick?(roomId)
            }
        } else if relatedUrl.hasSuffix("/applications") {
            // /teams/{id}/applications 또는 /studies/{id}/applications → 멤버관리 화면
            let parts = relatedUrl.split(separator: "/")
            if parts.count >= 3, let groupId = Int(parts[1]) {
                let kind: GroupKind = parts[0] == "teams" ? .project : .study
                onGroupApplicationClick?(groupId, kind)
            }
        } else if relatedUrl.hasPrefix("/teams/") || relatedUrl.hasPrefix("/studies/") {
            // /teams/{id} 또는 /studies/{id} → 그룹 상세 화면
            let parts = relatedUrl.split(separator: "/")
            if parts.count >= 2, let groupId = Int(parts[1]) {
                let kind: GroupKind = parts[0] == "teams" ? .project : .study
                onApplicationAcceptedClick?(groupId, kind)
            }
        }
    }
}

// MARK: - Notification List Item

private struct NotificationListItem: View {
    let notification: NotificationModel
    let onClick: () -> Void

    var body: some View {
        let backgroundColor = notification.isRead
            ? AppColors.surface
            : AppColors.primary.opacity(0.15)

        VStack(alignment: .leading, spacing: 8) {
            HStack {
                // Type badge
                Text(notification.type.label)
                    .font(.caption2)
                    .foregroundStyle(AppColors.onPrimary)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(AppColors.primary.opacity(0.8))
                    .clipShape(RoundedRectangle(cornerRadius: 4))

                // Date
                Text(formatDateText(notification.createdAt))
                    .font(.caption)
                    .foregroundStyle(AppColors.onSurface.opacity(0.6))
            }

            Text(notification.content)
                .font(.subheadline)
                .foregroundStyle(AppColors.onSurface)
                .lineLimit(2)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.vertical, 16)
        .padding(.horizontal, 20)
        .background(backgroundColor)
        .contentShape(Rectangle())
        .onTapGesture {
            onClick()
        }
    }
}
