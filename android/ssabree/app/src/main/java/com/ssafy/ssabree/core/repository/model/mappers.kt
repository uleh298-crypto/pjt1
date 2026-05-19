package com.ssafy.ssabree.core.repository.model

import com.ssafy.ssabree.core.datasource.remote.model.AnonResponse
import com.ssafy.ssabree.core.datasource.remote.model.CampusMealResponse
import com.ssafy.ssabree.core.datasource.remote.model.CampusResponse
import com.ssafy.ssabree.core.datasource.remote.model.CommentLikeResponse
import com.ssafy.ssabree.core.datasource.remote.model.CommentResponse
import com.ssafy.ssabree.core.datasource.remote.model.DdayResponse
import com.ssafy.ssabree.core.datasource.remote.model.GroupApplicationResponse
import com.ssafy.ssabree.core.datasource.remote.model.GroupDetailResponse
import com.ssafy.ssabree.core.datasource.remote.model.GroupMemberItemResponse
import com.ssafy.ssabree.core.datasource.remote.model.GroupMemberResponse
import com.ssafy.ssabree.core.datasource.remote.model.GroupNoticeResponse
import com.ssafy.ssabree.core.datasource.remote.model.GroupSummaryResponse
import com.ssafy.ssabree.core.datasource.remote.model.GroupTaskResponse
import com.ssafy.ssabree.core.datasource.remote.model.HomeResponse
import com.ssafy.ssabree.core.datasource.remote.model.TeamApplicationResponse
import com.ssafy.ssabree.core.datasource.remote.model.StudyMemberResponse
import com.ssafy.ssabree.core.datasource.remote.model.PortfolioSummaryResponse
import com.ssafy.ssabree.core.datasource.remote.model.InquiryResponse
import com.ssafy.ssabree.core.datasource.remote.model.MyCommentResponse
import com.ssafy.ssabree.core.datasource.remote.model.MyPageCountsResponse
import com.ssafy.ssabree.core.datasource.remote.model.MyPagePortfolioSummaryResponse
import com.ssafy.ssabree.core.datasource.remote.model.MyPageResponse
import com.ssafy.ssabree.core.datasource.remote.model.MyPageUserInfoResponse
import com.ssafy.ssabree.core.datasource.remote.model.PollOptionResponse
import com.ssafy.ssabree.core.datasource.remote.model.PollResponse
import com.ssafy.ssabree.core.datasource.remote.model.PortfolioImageResponse
import com.ssafy.ssabree.core.datasource.remote.model.PortfolioResponse
import com.ssafy.ssabree.core.datasource.remote.model.PortfolioStackResponse
import com.ssafy.ssabree.core.datasource.remote.model.PortfolioUrlResponse
import com.ssafy.ssabree.core.datasource.remote.model.PostDetailResponse
import com.ssafy.ssabree.core.datasource.remote.model.PostLikeResponse
import com.ssafy.ssabree.core.datasource.remote.model.PostResponse
import com.ssafy.ssabree.core.datasource.remote.model.PagedPostResponse
import com.ssafy.ssabree.core.datasource.remote.model.ProjectItemResponse
import com.ssafy.ssabree.core.datasource.remote.model.ProjectListResponse
import com.ssafy.ssabree.core.datasource.remote.model.ReplyResponse
import com.ssafy.ssabree.core.datasource.remote.model.ScrapResponse
import com.ssafy.ssabree.core.datasource.remote.model.StackResponse
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit


fun HomeResponse.toModel(): HomeModel =
    HomeModel(
        dDays = dDays.map { DDayModel(it.title, it.days) },
        team = teamThumbnail?.let { RecruitThumbModel(it.id, it.name ?: "제목이 없습니다.", it.count) },
        study = studyThumbnail?.let { RecruitThumbModel(it.id, it.name ?: "제목이 없습니다.", it.count) },
        campusMeals = campusMeals.map { it.toModel() },
        boards = boardsList.map { BoardThumbModel(it.boardId, it.name, it.recentPostTitle) }
    )

fun DdayResponse.toDdayItemModel(): DdayItemModel =
    DdayItemModel(
        id = id,
        title = title,
        targetDate = targetDate,
        dDay = calculateDdayFromApiDate(targetDate),
        iconKey = iconKey
    )

private fun calculateDdayFromApiDate(dateString: String): Int {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        val target = Calendar.getInstance().apply {
            time = sdf.parse(dateString) ?: return Int.MAX_VALUE
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diff = target.timeInMillis - today.timeInMillis
        TimeUnit.MILLISECONDS.toDays(diff).toInt()
    } catch (_: Exception) {
        Int.MAX_VALUE
    }
}

fun CampusMealResponse.toModel(): CampusMealModel =
    CampusMealModel(
        campusId = campusId,
        campusName = campusName,
        imageUrls = imageUrls
    )

fun PostResponse.toModel(): PostModel =
    PostModel(
        id = id,
        boardId = boardId,
        boardName = boardName,
        isMine = isMine,
        title = title,
        content = content,
        viewCount = viewCount,
        likeCount = likeCount,
        commentCount = commentCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        imageUrls = imageUrls ?: emptyList(),
        isBlinded = isBlinded
    )

fun PagedPostResponse.toModel(): PagedPostModel =
    PagedPostModel(
        posts = posts.map { it.toModel() },
        nextCursor = nextCursor,
        hasNext = hasNext
    )

fun PostDetailResponse.toModel(): PostDetailModel =
    PostDetailModel(
        createdAt = createdAt,
        updatedAt = updatedAt,
        id = id,
        boardId = boardId,
        isMine = isMine,
        authorId = authorId,
        title = title,
        content = content,
        isBlinded = isBlinded,
        imageUrls = imageUrls ?: emptyList(),
        poll = poll?.toModel(),
        likeCount = likeCount,
        isLiked = isLiked,
        commentCount = commentCount,
        scrapCount = scrapCount,
        isScraped = isScraped,
        comments = comments.map { it.toModel() }
    )

fun CommentResponse.toModel(): CommentModel =
    CommentModel(
        id = id,
        createdAt = createdAt,
        content = content,
        likeCount = likeCount,
        isLiked = isLiked,
        isBlinded = isBlinded,
        anon = anon?.toModel(),
        replies = replies.map { it.toModel() }
    )

fun ReplyResponse.toModel(): ReplyModel =
    ReplyModel(
        id = id,
        createdAt = createdAt,
        content = content,
        likeCount = likeCount,
        isLiked = isLiked,
        isBlinded = isBlinded,
        anon = anon?.toModel()
    )

fun PollResponse.toModel(): PollModel =
    PollModel(
        pollId = pollId,
        totalVotes = totalVotes,
        myVotedOptionId = myVotedOptionId,
        options = options.map { it.toModel() }
    )

fun PollOptionResponse.toModel(): PollOptionModel =
    PollOptionModel(
        optionId = optionId,
        text = text,
        voteCount = voteCount
    )

fun AnonResponse.toModel(): AnonModel =
    AnonModel(
        name = name,
        isAuthor = isAuthor,
        isMine = isMine
    )

fun PostLikeResponse.toModel(): PostLikeModel =
    PostLikeModel(
        liked = liked,
        likeCount = likeCount
    )

fun ScrapResponse.toModel(): ScrapModel =
    ScrapModel(
        success = success
    )

fun CommentLikeResponse.toModel(): CommentLikeModel =
    CommentLikeModel(
        liked = liked,
        likeCount = likeCount
    )

fun MyPageResponse.toModel(): MyPageModel =
    MyPageModel(
        user = user?.toModel(),
        counts = counts?.toModel(),
        portfolioSummary = portfolioSummary?.toModel()
    )

fun MyPageUserInfoResponse.toModel(): MyPageUserModel =
    MyPageUserModel(
        userId = userId,
        name = name,
        mattermostId = mattermostId,
        campus = campus,
        generation = generation,
        profileImageUrl = profileImageUrl
    )

fun MyPageCountsResponse.toModel(): MyPageCountsModel =
    MyPageCountsModel(
        postCount = postCount,
        commentCount = commentCount,
        scrapCount = scrapCount
    )

fun MyPagePortfolioSummaryResponse.toModel(): MyPagePortfolioSummaryModel =
    MyPagePortfolioSummaryModel(
        techStack = techStack ?: emptyMap(),
        ssafySwRating = ssafySwRating,
        solvedAcRank = solvedAcRank,
        solvedAcHandle = solvedAcHandle,
        solvedAcTierName = solvedAcTierName,
        solvedAcTierImageUrl = solvedAcTierImageUrl,
        solvedAcSolvedCount = solvedAcSolvedCount,
        links = links ?: emptyList(),
        projects = projects ?: emptyList()
    )

fun MyCommentResponse.toModel(): MyCommentModel =
    MyCommentModel(
        id = id,
        content = content,
        createdAt = createdAt,
        isReply = isReply,
        postId = postId,
        postTitle = postTitle,
        boardId = boardId,
        boardName = boardName
    )

fun InquiryResponse.toModel(): InquiryModel =
    InquiryModel(
        id = inquiryId,
        content = content,
        answer = answer,
        createdAt = createdAt
    )

fun PortfolioResponse.toModel(): PortfolioModel =
    PortfolioModel(
        id = id,
        memberId = memberId,
        memberName = memberName,
        title = title,
        description = description,
        introduction = introduction,
        bojHandle = bojHandle,
        solvedAcInfo = solvedAcInfo?.toModel(),
        solvedacRank = solvedacRank,
        swTestRank = swTestRank,
        isVisible = isVisible,
        createdAt = createdAt,
        updatedAt = updatedAt,
        stacks = stacks.map { it.toModel() },
        urls = urls.map { it.toModel() },
        images = images.map { it.toModel() }
    )

fun com.ssafy.ssabree.core.datasource.remote.model.SolvedAcInfoResponse.toModel(): SolvedAcInfoModel =
    SolvedAcInfoModel(
        tier = tier,
        tierName = tierName,
        tierImageUrl = tierImageUrl,
        rating = rating,
        solvedCount = solvedCount,
        rank = rank
    )

fun PortfolioStackResponse.toModel(): PortfolioStackModel =
    PortfolioStackModel(
        id = id,
        stackId = stackId,
        stackName = stackName,
        stackImgUrl = stackImgUrl,
        expertLevel = expertLevel
    )

fun PortfolioUrlResponse.toModel(): PortfolioUrlModel =
    PortfolioUrlModel(
        id = id,
        type = type,
        url = url
    )

fun PortfolioImageResponse.toModel(): PortfolioImageModel =
    PortfolioImageModel(
        id = id,
        imageUrl = imageUrl,
        orders = orders
    )

fun ProjectListResponse.toModels(): List<ProjectModel> =
    projects.map { it.toModel() }

fun ProjectItemResponse.toModel(): ProjectModel =
    ProjectModel(
        id = id,
        title = title,
        introduction = introduction,
        description = description,
        techStacks = techStacks ?: emptyList(),
        urls = urls ?: emptyList(),
        imageUrls = imageUrls ?: emptyList(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun StackResponse.toModel(): StackModel =
    StackModel(
        id = id,
        name = name,
        imgUrl = imgUrl
    )

fun CampusResponse.toModel(): CampusModel =
    CampusModel(
        id = id,
        name = name
    )

fun GroupMemberResponse.toModel(): GroupMemberModel =
    GroupMemberModel(
        id = id,
        email = email,
        name = name,
        studentNo = studentNo,
        mattermostId = mattermostId,
        profileImageUrl = profileImageUrl,
        portfolioId = null,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun GroupMemberItemResponse.toMemberModel(): GroupMemberModel =
    member.toModel()

fun StudyMemberResponse.toMemberModel(): GroupMemberModel =
    GroupMemberModel(
        id = memberId,
        email = memberEmail,
        name = memberName,
        mattermostId = null,
        profileImageUrl = memberProfileImageUrl,
        portfolioId = portfolioId,
        createdAt = createdAt,
        updatedAt = null
    )

fun com.ssafy.ssabree.core.datasource.remote.model.TeamMemberResponse.toMemberModel(): GroupMemberModel =
    GroupMemberModel(
        id = memberId,
        email = memberEmail,
        name = memberName,
        mattermostId = null,
        profileImageUrl = memberProfileImageUrl,
        portfolioId = portfolioId,
        createdAt = createdAt,
        updatedAt = null
    )

fun GroupSummaryResponse.toModel(): GroupSummaryModel =
    GroupSummaryModel(
        id = id,
        title = title,
        type = type,
        capacity = capacity,
        startDate = startDate,
        endDate = endDate,
        description = description,
        status = status,
        campus = campus?.toModel(),
        leader = leader?.toModel(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        currentMembers = currentMembers?.takeIf { it > 0 }
            ?: members?.size
            ?: leader?.let { 1 }
    )

fun GroupDetailResponse.toModel(): GroupDetailModel {
    // Use 'leader' field first, fall back to 'member' for backward compatibility
    val leaderInfo = leader ?: member
    return GroupDetailModel(
        id = id,
        title = title,
        type = type,
        capacity = capacity,
        startDate = startDate,
        endDate = endDate,
        description = description,
        status = status,
        campus = campus?.toModel(),
        leaderId = leaderInfo?.id,
        leaderName = leaderInfo?.name,
        leaderEmail = leaderInfo?.email,
        leaderMattermostId = leaderInfo?.mattermostId,
        leaderProfileImageUrl = leaderInfo?.profileImageUrl,
        members = members?.map { it.toModel() } ?: leaderInfo?.let { listOf(it.toModel()) } ?: emptyList(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        currentMembers = currentMembers
    )
}

fun PortfolioSummaryResponse.toModel(): PortfolioSummaryModel =
    PortfolioSummaryModel(
        id = id,
        memberId = member?.id ?: memberId,
        memberName = member?.name ?: memberName,
        memberEmail = member?.email ?: memberEmail,
        memberProfileImageUrl = member?.profileImageUrl ?: memberProfileImageUrl,
        title = title,
        description = description,
        introduction = introduction,
        bojHandle = bojHandle,
        solvedacRank = solvedacRank,
        swTestRank = swTestRank,
        isVisible = isVisible
    )

fun GroupApplicationResponse.toModel(): GroupApplicationModel =
    GroupApplicationModel(
        id = id,
        title = title,
        message = message,
        position = position,
        status = status,
        portfolio = portfolio?.toModel(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        // For backward compatibility
        memberId = memberId ?: portfolio?.member?.id,
        portfolioId = portfolioId ?: portfolio?.id
    )

fun TeamApplicationResponse.toModel(): GroupApplicationModel =
    GroupApplicationModel(
        id = id,
        title = title,
        message = message,
        position = position,
        status = status,
        portfolio = PortfolioSummaryModel(
            id = portfolio.id,
            memberId = portfolio.memberId,
            memberName = portfolio.memberName,
            memberEmail = portfolio.memberEmail,
            memberProfileImageUrl = portfolio.memberProfileImageUrl,
            title = portfolio.title,
            introduction = portfolio.introduction,
            bojHandle = portfolio.bojHandle,
            solvedacRank = portfolio.solvedacRank,
            swTestRank = portfolio.swTestRank
        ),
        createdAt = createdAt,
        updatedAt = updatedAt,
        memberId = portfolio.memberId,
        portfolioId = portfolio.id
    )

fun GroupApplicationResponse.toMyApplicationModel(): MyApplicationModel {
    val group = study ?: team
    if (group == null) {
        return MyApplicationModel(
            id = id,
            groupId = 0L,
            groupTitle = "삭제된 그룹",
            leaderName = null,
            status = "DELETED",
            position = position,
            createdAt = createdAt,
            isGroupDeleted = true
        )
    }
    return MyApplicationModel(
        id = id,
        groupId = group.id,
        groupTitle = group.title,
        leaderName = group.leader?.name,
        status = status,
        position = position,
        createdAt = createdAt,
        isGroupDeleted = false
    )
}

fun TeamApplicationResponse.toMyApplicationModel(): MyApplicationModel =
    MyApplicationModel(
        id = id,
        groupId = team.id,
        groupTitle = team.title,
        leaderName = team.leaderName,
        status = status,
        position = position,
        createdAt = createdAt,
        isGroupDeleted = false
    )

fun GroupNoticeResponse.toModel(): GroupNoticeModel =
    GroupNoticeModel(
        id = id,
        title = title,
        content = content,
        isPinned = isPinned,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun GroupTaskResponse.toModel(): GroupTaskModel =
    GroupTaskModel(
        id = id,
        title = title,
        content = content,
        startDate = startDate,
        endDate = endDate,
        status = status,
        creatorId = creatorId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
