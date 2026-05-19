import Foundation
import Observation

// MARK: - UI State

struct BoardSearchUiState {
    var query: String = ""
    var recentKeywords: [String] = []
    var results: [PostModel] = []
    var nextCursor: String? = nil
    var hasNext: Bool = false
    var isLoading: Bool = false
    var isLoadingMore: Bool = false
    var error: String? = nil
    var hasSearched: Bool = false  // 검색을 한 번이라도 했는지
}

// MARK: - ViewModel

@Observable
@MainActor
final class BoardSearchViewModel {
    private let postRepository: PostRepository
    private let keywordRepository: KeywordRepository
    private let pageSize = 20

    var uiState = BoardSearchUiState()

    init(postRepository: PostRepository, keywordRepository: KeywordRepository) {
        self.postRepository = postRepository
        self.keywordRepository = keywordRepository
        loadRecentKeywords()
    }

    // MARK: - Query Change

    func onQueryChange(_ text: String) {
        uiState.query = text
    }

    // MARK: - Search

    func onSearchSubmit() {
        let keyword = uiState.query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !keyword.isEmpty else { return }
        performSearch(keyword: keyword)
    }

    func onQuickSearch(keyword: String) {
        uiState.query = keyword
        performSearch(keyword: keyword)
    }

    private func performSearch(keyword: String) {
        keywordRepository.addRecentKeyword(keyword: keyword)
        loadRecentKeywords()

        Task {
            await load(keyword: keyword, cursor: nil, append: false)
        }
    }

    // MARK: - Load

    private func load(keyword: String, cursor: String?, append: Bool) async {
        if append {
            uiState.isLoadingMore = true
        } else {
            uiState.isLoading = true
            uiState.error = nil
            uiState.hasSearched = true
        }

        let result = await postRepository.getPosts(
            boardId: nil,
            keyword: keyword,
            cursor: cursor,
            limit: pageSize
        )

        switch result {
        case .success(let page):
            if append {
                uiState.results += page.data
            } else {
                uiState.results = page.data
            }
            uiState.nextCursor = page.nextCursor
            uiState.hasNext = page.hasNext
        case .failure(let error):
            uiState.error = error.localizedDescription
        }

        uiState.isLoading = false
        uiState.isLoadingMore = false
    }

    // MARK: - Load More (Infinite Scroll)

    func loadMoreIfNeeded(currentIndex: Int) {
        // 마지막 3개 아이템에 도달하면 더 로드
        let threshold = uiState.results.count - 3
        guard currentIndex >= threshold,
              uiState.hasNext,
              !uiState.isLoadingMore,
              !uiState.isLoading,
              !uiState.query.isEmpty else { return }

        Task {
            await load(keyword: uiState.query, cursor: uiState.nextCursor, append: true)
        }
    }

    // MARK: - Recent Keywords

    private func loadRecentKeywords() {
        uiState.recentKeywords = keywordRepository.getRecentKeywords()
    }

    func deleteRecentKeyword(_ keyword: String) {
        keywordRepository.deleteRecentKeyword(keyword: keyword)
        loadRecentKeywords()
    }

    // MARK: - Clear

    func clearQuery() {
        uiState.query = ""
    }

    func clearError() {
        uiState.error = nil
    }
}
