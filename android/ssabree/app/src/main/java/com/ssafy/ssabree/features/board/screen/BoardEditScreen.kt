@file:OptIn(ExperimentalMaterial3Api::class)

package com.ssafy.ssabree.features.board.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.ui.simpleViewModel

@Composable
fun BoardEditScreen(
    postId: Long,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {},
) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val viewModel: BoardEditViewModel = simpleViewModel {
        BoardEditViewModel(
            postRepository = container.postRepository,
            appContext = context.applicationContext
        )
    }

    val uiState by viewModel.uiState.collectAsState()

    // 게시글 로드
    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
    }

    // 다이얼로그 상태
    var showCancelDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    // 성공 시 다이얼로그 표시
    LaunchedEffect(uiState.isSubmitSuccess) {
        if (uiState.isSubmitSuccess) {
            showSuccessDialog = true
        }
    }

    // 실패 시 다이얼로그 표시
    LaunchedEffect(uiState.submitError) {
        if (uiState.submitError != null) {
            showErrorDialog = true
        }
    }

    // 뒤로 가기 처리
    BackHandler {
        showCancelDialog = true
    }

    // 수정 중단 확인 다이얼로그
    if (showCancelDialog) {
        SsabreeDialog(
            onDismissRequest = { showCancelDialog = false },
            title = "게시글 수정 중단",
            message = "수정을 중단하시겠습니까? 변경 사항이 저장되지 않습니다.",
            confirmText = "중단",
            dismissText = "계속 수정",
            onConfirm = {
                showCancelDialog = false
                onCancel()
            },
            onDismiss = { showCancelDialog = false }
        )
    }

    // 수정 성공 다이얼로그
    if (showSuccessDialog) {
        SsabreeDialog(
            onDismissRequest = { },
            title = "알림",
            message = "게시글을 수정했습니다.",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = {
                showSuccessDialog = false
                viewModel.clearSubmitSuccess()
                onSubmitSuccess()
            }
        )
    }

    // 수정 실패 다이얼로그
    if (showErrorDialog) {
        SsabreeDialog(
            onDismissRequest = {
                showErrorDialog = false
                viewModel.clearSubmitError()
            },
            title = "오류",
            message = "게시글 수정에 실패했습니다.\n${uiState.submitError ?: ""}",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = {
                showErrorDialog = false
                viewModel.clearSubmitError()
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "글 수정",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showCancelDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.onSubmit() },
                        enabled = uiState.isSubmitEnabled
                    ) {
                        Text(
                            text = "수정",
                            color = if (uiState.isSubmitEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .imePadding()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. 제목 입력
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("제목", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    TextField(
                        value = uiState.title,
                        onValueChange = viewModel::onTitleChange,
                        placeholder = { Text("제목을 입력하세요", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }

                // 2. 내용 입력
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("내용", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    TextField(
                        value = uiState.content,
                        onValueChange = viewModel::onContentChange,
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

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
