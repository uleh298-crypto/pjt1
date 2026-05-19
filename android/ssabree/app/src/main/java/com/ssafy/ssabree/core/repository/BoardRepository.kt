package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.repository.model.BoardModel

interface BoardRepository {
    suspend fun getBoards(): Result<List<BoardModel>>
    suspend fun getNotice(): Result<String?>
}
