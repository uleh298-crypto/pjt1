package com.ssafy.ssabree.features.login.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.AuthRepository
import kotlinx.coroutines.launch

private const val TAG = "LoginViewModel"

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(newEmail: String) {
        uiState = uiState.copy(
            email = newEmail,
            errorMessage = null
        )
    }

    fun onPasswordChange(newPw: String) {
        uiState = uiState.copy(
            password = newPw,
            errorMessage = null
        )
    }

    fun login(onLoginSuccess: () -> Unit) {
        val email = uiState.email.trim()
        val pw = uiState.password

        if (email.isBlank() || pw.isBlank()) {
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = authRepository.login(email, pw)

            result
                .onSuccess {
                    Log.d(TAG, "login: succeed")
                    // AuthRepositoryImpl에서 이미 토큰 저장, FCM 동기화까지 수행함
                    uiState = uiState.copy(
                        isLoading = false,
                        isLoginSuccess = true
                    )
                    onLoginSuccess()
                }
                .onFailure { e ->
                    Log.d(TAG, "login: failed (${e.message})")
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
        }
    }
}
