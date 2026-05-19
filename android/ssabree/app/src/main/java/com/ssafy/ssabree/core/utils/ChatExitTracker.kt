package com.ssafy.ssabree.core.utils

/**
 * 사용자가 나간(삭제한) 쪽지를 다시 시작하지 못하도록 막기 위한 로컬 추적기.
 * 서버에서 별도 상태를 주지 않으므로, 클라이언트 한정으로만 동작한다.
 */
object ChatExitTracker {
    private val exitedPostIds = mutableSetOf<Long>()

    fun markExited(postId: Long) {
        exitedPostIds.add(postId)
    }

    fun isExited(postId: Long): Boolean = exitedPostIds.contains(postId)
}
