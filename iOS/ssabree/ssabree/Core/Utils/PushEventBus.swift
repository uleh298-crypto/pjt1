import Foundation
import Combine

/// 푸시 알림에서 앱 내 네비게이션 이벤트를 전파하는 이벤트 버스

enum PushEvent {
    case openChat(roomId: Int)
    case openPost(postId: Int)
    case openGroupApplication(groupId: Int, groupType: String)
    case openApplicationAccepted(groupId: Int, groupType: String)
}

final class PushEventBus {
    static let shared = PushEventBus()

    private let _events = PassthroughSubject<PushEvent, Never>()
    var events: AnyPublisher<PushEvent, Never> { _events.eraseToAnyPublisher() }

    /// 콜드 스타트 시 구독자가 아직 없을 때 이벤트를 임시 저장
    private(set) var pendingEvent: PushEvent?

    private init() {}

    func send(_ event: PushEvent) {
        pendingEvent = event
        _events.send(event)
    }

    func consumePending() -> PushEvent? {
        let event = pendingEvent
        pendingEvent = nil
        return event
    }
}
