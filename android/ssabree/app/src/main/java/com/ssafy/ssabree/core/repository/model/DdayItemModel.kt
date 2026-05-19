package com.ssafy.ssabree.core.repository.model

data class DdayItemModel(
    val id: Int,
    val title: String,
    val targetDate: String,
    val dDay: Int,
    val iconKey: String?
)
