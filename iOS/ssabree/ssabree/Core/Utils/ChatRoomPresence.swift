import Foundation
import Combine

/// 현재 활성화된 채팅방을 추적하는 싱글톤
/// Android의 ChatRoomPresence와 동일한 역할
/// 포그라운드 상태에서 특정 채팅방이 열려있을 때 해당 채팅방 알림을 억제하기 위해 사용
final class ChatRoomPresence {
    static let shared = ChatRoomPresence()

    private let _activeRoomId = CurrentValueSubject<Int?, Never>(nil)
    var activeRoomId: AnyPublisher<Int?, Never> { _activeRoomId.eraseToAnyPublisher() }
    var activeRoomIdValue: Int? { _activeRoomId.value }

    private init() {}

    /// 채팅방 진입 시 호출
    func enter(roomId: Int) {
        _activeRoomId.send(roomId)
    }

    /// 채팅방 퇴장 시 호출
    func exit(roomId: Int? = nil) {
        if roomId == nil || _activeRoomId.value == roomId {
            _activeRoomId.send(nil)
        }
    }
}
