import SwiftUI

struct BoardSearchView: View {
    @Environment(\.dismiss) private var dismiss
    @State var viewModel: BoardSearchViewModel
    @FocusState private var isSearchFocused: Bool

    var onPostTap: (Int, Int) -> Void = { _, _ in }

    var body: some View {
        VStack(spacing: 0) {
            // Search Bar
            searchBar

            Divider()

            // Content
            ZStack {
                if viewModel.uiState.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if viewModel.uiState.hasSearched && viewModel.uiState.results.isEmpty {
                    // 검색 결과 없음
                    Text("검색 결과가 없습니다.")
                        .foregroundStyle(AppColors.onSurface.opacity(0.6))
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if !viewModel.uiState.hasSearched || isSearchFocused {
                    // 검색 전 또는 포커스 상태: 최근 검색어 표시
                    recentKeywordsView
                } else {
                    // 검색 결과
                    searchResultsView
                }
            }
            .background(AppColors.background)
        }
        .background(AppColors.background)
        .navigationBarHidden(true)
        .onAppear {
            isSearchFocused = true
        }
    }

    // MARK: - Search Bar

    private var searchBar: some View {
        HStack(spacing: 12) {
            // Back Button
            Button(action: { dismiss() }) {
                Image(systemName: "chevron.left")
                    .font(.title3)
                    .foregroundStyle(AppColors.onSurface)
            }

            // Search Field
            HStack(spacing: 8) {
                Image(systemName: "magnifyingglass")
                    .font(.subheadline)
                    .foregroundStyle(AppColors.onSurface.opacity(0.5))

                TextField("검색어를 입력하세요", text: Binding(
                    get: { viewModel.uiState.query },
                    set: { viewModel.onQueryChange($0) }
                ))
                .focused($isSearchFocused)
                .submitLabel(.search)
                .onSubmit {
                    viewModel.onSearchSubmit()
                    isSearchFocused = false
                }

                if !viewModel.uiState.query.isEmpty {
                    Button(action: { viewModel.clearQuery() }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.subheadline)
                            .foregroundStyle(AppColors.onSurface.opacity(0.5))
                    }
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(AppColors.surfaceVariant.opacity(0.5))
            .clipShape(Capsule())
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(AppColors.background)
    }

    // MARK: - Recent Keywords View

    private var recentKeywordsView: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                if !viewModel.uiState.recentKeywords.isEmpty {
                    Text("최근 검색어")
                        .font(.subheadline)
                        .fontWeight(.bold)
                        .foregroundStyle(AppColors.onSurface)
                        .padding(.horizontal, 16)

                    FlowLayout(spacing: 8) {
                        ForEach(viewModel.uiState.recentKeywords, id: \.self) { keyword in
                            RecentSearchChip(
                                keyword: keyword,
                                onTap: {
                                    viewModel.onQuickSearch(keyword: keyword)
                                    isSearchFocused = false
                                },
                                onDelete: {
                                    viewModel.deleteRecentKeyword(keyword)
                                }
                            )
                        }
                    }
                    .padding(.horizontal, 16)
                }

                Spacer()
            }
            .padding(.top, 16)
        }
    }

    // MARK: - Search Results View

    private var searchResultsView: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                ForEach(Array(viewModel.uiState.results.enumerated()), id: \.element.id) { index, post in
                    SearchPostItemView(post: post)
                        .padding(.horizontal, 10)
                        .onTapGesture {
                            onPostTap(post.boardId, post.id)
                        }
                        .onAppear {
                            viewModel.loadMoreIfNeeded(currentIndex: index)
                        }

                    Divider()
                        .foregroundStyle(AppColors.onSurface.opacity(0.1))
                        .padding(.horizontal, 20)
                        .padding(.vertical, 8)
                }

                // Loading More
                if viewModel.uiState.isLoadingMore {
                    ProgressView()
                        .padding(.vertical, 16)
                }
            }
            .padding(.top, 8)
        }
    }
}

// MARK: - Recent Search Chip

private struct RecentSearchChip: View {
    let keyword: String
    let onTap: () -> Void
    let onDelete: () -> Void

    var body: some View {
        HStack(spacing: 6) {
            Text(keyword)
                .font(.subheadline)
                .foregroundStyle(AppColors.onSurface)

            Button(action: onDelete) {
                Image(systemName: "xmark")
                    .font(.caption2)
                    .foregroundStyle(AppColors.onSurface.opacity(0.5))
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(AppColors.onSurface.opacity(0.3), lineWidth: 1)
        )
        .onTapGesture {
            onTap()
        }
    }
}

// MARK: - Flow Layout (for chips)

private struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let result = FlowResult(
            in: proposal.replacingUnspecifiedDimensions().width,
            subviews: subviews,
            spacing: spacing
        )
        return result.size
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = FlowResult(
            in: bounds.width,
            subviews: subviews,
            spacing: spacing
        )
        for (index, subview) in subviews.enumerated() {
            subview.place(at: CGPoint(x: bounds.minX + result.positions[index].x,
                                      y: bounds.minY + result.positions[index].y),
                         proposal: .unspecified)
        }
    }

    struct FlowResult {
        var size: CGSize = .zero
        var positions: [CGPoint] = []

        init(in maxWidth: CGFloat, subviews: Subviews, spacing: CGFloat) {
            var x: CGFloat = 0
            var y: CGFloat = 0
            var rowHeight: CGFloat = 0

            for subview in subviews {
                let size = subview.sizeThatFits(.unspecified)

                if x + size.width > maxWidth && x > 0 {
                    x = 0
                    y += rowHeight + spacing
                    rowHeight = 0
                }

                positions.append(CGPoint(x: x, y: y))
                rowHeight = max(rowHeight, size.height)
                x += size.width + spacing
            }

            self.size = CGSize(width: maxWidth, height: y + rowHeight)
        }
    }
}

// MARK: - Search Post Item View

private struct SearchPostItemView: View {
    let post: PostModel

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(alignment: .top, spacing: 12) {
                // Text Content
                VStack(alignment: .leading, spacing: 0) {
                    // Board Pill
                    Text(post.boardName)
                        .font(.caption)
                        .fontWeight(.semibold)
                        .foregroundStyle(AppColors.onSurface.opacity(0.7))
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(AppColors.surfaceVariant)
                        .clipShape(Capsule())

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

                HStack(spacing: 6) {
                    Image(systemName: "eye")
                        .font(.caption)
                    Text("\(post.viewCount)")
                        .font(.caption)
                        .fontWeight(.medium)
                }
                .foregroundStyle(AppColors.primary)

                Spacer().frame(width: 10)

                HStack(spacing: 6) {
                    Image(systemName: "hand.thumbsup")
                        .font(.caption)
                    Text("\(post.likeCount)")
                        .font(.caption)
                        .fontWeight(.medium)
                }
                .foregroundStyle(AppColors.primary)

                Spacer().frame(width: 10)

                HStack(spacing: 6) {
                    Image(systemName: "bubble.left")
                        .font(.caption)
                    Text("\(post.commentCount)")
                        .font(.caption)
                        .fontWeight(.medium)
                }
                .foregroundStyle(AppColors.primary)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.08), radius: 4, x: 0, y: 2)
    }
}
