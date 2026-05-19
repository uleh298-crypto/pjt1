package com.ssafy.ssabree.features.join.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.features.join.component.JoinStep1
import com.ssafy.ssabree.features.join.component.JoinStep2
import com.ssafy.ssabree.features.join.component.JoinStep3
import com.ssafy.ssabree.features.join.component.TopBar
import com.ssafy.ssabree.features.join.viewmodel.JoinViewModel

@Composable
fun JoinScreen(
    onBackClick: () -> Unit = {},
    onJoinCompleted: () -> Unit = {}
) {
    val container = LocalAppContainer.current
    val viewModel: JoinViewModel = simpleViewModel {
        JoinViewModel(container.authRepository, container.campusRepository)
    }
    val uiState = viewModel.uiState
    val context = LocalContext.current

    LaunchedEffect(uiState.isJoinSuccess) {
        if (uiState.isJoinSuccess) {
            Toast.makeText(context, "회원가입이 완료되었습니다. 다시 로그인해 주세요.", Toast.LENGTH_SHORT).show()
            onJoinCompleted()
        }
    }

    LaunchedEffect(uiState.isVerified) {
        if (uiState.isVerified) {
            Toast.makeText(context, "Mattermost 인증 완료", Toast.LENGTH_SHORT).show()
        }
    }

    BackHandler {
        if (!viewModel.goBack()) {
            onBackClick()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopBar(onBackClick = {
                if (!viewModel.goBack()) {
                    onBackClick()
                }
            })

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
            ) {
                when (uiState.step) {
                    1 -> JoinStep1(
                        name = uiState.name,
                        onNameChange = viewModel::onNameChange,
                        studentId = uiState.studentId,
                        onStudentIdChange = viewModel::onStudentIdChange,
                        generations = uiState.generations,
                        selectedGeneration = uiState.selectedGeneration,
                        isGenerationDropdownExpanded = uiState.isGenerationDropdownExpanded,
                        onGenerationDropdownClick = viewModel::onGenerationDropdownClick,
                        onGenerationDropdownDismiss = viewModel::onGenerationDropdownDismiss,
                        onGenerationSelected = viewModel::onGenerationSelected,
                        // 캠퍼스 드롭다운
                        campuses = uiState.campuses,
                        selectedCampus = uiState.selectedCampus,
                        isCampusDropdownExpanded = uiState.isCampusDropdownExpanded,
                        isLoadingCampuses = uiState.isLoadingCampuses,
                        onCampusDropdownClick = viewModel::onCampusDropdownClick,
                        onCampusDropdownDismiss = viewModel::onCampusDropdownDismiss,
                        onCampusSelected = viewModel::onCampusSelected,
                        // 반 드롭다운
                        classes = uiState.classes,
                        selectedClass = uiState.selectedClass,
                        isClassDropdownExpanded = uiState.isClassDropdownExpanded,
                        isLoadingClasses = uiState.isLoadingClasses,
                        onClassDropdownClick = viewModel::onClassDropdownClick,
                        onClassDropdownDismiss = viewModel::onClassDropdownDismiss,
                        onClassSelected = viewModel::onClassSelected,
                        // 스텝 완료
                        isStep1Valid = uiState.isStep1Valid,
                        onStepCompleted = { viewModel.goToStep(2) }
                    )

                    2 -> JoinStep2(
                        // MM 인증
                        mattermostId = uiState.mattermostId,
                        onMattermostIdChange = viewModel::onMattermostIdChange,
                        verificationCode = uiState.verificationCode,
                        onVerificationCodeChange = viewModel::onVerificationCodeChange,
                        selectedGeneration = uiState.selectedGeneration,
                        isCodeSent = uiState.isCodeSent,
                        isSendingCode = uiState.isSendingCode,
                        isVerifying = uiState.isVerifying,
                        errorMessage = uiState.error?.message,
                        onSendCode = viewModel::sendVerificationCode,
                        onVerify = viewModel::verifyCode,
                    )

                    3 -> JoinStep3(
                        email = uiState.email,
                        onEmailChange = viewModel::onEmailChange,
                        isCheckingEmail = uiState.isCheckingEmail,
                        isEmailAvailable = uiState.isEmailAvailable,
                        onCheckEmailDuplicate = viewModel::checkEmailDuplicate,
                        password = uiState.password,
                        onPasswordChange = viewModel::onPasswordChange,
                        passwordConfirm = uiState.passwordConfirm,
                        onPasswordConfirmChange = viewModel::onPasswordConfirmChange,
                        passwordMismatch = uiState.passwordMismatch,
                        isLoading = uiState.isLoading,
                        isStep3Valid = uiState.isStep3Valid,
                        errorMessage = uiState.error?.message,
                        onJoin = viewModel::signUp,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun JoinScreenPreview() {
    JoinScreen()
}
