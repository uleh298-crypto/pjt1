import Foundation
import Observation

struct MyGroupUiState {
    var myGroups: [GroupModel] = []
    var applications: [ApplicationModel] = []
    var isLoading: Bool = false
    var error: String? = nil
}

@Observable
final class MyGroupViewModel {
    private let myGroupRepository: MyGroupRepository
    var uiState = MyGroupUiState()
    private var hasLoadedInitialData = false

    init(myGroupRepository: MyGroupRepository) {
        self.myGroupRepository = myGroupRepository
        // init에서 Task 제거 - View의 .task modifier에서 loadInitialDataIfNeeded 호출
    }

    /// View의 .task modifier에서 호출 - 최초 1회만 데이터 로드
    /// Task.detached를 사용하여 SwiftUI의 .task cancellation으로부터 보호
    @MainActor
    func loadInitialDataIfNeeded() async {
        guard !hasLoadedInitialData else { return }
        hasLoadedInitialData = true

        await Task.detached { @MainActor [self] in
            await self.loadData()
        }.value
    }

    @MainActor
    func loadData() async {
        uiState.isLoading = true
        uiState.error = nil
        
        async let groupsResult = myGroupRepository.getMyGroups()
        async let appsResult = myGroupRepository.getApplications()
        
        let (groups, apps) = await (groupsResult, appsResult)
        
        switch groups {
        case .success(let data):
            uiState.myGroups = data
        case .failure(let error):
            uiState.error = error.localizedDescription
        }
        
        switch apps {
        case .success(let data):
            uiState.applications = data
        case .failure:
            // Fail silently or log error
            break
        }
        
        uiState.isLoading = false
    }
}
