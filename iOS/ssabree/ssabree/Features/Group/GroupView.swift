import SwiftUI

struct GroupView: View {
    var viewModel: GroupViewModel
    var onBackClick: () -> Void = {}
    var onDetailClick: (Int) -> Void = { _ in }
    var onFabClick: (GroupKind) -> Void = { _ in }

    @State private var isSearchMode = false
    @State private var searchQuery = ""
    @State private var committedQuery = ""

    private var normalizedQuery: String { committedQuery.trimmingCharacters(in: .whitespaces) }
    private var isValidQuery: Bool { normalizedQuery.count >= 2 }

    private var filters: [String] {
        if viewModel.uiState.groupKind == .study {
            return GroupTypeMapper.studyFilterLabels()
        } else {
            return GroupTypeMapper.teamFilterLabels()
        }
    }

    private var visibleGroups: [GroupListItemUiModel] {
        if !isSearchMode {
            return viewModel.uiState.filteredGroups
        } else if normalizedQuery.isEmpty {
            return []
        } else {
            return viewModel.uiState.filteredGroups.filter {
                $0.title.localizedCaseInsensitiveContains(normalizedQuery)
            }
        }
    }

    private var isSubmittedQuery: Bool {
        !committedQuery.isEmpty && searchQuery.trimmingCharacters(in: .whitespaces) == committedQuery
    }

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            VStack(spacing: 0) {
                // Header
                if isSearchMode {
                    searchHeader
                } else {
                    normalHeader
                }

                // Filter Row (only when not in search mode)
                if !isSearchMode {
                    groupHeaderRow
                    filterRow
                }

                // Content
                if isSearchMode && (!isSubmittedQuery || viewModel.uiState.showMinLengthError) {
                    recentKeywordsView
                } else {
                    groupListView
                }
            }
            .background(AppColors.background)
            .navigationBarBackButtonHidden(true)
            .task {
                await viewModel.load()
                viewModel.loadRecentKeywords()
            }
            .refreshable {
                await viewModel.load()
            }

            // FAB Button (only when not in search mode)
            if !isSearchMode {
                fabButton
            }
        }
    }

    // MARK: - Normal Header

    private var normalHeader: some View {
        HStack {
            Button(action: onBackClick) {
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundStyle(AppColors.onBackground)
            }

            Spacer()

            Text(viewModel.uiState.groupKind == .study ? "스터디" : "프로젝트")
                .font(.title3)
                .fontWeight(.bold)
                .foregroundStyle(AppColors.onBackground)

            Spacer()

            Button(action: {
                isSearchMode = true
                viewModel.loadRecentKeywords()
            }) {
                Image(systemName: "magnifyingglass")
                    .font(.title3)
                    .foregroundStyle(AppColors.onBackground)
            }
        }
        .padding()
        .background(AppColors.surface)
    }

    // MARK: - Search Header

    private var searchHeader: some View {
        HStack(spacing: 8) {
            Button(action: exitSearchMode) {
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundStyle(AppColors.onBackground)
            }

            HStack(spacing: 8) {
                Image(systemName: "magnifyingglass")
                    .foregroundStyle(AppColors.onSurface.opacity(0.6))
                    .font(.callout)

                TextField("검색어를 입력하세요", text: $searchQuery)
                    .textFieldStyle(.plain)
                    .font(.body)
                    .submitLabel(.search)
                    .onSubmit {
                        let trimmed = searchQuery.trimmingCharacters(in: .whitespaces)
                        viewModel.onSearchSubmit(trimmed)
                        committedQuery = trimmed
                    }
                    .onChange(of: searchQuery) { _, _ in
                        viewModel.clearMinLengthError()
                    }

                if !searchQuery.isEmpty {
                    Button(action: { searchQuery = "" }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundStyle(AppColors.onSurface.opacity(0.5))
                            .font(.callout)
                    }
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(AppColors.surface)
            .clipShape(Capsule())
        }
        .padding(.horizontal)
        .padding(.vertical, 8)
        .background(AppColors.background)
    }

    // MARK: - Group Header Row

    private var groupHeaderRow: some View {
        HStack {
            Text(viewModel.uiState.groupKind == .study ? "스터디" : "프로젝트")
                .font(.title2)
                .fontWeight(.bold)
                .foregroundStyle(AppColors.onBackground)

            Spacer().frame(width: 8)

            Text("모집")
                .font(.subheadline)
                .foregroundStyle(AppColors.onSurface.opacity(0.6))

            Spacer()
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 10)
    }

    // MARK: - Filter Row

    private var filterRow: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 6) {
                ForEach(filters, id: \.self) { filter in
                    FilterChipButton(
                        label: filter,
                        isSelected: viewModel.uiState.selectedFilter == filter,
                        onTap: { viewModel.onFilterSelected(filter) }
                    )
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 6)
        }
    }

    // MARK: - Recent Keywords View

    private var recentKeywordsView: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                if viewModel.uiState.showMinLengthError {
                    Text("두 글자 이상 입력해 주세요.")
                        .font(.body)
                        .foregroundStyle(AppColors.onSurface.opacity(0.7))
                }

                if !viewModel.uiState.recentKeywords.isEmpty {
                    Text("최근 검색어")
                        .font(.headline)
                        .fontWeight(.bold)
                        .foregroundStyle(AppColors.onBackground)

                    FlowLayout(spacing: 8) {
                        ForEach(viewModel.uiState.recentKeywords, id: \.self) { keyword in
                            RecentKeywordChip(
                                text: keyword,
                                onDelete: { viewModel.deleteRecentKeyword(keyword) },
                                onClick: { searchQuery = keyword }
                            )
                        }
                    }
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 16)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }

    // MARK: - Group List View

    private var groupListView: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                if viewModel.uiState.isLoading {
                    ProgressView()
                        .padding()
                } else if let error = viewModel.uiState.errorMessage {
                    Text(error)
                        .foregroundStyle(AppColors.error)
                        .padding()
                } else if visibleGroups.isEmpty {
                    if isSubmittedQuery {
                        Text("검색 결과가 없습니다.")
                            .foregroundStyle(AppColors.onSurface.opacity(0.6))
                            .padding(.top, 40)
                    } else {
                        Text("등록된 그룹이 없습니다.")
                            .foregroundStyle(AppColors.onSurface.opacity(0.6))
                            .padding()
                    }
                } else {
                    ForEach(visibleGroups) { group in
                        GroupCard(group: group, onDetailClick: onDetailClick)
                    }
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 6)
            .padding(.bottom, 90) // Space for FAB
        }
    }

    // MARK: - FAB Button

    private var fabButton: some View {
        Button(action: { onFabClick(viewModel.uiState.groupKind) }) {
            Image(systemName: "plus")
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

    private func exitSearchMode() {
        isSearchMode = false
        searchQuery = ""
        committedQuery = ""
        viewModel.clearMinLengthError()
    }
}

// MARK: - Filter Chip Button

private struct FilterChipButton: View {
    let label: String
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            Text(label)
                .font(.caption)
                .fontWeight(.medium)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(isSelected ? AppColors.primary : AppColors.surface)
                .foregroundStyle(isSelected ? .white : AppColors.onSurface)
                .clipShape(RoundedRectangle(cornerRadius: 10))
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Recent Keyword Chip

private struct RecentKeywordChip: View {
    let text: String
    let onDelete: () -> Void
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 6) {
                Text(text)
                    .font(.subheadline)
                    .foregroundStyle(AppColors.onSurface.opacity(0.8))
                    .lineLimit(1)

                Button(action: onDelete) {
                    Image(systemName: "xmark")
                        .font(.caption2)
                        .foregroundStyle(AppColors.onSurface.opacity(0.5))
                }
            }
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(AppColors.onSurface.opacity(0.2), lineWidth: 1)
            )
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Group Card

private struct GroupCard: View {
    let group: GroupListItemUiModel
    let onDetailClick: (Int) -> Void

    private var contentOpacity: Double { group.isClosed ? 0.5 : 1.0 }

    var body: some View {
        Button(action: { onDetailClick(group.id) }) {
            VStack(alignment: .leading, spacing: 0) {
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text(group.title)
                            .font(.headline)
                            .fontWeight(.semibold)
                            .foregroundStyle(AppColors.onSurface)
                            .lineLimit(1)

                        HStack {
                            Text(group.categoryLabel)
                                .font(.caption)
                                .foregroundStyle(AppColors.onSurface.opacity(0.6))

                            Spacer().frame(width: 10)

                            if !group.dDay.isEmpty {
                                Text(group.dDay)
                                    .font(.caption)
                                    .foregroundStyle(AppColors.error)
                            }
                        }
                    }

                    Spacer()

                    Image(systemName: "chevron.right")
                        .font(.caption)
                        .foregroundStyle(AppColors.onSurface.opacity(0.4))
                }

                Spacer().frame(height: 10)

                HStack {
                    Image(systemName: "person.3.fill")
                        .font(.caption)
                        .foregroundStyle(AppColors.onSurface.opacity(0.5))

                    Spacer().frame(width: 6)

                    Text("\(group.currentMembers)/\(group.maxMembers)명")
                        .font(.caption)
                        .foregroundStyle(AppColors.onSurface.opacity(0.5))
                }

                if let message = group.closedMessage {
                    Spacer().frame(height: 8)

                    Text(message)
                        .font(.caption2)
                        .foregroundStyle(AppColors.onSurface.opacity(0.5))
                }
            }
            .padding(16)
            .background(AppColors.surface)
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .shadow(color: .black.opacity(0.04), radius: 2, x: 0, y: 1)
            .opacity(contentOpacity)
        }
        .buttonStyle(.plain)
        .disabled(group.isClosed)
    }
}

// MARK: - Flow Layout

private struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let result = arrange(proposal: proposal, subviews: subviews)
        return result.size
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = arrange(proposal: proposal, subviews: subviews)
        for (index, frame) in result.frames.enumerated() {
            subviews[index].place(at: CGPoint(x: bounds.minX + frame.minX, y: bounds.minY + frame.minY), proposal: .unspecified)
        }
    }

    private func arrange(proposal: ProposedViewSize, subviews: Subviews) -> (size: CGSize, frames: [CGRect]) {
        let maxWidth = proposal.width ?? .infinity
        var currentX: CGFloat = 0
        var currentY: CGFloat = 0
        var lineHeight: CGFloat = 0
        var frames: [CGRect] = []

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if currentX + size.width > maxWidth && currentX > 0 {
                currentX = 0
                currentY += lineHeight + spacing
                lineHeight = 0
            }
            frames.append(CGRect(origin: CGPoint(x: currentX, y: currentY), size: size))
            currentX += size.width + spacing
            lineHeight = max(lineHeight, size.height)
        }

        let totalHeight = currentY + lineHeight
        return (CGSize(width: maxWidth, height: totalHeight), frames)
    }
}
