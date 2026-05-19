package com.ssafy.ssabree.core.repository.test

import com.ssafy.ssabree.core.repository.MemberRepository
import com.ssafy.ssabree.core.repository.model.MyCommentModel
import com.ssafy.ssabree.core.repository.model.MyPageCountsModel
import com.ssafy.ssabree.core.repository.model.MyPageModel
import com.ssafy.ssabree.core.repository.model.MyPagePortfolioSummaryModel
import com.ssafy.ssabree.core.repository.model.MyPageUserModel
import com.ssafy.ssabree.core.repository.model.PostModel

class FakeMemberRepository : MemberRepository {
    private val myPage = MyPageModel(
        user = MyPageUserModel(
            userId = 1L,
            name = "Kim",
            mattermostId = "kim_ssafy",
            campus = "Gumi",
            generation = 14,
            profileImageUrl = null
        ),
        counts = MyPageCountsModel(
            postCount = 3,
            commentCount = 2,
            scrapCount = 1
        ),
        portfolioSummary = MyPagePortfolioSummaryModel(
            techStack = mapOf("Kotlin" to "High", "Spring" to "Mid", "Compose" to "Mid"),
            ssafySwRating = "A+",
            solvedAcRank = "gold",
            solvedAcHandle = "koosaga",
            solvedAcTierName = "Gold 1",
            solvedAcTierImageUrl = "https://static.solved.ac/tier_small/15.svg",
            solvedAcSolvedCount = 500,
            links = listOf("https://github.com/ssafy-user"),
            projects = listOf("Autonomous Simulator", "Recommendation System")
        )
    )

    override suspend fun getMyMemberId(): Result<Long> {
        return Result.success(1L)
    }

    override suspend fun getMember(id: Long): Result<com.ssafy.ssabree.core.datasource.remote.model.MemberResponse> {
        return Result.success(
            com.ssafy.ssabree.core.datasource.remote.model.MemberResponse(
                id = id,
                email = "member$id@example.com",
                name = "Member$id",
                studentNo = null,
                campus = null,
                generation = null,
                classNo = null,
                mattermostId = "member_$id",
                profileImageUrl = null,
                deletedAt = null,
                createdAt = null,
                updatedAt = null
            )
        )
    }

    override suspend fun getMyPage(): Result<MyPageModel> {
        return Result.success(myPage)
    }

    override suspend fun getMyPosts(): Result<List<PostModel>> {
        return Result.success(emptyList())
    }

    override suspend fun getMyComments(): Result<List<MyCommentModel>> {
        return Result.success(emptyList())
    }

    override suspend fun getMyScraps(): Result<List<PostModel>> {
        return Result.success(emptyList())
    }

    override suspend fun updateProfileImage(profileImageUrl: String): Result<Unit> {
        return Result.success(Unit)
    }
}
