package com.ssafy.ssabree.features.board.model

import com.ssafy.ssabree.core.repository.model.CommentModel
import com.ssafy.ssabree.core.repository.model.PollModel
import com.ssafy.ssabree.core.repository.model.PollOptionModel
import com.ssafy.ssabree.core.repository.model.PostDetailModel
import com.ssafy.ssabree.core.repository.model.PostModel
import com.ssafy.ssabree.core.repository.model.ReplyModel
import com.ssafy.ssabree.core.utils.toAdaptiveKstText
import com.ssafy.ssabree.core.utils.toRelativeTimeText

private fun String.toPreview(maxLength: Int = 80): String {
    return if (length <= maxLength) this else substring(0, maxLength).trimEnd() + "..."
}

fun PostModel.toUiModel(): PostUiModel {
    return PostUiModel(
        id = id,
        badge = null,
        boardName = boardName,
        title = title,
        preview = content.toPreview(),
        dateText = createdAt.toRelativeTimeText(),
        viewCount = viewCount,
        likeCount = likeCount,
        commentCount = commentCount,
        imageUrl = imageUrls.firstOrNull(),
        isBlinded = isBlinded
    )
}

fun PostDetailModel.toUiModel(): PostDetailUiModel {
    return PostDetailUiModel(
        id = id,
        boardId = boardId,
        boardName = "Board",
        campusName = "",
        authorName = "싸용자",
        authorProfileUrl = null,
        isAuthor = isMine,
        authorId = authorId,
        dateText = createdAt.toAdaptiveKstText(),
        title = title,
        content = content,
        imageUrls = imageUrls,
        poll = poll?.toUiModel(),
        likeCount = likeCount,
        isLiked = isLiked,
        commentCount = commentCount,
        bookmarkCount = scrapCount,
        isBookmarked = isScraped,
        comments = comments.map { it.toUiModel() }
    )
}

fun CommentModel.toUiModel(): CommentUiModel {
    return CommentUiModel(
        id = id,
        authorName = anon?.name ?: "탈퇴한 싸용자",
        authorProfileUrl = null,
        isAuthor = anon?.isAuthor ?: false,
        isMine = anon?.isMine ?: false,
        content = content,
        dateText = createdAt.toAdaptiveKstText(),
        likeCount = likeCount,
        isLiked = isLiked,
        isBlinded = isBlinded,
        replies = replies.map { it.toUiModel() }
    )
}

fun ReplyModel.toUiModel(): CommentUiModel {
    return CommentUiModel(
        id = id,
        authorName = anon?.name ?: "탈퇴한 싸용자",
        authorProfileUrl = null,
        isAuthor = anon?.isAuthor ?: false,
        isMine = anon?.isMine ?: false,
        content = content,
        dateText = createdAt.toAdaptiveKstText(),
        likeCount = likeCount,
        isLiked = isLiked,
        isBlinded = isBlinded,
        replies = emptyList()
    )
}

fun PollModel.toUiModel(): PollUiModel =
    PollUiModel(
        pollId = pollId,
        totalVotes = totalVotes,
        myVotedOptionId = myVotedOptionId,
        options = options.map { it.toUiModel(myVotedOptionId) }
    )

fun PollOptionModel.toUiModel(myVote: Long?): PollOptionUiModel =
    PollOptionUiModel(
        optionId = optionId,
        text = text,
        voteCount = voteCount,
        isSelected = myVote == optionId
    )
