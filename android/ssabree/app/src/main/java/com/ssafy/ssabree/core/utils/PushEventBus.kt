package com.ssafy.ssabree.core.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface PushEvent {
    data class OpenChat(val roomId: Long) : PushEvent
    data class OpenPost(val postId: Long) : PushEvent
    data class OpenGroupApplication(val groupId: Long, val groupType: String) : PushEvent
    data class OpenApplicationAccepted(val groupId: Long, val groupType: String) : PushEvent
}

object PushEventBus {
    private val _events = MutableSharedFlow<PushEvent>(replay = 0, extraBufferCapacity = 1)
    val events: SharedFlow<PushEvent> = _events.asSharedFlow()

    fun send(event: PushEvent) {
        _events.tryEmit(event)
    }
}
