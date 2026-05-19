package com.ssafy.ssabree.core.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class AuthLogoutReason {
    RETRY_EXCEEDED,
    REFRESH_TOKEN_MISSING,
    REFRESH_FAILED,
    USER_LOGOUT,
    USER_WITHDRAW
}

sealed interface AuthEvent {
    data class Logout(val reason: AuthLogoutReason) : AuthEvent
}

object AuthEventBus {
    private val _events = MutableSharedFlow<AuthEvent>(replay = 0, extraBufferCapacity = 1)
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    fun send(event: AuthEvent) {
        _events.tryEmit(event)
    }
}
