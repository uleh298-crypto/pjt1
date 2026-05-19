import SwiftUI

struct MyGroupView: View {
    var studyViewModel: MyGroupAllViewModel
    var projectViewModel: MyGroupAllViewModel
    var onDetailClick: (GroupKind, Int, Bool) -> Void = { _, _, _ in }
    var onMyApplicationsClick: (GroupKind) -> Void = { _ in }

    @State private var isSearchMode = false
    @State private var searchQuery = ""
    @State private var committedQuery = ""
    @State private var showApplicationsSheet = false

    private var normalizedQuery: String { committedQuery.trimmingCharacters(in: .whitespaces) }
    private var isValidQuery: Bool { normalizedQuery.count >= 2 }
    private var showMinLengthMessage: Bool {
        isSearchMode && (!searchQuery.isEmpty || !committedQuery.isEmpty) && !isValidQuery
    }

    private var visibleProjectGroups: [MyGroupItemUiModel] {
        if !isSearchMode {
            return projectViewModel.uiState.filteredGroups
        } else if !isValidQuery {
            return []
        } else {
            return projectViewModel.uiState.filteredGroups.filter {
                $0.title.localizedCaseInsensitiveContains(normalizedQuery)
            }
        }
    }

    private var visibleStudyGroups: [MyGroupItemUiModel] {
        if !isSearchMode {
            return studyViewModel.uiState.filteredGroups
        } else if !isValidQuery {
            return []
        } else {
            return studyViewModel.uiState.filteredGroups.filter {
                $0.title.localizedCaseInsensitiveContains(normalizedQuery)
            }
        }
    }

    private var isRefreshing: Bool {
        studyViewModel.uiState.isLoading || projectViewModel.uiState.isLoading
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header / Search Bar
            if isSearchMode {
                searchHeader
            } else {
                normalHeader
            }

            // Content
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 12) {
                    if showMinLengthMessage {
                        Text("두 글자 이상 입력해 주세요.")
                            .font(.caption)
                            .foregroundStyle(AppColors.onSurface.opacity(0.7))
                            .padding(.horizontal, 20)
                    } else {
                        // Project Section
                        GroupSectionHeader(title: "프로젝트")
                            .padding(.horizontal, 20)

                        if !isSearchMode {
                            GroupFilterRow(
                                filters: GroupTypeMapper.teamFilterLabels(),
                                selectedFilter: projectViewModel.uiState.selectedFilter,
                                onFilterSelected: projectViewModel.onFilterSelected
                            )
                            .padding(.horizontal, 20)
                        }

                        if visibleProjectGroups.isEmpty {
                            EmptyGroupText()
                                .padding(.horizontal, 20)
                        } else {
                            ForEach(visibleProjectGroups) { group in
                                MyGroupCard(group: group) { groupId, isLeader in
                                    onDetailClick(.project, groupId, isLeader)
                                }
                                .padding(.horizontal, 20)
                            }
                        }

                        Spacer().frame(height: 16)

                        // Study Section
                        GroupSectionHeader(title: "스터디")
                            .padding(.horizontal, 20)

                        if !isSearchMode {
                            GroupFilterRow(
                                filters: GroupTypeMapper.studyFilterLabels(),
                                selectedFilter: studyViewModel.uiState.selectedFilter,
                                onFilterSelected: studyViewModel.onFilterSelected
                            )
                            .padding(.horizontal, 20)
                        }

                        if visibleStudyGroups.isEmpty {
                            EmptyGroupText()
                                .padding(.horizontal, 20)
                        } else {
                            ForEach(visibleStudyGroups) { group in
                                MyGroupCard(group: group) { groupId, isLeader in
                                    onDetailClick(.study, groupId, isLeader)
                                }
                                .padding(.horizontal, 20)
                            }
                        }
                    }
                }
                .padding(.top, 8)
                .padding(.bottom, 20)
            }
            .refreshable {
                await studyViewModel.load()
                await projectViewModel.load()
            }
        }
        .background(AppColors.background)
        .onAppear {
            Task {
                await studyViewModel.load()
                await projectViewModel.load()
            }
        }
        .confirmationDialog("내 지원내역", isPresented: $showApplicationsSheet, titleVisibility: .visible) {
            Button("스터디 지원내역") {
                onMyApplicationsClick(.study)
            }
            Button("프로젝트 지원내역") {
                onMyApplicationsClick(.project)
            }
            Button("취소", role: .cancel) {}
        }
    }

    // MARK: - Normal Header

    private var normalHeader: some View {
        HStack {
            Text("나의 그룹")
                .font(.title3)
                .fontWeight(.bold)
                .foregroundStyle(AppColors.onBackground)
            Spacer()
            HStack(spacing: 16) {
                Button(action: { isSearchMode = true }) {
                    Image(systemName: "magnifyingglass")
                        .font(.title3)
                        .foregroundStyle(AppColors.onBackground)
                }
                Button(action: { showApplicationsSheet = true }) {
                    Image(systemName: "doc.text")
                        .font(.title3)
                        .foregroundStyle(AppColors.onBackground)
                }
            }
        }
        .padding()
        .background(AppColors.surface)
    }

    // MARK: - Search Header

    private var searchHeader: some View {
        HStack(spacing: 8) {
            HStack(spacing: 8) {
                Image(systemName: "magnifyingglass")
                    .foregroundStyle(AppColors.onSurface.opacity(0.6))
                    .font(.callout)

                TextField("검색어를 입력하세요", text: $searchQuery)
                    .textFieldStyle(.plain)
                    .font(.body)
                    .submitLabel(.search)
                    .onSubmit {
                        committedQuery = searchQuery.trimmingCharacters(in: .whitespaces)
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

            Button("취소") {
                exitSearchMode()
            }
            .foregroundStyle(AppColors.primary)
        }
        .padding(.horizontal)
        .padding(.vertical, 8)
        .background(AppColors.background)
    }

    private func exitSearchMode() {
        isSearchMode = false
        searchQuery = ""
        committedQuery = ""
    }
}

// MARK: - Section Header

private struct GroupSectionHeader: View {
    let title: String

    var body: some View {
        Text(title)
            .font(.title2)
            .fontWeight(.bold)
            .foregroundStyle(AppColors.onBackground)
    }
}

// MARK: - Filter Row

private struct GroupFilterRow: View {
    let filters: [String]
    let selectedFilter: String
    let onFilterSelected: (String) -> Void

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 6) {
                ForEach(filters, id: \.self) { filter in
                    FilterChipView(
                        label: filter,
                        isSelected: selectedFilter == filter,
                        onTap: { onFilterSelected(filter) }
                    )
                }
            }
            .padding(.vertical, 6)
        }
    }
}

private struct FilterChipView: View {
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

// MARK: - Empty Text

private struct EmptyGroupText: View {
    var body: some View {
        Text("아직 가입한 그룹이 없습니다.")
            .font(.caption)
            .foregroundStyle(AppColors.onSurface.opacity(0.6))
            .padding(.vertical, 8)
    }
}

// MARK: - My Group Card

private struct MyGroupCard: View {
    let group: MyGroupItemUiModel
    let onDetailClick: (Int, Bool) -> Void

    // 최대 4개의 프로필 이미지 표시 (안드로이드와 동일)
    private var profileItems: [String] {
        let maxCount = min(max(group.currentMembers, 1), 4)
        let urls = Array(group.memberProfileImageUrls.prefix(maxCount))
        // 빈 이미지 URL로 placeholder 채우기
        return urls + Array(repeating: "", count: maxCount - urls.count)
    }

    var body: some View {
        Button(action: { onDetailClick(group.id, group.isLeader) }) {
            VStack(alignment: .leading, spacing: 8) {
                Text(group.title)
                    .font(.headline)
                    .fontWeight(.semibold)
                    .foregroundStyle(AppColors.onSurface)
                    .lineLimit(1)

                HStack(spacing: 8) {
                    // Role Badge - 안드로이드 스타일
                    Text(group.role)
                        .font(.system(size: 11, weight: .bold))
                        .padding(.horizontal, 8)
                        .padding(.vertical, 2)
                        .background(
                            group.isLeader
                                ? AppColors.primaryContainer.opacity(0.6)
                                : AppColors.surfaceVariant
                        )
                        .foregroundStyle(
                            group.isLeader
                                ? AppColors.primary
                                : AppColors.onSurfaceVariant
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 6))

                    Text(group.category)
                        .font(.caption)
                        .foregroundStyle(AppColors.onSurface.opacity(0.6))
                }

                HStack(spacing: 6) {
                    Image(systemName: "person.3.fill")
                        .font(.system(size: 14))
                        .foregroundStyle(AppColors.onSurface.opacity(0.6))
                    Text("\(group.currentMembers)명")
                        .font(.caption)
                        .foregroundStyle(AppColors.onSurface.opacity(0.6))
                }

                // Member Profile Images (최대 4개, placeholder 포함)
                HStack(spacing: 6) {
                    ForEach(Array(profileItems.enumerated()), id: \.offset) { _, url in
                        if url.isEmpty {
                            // 기본 프로필 아이콘
                            Circle()
                                .fill(AppColors.surfaceVariant)
                                .frame(width: 26, height: 26)
                                .overlay(
                                    Image(systemName: "person.fill")
                                        .font(.system(size: 12))
                                        .foregroundStyle(AppColors.onSurfaceVariant)
                                )
                        } else {
                            AsyncImage(url: URL(string: normalizeImageUrl(url))) { image in
                                image
                                    .resizable()
                                    .scaledToFill()
                            } placeholder: {
                                Circle()
                                    .fill(AppColors.surfaceVariant)
                                    .overlay(
                                        Image(systemName: "person.fill")
                                            .font(.system(size: 12))
                                            .foregroundStyle(AppColors.onSurfaceVariant)
                                    )
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
