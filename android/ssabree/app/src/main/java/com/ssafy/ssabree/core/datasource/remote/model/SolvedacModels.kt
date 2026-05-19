package com.ssafy.ssabree.core.datasource.remote.model

data class SolvedacVerifyResponse(
    val handle: String,
    val tier: Int,
    val rating: Int,
    val solvedCount: Int,
    val classValue: Int,
    val rank: Int
)
