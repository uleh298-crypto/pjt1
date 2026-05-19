package com.ssafy.ssabree.features.home.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.HomeRepository
import com.ssafy.ssabree.core.repository.MemberRepository
import com.ssafy.ssabree.core.repository.model.BoardThumbModel
import com.ssafy.ssabree.core.repository.model.CampusMealModel
import com.ssafy.ssabree.core.repository.model.DDayModel
import com.ssafy.ssabree.core.repository.model.RecruitThumbModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

data class HomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedLocation: String = "",
    val locationOptions: List<String> = emptyList(),
    val dDays: List<DDayModel> = emptyList(),
    val team: RecruitThumbModel? = null,
    val study: RecruitThumbModel? = null,
    val campusMeals: List<CampusMealModel> = emptyList(),
    val boards: List<BoardThumbModel> = emptyList(),
    val enlargedMealImageIndex: Int? = null
) {
    val selectedMealImageUrls: List<String>
        get() = campusMeals.find { it.campusName == selectedLocation }?.imageUrls ?: emptyList()
}

class HomeViewModel(
    private val homeRepository: HomeRepository,
    private val memberRepository: MemberRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
    }

    fun loadHome() = viewModelScope.launch {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                dDays = emptyList(),
                team = null,
                study = null,
                boards = emptyList()
            )
        }

        val userCampus = memberRepository.getMyPage().getOrNull()?.user?.campus

        homeRepository.fetchHome()
            .onSuccess { home ->
                Log.d(TAG, "loadHome: succeed")
                val campusNames = home.campusMeals.map { it.campusName }
                val selectedCampus = when {
                    userCampus != null && campusNames.contains(userCampus) -> userCampus
                    uiState.value.selectedLocation.isNotBlank() -> uiState.value.selectedLocation
                    else -> campusNames.firstOrNull() ?: ""
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        locationOptions = campusNames,
                        selectedLocation = selectedCampus,
                        dDays = home.dDays,
                        team = home.team,
                        study = home.study,
                        campusMeals = home.campusMeals,
                        boards = home.boards
                    )
                }
            }
            .onFailure { e ->
                Log.d(TAG, "loadHome: failed (${e.message})")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Unknown error"
                    )
                }
            }
    }

    fun onLocationSelected(location: String) {
        _uiState.update { it.copy(selectedLocation = location) }
    }

    fun onMealImageClick(index: Int) {
        _uiState.update { state ->
            val next = if (state.enlargedMealImageIndex == index) null else index
            state.copy(enlargedMealImageIndex = next)
        }
    }

    fun onMealImageDismiss() {
        _uiState.update { it.copy(enlargedMealImageIndex = null) }
    }
}
