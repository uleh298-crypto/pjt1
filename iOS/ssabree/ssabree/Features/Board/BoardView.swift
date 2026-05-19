import SwiftUI

struct BoardView: View {
    @Environment(\.dismiss) private var dismiss
    var viewModel: BoardViewModel  // @State 제거 - 외부에서 주입받음
    var onWriteTap: (Int) -> Void = { _ in }
    var onPostTap: (Int, Int) -> Void = { _, _ in }
    var onSearchTap: () -> Void = {}

    init(
        viewModel: BoardViewModel,
        onWriteTap: @escaping (Int) -> Void = { _ in },
        onPostTap: @escaping (Int, Int) -> Void = { _, _ in },
        onSearchTap: @escaping () -> Void = {}
    ) {
        self.viewModel = viewModel
        self.onWriteTap = onWriteTap
        self.onPostTap = onPostTap
        self.onSearchTap = onSearchTap
    }

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            VStack(spacing: 0) {
                // Header
                HStack {
                    Text("게시판")
                        .font(.headline)
                        .fontWeight(.bold)
                        .foregroundStyle(AppColors.onBackground)

                    Spacer()

                    Button(action: onSearchTap) {
                        Image(systemName: "magnifyingglass")
                            .font(.title3)
                            .foregroundStyle(AppColors.onBackground)
                    }
                }
                .padding()
                .background(Color.clear)

                // Hot Chip + Filter Chip
                HStack(spacing: 8) {
                    HotChipButton(
                        isSelected: viewModel.uiState.isHotSelected,
                        onTap: { viewModel.onHotSelected() }
                    )

                    FilterDropdownButton(
                        text: viewModel.uiState.selectedFilter.label,
                        options: viewModel.uiState.filterOptions,
                        isActive: !viewModel.uiState.isHotSelected,
                        onSelect: { viewModel.onFilterSelected(filter: $0) }
                    )
                    Spacer()
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 8)

                // Content
                ScrollView {
                    LazyVStack(spacing: 0) {
                        // Admin Notice Card
                        if let notice = viewModel.uiState.noticeContent, !notice.isEmpty {
                            AdminNoticeCard(
                                title: "관리자 공지사항",
                                content: notice
                            )
                            .padding(.horizontal, 20)
                            .padding(.vertical, 4)

                            Spacer().frame(height: 16)
                        }

                        // Posts
                        ForEach(Array(viewModel.uiState.posts.enumerated()), id: \.element.id) { index, post in
                            PostItemView(post: post)
                                .padding(.horizontal, 10)
                                .onTapGesture {
                                    onPostTap(post.boardId, post.id)
                                }
                                .onAppear {
                                    viewModel.onListEndReached(index: index)
                                }

                            if post.id != viewModel.uiState.posts.last?.id {
                                Divider()
                                    .foregroundStyle(AppColors.onSurface.opacity(0.1))
                                    .padding(.horizontal, 20)
                                    .padding(.vertical, 8)
                            }
                        }

                        // Loading More Indicator
                        if viewModel.uiState.isLoadingMore {
                            ProgressView()
                                .padding(.vertical, 16)
                        }
                    }
                    .padding(.bottom, 80)
                }
                .refreshable {
                    await viewModel.onRefresh()
                }
                .background(AppColors.background)
            }
            .background(AppColors.background)

            // FAB
            Button(action: {
                // 선택된 게시판 id 전달 (Hot이나 전체보기일 경우 첫 번째 게시판 또는 0)
                let boardId: Int
                if viewModel.uiState.isHotSelected {
                    boardId = viewModel.uiState.filterOptions.first(where: { $0.id != nil })?.id ?? 0
                } else {
                    boardId = viewModel.uiState.selectedFilter.id
                        ?? viewModel.uiState.filterOptions.first(where: { $0.id != nil })?.id
                        ?? 0
                }
                onWriteTap(boardId)
            }) {
                Image(systemName: "pencil")
                    .font(.system(size: 20))
                    .foregroundStyle(.white)
                    .frame(width: 56, height: 56)
                    .background(AppColors.primary)
                    .clipShape(Circle())
                    .shadow(color: .black.opacity(0.2), radius: 4, x: 0, y: 2)
            }
            .padding(.trailing, 20)
            .padding(.bottom, 20)
        }
        .navigationBarHidden(true)
        .task {
            await viewModel.loadInitialDataIfNeeded()
        }
    }
}

// MARK: - Hot Chip Button

private struct HotChipButton: View {
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 6) {
                Image(systemName: "flame.fill")
                    .font(.caption)
                Text("Hot")
                    .font(.subheadline)
                    .fontWeight(.semibold)
            }
            .foregroundStyle(isSelected ? AppColors.onPrimary : AppColors.onSurface)
            .padding(.horizontal, 14)
            .padding(.vertical, 6)
            .frame(minHeight: 36)
            .background(isSelected ? AppColors.primary : Color.clear)
            .clipShape(RoundedRectangle(cornerRadius: 20))
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(isSelected ? AppColors.primary : AppColors.onSurface.opacity(0.3), lineWidth: 1)
            )
        }
    }
}

// MARK: - Filter Dropdown Button

private struct FilterDropdownButton: View {
    let text: String
    let options: [BoardFilterOption]
    let isActive: Bool
    let onSelect: (BoardFilterOption) -> Void

    init(text: String, options: [BoardFilterOption], isActive: Bool = true, onSelect: @escaping (BoardFilterOption) -> Void) {
        self.text = text
        self.options = options
        self.isActive = isActive
        self.onSelect = onSelect
    }

    var body: some View {
        Menu {
            ForEach(options, id: \.label) { option in
                Button(option.label) {
                    onSelect(option)
                }
            }
        } label: {
            HStack(spacing: 4) {
                Text(text)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundStyle(isActive ? AppColors.onPrimary : AppColors.onSurface)
                Image(systemName: "chevron.down")
                    .font(.caption)
                    .foregroundStyle(isActive ? AppColors.onPrimary.opacity(0.7) : AppColors.onSurface.opacity(0.7))
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 6)
            .frame(minHeight: 36)
            .background(isActive ? AppColors.primary : Color.clear)
            .clipShape(RoundedRectangle(cornerRadius: 20))
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(isActive ? AppColors.primary : AppColors.onSurface.opacity(0.3), lineWidth: 1)
            )
        }
    }
}

// MARK: - Admin Notice Card

private struct AdminNoticeCard: View {
    let title: String
    let content: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.subheadline)
                .fontWeight(.bold)
                .foregroundStyle(AppColors.primary)

            Text(content)
                .font(.footnote)
                .foregroundStyle(AppColors.onSurface.opacity(0.7))
                .lineSpacing(4)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(20)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .shadow(color: .black.opacity(0.05), radius: 2, y: 1)
    }
}

// MARK: - Post Item View

private struct PostItemView: View {
    let post: PostModel

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(alignment: .top, spacing: 12) {
                // Text Content
                VStack(alignment: .leading, spacing: 0) {
                    // Board Pill
                    BoardPill(text: post.boardName)

                    Spacer().frame(height: 10)

                    // Title & Preview
                    if post.isBlinded {
                        Text("험한 말은 싸피봇이 처리했으니 안심하라구!")
                            .font(.subheadline)
                            .foregroundStyle(AppColors.onSurface.opacity(0.6))
                            .lineLimit(1)

                        Spacer().frame(height: 8)

                        Image("bot")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 100, height: 100)
                    } else {
                        Text(post.title)
                            .font(.subheadline)
                            .fontWeight(.bold)
                            .foregroundStyle(AppColors.onSurface)
                            .lineLimit(1)

                        Spacer().frame(height: 6)

                        Text(post.preview)
                            .font(.footnote)
                            .foregroundStyle(AppColors.onSurface.opacity(0.7))
                            .lineLimit(3)
                    }
                }

                Spacer(minLength: 0)

                // Thumbnail Image
                if !post.isBlinded, let imageUrl = post.imageUrl, !imageUrl.isEmpty {
                    CachedAsyncImage(url: imageUrl) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    } placeholder: {
                        Rectangle()
                            .fill(AppColors.surfaceVariant)
                    }
                    .frame(width: 70, height: 70)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }

            Spacer().frame(height: 12)

            // Bottom: Date & Counts
            HStack {
                Text(post.dateText)
                    .font(.caption)
                    .foregroundStyle(AppColors.onSurface.opacity(0.6))

                Spacer()

                CountChip(icon: "eye", count: post.viewCount)
                Spacer().frame(width: 10)
                CountChip(icon: "hand.thumbsup", count: post.likeCount)
                Spacer().frame(width: 10)
                CountChip(icon: "bubble.left", count: post.commentCount)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 4, x: 0, y: 2)
    }
}

// MARK: - Board Pill

private struct BoardPill: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.caption)
            .fontWeight(.semibold)
            .foregroundStyle(AppColors.onSurface.opacity(0.7))
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(AppColors.surfaceVariant)
            .clipShape(Capsule())
    }
}

// MARK: - Count Chip

private struct CountChip: View {
    let icon: String
    let count: Int

    var body: some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.caption)
            Text("\(count)")
                .font(.caption)
                .fontWeight(.medium)
        }
        .foregroundStyle(AppColors.primary)
    }
}
