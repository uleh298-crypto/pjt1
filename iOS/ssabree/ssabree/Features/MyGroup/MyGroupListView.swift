import SwiftUI

struct MyGroupListView: View {
    var viewModel: MyGroupAllViewModel
    var onBackClick: () -> Void = {}
    var onDetailClick: (Int, Bool) -> Void = { _, _ in }

    @State private var isSearchMode = false
    @State private var searchQuery = ""

    private var filters: [String] {
        if viewModel.uiState.groupKind == .study {
            return GroupTypeMapper.studyFilterLabels()
        } else {
            return GroupTypeMapper.teamFilterLabels()
        }
    }

    private var visibleGroups: [MyGroupItemUiModel] {
        if searchQuery.isEmpty {
            return viewModel.uiState.filteredGroups
        } else {
            return viewModel.uiState.filteredGroups.filter {
                $0.title.localizedCaseInsensitiveContains(searchQuery)
            }
        }
    }

    var body: some View {
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
            groupGridView
        }
        .background(AppColors.background)
        .refreshable {
            await viewModel.load()
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

            Text(viewModel.uiState.groupKind == .study ? "나의 스터디" : "나의 프로젝트")
                .font(.title3)
                .fontWeight(.bold)
                .foregroundStyle(AppColors.onBackground)

            Spacer()

            Button(action: { isSearchMode = true }) {
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

            Text("참여 중")
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

    // MARK: - Group Grid View

    private var groupGridView: some View {
        ScrollView {
            LazyVGrid(
                columns: [GridItem(.flexible()), GridItem(.flexible())],
                spacing: 12
            ) {
                if viewModel.uiState.isLoading {
                    ProgressView()
                        .gridCellColumns(2)
                        .padding()
                } else if visibleGroups.isEmpty {
                    Text(searchQuery.isEmpty ? "아직 가입한 그룹이 없습니다." : "검색 결과가 없습니다.")
                        .foregroundStyle(AppColors.onSurface.opacity(0.6))
                        .gridCellColumns(2)
                        .padding(.top, 40)
                } else {
                    ForEach(visibleGroups) { group in
                        MyGroupListCard(group: group, onDetailClick: onDetailClick)
                    }
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 6)
            .padding(.bottom, 20)
        }
    }

    private func exitSearchMode() {
        isSearchMode = false
        searchQuery = ""
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

// MARK: - My Group List Card

private struct MyGroupListCard: View {
    let group: MyGroupItemUiModel
    let onDetailClick: (Int, Bool) -> Void

    var body: some View {
        Button(action: { onDetailClick(group.id, group.isLeader) }) {
            VStack(alignment: .leading, spacing: 8) {
                Text(group.title)
                    .font(.headline)
                    .fontWeight(.semibold)
                    .foregroundStyle(AppColors.onSurface)
                    .lineLimit(1)

                HStack(spacing: 8) {
                    // Role Badge
                    Text(group.role)
                        .font(.caption2)
                        .fontWeight(.bold)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 2)
                        .background(
                            group.isLeader
                                ? AppColors.primary.opacity(0.15)
                                : AppColors.surface
                        )
                        .foregroundStyle(
                            group.isLeader
                                ? AppColors.primary
                                : AppColors.onSurface.opacity(0.7)
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 6))

                    Text(group.category)
                        .font(.caption)
                        .foregroundStyle(AppColors.onSurface.opacity(0.6))
                }

                HStack(spacing: 6) {
                    Image(systemName: "person.3.fill")
                        .font(.caption)
                        .foregroundStyle(AppColors.onSurface.opacity(0.6))
                    Text("\(group.currentMembers)명")
                        .font(.caption)
                        .foregroundStyle(AppColors.onSurface.opacity(0.6))
                }

                // Member Profile Images
                if !group.memberProfileImageUrls.isEmpty {
                    HStack(spacing: 6) {
                        ForEach(Array(group.memberProfileImageUrls.enumerated()), id: \.offset) { _, url in
                            AsyncImage(url: URL(string: normalizeImageUrl(url))) { image in
                                image
                                    .resizable()
                                    .scaledToFill()
                            } placeholder: {
                                Circle()
                                    .fill(AppColors.surface)
                            }
                            .frame(width: 26, height: 26)
                            .clipShape(Circle())
                        }
                    }
                }
            }
            .padding(14)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(AppColors.surface)
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .shadow(color: .black.opacity(0.03), radius: 2, x: 0, y: 1)
        }
        .buttonStyle(.plain)
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
