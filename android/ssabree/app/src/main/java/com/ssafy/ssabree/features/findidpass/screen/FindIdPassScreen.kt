package com.ssafy.ssabree.features.findidpass.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.features.findidpass.component.FindIdPassTopBar
import com.ssafy.ssabree.features.findidpass.component.FindIdStep1
import com.ssafy.ssabree.features.findidpass.component.FindIdStep2
import com.ssafy.ssabree.features.findidpass.component.FindPassStep1
import com.ssafy.ssabree.features.findidpass.component.FindPassStep2
import com.ssafy.ssabree.features.findidpass.component.FindPassStep3
import com.ssafy.ssabree.features.findidpass.viewmodel.FindIdPassViewModel

@Composable
fun FindIdPassScreen(
    onBackClick: () -> Unit,
    onConfirm: () -> Unit,
    onResetConfirm: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: FindIdPassViewModel = simpleViewModel {
        FindIdPassViewModel(container.authRepository)
    }
    val uiState = viewModel.uiState

    var selectedTabIndex by remember { mutableStateOf(0) }
    var findIdStep by remember { mutableStateOf(1) }
    var foundId by remember { mutableStateOf("") }
    var findPassStep by remember { mutableStateOf(1) }
    val generations = listOf(14, 15)

    LaunchedEffect(uiState.foundEmail) {
        uiState.foundEmail?.let { email ->
            foundId = email
            findIdStep = 2
            viewModel.consumeFoundEmail()
        }
    }

    LaunchedEffect(uiState.isFindPassVerified) {
        if (uiState.isFindPassVerified) {
            findPassStep = 2
            viewModel.consumeFindPassVerified()
        }
    }

    LaunchedEffect(uiState.isResetSuccess) {
        if (uiState.isResetSuccess) {
            viewModel.consumeResetSuccess()
            onResetConfirm()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            FindIdPassTopBar(onBackClick = onBackClick)

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    divider = {
                        Divider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            thickness = 1.dp
                        )
                    }
                ) {
                    val tabs = listOf("아이디", "비밀번호")
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                when (selectedTabIndex) {
                    0 -> when (findIdStep) {
                        1 -> FindIdStep1(
                            mattermostId = uiState.findIdMattermostId,
                            onMattermostIdChange = viewModel::onFindIdMattermostIdChange,
                            name = uiState.findIdName,
                            onNameChange = viewModel::onFindIdNameChange,
                            generations = generations,
                            selectedGeneration = uiState.findIdGeneration,
                            isGenerationDropdownExpanded = uiState.isFindIdGenerationDropdownExpanded,
                            onGenerationDropdownClick = viewModel::onFindIdGenerationDropdownClick,
                            onGenerationDropdownDismiss = viewModel::onFindIdGenerationDropdownDismiss,
                            onGenerationSelected = viewModel::onFindIdGenerationSelected,
                            verificationCode = uiState.findIdVerificationCode,
                            onVerificationCodeChange = viewModel::onFindIdVerificationCodeChange,
                            isCodeSent = uiState.isFindIdCodeSent,
                            isLoading = uiState.isFindIdLoading,
                            errorMessage = uiState.findIdError,
                            onSendCode = viewModel::sendFindIdCode,
                            onVerify = viewModel::verifyFindId
                        )

                        else -> FindIdStep2(
                            foundId = foundId,
                            onConfirm = onConfirm
                        )
                    }

                    1 -> when (findPassStep) {
                        1 -> FindPassStep1(
                            mattermostId = uiState.findPassMattermostId,
                            onMattermostIdChange = viewModel::onFindPassMattermostIdChange,
                            name = uiState.findPassName,
                            onNameChange = viewModel::onFindPassNameChange,
                            generations = generations,
                            selectedGeneration = uiState.findPassGeneration,
                            isGenerationDropdownExpanded = uiState.isFindPassGenerationDropdownExpanded,
                            onGenerationDropdownClick = viewModel::onFindPassGenerationDropdownClick,
                            onGenerationDropdownDismiss = viewModel::onFindPassGenerationDropdownDismiss,
                            onGenerationSelected = viewModel::onFindPassGenerationSelected,
                            verificationCode = uiState.findPassVerificationCode,
                            onVerificationCodeChange = viewModel::onFindPassVerificationCodeChange,
                            isCodeSent = uiState.isFindPassCodeSent,
                            isLoading = uiState.isFindPassLoading,
                            errorMessage = uiState.findPassError,
                            onSendCode = viewModel::sendFindPassCode,
                            onVerify = viewModel::verifyFindPass
                        )

                        2 -> FindPassStep2(
                            onConfirm = { findPassStep = 3 }
                        )

                        else -> FindPassStep3(
                            newPassword = uiState.newPassword,
                            newPasswordCheck = uiState.newPasswordCheck,
                            onNewPasswordChange = viewModel::onNewPasswordChange,
                            onNewPasswordCheckChange = viewModel::onNewPasswordCheckChange,
                            isLoading = uiState.isResetLoading,
                            errorMessage = uiState.findPassError,
                            onResetConfirm = viewModel::resetPassword
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FindIdPassScreenPreview() {
    FindIdPassScreen(onBackClick = {}, onConfirm = {}, onResetConfirm = {})
}
