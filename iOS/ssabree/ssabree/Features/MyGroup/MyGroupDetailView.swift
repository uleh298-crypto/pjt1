import SwiftUI

struct MyGroupDetailView: View {
    let groupId: Int
    let groupKind: GroupKind
    let isLeader: Bool
    @State var viewModel: MyGroupDetailViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var showMenu = false
    @State private var showDeleteDialog = false
    @State private var showLeaveDialog = false
    @State private var selectedTaskFilter = "ALL"

    var body: some View {
        VStack(spacing: 0) {
            // Top App Bar
            topAppBar

            if viewModel.uiState.isLoading && viewModel.uiState.title.isEmpty {
                Spacer()
                ProgressView()
                Spacer()
            } else {
                ScrollView {
                    VStack(spacing: 32) {
                        // 그룹 정보 헤더
                        groupInfoHeader

                        // 공지사항 섹션
                        noticeSection

                        // 일정 섹션
                        taskSection
                    }
                    .padding(.horizontal, 24)
                    .padding(.vertical, 12)
                }
                .refreshable {
                    await viewModel.load()
                }
            }
        }
        .background(AppColors.background)
        .navigationBarBackButtonHidden(true)
        .task {
            await viewModel.load()
        }
        .onChange(of: viewModel.uiState.deleteSuccess) { _, success in
            if success { dismiss() }
        }
        .onChange(of: viewModel.uiState.leaveSuccess) { _, success in
            if success { dismiss() }
        }
        .alert("그룹 삭제", isPresented: $showDeleteDialog) {
            Button("취소", role: .cancel) {}
            Button("삭제", role: .destructive) {
                Task { await viewModel.deleteGroup() }
            }
        } message: {
            Text("정말로 이 그룹을 삭제하시겠습니까?\n삭제된 그룹은 복구할 수 없습니다.")
        }
        .alert("알림", isPresented: $showLeaveDialog) {
            Button("취소", role: .cancel) {}
            Button("나가기", role: .destructive) {
                Task { await viewModel.leaveGroup() }
            }
        } message: {
            Text("정말 그룹을 나가시겠습니까?")
        }
        .alert("오류", isPresented: .init(
            get: { viewModel.uiState.errorMessage != nil },
            set: { if !$0 { viewModel.clearError() } }
        )) {
            Button("확인", role: .cancel) { viewModel.clearError() }
        } message: {
            Text(viewModel.uiState.errorMessage ?? "")
        }
    }

    // MARK: - Top App Bar

    private var topAppBar: some View {
        HStack {
            Button(action: { dismiss() }) {
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundStyle(AppColors.onBackground)
            }

            Spacer()

            Text("그룹 상세")
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(AppColors.onBackground)

            Spacer()

            if isLeader {
                Menu {
                    NavigationLink(value: AppRoute.groupEdit(kind: groupKind, id: groupId)) {
                        Label("수정", systemImage: "pencil")
                    }
                    NavigationLink(value: AppRoute.memberManage(groupId: groupId, groupKind: groupKind)) {
                        Label("멤버 관리", systemImage: "person.2.fill")
                    }
                    Button("삭제", role: .destructive) {
                        showDeleteDialog = true
                    }
                } label: {
                    Image(systemName: "ellipsis")
                        .font(.title3)
                        .foregroundStyle(AppColors.onBackground)
                }
            } else {
                Button(action: { showLeaveDialog = true }) {
                    Text("나가기")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundStyle(AppColors.error)
                }
            }
        }
        .padding()
        .background(AppColors.background)
    }

    // MARK: - Group Info Header

    private var groupInfoHeader: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Status Badge
            Text("진행중")
                .font(.system(size: 12, weight: .bold))
                .foregroundStyle(AppColors.primary)
                .padding(.horizontal, 10)
                .padding(.vertical, 4)
                .background(AppColors.primaryContainer.opacity(0.5))
                .clipShape(RoundedRectangle(cornerRadius: 8))

            Spacer().frame(height: 12)

            // Title
            Text(viewModel.uiState.title)
                .font(.system(size: 20, weight: .heavy))
                .foregroundStyle(AppColors.onSurface)

            Spacer().frame(height: 20)

            // Member List
            Text("멤버 목록")
                .font(.system(size: 14, weight: .bold))
                .foregroundStyle(AppColors.onSurfaceVariant)

            Spacer().frame(height: 12)

            if viewModel.uiState.members.isEmpty {
                Text("멤버 정보가 없습니다.")
                    .font(.system(size: 12))
                    .foregroundStyle(AppColors.onSurfaceVariant)
            } else {
                ForEach(Array(viewModel.uiState.members.enumerated()), id: \.element.id) { index, member in
                    if let portfolioId = member.portfolioId {
                        NavigationLink(value: AppRoute.applicantPortfolio(portfolioId: portfolioId)) {
                            MemberItem(
                                name: member.name,
                                profileImageUrl: member.profileImageUrl,
                                mattermostId: member.mattermostId,
                                isLeader: viewModel.uiState.leaderId != nil && member.memberId == viewModel.uiState.leaderId
                            )
                        }
                        .buttonStyle(.plain)
                    } else {
                        MemberItem(
                            name: member.name,
                            profileImageUrl: member.profileImageUrl,
                            mattermostId: member.mattermostId,
                            isLeader: viewModel.uiState.leaderId != nil && member.memberId == viewModel.uiState.leaderId
                        )
                    }

                    if index != viewModel.uiState.members.count - 1 {
                        Divider()
                            .foregroundStyle(AppColors.outlineVariant.opacity(0.3))
                    }
                }
            }
        }
        .padding(24)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 24))
        .shadow(color: Color.black.opacity(0.05), radius: 4, x: 0, y: 2)
    }

    // MARK: - Notice Section

    private var noticeSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("공지사항")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(AppColors.onSurface)

                Spacer()

                NavigationLink(value: AppRoute.announcements(groupId: groupId, groupKind: groupKind, isLeader: isLeader)) {
                    Text("전체보기")
                        .font(.system(size: 12))
                        .foregroundStyle(AppColors.onSurfaceVariant)
                }
            }

            VStack(spacing: 0) {
                if viewModel.uiState.notices.isEmpty {
                    NoticeItem(title: "등록된 공지사항이 없습니다.", createdAt: nil)
                } else {
                    ForEach(Array(viewModel.uiState.notices.prefix(2).enumerated()), id: \.element.id) { index, notice in
                        NoticeItem(title: notice.title, createdAt: notice.createdAt)

                        if index != min(1, viewModel.uiState.notices.count - 1) {
                            Divider()
                                .foregroundStyle(AppColors.outlineVariant.opacity(0.3))
                        }
                    }
                }
            }
            .background(AppColors.surface)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .shadow(color: Color.black.opacity(0.03), radius: 2, x: 0, y: 1)
        }
    }

    // MARK: - Task Section

    private var taskSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack(alignment: .center) {
                Text("일정")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(AppColors.onSurface)

                NavigationLink(value: AppRoute.addTask(groupId: groupId, groupKind: groupKind)) {
                    Image(systemName: "plus.circle.fill")
                        .font(.system(size: 24))
                        .foregroundStyle(AppColors.primary)
                }
            }

            // Task Filter Tabs
            HStack(spacing: 0) {
                ForEach([("ALL", "전체"), ("TODO", "예정"), ("IN_PROGRESS", "진행"), ("DONE", "완료")], id: \.0) { filter, label in
                    Button(action: { selectedTaskFilter = filter }) {
                        Text(label)
                            .font(.system(size: 13, weight: selectedTaskFilter == filter ? .bold : .regular))
                            .foregroundStyle(selectedTaskFilter == filter ? AppColors.primary : AppColors.onSurfaceVariant)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 10)
                    }
                    .overlay(alignment: .bottom) {
                        if selectedTaskFilter == filter {
                            Rectangle()
                                .fill(AppColors.primary)
                                .frame(height: 2)
                        }
                    }
                }
            }
            .background(AppColors.surface.opacity(0.5))

            // Filtered Tasks
            let filteredTasks = viewModel.uiState.tasks.filter { task in
                switch selectedTaskFilter {
                case "TODO": return task.status == "TODO"
                case "IN_PROGRESS": return task.status == "IN_PROGRESS"
                case "DONE": return task.status == "DONE"
                default: return true
                }
            }

            if filteredTasks.isEmpty {
                VStack(alignment: .leading, spacing: 6) {
                    Text("일정 없음")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)
                    Text("등록된 일정이 없습니다.")
                        .font(.system(size: 13))
                        .foregroundStyle(AppColors.onSurfaceVariant)
                }
                .padding(.vertical, 8)
            } else {
                ForEach(filteredTasks.prefix(3)) { task in
                    NavigationLink(value: AppRoute.taskDetail(groupId: groupId, groupKind: groupKind, taskId: task.id)) {
                        TaskItemContent(task: task)
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }
}

// MARK: - Member Item

private struct MemberItem: View {
    let name: String
    let profileImageUrl: String?
    let mattermostId: String
    let isLeader: Bool

    var body: some View {
        HStack(spacing: 12) {
            // Profile Image
            if let url = profileImageUrl, !url.isEmpty {
                AsyncImage(url: URL(string: normalizeImageUrl(url))) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    Circle().fill(AppColors.surfaceVariant)
                }
                .frame(width: 36, height: 36)
                .clipShape(Circle())
            } else {
                Circle()
                    .fill(AppColors.surfaceVariant)
                    .frame(width: 36, height: 36)
                    .overlay(
                        Image(systemName: "person.fill")
                            .font(.system(size: 16))
                            .foregroundStyle(AppColors.onSurfaceVariant)
                    )
            }

            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 6) {
                    Text(name)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)

                    if isLeader {
                        Text("팀장")
                            .font(.system(size: 10))
                            .foregroundStyle(AppColors.primary)
                            .padding(.horizontal, 4)
                            .padding(.vertical, 1)
                            .background(AppColors.primaryContainer.opacity(0.5))
                            .clipShape(RoundedRectangle(cornerRadius: 4))
                    }
                }

                Text(mattermostId)
                    .font(.system(size: 12))
                    .foregroundStyle(AppColors.onSurfaceVariant)
            }

            Spacer()
        }
        .padding(.vertical, 12)
    }

    private func normalizeImageUrl(_ rawUrl: String) -> String {
        let trimmed = rawUrl.trimmingCharacters(in: .whitespaces)
        guard !trimmed.isEmpty else { return "" }

        if trimmed.lowercased().hasPrefix("http://") || trimmed.lowercased().hasPrefix("https://") {
            return trimmed
        }

        let normalized = trimmed.replacingOccurrences(of: "\\", with: "/")
        if let uploadsIndex = normalized.range(of: "/uploads/") {
            let relative = String(normalized[uploadsIndex.lowerBound...])
            return APIClient.baseURL.trimmingCharacters(in: CharacterSet(charactersIn: "/")) + relative
        }

        return APIClient.baseURL.trimmingCharacters(in: CharacterSet(charactersIn: "/")) + "/uploads/" + normalized.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
    }
}

// MARK: - Notice Item

private struct NoticeItem: View {
    let title: String
    let createdAt: String?

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "megaphone.fill")
                .foregroundStyle(AppColors.onSurfaceVariant)

            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundStyle(AppColors.onSurface)

                if let createdAt = createdAt {
                    Text(createdAt.toRelativeTimeText())
                        .font(.system(size: 12))
                        .foregroundStyle(AppColors.onSurfaceVariant)
                }
            }

            Spacer()
        }
        .padding(16)
    }

}

// MARK: - Task Item Content

private struct TaskItemContent: View {
    let task: TaskUiModel

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .center, spacing: 12) {
                // Author Profile
                if let url = task.authorProfileImageUrl, !url.isEmpty {
                    AsyncImage(url: URL(string: normalizeImageUrl(url))) { image in
                        image.resizable().scaledToFill()
                    } placeholder: {
                        Circle().fill(AppColors.surfaceVariant)
                    }
                    .frame(width: 40, height: 40)
                    .clipShape(Circle())
                } else {
                    Circle()
                        .fill(AppColors.surfaceVariant)
                        .frame(width: 40, height: 40)
                        .overlay(
                            Image(systemName: "person.fill")
                                .font(.system(size: 18))
                                .foregroundStyle(AppColors.onSurfaceVariant)
                        )
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(task.title)
                        .font(.system(size: 15, weight: .bold))
                        .foregroundStyle(AppColors.onSurface)

                    if let author = task.authorName {
                        Text("작성자: \(author)")
                            .font(.system(size: 12))
                            .foregroundStyle(AppColors.onSurfaceVariant)
                    }
                }

                Spacer()

                // Status Badge
                Text(task.statusLabel)
                    .font(.system(size: 11, weight: .bold))
                    .foregroundStyle(AppColors.onSurface.opacity(0.6))
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(statusColor.opacity(0.5))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }

            Text(task.content)
                .font(.system(size: 13))
                .foregroundStyle(AppColors.onSurface)
                .lineLimit(2)

            Text("\(task.startDate) ~ \(task.endDate)")
                .font(.system(size: 12))
                .foregroundStyle(AppColors.onSurfaceVariant)
        }
        .padding(16)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: Color.black.opacity(0.03), radius: 2, x: 0, y: 1)
    }

    private var statusColor: Color {
        switch task.status {
        case "TODO": return AppColors.outlineVariant
        case "IN_PROGRESS": return AppColors.primary
        case "DONE": return AppColors.primary.opacity(0.5)
        default: return AppColors.outlineVariant
        }
    }

    private func normalizeImageUrl(_ rawUrl: String) -> String {
        let trimmed = rawUrl.trimmingCharacters(in: .whitespaces)
        guard !trimmed.isEmpty else { return "" }

        if trimmed.lowercased().hasPrefix("http://") || trimmed.lowercased().hasPrefix("https://") {
            return trimmed
        }

        let normalized = trimmed.replacingOccurrences(of: "\\", with: "/")
        if let uploadsIndex = normalized.range(of: "/uploads/") {
            let relative = String(normalized[uploadsIndex.lowerBound...])
            return APIClient.baseURL.trimmingCharacters(in: CharacterSet(charactersIn: "/")) + relative
        }

        return APIClient.baseURL.trimmingCharacters(in: CharacterSet(charactersIn: "/")) + "/uploads/" + normalized.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
    }
}
