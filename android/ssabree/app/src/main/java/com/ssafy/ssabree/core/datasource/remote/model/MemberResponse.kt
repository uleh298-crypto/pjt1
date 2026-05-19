package com.ssafy.ssabree.core.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class MemberResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("email") val email: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("studentNo") val studentNo: Int?,
    @SerializedName("campus") val campus: String?,
    @SerializedName("generation") val generation: Int?,
    @SerializedName("classNo") val classNo: Int?,
    @SerializedName("mattermostId") val mattermostId: String?,
    @SerializedName("profileImageUrl") val profileImageUrl: String?,
    @SerializedName("deletedAt") val deletedAt: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)
