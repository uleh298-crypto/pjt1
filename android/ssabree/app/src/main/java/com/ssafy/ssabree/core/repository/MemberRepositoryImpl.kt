package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.datasource.remote.MemberService
import com.ssafy.ssabree.core.datasource.remote.model.UpdateProfileRequest
import com.ssafy.ssabree.core.repository.model.MyCommentModel
import com.ssafy.ssabree.core.repository.model.MyPageModel
import com.ssafy.ssabree.core.repository.model.PostModel
import com.ssafy.ssabree.core.repository.model.toModel
import com.ssafy.ssabree.core.utils.RetrofitClient

class MemberRepositoryImpl : MemberRepository {
    private val memberService = RetrofitClient.instance.create(MemberService::class.java)

    override suspend fun getMyMemberId(): Result<Long> {
        return runCatching {
            memberService.getMe().id
        }
    }

    override suspend fun getMember(id: Long): Result<com.ssafy.ssabree.core.datasource.remote.model.MemberResponse> {
        return runCatching {
            memberService.getMember(id)
        }
    }

    override suspend fun getMyPage(): Result<MyPageModel> {
        return runCatching {
            memberService.getMyPage().toModel()
        }
    }

    override suspend fun getMyPosts(): Result<List<PostModel>> {
        return runCatching {
            memberService.getMyPosts().map { it.toModel() }
        }
    }

    override suspend fun getMyComments(): Result<List<MyCommentModel>> {
        return runCatching {
            memberService.getMyComments().map { it.toModel() }
        }
    }

    override suspend fun getMyScraps(): Result<List<PostModel>> {
        return runCatching {
            memberService.getMyScraps().map { it.toModel() }
        }
    }

    override suspend fun updateProfileImage(profileImageUrl: String): Result<Unit> {
        return runCatching {
            memberService.updateMyProfile(UpdateProfileRequest(profileImageUrl))
            Unit
        }
    }
}
