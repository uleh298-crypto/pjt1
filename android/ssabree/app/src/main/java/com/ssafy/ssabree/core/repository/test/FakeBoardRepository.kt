package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.repository.BoardRepository
import com.ssafy.ssabree.core.repository.model.BoardModel

class FakeBoardRepository : BoardRepository {
    override suspend fun getBoards(): Result<List<BoardModel>> {
        val boards = listOf(
            BoardModel(id = 1L, name = "공지사항", category = "NOTICE", description = null),
            BoardModel(id = 2L, name = "자유게시판", category = "FREE", description = null),
            BoardModel(id = 3L, name = "서울캠퍼스", category = "SEOUL", description = null),
            BoardModel(id = 4L, name = "구미캠퍼스", category = "GUMI", description = null)
        )
        return runCatching { boards }
    }

    override suspend fun getNotice(): Result<String?> {
        return runCatching { "비방, 욕설, 성희롱 등은 경고없이 밴 당합니다~!\n경고 10회 누적 시 실명공개~!~!!!!" }
    }
}
