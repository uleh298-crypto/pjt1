package com.ssafy.ssabree.core.repository.model

data class SolvedacVerifyInfo(
    val handle: String,
    val tier: Int,
    val rating: Int,
    val solvedCount: Int,
    val classValue: Int,
    val rank: Int
)
