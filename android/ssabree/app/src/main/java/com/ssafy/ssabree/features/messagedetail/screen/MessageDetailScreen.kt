package com.ssafy.ssabree.features.messagedetail.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.core.utils.WebSocketConnectionState
import com.ssafy.ssabree.features.messagedetail.viewmodel.ChatMessageUiModel
import com.ssafy.ssabree.features.messagedetail.viewmodel.MessageDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDetailScreen(
    roomId: Long = 0,
    postId: Long = 0,
    onBackClick: () -> Unit = {},
    onExitSuccess: () -> Unit = {}
) {
    val container = LocalAppContainer.current
    val viewModel: MessageDetailViewModel = simpleViewModel {
        MessageDetailViewModel(container.chatRepository, container.memberRepository)
    }
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(roomId, postId) {
        if (roomId > 0) {
            viewModel.loadChatRoom(roomId)
        } else if (postId > 0) {
            viewModel.initNewChatRoom(postId)
        }
    }

    // 키보드 상태 감지
    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current
    val imeHeight = imeInsets.getBottom(density)

    // 메시지 추가 또는 키보드가 올라올 때 마지막으로 스크롤
    LaunchedEffect(uiState.messages.size, imeHeight) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // 채팅방 나가기 성공 시 처리
    LaunchedEffect(uiState.exitSuccess) {
        if (uiState.exitSuccess) {
            onExitSuccess()
        }
    }

    // 채팅방 나가기 확인 다이얼로그
    if (showExitDialog) {
        SsabreeDialog(
            onDismissRequest = { showExitDialog = false },
            title = "채팅방 나가기",
            message = "정말로 채팅방을 나가시겠습니까?\n나가면 쪽지 복구가 어렵습니다.",
            confirmText = "나가기",
            dismissText = "취소",
            onConfirm = {
                showExitDialog = false
                viewModel.exitChatRoom()
            },
            onDismiss = { showExitDialog = false }
        )
    }

    if (uiState.showInvalidRoomDialog) {
        SsabreeDialog(
            onDismissRequest = {
                viewModel.clearInvalidRoomDialog()
                onBackClick()
            },
            title = "알림",
            message = "삭제된 채팅방입니다.",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = {
                viewModel.clearInvalidRoomDialog()
                onBackClick()
            }
        )
    }

    if (uiState.showClosedChatDialog) {
        SsabreeDialog(
            onDismissRequest = {
                viewModel.clearClosedChatDialog()
                onBackClick()
            },
            title = "쪽지",
            message = "이미 종료된 채팅입니다.",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = {
                viewModel.clearClosedChatDialog()
                onBackClick()
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when {
                                uiState.isNewChatMode -> "새 쪽지"
                                uiState.chatRoom?.chatRoomName?.isNotEmpty() == true -> uiState.chatRoom!!.chatRoomName
                                else -> "쪽지"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        when {
                            uiState.isNewChatMode -> {
                                Text(
                                    text = "메시지를 보내면 채팅방이 생성됩니다",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            uiState.connectionState is WebSocketConnectionState.Connected -> {
                                Text(
                                    text = "연결됨",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { isMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "더보기",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }
                        ) {
                            if (!uiState.isNewChatMode && uiState.roomId > 0) {
                                DropdownMenuItem(
                                    text = { Text("채팅방 나가기", fontSize = 14.sp) },
                                    onClick = {
                                        isMenuExpanded = false
                                        showExitDialog = true
                                    },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                                )
                            }
                        }
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(25.dp)
                            )
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = uiState.messageText,
                            onValueChange = { viewModel.onMessageTextChange(it) },
                            modifier = Modifier.weight(1f),
                            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                            decorationBox = { innerTextField ->
                                if (uiState.messageText.isEmpty()) {
                                    Text("메시지를 입력하세요", color = Color.Gray, fontSize = 14.sp)
                                }
                                innerTextField()
                            }
                        )
                        IconButton(
                            onClick = { viewModel.sendMessage() },
                            enabled = uiState.messageText.isNotBlank() && !uiState.isSending
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (uiState.messageText.isNotBlank() && !uiState.isSending) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "최대 255자",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${uiState.messageText.length}/255",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "오류가 발생했습니다",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.loadChatRoom(roomId) }) {
                            Text("다시 시도")
                        }
                    }
                }
                uiState.messages.isEmpty() -> {
                    Text(
                        text = if (uiState.isNewChatMode) {
                            "첫 메시지를 보내 대화를 시작하세요!"
                        } else {
                            "메시지가 없습니다.\n첫 메시지를 보내보세요!"
                        },
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        items(uiState.messages, key = { it.id }) { message ->
                            ChatBubble(message = message)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessageUiModel) {
    if (message.isMe) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    modifier = Modifier.widthIn(max = 260.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
                ) {
                    Text(
                        text = message.content,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = message.time, fontSize = 12.sp, color = Color.Gray)
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = message.senderName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier.widthIn(max = 260.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
                ) {
                    Text(
                        text = message.content,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = message.time, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessageDetailScreenPreview() {
    MessageDetailScreen()
}
