package com.ssafy.ssabree.features.mygroup.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.GroupRepository
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.mygroup.model.MyApplicationUiModel
import com.ssafy.ssabree.features.mygroup.model.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class MyApplicationsUiState(
    val isLoading: Boolean = false,
    val applications: List<MyApplicationUiModel> = emptyList(),
    val errorMessage: String? = null
)

class MyApplicationsViewModel(
    private val groupRepository: GroupRepository,
    private val groupKind: GroupKind
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyApplicationsUiState())
    val uiState: StateFlow<MyApplicationsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = if (groupKind == GroupKind.STUDY) {
            groupRepository.getMyStudyApplications()
        } else {
            groupRepository.getMyTeamApplications()
        }
        result.onSuccess { applications ->
            val sorted = applications
                .map { model -> model.toUiModel() }
                .sortedWith(
                    compareByDescending<MyApplicationUiModel> { it.isPending }
                        .thenByDescending { parseCreatedAtMillis(it.createdAt) }
                )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    applications = sorted
                )
            }
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "지원 내역을 불러오지 못했습니다."
                )
            }
        }
    }

    fun cancelApplication(applicationId: Long) = viewModelScope.launch {
        val result = if (groupKind == GroupKind.STUDY) {
            groupRepository.cancelStudyApplication(applicationId)
        } else {
            groupRepository.cancelTeamApplication(applicationId)
        }
        result.onSuccess {
            load()
        }.onFailure { error ->
            _uiState.update {
                it.copy(errorMessage = error.message ?: "지원 취소에 실패했습니다.")
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun parseCreatedAtMillis(createdAt: String?): Long {
        if (createdAt.isNullOrBlank()) return Long.MIN_VALUE
        val zone = ZoneId.of("Asia/Seoul")
        return try {
            ZonedDateTime.parse(createdAt).withZoneSameInstant(zone).toInstant().toEpochMilli()
        } catch (_: Exception) {
            try {
                OffsetDateTime.parse(createdAt).toZonedDateTime().withZoneSameInstant(zone).toInstant().toEpochMilli()
            } catch (_: Exception) {
                try {
                    LocalDateTime.parse(createdAt)
                        .atZone(ZoneId.of("UTC"))
                        .withZoneSameInstant(zone)
                        .toInstant()
                        .toEpochMilli()
                } catch (_: Exception) {
                    Long.MIN_VALUE
                }
            }
        }
    }
}
