package com.ssafy.ssabree.features.findidpass.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.AuthRepository
import kotlinx.coroutines.launch

private const val TAG = "FindIdPassViewModel"
private const val CODE_LENGTH = 6

data class FindIdPassUiState(
    val findIdMattermostId: String = "",
    val findIdGeneration: Int? = null,
    val findIdName: String = "",
    val findIdVerificationCode: String = "",
    val isFindIdGenerationDropdownExpanded: Boolean = false,
    val isFindIdCodeSent: Boolean = false,
    val isFindIdLoading: Boolean = false,
    val findIdError: String? = null,
    val foundEmail: String? = null,

    val findPassMattermostId: String = "",
    val findPassGeneration: Int? = null,
    val findPassName: String = "",
    val findPassVerificationCode: String = "",
    val isFindPassGenerationDropdownExpanded: Boolean = false,
    val isFindPassCodeSent: Boolean = false,
    val isFindPassVerified: Boolean = false,
    val isFindPassLoading: Boolean = false,
    val findPassError: String? = null,

    val newPassword: String = "",
    val newPasswordCheck: String = "",
    val isResetLoading: Boolean = false,
    val isResetSuccess: Boolean = false,
)

class FindIdPassViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private fun normalizeMattermostId(value: String): String {
        return value.trim().removePrefix("@")
    }

    var uiState by mutableStateOf(FindIdPassUiState())
        private set

    fun onFindIdMattermostIdChange(value: String) {
        uiState = uiState.copy(findIdMattermostId = value, findIdError = null)
    }

    fun onFindIdNameChange(value: String) {
        uiState = uiState.copy(findIdName = value, findIdError = null)
    }

    fun onFindIdGenerationDropdownClick() {
        uiState = uiState.copy(isFindIdGenerationDropdownExpanded = true)
    }

    fun onFindIdGenerationDropdownDismiss() {
        uiState = uiState.copy(isFindIdGenerationDropdownExpanded = false)
    }

    fun onFindIdGenerationSelected(value: Int) {
        uiState = uiState.copy(
            findIdGeneration = value,
            isFindIdGenerationDropdownExpanded = false,
            findIdError = null
        )
    }

    fun onFindIdVerificationCodeChange(value: String) {
        uiState = uiState.copy(
            findIdVerificationCode = value.filter { it.isDigit() }.take(CODE_LENGTH),
            findIdError = null
        )
    }

    fun sendFindIdCode() {
        val mattermostId = normalizeMattermostId(uiState.findIdMattermostId)
        val generation = uiState.findIdGeneration
        val name = uiState.findIdName
        if (name.isBlank() || mattermostId.isBlank() || generation == null) {
            uiState = uiState.copy(findIdError = "이름, Mattermost ID와 기수를 입력해주세요.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isFindIdLoading = true, findIdError = null)
            authRepository.requestSsafyVerification(mattermostId, generation, name)
                .onSuccess {
                    Log.d(TAG, "sendFindIdCode: succeed")
                    uiState = uiState.copy(isFindIdLoading = false, isFindIdCodeSent = true)
                }
                .onFailure { e ->
                    Log.d(TAG, "sendFindIdCode: failed (${e.message})")
                    uiState = uiState.copy(isFindIdLoading = false, findIdError = e.message)
                }
        }
    }

    fun verifyFindId() {
        val mattermostId = normalizeMattermostId(uiState.findIdMattermostId)
        val code = uiState.findIdVerificationCode
        if (mattermostId.isBlank() || code.length != CODE_LENGTH) return

        viewModelScope.launch {
            uiState = uiState.copy(isFindIdLoading = true, findIdError = null)
            authRepository.confirmSsafyVerification(mattermostId, code)
                .onSuccess {
                    Log.d(TAG, "verifyFindId - confirmSsafyVerification: succeed")
                    authRepository.findId(mattermostId)
                        .onSuccess { email ->
                            Log.d(TAG, "verifyFindId - findId: succeed")
                            uiState = uiState.copy(
                                isFindIdLoading = false,
                                foundEmail = email
                            )
                        }
                        .onFailure { e ->
                            Log.d(TAG, "verifyFindId - findId: failed (${e.message})")
                            uiState = uiState.copy(
                                isFindIdLoading = false,
                                findIdError = e.message
                            )
                        }
                }
                .onFailure { e ->
                    Log.d(TAG, "verifyFindId - confirmSsafyVerification: failed (${e.message})")
                    uiState = uiState.copy(
                        isFindIdLoading = false,
                        findIdError = e.message
                    )
                }
        }
    }

    fun onFindPassMattermostIdChange(value: String) {
        uiState = uiState.copy(findPassMattermostId = value, findPassError = null)
    }

    fun onFindPassNameChange(value: String) {
        uiState = uiState.copy(findPassName = value, findPassError = null)
    }

    fun onFindPassGenerationDropdownClick() {
        uiState = uiState.copy(isFindPassGenerationDropdownExpanded = true)
    }

    fun onFindPassGenerationDropdownDismiss() {
        uiState = uiState.copy(isFindPassGenerationDropdownExpanded = false)
    }

    fun onFindPassGenerationSelected(value: Int) {
        uiState = uiState.copy(
            findPassGeneration = value,
            isFindPassGenerationDropdownExpanded = false,
            findPassError = null
        )
    }

    fun onFindPassVerificationCodeChange(value: String) {
        uiState = uiState.copy(
            findPassVerificationCode = value.filter { it.isDigit() }.take(CODE_LENGTH),
            findPassError = null
        )
    }

    fun sendFindPassCode() {
        val mattermostId = normalizeMattermostId(uiState.findPassMattermostId)
        val generation = uiState.findPassGeneration
        val name = uiState.findPassName
        if (name.isBlank() || mattermostId.isBlank() || generation == null) {
            uiState = uiState.copy(findPassError = "이름, Mattermost ID와 기수를 입력해주세요.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isFindPassLoading = true, findPassError = null)
            authRepository.requestSsafyVerification(mattermostId, generation, name)
                .onSuccess {
                    Log.d(TAG, "sendFindPassCode: succeed")
                    uiState = uiState.copy(isFindPassLoading = false, isFindPassCodeSent = true)
                }
                .onFailure { e ->
                    Log.d(TAG, "sendFindPassCode: failed (${e.message})")
                    uiState = uiState.copy(isFindPassLoading = false, findPassError = e.message)
                }
        }
    }

    fun verifyFindPass() {
        val mattermostId = normalizeMattermostId(uiState.findPassMattermostId)
        val code = uiState.findPassVerificationCode
        if (mattermostId.isBlank() || code.length != CODE_LENGTH) return

        viewModelScope.launch {
            uiState = uiState.copy(isFindPassLoading = true, findPassError = null)
            authRepository.confirmSsafyVerification(mattermostId, code)
                .onSuccess {
                    Log.d(TAG, "verifyFindPass: succeed")
                    uiState = uiState.copy(
                        isFindPassLoading = false,
                        isFindPassVerified = true
                    )
                }
                .onFailure { e ->
                    Log.d(TAG, "verifyFindPass: failed (${e.message})")
                    uiState = uiState.copy(
                        isFindPassLoading = false,
                        findPassError = e.message
                    )
                }
        }
    }

    fun onNewPasswordChange(value: String) {
        uiState = uiState.copy(newPassword = value, findPassError = null)
    }

    fun onNewPasswordCheckChange(value: String) {
        uiState = uiState.copy(newPasswordCheck = value, findPassError = null)
    }

    fun resetPassword() {
        val mattermostId = normalizeMattermostId(uiState.findPassMattermostId)
        val newPassword = uiState.newPassword
        val newPasswordCheck = uiState.newPasswordCheck

        if (mattermostId.isBlank() || newPassword.isBlank() || newPasswordCheck.isBlank()) return
        if (newPassword != newPasswordCheck) {
            uiState = uiState.copy(findPassError = "비밀번호가 일치하지 않습니다.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isResetLoading = true, findPassError = null)
            authRepository.resetPassword(mattermostId, newPassword)
                .onSuccess {
                    Log.d(TAG, "resetPassword: succeed")
                    uiState = uiState.copy(
                        isResetLoading = false,
                        isResetSuccess = true
                    )
                }
                .onFailure { e ->
                    Log.d(TAG, "resetPassword: failed (${e.message})")
                    uiState = uiState.copy(
                        isResetLoading = false,
                        findPassError = e.message
                    )
                }
        }
    }

    fun consumeFoundEmail() {
        uiState = uiState.copy(foundEmail = null)
    }

    fun consumeResetSuccess() {
        uiState = uiState.copy(isResetSuccess = false)
    }

    fun consumeFindPassVerified() {
        uiState = uiState.copy(isFindPassVerified = false)
    }
}
