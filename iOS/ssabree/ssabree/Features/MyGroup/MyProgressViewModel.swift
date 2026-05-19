import Foundation
import Observation

struct MyProgressUiState {
    var progressList: [ProgressModel] = []
    var isLoading: Bool = false
    var error: String? = nil
}

@Observable
final class MyProgressViewModel {
    private let myGroupRepository: MyGroupRepository
    var uiState = MyProgressUiState()
    
    init(myGroupRepository: MyGroupRepository) {
        self.myGroupRepository = myGroupRepository
    }
    
    @MainActor
    func loadProgress(groupId: Int) async {
        uiState.isLoading = true
        let result = await myGroupRepository.getProgress(groupId: groupId)
        switch result {
        case .success(let data):
            uiState.progressList = data
            uiState.error = nil
        case .failure(let error):
            uiState.error = error.localizedDescription
        }
        uiState.isLoading = false
    }
}
