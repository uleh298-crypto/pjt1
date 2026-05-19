package com.ssafy.ssabree.core.datasource.remote

import com.ssafy.ssabree.core.datasource.remote.model.BoardResponse
import com.ssafy.ssabree.core.datasource.remote.model.BoardNoticeResponse
import retrofit2.http.GET

interface BoardService {
    @GET("/api/boards")
    suspend fun getBoards(): List<BoardResponse>

    @GET("/api/boards/notice")
    suspend fun getNotice(): BoardNoticeResponse
}
