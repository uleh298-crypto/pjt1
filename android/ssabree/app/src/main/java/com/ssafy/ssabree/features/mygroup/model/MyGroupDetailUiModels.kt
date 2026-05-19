package com.ssafy.ssabree.features.mygroup.model

import com.ssafy.ssabree.core.repository.model.GroupApplicationModel
import com.ssafy.ssabree.core.repository.model.GroupNoticeModel
import com.ssafy.ssabree.core.repository.model.GroupTaskModel

data class ApplicationUiModel(
    val id: Long,
    val title: String,
    val message: String,
    val position: String,
    val status: String,
    // New fields from portfolio
    val applicantName: String? = null,
    val applicantProfileImageUrl: String? = null,
    val portfolioTitle: String? = null,
    val portfolioId: Long? = null
)

data class NoticeUiModel(
    val id: Long,
    val title: String,
    val content: String,
    val isPinned: Boolean,
    val createdAt: String
)

data class TaskUiModel(
    val id: Long,
    val title: String,
    val content: String,
    val startDate: String,
    val endDate: String,
    val status: String,
    val creatorId: Long? = null,
    val authorName: String? = null,
    val authorProfileImageUrl: String? = null
)

data class MemberUiModel(
    val id: Long,
    val name: String,
    val mattermostId: String,
    val profileImageUrl: String?,
    val portfolioId: Long? = null
)

fun GroupApplicationModel.toUiModel(): ApplicationUiModel =
    ApplicationUiModel(
        id = id,
        title = title,
        message = message,
        position = position,
        status = status,
        applicantName = portfolio?.memberName,
        applicantProfileImageUrl = portfolio?.memberProfileImageUrl,
        portfolioTitle = portfolio?.title,
        portfolioId = portfolio?.id ?: portfolioId
    )

fun GroupNoticeModel.toUiModel(): NoticeUiModel =
    NoticeUiModel(
        id = id,
        title = title,
        content = content,
        isPinned = isPinned,
        createdAt = createdAt
    )

fun GroupTaskModel.toUiModel(): TaskUiModel =
    TaskUiModel(
        id = id,
        title = title,
        content = content,
        startDate = startDate,
        endDate = endDate,
        status = status,
        creatorId = creatorId
    )

fun com.ssafy.ssabree.core.repository.model.GroupMemberModel.toUiModel(): MemberUiModel =
    MemberUiModel(
        id = id,
        name = name ?: "-",
        mattermostId = mattermostId ?: "-",
        profileImageUrl = profileImageUrl,
        portfolioId = portfolioId
    )
