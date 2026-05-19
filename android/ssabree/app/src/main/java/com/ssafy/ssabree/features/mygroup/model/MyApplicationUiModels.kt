package com.ssafy.ssabree.features.mygroup.model

import com.ssafy.ssabree.core.repository.model.MyApplicationModel

data class MyApplicationUiModel(
    val id: Long,
    val groupId: Long,
    val groupTitle: String,
    val leaderName: String?,
    val status: String,
    val position: String,
    val createdAt: String?,
    val isGroupDeleted: Boolean
) {
    val isPending: Boolean
        get() = status == "PENDING" && !isGroupDeleted

    val statusMessage: String
        get() = when (status) {
            "DELETED" -> "그룹이 삭제됐습니다."
            "APPROVED" -> "지원서가 수락됐습니다."
            "REJECTED" -> "지원서가 거절됐습니다."
            else -> if (isGroupDeleted) "그룹이 삭제됐습니다." else "승인 대기중입니다."
        }
}

fun MyApplicationModel.toUiModel(): MyApplicationUiModel =
    MyApplicationUiModel(
        id = id,
        groupId = groupId,
        groupTitle = groupTitle,
        leaderName = leaderName,
        status = status,
        position = position,
        createdAt = createdAt,
        isGroupDeleted = isGroupDeleted
    )
