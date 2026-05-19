package com.ssafy.ssabree.core.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class Campus(
    val id: Int,
    val name: String
)

data class Ban(
    val id: Int,
    val name: String,
    val campus: Campus,
    val generation: Int?,
    val classNo: Int?,
    val trackType: String?,
    val createdAt: String?,
    val deletedAt: String?,
    val updatedAt: String?
)
