package com.ssafy.ssabree.features.join.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.datasource.remote.model.Ban
import com.ssafy.ssabree.core.datasource.remote.model.Campus
import com.ssafy.ssabree.core.repository.AuthRepository
import com.ssafy.ssabree.core.repository.CampusRepository
import com.ssafy.ssabree.core.repository.model.SignUpInfo
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private const val TAG = "JoinViewModel"

sealed class JoinError(val message: String) {
    class Network(msg: String = "네트워크 연결을 확인해주세요.") : JoinError(msg)
    class Server(msg: String = "서버 오류가 발생했습니다.") : JoinError(msg)
    class Validation(msg: String) : JoinError(msg)
    class Unknown(msg: String = "알 수 없는 오류가 발생했습니다.") : JoinError(msg)
}

private fun Throwable.toJoinError(): JoinError = when {
    this is UnknownHostException -> JoinError.Network()
    this is SocketTimeoutException -> JoinError.Network()
    message?.contains("500") == true -> JoinError.Server()
    else -> JoinError.Unknown(message ?: "알 수 없는 오류")
}

private fun normalizeJoinValidationMessage(message: String): String {
    val lower = message.lowercase()
    return if (
        lower.contains("password") ||
        lower.contains("비밀번호") ||
        lower.contains("pw")
    ) {
        "비밀번호 형식이 올바르지 않습니다. 8자 이상, 영문/숫자/특수문자(@\$!%*#?&)를 포함해주세요."
    } else {
        message
    }
}

data class JoinUiState(
    // Step 1: 개인 정보
    val name: String = "",
    val studentId: String = "",

    // 기수 선택 (Step 1)
    val generations: List<Int> = listOf(14, 15),
    val selectedGeneration: Int? = null,
    val isGenerationDropdownExpanded: Boolean = false,

    // 캠퍼스 선택
    val campuses: List<Campus> = emptyList(),
    val selectedCampus: Campus? = null,
    val isCampusDropdownExpanded: Boolean = false,
    val isLoadingCampuses: Boolean = false,

    // 반 선택
    val campusClasses: List<Ban> = emptyList(),
    val classes: List<Ban> = emptyList(),
    val selectedClass: Ban? = null,
    val isClassDropdownExpanded: Boolean = false,
    val isLoadingClasses: Boolean = false,

    // Step 2: Mattermost 인증
    val mattermostId: String = "",
    val verificationCode: String = "",
    val isCodeSent: Boolean = false,
    val isVerified: Boolean = false,
    val isSendingCode: Boolean = false,
    val isVerifying: Boolean = false,

    // Step 3: 계정 정보
    val email: String = "",
    val isCheckingEmail: Boolean = false,
    val isEmailAvailable: Boolean? = null,  // null: 미확인 true: 사용 가능 false: 중복
    val password: String = "",
    val passwordConfirm: String = "",

    // 공통
    val step: Int = 1,
    val isLoading: Boolean = false,
    val error: JoinError? = null,
    val isJoinSuccess: Boolean = false,
) {
    val isStep1Valid: Boolean
        get() = name.isNotBlank() &&
            studentId.isNotBlank() &&
            selectedGeneration != null &&
            selectedCampus != null &&
            selectedClass != null

    val isStep2Valid: Boolean
        get() = verificationCode.length == 6

    val isStep3Valid: Boolean
        get() = email.isNotBlank() &&
                password.isNotBlank() &&
                passwordConfirm.isNotBlank() &&
                password == passwordConfirm &&
                !isLoading

    val passwordMismatch: Boolean
        get() = passwordConfirm.isNotBlank() && password != passwordConfirm
}

class JoinViewModel(
    private val authRepository: AuthRepository,
    private val campusRepository: CampusRepository
) : ViewModel() {

    var uiState by mutableStateOf(JoinUiState())
        private set

    init {
        loadCampuses()
    }

    private fun loadCampuses() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoadingCampuses = true)

            campusRepository.getCampuses()
                .onSuccess { campusList ->
                    Log.d(TAG, "loadCampuses: succeed")
                    uiState = uiState.copy(
                        isLoadingCampuses = false,
                        campuses = campusList
                    )
                }
                .onFailure { e ->
                    Log.d(TAG, "loadCampuses: failed (${e.message})")
                    uiState = uiState.copy(
                        isLoadingCampuses = false,
                        error = e.toJoinError()
                    )
                }
        }
    }

    private fun loadClasses(campusId: Int) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoadingClasses = true, classes = emptyList(), selectedClass = null)

            campusRepository.getClasses(campusId)
                .onSuccess { classList ->
                    Log.d(TAG, "loadClasses: succeed")
                    val filtered = filterClasses(classList, uiState.selectedGeneration)
                    uiState = uiState.copy(
                        isLoadingClasses = false,
                        campusClasses = classList,
                        classes = filtered,
                        selectedClass = null
                    )
                }
                .onFailure { e ->
                    Log.d(TAG, "loadClasses: failed (${e.message})")
                    uiState = uiState.copy(
                        isLoadingClasses = false,
                        error = e.toJoinError()
                    )
                }
        }
    }

    // --- Step Navigation ---

    fun goToStep(step: Int) {
        uiState = uiState.copy(step = step, error = null)
    }

    /**
     * 뒤로가기 처리
     * @return true: 이전 스텝으로 이동, false: 화면 종료 필요
     */
    fun goBack(): Boolean = when (uiState.step) {
        1 -> false  // Step 1: 화면 종료
        2 -> {
            uiState = uiState.copy(
                step = 1,
                error = null,
                mattermostId = "",
                verificationCode = "",
                isCodeSent = false,
                isVerified = false,
                isSendingCode = false,
                isVerifying = false,
                isGenerationDropdownExpanded = false
            )
            true
        }
        3 -> {
            uiState = uiState.copy(
                step = 2,
                error = null,
                mattermostId = "",
                verificationCode = "",
                isCodeSent = false,
                isVerified = false,
                isSendingCode = false,
                isVerifying = false,
                isGenerationDropdownExpanded = false,
                email = "",
                isCheckingEmail = false,
                isEmailAvailable = null,
                password = "",
                passwordConfirm = "",
                isLoading = false
            )
            true
        }
        else -> false
    }

    // --- Step 1 ---

    fun onNameChange(value: String) {
        uiState = uiState.copy(name = value)
    }

    fun onStudentIdChange(value: String) {
        uiState = uiState.copy(studentId = value.filter { it.isDigit() })
    }

    // --- 캠퍼스 선택 ---

    fun onCampusDropdownClick() {
        uiState = uiState.copy(isCampusDropdownExpanded = true)
    }

    fun onCampusDropdownDismiss() {
        uiState = uiState.copy(isCampusDropdownExpanded = false)
    }

    fun onCampusSelected(campus: Campus) {
        uiState = uiState.copy(
            selectedCampus = campus,
            isCampusDropdownExpanded = false,
            selectedClass = null,
            classes = emptyList(),
            campusClasses = emptyList()
        )
        loadClasses(campus.id)
    }

    // --- 반 선택 ---

    fun onClassDropdownClick() {
        if (uiState.selectedCampus != null && uiState.selectedGeneration != null) {
            uiState = uiState.copy(isClassDropdownExpanded = true)
        }
    }

    fun onClassDropdownDismiss() {
        uiState = uiState.copy(isClassDropdownExpanded = false)
    }

    fun onClassSelected(ban: Ban) {
        uiState = uiState.copy(
            selectedClass = ban,
            isClassDropdownExpanded = false
        )
    }

    private fun filterClasses(source: List<Ban>, generation: Int?): List<Ban> {
        if (generation == null) return emptyList()
        return source.filter { it.generation == generation }
    }

    // --- 기수 선택 ---

    fun onGenerationDropdownClick() {
        uiState = uiState.copy(isGenerationDropdownExpanded = true)
    }

    fun onGenerationDropdownDismiss() {
        uiState = uiState.copy(isGenerationDropdownExpanded = false)
    }

    fun onGenerationSelected(generation: Int) {
        val filteredClasses = filterClasses(uiState.campusClasses, generation)
        uiState = uiState.copy(
            selectedGeneration = generation,
            isGenerationDropdownExpanded = false,
            classes = filteredClasses,
            selectedClass = null
        )
    }

    // --- Step 2 ---

    fun onMattermostIdChange(value: String) {
        uiState = uiState.copy(mattermostId = value, error = null)
    }

    fun onVerificationCodeChange(value: String) {
        uiState = uiState.copy(
            verificationCode = value.filter { it.isDigit() }.take(6),
            error = null
        )
    }

    fun sendVerificationCode() {
        val mmId = uiState.mattermostId.trim()
        val generation = uiState.selectedGeneration
        val name = uiState.name
        if (mmId.isBlank() || generation == null) return

        viewModelScope.launch {
            uiState = uiState.copy(isSendingCode = true, error = null)

            authRepository.requestSsafyVerification(mmId, generation = generation, name = name)
                .onSuccess {
                    Log.d(TAG, "sendVerificationCode: succeed")
                    uiState = uiState.copy(
                        isSendingCode = false,
                        isCodeSent = true
                    )
                }
                .onFailure { e ->
                    Log.d(TAG, "sendVerificationCode: failed (${e.message})")
                    uiState = uiState.copy(
                        isSendingCode = false,
                        error = e.toJoinError()
                    )
                }
        }
    }

    fun verifyCode() {
        val mmId = uiState.mattermostId.trim()
        val code = uiState.verificationCode
        if (code.length != 6) return

        viewModelScope.launch {
            uiState = uiState.copy(isVerifying = true, error = null)

            authRepository.confirmSsafyVerification(mmId, code)
                .onSuccess {
                    Log.d(TAG, "verifyCode: succeed")
                    uiState = uiState.copy(
                        isVerifying = false,
                        isVerified = true,
                        step = 3
                    )
                }
                .onFailure { e ->
                    Log.d(TAG, "verifyCode: failed (${e.message})")
                    uiState = uiState.copy(
                        isVerifying = false,
                        error = e.toJoinError()
                    )
                }
        }
    }

    // --- Step 3 ---

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, error = null, isEmailAvailable = null)
    }

    fun checkEmailDuplicate() {
        val email = uiState.email.trim()
        if (email.isBlank()) return

        viewModelScope.launch {
            uiState = uiState.copy(isCheckingEmail = true, error = null)

            authRepository.isEmailUsed(email)
                .onSuccess { isUsed ->
                    Log.d(TAG, "checkEmailDuplicate: succeed (isUsed=$isUsed)")
                    uiState = uiState.copy(
                        isCheckingEmail = false,
                        isEmailAvailable = !isUsed,
                        error = null
                    )
                }
                .onFailure { e ->
                    Log.d(TAG, "checkEmailDuplicate: failed (${e.message})")
                    uiState = uiState.copy(
                        isCheckingEmail = false,
                        isEmailAvailable = null,
                        error = e.toJoinError()
                    )
                }
        }
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, error = null)
    }

    fun onPasswordConfirmChange(value: String) {
        uiState = uiState.copy(passwordConfirm = value, error = null)
    }

    fun signUp() {
        if (!uiState.isStep3Valid) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            val info = SignUpInfo(
                email = uiState.email.trim(),
                password = uiState.password,
                name = uiState.name.trim(),
                studentNo = uiState.studentId.toIntOrNull(),
                campus = uiState.selectedCampus?.id,
                generation = uiState.selectedGeneration,
                classNo = uiState.selectedClass?.classNo,
                mattermostId = uiState.mattermostId.trim()
            )

            authRepository.signUp(info)
                .onSuccess {
                    Log.d(TAG, "signUp: succeed")
                    uiState = uiState.copy(
                        isLoading = false,
                        isJoinSuccess = true
                    )
                }
                .onFailure { e ->
                    Log.d(TAG, "signUp: failed (${e.message})")
                    val msg = e.message
                    uiState = uiState.copy(
                        isLoading = false,
                        error = if (msg.isNullOrBlank()) null else JoinError.Validation(normalizeJoinValidationMessage(msg))
                    )
                }
        }
    }
}
