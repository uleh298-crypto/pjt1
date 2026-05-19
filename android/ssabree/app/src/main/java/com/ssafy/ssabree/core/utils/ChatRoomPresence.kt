package com.ssafy.ssabree.core.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 현재 열려 있는 채팅방(roomId)을 공유하는 전역 상태.
 * 채팅 상세 화면 진입/이탈 시 업데이트하여
 * FCM 알림 억제 등에 활용한다.
 */
object ChatRoomPresence {
    private val _activeRoomId = MutableStateFlow<Long?>(null)
    val activeRoomId: StateFlow<Long?> = _activeRoomId.asStateFlow()

    fun enter(roomId: Long) {
        _activeRoomId.value = roomId
    }

    fun exit(roomId: Long? = null) {
        if (roomId == null || _activeRoomId.value == roomId) {
            _activeRoomId.value = null
        }
    }
}
