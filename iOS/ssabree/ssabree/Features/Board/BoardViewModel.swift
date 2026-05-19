import Foundation
import Observation

// MARK: - Filter Option

struct BoardFilterOption: Equatable {
    let id: Int?
    let label: String

    static let all = BoardFilterOption(id: nil, label: "전체보기")
}

// MARK: - UI State

struct BoardUiState {
    var selectedFilter: BoardFilterOption = .all
    var filterMenuExpanded: Bool = false
    var posts: [PostModel] = []
    var filterOptions: [BoardFilterOption] = [.all]
    var nextCursor: String? = nil
    var hasNext: Bool = true
    var isRefreshing: Bool = false
    var isLoadingMore: Bool = false
    var errorMessage: String? = nil
    var selectedBoardName: String = "게시판"
    var isHotSelected: Bool = false
    var noticeContent: String? = nil
}

// MARK: - ViewModel

@Observable
@MainActor
final class BoardViewModel {
    private let boardRepository: BoardRepository
    private let postRepository: PostRepository
    private let pageSize = 20

    private(set) var uiState = BoardUiState()
    private var hasLoadedInitialData = false
    private let initialBoardId: Int?
    private var lastHotToggleTime: CFAbsoluteTime = 0

    init(boardRepository: BoardRepository, postRepository: PostRepository, initialBoardId: Int? = nil) {
        self.boardRepository = boardRepository
        self.postRepository = postRepository
        self.initialBoardId = initialBoardId
        // init에서 Task 제거 - View의 .task modifier에서 loadInitialDataIfNeeded 호출
    }

    /// View의 .task modifier에서 호출 - 최초 1회만 데이터 로드
    /// Task.detached를 사용하여 SwiftUI의 .task cancellation으로부터 보호
    func loadInitialDataIfNeeded() async {
        guard !hasLoadedInitialData else { return }
        hasLoadedInitialData = true

        // Task.detached: 부모 Task의 cancellation을 상속받지 않음
        // SwiftUI .task가 cancel되어도 네트워크 요청은 계속 진행됨
        await Task.detached { @MainActor [self] in
            // 게시판 목록 + 공지사항 로드
            async let boardsTask: () = self.loadBoardOptions()
            async let noticeTask: () = self.loadNotice()
            _ = await (boardsTask, noticeTask)
            // 기본적으로 전체 게시글 로드 (isHotSelected = false 이므로)
            await self.loadPosts(boardId: nil, cursor: nil, append: false)
        }.value
    }

    private func loadBoardOptions() async {
        let boardsResult = await boardRepository.getBoards()
        var filterOptions: [BoardFilterOption] = [.all]

        switch boardsResult {
        case .success(let boards):
            filterOptions += boards.map { BoardFilterOption(id: $0.id, label: $0.title) }
        case .failure(let error):
            print("[BoardViewModel] getBoards failed: \(error)")
        }

        uiState.filterOptions = filterOptions
    }

    private func loadNotice() async {
        let result = await boardRepository.getNotice()
        switch result {
        case .success(let content):
            uiState.noticeContent = content
        case .failure(let error):
            print("[BoardViewModel] getNotice failed: \(error)")
        }
    }

    func refreshBoardsAndPosts(boardIdToSelect: Int? = nil) async {
        uiState.isRefreshing = true
        uiState.errorMessage = nil

        async let boardsTask: () = loadBoardOptions()
        async let noticeTask: () = loadNotice()
        _ = await (boardsTask, noticeTask)

        let selected = uiState.filterOptions.first { $0.id == boardIdToSelect }
            ?? uiState.filterOptions.first
            ?? .all

        uiState.selectedFilter = selected
        uiState.selectedBoardName = selected.label
        uiState.isHotSelected = false
        uiState.posts = []
        uiState.nextCursor = nil
        uiState.hasNext = true

        await loadPosts(boardId: selected.id, cursor: nil, append: false)
    }

    private func loadPosts(boardId: Int?, cursor: String?, append: Bool) async {
        if append {
            uiState.isLoadingMore = true
        } else {
            uiState.isRefreshing = true
        }
        uiState.errorMessage = nil

        let result = await postRepository.getPosts(boardId: boardId, keyword: nil, cursor: cursor, limit: pageSize)

        switch result {
        case .success(let page):
            if append {
                uiState.posts += page.data
            } else {
                uiState.posts = page.data
            }
            uiState.nextCursor = page.nextCursor
            uiState.hasNext = page.hasNext
            uiState.isRefreshing = false
            uiState.isLoadingMore = false
            uiState.errorMessage = nil

        case .failure(let error):
            print("[BoardViewModel] loadPosts failed: \(error)")
            uiState.isRefreshing = false
            uiState.isLoadingMore = false
            uiState.errorMessage = error.localizedDescription
        }
    }

    private func loadHotPosts(cursor: String?, append: Bool) async {
        if append {
            uiState.isLoadingMore = true
        } else {
            uiState.isRefreshing = true
        }
        uiState.errorMessage = nil

        let result = await postRepository.getHotPosts(cursor: cursor, limit: pageSize)

        switch result {
        case .success(let page):
            if append {
                uiState.posts += page.data
            } else {
                uiState.posts = page.data
            }
            uiState.nextCursor = page.nextCursor
            uiState.hasNext = page.hasNext
            uiState.isRefreshing = false
            uiState.isLoadingMore = false
            uiState.errorMessage = nil

        case .failure(let error):
            print("[BoardViewModel] loadHotPosts failed: \(error)")
            uiState.isRefreshing = false
            uiState.isLoadingMore = false
            uiState.errorMessage = error.localizedDescription
        }
    }

    /// Pull-to-refresh: 현재 선택된 게시판의 게시글만 다시 로드
    /// Task.detached로 .refreshable의 cancellation으로부터 보호
    func onRefresh() async {
        await Task.detached { @MainActor [self] in
            self.uiState.posts = []
            self.uiState.nextCursor = nil
            self.uiState.hasNext = true

            if self.uiState.isHotSelected {
                await self.loadHotPosts(cursor: nil, append: false)
            } else {
                await self.loadPosts(boardId: self.uiState.selectedFilter.id, cursor: nil, append: false)
            }
        }.value
    }

    func onHotSelected() {
        // 600ms 디바운스
        let now = CFAbsoluteTimeGetCurrent()
        if now - lastHotToggleTime < 0.6 { return }
        lastHotToggleTime = now

        // 이미 Hot이면 해제하고 현재 선택된 게시판으로 복귀
        if uiState.isHotSelected {
            uiState.isHotSelected = false
            uiState.selectedBoardName = uiState.selectedFilter.label
            uiState.posts = []
            uiState.nextCursor = nil
            uiState.hasNext = true
            uiState.isRefreshing = true

            Task.detached { @MainActor [self] in
                await self.loadPosts(boardId: self.uiState.selectedFilter.id, cursor: nil, append: false)
            }
            return
        }

        uiState.isHotSelected = true
        uiState.selectedBoardName = "Hot 게시글"
        uiState.posts = []
        uiState.nextCursor = nil
        uiState.hasNext = true
        uiState.isRefreshing = true

        Task.detached { @MainActor [self] in
            await self.loadHotPosts(cursor: nil, append: false)
        }
    }

    func onFilterChipClick() {
        uiState.filterMenuExpanded = true
    }

    func onFilterMenuDismiss() {
        uiState.filterMenuExpanded = false
    }

    func onFilterSelected(filter: BoardFilterOption) {
        uiState.isHotSelected = false
        uiState.selectedFilter = filter
        uiState.selectedBoardName = filter.label
        uiState.filterMenuExpanded = false
        uiState.isRefreshing = true
        uiState.posts = []
        uiState.nextCursor = nil
        uiState.hasNext = true

        Task.detached { @MainActor [self] in
            await self.loadPosts(boardId: filter.id, cursor: nil, append: false)
        }
    }

    func onListEndReached(index: Int) {
        let shouldLoadMore = uiState.hasNext &&
            !uiState.isLoadingMore &&
            !uiState.isRefreshing &&
            index >= uiState.posts.count - 3

        if shouldLoadMore {
            Task.detached { @MainActor [self] in
                if self.uiState.isHotSelected {
                    await self.loadHotPosts(cursor: self.uiState.nextCursor, append: true)
                } else {
                    await self.loadPosts(boardId: self.uiState.selectedFilter.id, cursor: self.uiState.nextCursor, append: true)
                }
            }
        }
    }
}
