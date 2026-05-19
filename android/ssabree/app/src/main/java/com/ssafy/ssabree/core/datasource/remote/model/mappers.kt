package com.ssafy.ssabree.core.datasource.remote.model

import com.ssafy.ssabree.core.repository.model.SignUpInfo
import com.ssafy.ssabree.core.repository.model.CommentCreateInfo
import com.ssafy.ssabree.core.repository.model.PostCreateInfo
import com.ssafy.ssabree.core.repository.model.PostUpdateInfo
import com.ssafy.ssabree.core.repository.model.ReplyCreateInfo
import com.ssafy.ssabree.core.repository.model.VoteInfo
import com.ssafy.ssabree.core.repository.model.PollCreateInfo
import com.ssafy.ssabree.core.repository.model.GroupApplyInfo
import com.ssafy.ssabree.core.repository.model.GroupCreateInfo
import com.ssafy.ssabree.core.repository.model.GroupUpdateInfo
import com.ssafy.ssabree.core.repository.model.GroupNoticeCreateInfo
import com.ssafy.ssabree.core.repository.model.GroupTaskCreateInfo
import com.ssafy.ssabree.core.repository.model.GroupTaskStatusUpdateInfo
import com.ssafy.ssabree.core.repository.model.PortfolioCreateInfo
import com.ssafy.ssabree.core.repository.model.PortfolioImageUpdateInfo
import com.ssafy.ssabree.core.repository.model.PortfolioStackUpdateInfo
import com.ssafy.ssabree.core.repository.model.PortfolioUpdateInfo
import com.ssafy.ssabree.core.repository.model.PortfolioUrlUpdateInfo
import com.ssafy.ssabree.core.repository.model.ProjectCreateInfo
import com.ssafy.ssabree.core.repository.model.ProjectUpdateInfo

fun SignUpInfo.toSignUpRequest(): SignUpRequest {
    return SignUpRequest(
        email = email,
        password = password,
        name = name,
        studentNo = studentNo,
        campus = campus,
        generation = generation,
        classNo = classNo,
        mattermostId = mattermostId
    )
}

fun PostCreateInfo.toPostCreateRequest(): PostCreateRequest {
    return PostCreateRequest(
        title = title,
        content = content,
        boardId = boardId,
        imageUrls = imageUrls,
        poll = poll?.toPollCreateRequest()
    )
}

fun PostUpdateInfo.toPostUpdateRequest(): PostUpdateRequest {
    return PostUpdateRequest(
        title = title,
        content = content
    )
}

fun CommentCreateInfo.toCommentCreateRequest(): CommentCreateRequest {
    return CommentCreateRequest(
        content = content
    )
}

fun ReplyCreateInfo.toReplyCreateRequest(): ReplyCreateRequest {
    return ReplyCreateRequest(
        content = content
    )
}

fun VoteInfo.toVoteRequest(): VoteRequest {
    return VoteRequest(
        optionId = optionId
    )
}

fun PollCreateInfo.toPollCreateRequest(): PollCreateRequest {
    return PollCreateRequest(
        title = title,
        options = options
    )
}
fun GroupCreateInfo.toGroupCreateRequest(): GroupCreateRequest {
    return GroupCreateRequest(
        title = title,
        type = type,
        capacity = capacity,
        startDate = startDate,
        endDate = endDate,
        campusId = campusId,
        description = description
    )
}

fun GroupUpdateInfo.toGroupUpdateRequest(): GroupUpdateRequest {
    return GroupUpdateRequest(
        title = title,
        type = type,
        capacity = capacity,
        startDate = startDate,
        endDate = endDate,
        campusId = campusId,
        description = description,
        status = status
    )
}

fun GroupApplyInfo.toGroupApplicationRequest(): GroupApplicationRequest {
    return GroupApplicationRequest(
        portfolioId = portfolioId,
        title = title,
        message = message,
        position = position
    )
}

fun GroupNoticeCreateInfo.toNoticeRequest(): GroupNoticeRequest {
    return GroupNoticeRequest(
        title = title,
        content = content,
        isPinned = isPinned,
        sendPushNotification = sendPushNotification
    )
}

fun GroupTaskCreateInfo.toTaskRequest(): GroupTaskRequest {
    return GroupTaskRequest(
        title = title,
        content = content,
        startDate = startDate,
        endDate = endDate,
        status = status
    )
}

fun GroupTaskStatusUpdateInfo.toTaskStatusRequest(): GroupTaskStatusRequest {
    return GroupTaskStatusRequest(
        status = status
    )
}

fun PortfolioUpdateInfo.toPortfolioUpdateRequest(): PortfolioUpdateRequest {
    return PortfolioUpdateRequest(
        title = title,
        description = description,
        introduction = introduction,
        bojHandle = bojHandle,
        solvedacRank = solvedacRank,
        swTestRank = swTestRank,
        isVisible = isVisible,
        stacks = stacks.map { it.toPortfolioStackRequest() },
        urls = urls.map { it.toPortfolioUrlRequest() },
        images = images.map { it.toPortfolioImageRequest() }
    )
}

fun PortfolioCreateInfo.toPortfolioCreateRequest(): PortfolioCreateRequest {
    return PortfolioCreateRequest(
        title = title,
        description = description,
        introduction = introduction,
        bojHandle = bojHandle,
        solvedacRank = solvedacRank,
        swTestRank = swTestRank,
        isVisible = isVisible,
        stacks = stacks.map { it.toPortfolioStackRequest() },
        urls = urls.map { it.toPortfolioUrlRequest() },
        images = images.map { it.toPortfolioImageRequest() }
    )
}

fun PortfolioStackUpdateInfo.toPortfolioStackRequest(): PortfolioStackRequest {
    return PortfolioStackRequest(
        stackId = stackId,
        expertLevel = expertLevel
    )
}

fun PortfolioUrlUpdateInfo.toPortfolioUrlRequest(): PortfolioUrlRequest {
    return PortfolioUrlRequest(
        url = url
    )
}

fun PortfolioImageUpdateInfo.toPortfolioImageRequest(): PortfolioImageRequest {
    return PortfolioImageRequest(
        imageUrl = imageUrl,
        orders = orders
    )
}

fun ProjectCreateInfo.toProjectCreateRequest(): ProjectCreateRequest {
    return ProjectCreateRequest(
        portfolioId = portfolioId,
        title = title,
        introduction = introduction,
        description = description,
        techStacks = techStacks,
        urls = urls,
        imageUrls = imageUrls
    )
}

fun ProjectUpdateInfo.toProjectUpdateRequest(): ProjectUpdateRequest {
    return ProjectUpdateRequest(
        title = title,
        introduction = introduction,
        description = description,
        techStacks = techStacks,
        urls = urls,
        imageUrls = imageUrls
    )
}
