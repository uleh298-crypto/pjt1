import SwiftUI

struct AnnouncementsView: View {
    let groupId: Int
    let groupKind: GroupKind
    let isLeader: Bool
    @State var viewModel: AnnouncementsViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var showOldAnnouncements = false
    @State private var showSearchDialog = false
    @State private var searchText = ""
    @State private var showDeleteDialog = false
    @State private var showDeleteSuccessDialog = false
    @State private var noticeToDelete: NoticeUiModel? = nil
    @State private var editAnnouncementId: Int? = nil
    @State private var navigateToEdit = false

    private var filteredAnnouncements: [NoticeUiModel] {
        let query = searchText.trimmingCharacters(in: .whitespaces)
        if query.isEmpty { return viewModel.uiState.announcements }
        return viewModel.uiState.announcements.filter {
            $0.title.localizedCaseInsensitiveContains(query) || $0.content.localizedCaseInsensitiveContains(query)
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            // Top App Bar
            HStack {
                Button(action: { dismiss() }) {
                    Image(systemName: "chevron.left")
                        .font(.title3)
                        .foregroundStyle(AppColors.onBackground)
                }

                Spacer()

                Text("공지사항")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(AppColors.onBackground)

                Spacer()

                Button(action: { showSearchDialog = true }) {
                    Image(systemName: "magnifyingglass")
                        .font(.title3)
                        .foregroundStyle(AppColors.onBackground)
                }
            }
            .padding()
            .background(AppColors.background)

            // Content
            ZStack(alignment: .bottomTrailing) {
                ScrollView {
                    VStack(spacing: 0) {
                        // Card container
                        VStack(spacing: 0) {
                            if viewModel.uiState.isLoading {
                                ProgressView()
                                    .padding(28)
                                    .frame(maxWidth: .infinity)
                            } else if filteredAnnouncements.isEmpty {
                                Text(searchText.isEmpty ? "공지사항이 없습니다." : "검색 결과가 없습니다.")
                                    .font(.system(size: 14))
                                    .foregroundStyle(AppColors.onSurfaceVariant)
                                    .padding(.vertical, 28)
                                    .frame(maxWidth: .infinity)
                            } else {
                                // First 5 announcements
                                ForEach(Array(filteredAnnouncements.prefix(5).enumerated()), id: \.element.id) { index, announcement in
                                    AnnouncementListItem(
                                        announcement: announcement,
                                        canManage: isLeader,
                                        groupId: groupId,
                                        groupKind: groupKind,
                                        onEditClick: {
                                            editAnnouncementId = announcement.id
                                            navigateToEdit = true
                                        },
                                        onDeleteClick: {
                                            noticeToDelete = announcement
                                            showDeleteDialog = true
                                        }
                                    )

                                    if index < 4 && index < filteredAnnouncements.count - 1 {
                                        Divider()
                                            .foregroundStyle(AppColors.outlineVariant.opacity(0.5))
                                            .padding(.horizontal, 20)
                                    }
                                }

                                // Old announcements (beyond 5)
                                if filteredAnnouncements.count > 5 {
                                    if showOldAnnouncements {
                                        ForEach(Array(filteredAnnouncements.dropFirst(5).enumerated()), id: \.element.id) { _, announcement in
                                            Divider()
                                                .foregroundStyle(AppColors.outlineVariant.opacity(0.5))
                                                .padding(.horizontal, 20)

                                            AnnouncementListItem(
                                                announcement: announcement,
                                                canManage: isLeader,
                                                groupId: groupId,
                                                groupKind: groupKind,
                                                onEditClick: {
                                                    editAnnouncementId = announcement.id
                                                    navigateToEdit = true
                                                },
                                                onDeleteClick: {
                                                    noticeToDelete = announcement
                                                    showDeleteDialog = true
                                                }
                                            )
                                        }

                                        Spacer().frame(height: 16)
                                    } else {
                                        Divider()
                                            .foregroundStyle(AppColors.outlineVariant.opacity(0.5))
                                            .padding(.horizontal, 20)

                                        Button(action: { showOldAnnouncements = true }) {
                                            Text("이전 공지사항 더보기")
                                                .font(.system(size: 14))
                                                .foregroundStyle(AppColors.primary)
                                                .frame(maxWidth: .infinity)
                                                .padding(.vertical, 12)
                                        }
                                    }
                                }
                            }
                        }
                        .background(AppColors.surface)
                        .clipShape(RoundedRectangle(cornerRadius: 24))
                        .shadow(color: Color.black.opacity(0.05), radius: 2, x: 0, y: 1)

                        Spacer().frame(height: 100)
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 16)
                }
                .background(AppColors.background)
                .refreshable {
                    await viewModel.loadAnnouncements(groupId: groupId, groupKind: groupKind)
                }

                // FAB - 팀장만 공지사항 작성 가능
                if isLeader {
                    NavigationLink(value: AppRoute.writeAnnouncement(groupId: groupId, groupKind: groupKind)) {
                        Image(systemName: "pencil")
                            .font(.system(size: 20))
                            .foregroundStyle(.white)
                            .frame(width: 56, height: 56)
                            .background(AppColors.primary)
                            .clipShape(Circle())
                            .shadow(color: .black.opacity(0.2), radius: 4, x: 0, y: 2)
                    }
                    .padding(20)
                }
            }
        }
        .navigationBarBackButtonHidden(true)
        .task {
            await viewModel.loadAnnouncements(groupId: groupId, groupKind: groupKind)
        }
        .alert("공지사항 검색", isPresented: $showSearchDialog) {
            TextField("제목/내용 검색", text: $searchText)
            Button("초기화") { searchText = "" }
            Button("확인") { showSearchDialog = false }
        }
        .alert("삭제 확인", isPresented: $showDeleteDialog, presenting: noticeToDelete) { notice in
            Button("취소", role: .cancel) { noticeToDelete = nil }
            Button("삭제", role: .destructive) {
                Task {
                    await viewModel.deleteAnnouncement(groupId: groupId, groupKind: groupKind, announcementId: notice.id)
                }
                noticeToDelete = nil
            }
        } message: { _ in
            Text("공지사항을 삭제하시겠습니까?")
        }
        .alert("알림", isPresented: $showDeleteSuccessDialog) {
            Button("확인", role: .cancel) {}
        } message: {
            Text("공지사항이 삭제되었습니다.")
        }
        .onChange(of: viewModel.uiState.isDeleteSuccess) { _, isSuccess in
            if isSuccess {
                showDeleteSuccessDialog = true
                viewModel.resetDeleteSuccess()
            }
        }
        .navigationDestination(isPresented: $navigateToEdit) {
            if let announcementId = editAnnouncementId {
                EditAnnouncementView(
                    groupId: groupId,
                    groupKind: groupKind,
                    announcementId: announcementId,
                    viewModel: EditAnnouncementViewModel(
                        groupService: viewModel.groupService
                    )
                )
            }
        }
        .onChange(of: navigateToEdit) { _, isNavigating in
            if !isNavigating {
                Task {
                    await viewModel.loadAnnouncements(groupId: groupId, groupKind: groupKind)
                }
            }
        }
    }
}

// MARK: - Announcement List Item (Android AnnouncementListItem 동일)

private struct AnnouncementListItem: View {
    let announcement: NoticeUiModel
    let canManage: Bool
    let groupId: Int
    let groupKind: GroupKind
    let onEditClick: () -> Void
    let onDeleteClick: () -> Void
    @State private var isExpanded = false

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(spacing: 0) {
                Button(action: { withAnimation { isExpanded.toggle() } }) {
                    HStack(spacing: 16) {
                        // Campaign icon circle
                        Circle()
                            .fill(announcement.isPinned ? AppColors.primary.opacity(0.15) : AppColors.surfaceVariant.opacity(0.5))
                            .frame(width: 44, height: 44)
                            .overlay(
                                Image(systemName: "megaphone.fill")
                                    .font(.system(size: 18))
                                    .foregroundStyle(announcement.isPinned ? AppColors.primary : AppColors.outlineVariant)
                            )

                        VStack(alignment: .leading, spacing: 2) {
                            HStack(spacing: 6) {
                                Text(announcement.title)
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundStyle(AppColors.onSurface)
                                    .lineLimit(1)

                                if announcement.isPinned {
                                    Image(systemName: "pin.fill")
                                        .font(.system(size: 12))
                                        .foregroundStyle(AppColors.primary)
                                }
                            }

                            if let createdAt = announcement.createdAt {
                                Text(createdAt.toRelativeTimeText())
                                    .font(.system(size: 12))
                                    .foregroundStyle(AppColors.onSurfaceVariant)
                            }
                        }

                        Spacer()
                    }
                }
                .buttonStyle(.plain)

                // MoreVert menu (leader only)
                if canManage {
                    Menu {
                        Button {
                            onEditClick()
                        } label: {
                            Label("수정", systemImage: "pencil")
                        }
                        Button(role: .destructive) {
                            onDeleteClick()
                        } label: {
                            Label("삭제", systemImage: "trash")
                        }
                    } label: {
                        Image(systemName: "ellipsis")
                            .font(.system(size: 16))
                            .foregroundStyle(AppColors.onSurfaceVariant)
                            .frame(width: 32, height: 32)
                    }
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 12)

            // Expandable content
            if isExpanded {
                Text(announcement.content)
                    .font(.system(size: 14))
                    .foregroundStyle(AppColors.onSurface.opacity(0.8))
                    .lineSpacing(4)
                    .padding(.top, 0)
                    .padding(.bottom, 20)
                    .padding(.leading, 76) // 20 padding + 44 icon + 12 spacing
                    .padding(.trailing, 20)
            }
        }
    }
}
