package com.ssafy.ssabree.core.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class HomeResponse(
    @SerializedName("dDays") val dDays: List<HomeDdayResponse>,
    @SerializedName("teamThumbnail") val teamThumbnail: RecruitThumbResponse?,
    @SerializedName("studyThumbnail") val studyThumbnail: RecruitThumbResponse?,
    @SerializedName("campusMeals") val campusMeals: List<CampusMealResponse>,
    @SerializedName("boardsList") val boardsList: List<BoardThumbResponse>
)

data class HomeDdayResponse(
    @SerializedName("title") val title: String,
    @SerializedName("days") val days: Int
)

data class RecruitThumbResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String?,
    @SerializedName("count") val count: Int
)

data class BoardThumbResponse(
    @SerializedName("boardId") val boardId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("recentPostTitle") val recentPostTitle: String?
)

data class CampusMealResponse(
    @SerializedName("campusId") val campusId: Long,
    @SerializedName("campusName") val campusName: String,
    @SerializedName("imageUrls") val imageUrls: List<String>
)
