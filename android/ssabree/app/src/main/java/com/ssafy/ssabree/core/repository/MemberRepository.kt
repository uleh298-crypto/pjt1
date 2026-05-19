package com.ssafy.ssabree.core.repository

import com.ssafy.ssabree.core.repository.model.MyCommentModel
import com.ssafy.ssabree.core.repository.model.MyPageModel
import com.ssafy.ssabree.core.repository.model.PostModel
import com.ssafy.ssabree.core.datasource.remote.model.MemberResponse

interface MemberRepository {
    suspend fun getMyMemberId(): Result<Long>
    suspend fun getMember(id: Long): Result<MemberResponse>
    suspend fun getMyPage(): Result<MyPageModel>
    suspend fun getMyPosts(): Result<List<PostModel>>
    suspend fun getMyComments(): Result<List<MyCommentModel>>
    suspend fun getMyScraps(): Result<List<PostModel>>
    suspend fun updateProfileImage(profileImageUrl: String): Result<Unit>
}
