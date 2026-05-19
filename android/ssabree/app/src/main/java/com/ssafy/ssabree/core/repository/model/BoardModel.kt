package com.ssafy.ssabree.core.repository.model

import com.ssafy.ssabree.core.datasource.remote.model.BoardResponse

data class BoardModel(
    val id: Long,
    val name: String,
    val category: String?,
    val description: String?
)

fun BoardResponse.toModel(): BoardModel {
    return BoardModel(
        id = id,
        name = name,
        category = category,
        description = description
    )
}
