package com.ssafy.ssabree.features.mypage.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.ssabree.core.designsystem.component.SsabreeBottomSheet
import com.ssafy.ssabree.core.designsystem.component.SsabreeBottomSheetItem
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.designsystem.theme.LocalOnThemeModeChange
import com.ssafy.ssabree.core.designsystem.theme.LocalThemeMode
import com.ssafy.ssabree.core.designsystem.theme.ThemeMode
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.utils.AuthLogoutReason
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onBackClick: () -> Unit,
    onNotificationDetailClick: () -> Unit,
    onInquiryClick: () -> Unit,
    onCommunityRulesClick: () -> Unit,
    onTermsClick: () -> Unit,
    onLogout: (AuthLogoutReason) -> Unit
) {
    val container = LocalAppContainer.current
    val authRepository = remember { container.authRepository }
    val currentThemeMode = LocalThemeMode.current
    val onThemeModeChange = LocalOnThemeModeChange.current
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showWithdrawErrorDialog by remember { mutableStateOf(false) }
    var withdrawErrorMessage by remember { mutableStateOf("") }
    var isWithdrawing by remember { mutableStateOf(false) }

    if (showSheet) {
        SsabreeBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            title = "다크 모드"
        ) {
            ThemeMode.entries.forEach { mode ->
                SsabreeBottomSheetItem(
                    text = mode.displayName,
                    selected = (mode == currentThemeMode),
                    onClick = {
                        onThemeModeChange(mode)
                        showSheet = false
                    }
                )
            }
        }
    }

    if (showWithdrawDialog) {
        SsabreeDialog(
            onDismissRequest = { if (!isWithdrawing) showWithdrawDialog = false },
            title = "회원 탈퇴",
            message = "정말 탈퇴하시겠습니까?",
            confirmText = "탈퇴",
            dismissText = "취소",
            onConfirm = {
                scope.launch {
                    isWithdrawing = true
                    authRepository.withdraw()
                        .onSuccess { onLogout(AuthLogoutReason.USER_WITHDRAW) }
                        .onFailure { throwable ->
                            withdrawErrorMessage =
                                throwable.message ?: "회원 탈퇴에 실패했습니다. 잠시 후 다시 시도해주세요."
                            showWithdrawDialog = false
                            showWithdrawErrorDialog = true
                        }
                    isWithdrawing = false
                }
            },
            onDismiss = { showWithdrawDialog = false }
        )
    }

    if (showWithdrawErrorDialog) {
        SsabreeDialog(
            onDismissRequest = { showWithdrawErrorDialog = false },
            title = "회원 탈퇴",
            message = withdrawErrorMessage,
            confirmText = "확인",
            onConfirm = { showWithdrawErrorDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("설정", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            SettingContainer(title = "앱 설정") {
                SettingItem("다크 모드", currentThemeMode.displayName) { showSheet = true }
                SettingItem("알림 설정") { onNotificationDetailClick() }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingContainer(title = "커뮤니티") {
                SettingItem("이용 규칙") { onCommunityRulesClick() }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingContainer(title = "계정") {
                SettingItem("회원 탈퇴") { showWithdrawDialog = true }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingContainer(title = "이용안내") {
                SettingItem("앱 버전", "1.0.3 V")
                SettingItem("문의사항") { onInquiryClick() }
                SettingItem("서비스 이용 약관"){ onTermsClick() }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SettingContainer(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingItem(title: String, value: String? = null, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        if (value != null) {
            Text(text = value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
