import Foundation
import Combine
import UIKit

/// 앱의 포그라운드/백그라운드 상태를 추적하는 싱글톤
/// Android의 AppForegroundTracker와 동일한 역할
final class AppForegroundTracker {
    static let shared = AppForegroundTracker()

    private let _isForeground = CurrentValueSubject<Bool, Never>(true)
    var isForeground: AnyPublisher<Bool, Never> { _isForeground.eraseToAnyPublisher() }
    var isForegroundValue: Bool { _isForeground.value }

    private var cancellables = Set<AnyCancellable>()

    private init() {
        // 앱이 포그라운드로 진입
        NotificationCenter.default.publisher(for: UIApplication.didBecomeActiveNotification)
            .sink { [weak self] _ in
                self?._isForeground.send(true)
            }
            .store(in: &cancellables)

        // 앱이 백그라운드로 진입
        NotificationCenter.default.publisher(for: UIApplication.willResignActiveNotification)
            .sink { [weak self] _ in
                self?._isForeground.send(false)
            }
            .store(in: &cancellables)
    }
}
