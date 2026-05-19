package com.ssafy.ssabree.features.group.model

enum class GroupKind(val routeValue: String, val displayName: String) {
    STUDY("study", "스터디"),
    PROJECT("project", "프로젝트");

    companion object {
        fun fromRoute(value: String?): GroupKind {
            return values().firstOrNull { it.routeValue == value } ?: STUDY
        }
    }
}
