package com.ssafy.ssabree.features.mygroup.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.app.FakeAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.repository.model.GroupNoticeCreateInfo
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.features.group.model.GroupKind

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteAnnouncementScreen(
    groupId: Long,
    groupKind: GroupKind,
    isEditMode: Boolean = false,
    noticeId: Long = 0L,
    initialTitle: String = "",
    initialContent: String = "",
    initialIsPinned: Boolean = false,
    onBackClick: () -> Unit = {},
    onCompleteClick: () -> Unit = {}
) {
    val container = LocalAppContainer.current
    val viewModel: AnnouncementEditorViewModel = simpleViewModel(key = "announcement-create-${groupKind.routeValue}-$groupId") {
        AnnouncementEditorViewModel(container.myGroupRepository, groupKind, groupId)
    }
    val uiState by viewModel.uiState.collectAsState()

    var title by remember { mutableStateOf(initialTitle) }
    var content by remember { mutableStateOf(initialContent) }
    var isPinned by remember { mutableStateOf(initialIsPinned) }

    val isFormValid = title.isNotBlank() && content.isNotBlank()

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "공지사항 수정" else "공지사항 작성",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val info = GroupNoticeCreateInfo(
                                title = title,
                                content = content,
                                isPinned = isPinned,
                                sendPushNotification = false
                            )
                            if (isEditMode) {
                                viewModel.updateNotice(noticeId, info)
                            } else {
                                viewModel.createNotice(info)
                            }
                        },
                        enabled = isFormValid && !uiState.isSubmitting
                    ) {
                        Text(
                            text = "등록",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFormValid && !uiState.isSubmitting)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 제목 섹션
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("제목", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("제목을 입력하세요", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            // 내용 섹션
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("내용", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                TextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("내용을 입력하세요", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            // 설정 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PushPin, contentDescription = null, tint = Color.Gray)
                        Spacer(Modifier.width(12.dp))
                        Text("상단 고정", fontSize = 15.sp)
                    }
                    Switch(checked = isPinned, onCheckedChange = { isPinned = it })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (uiState.isSuccess) {
        SsabreeDialog(
            onDismissRequest = {},
            title = "알림",
            message = if (isEditMode) "공지사항이 수정되었습니다." else "공지사항이 등록되었습니다.",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = {
                viewModel.resetResult()
                onCompleteClick()
            }
        )
    }

    uiState.errorMessage?.let { message ->
        SsabreeDialog(
            onDismissRequest = { viewModel.resetResult() },
            title = "오류",
            message = message,
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = { viewModel.resetResult() }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WriteAnnouncementScreenPreview() {
    CompositionLocalProvider(LocalAppContainer provides FakeAppContainer()) {
        WriteAnnouncementScreen(groupId = 1L, groupKind = GroupKind.STUDY, isEditMode = true, initialTitle = "기존 공지 제목")
    }
}
