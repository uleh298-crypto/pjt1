package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.datasource.remote.BoardService
import com.ssafy.ssabree.core.repository.model.BoardModel
import com.ssafy.ssabree.core.repository.model.toModel
import com.ssafy.ssabree.core.utils.RetrofitClient

class BoardRepositoryImpl : BoardRepository {

    private val boardService: BoardService = RetrofitClient.instance.create(BoardService::class.java)

    override suspend fun getBoards(): Result<List<BoardModel>> {
        return runCatching {
            boardService.getBoards().map { it.toModel() }
        }
    }

    override suspend fun getNotice(): Result<String?> {
        return runCatching {
            boardService.getNotice().content
        }
    }
}
