package com.ssafy.ssabree.features.message.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.ssabree.R
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.repository.model.ChatRoomModel
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.core.utils.formatRelativeTime
import com.ssafy.ssabree.features.message.viewmodel.MessageViewModel
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    onMessageClick: (Long) -> Unit = {}
) {
    val container = LocalAppContainer.current
    val viewModel: MessageViewModel = simpleViewModel {
        MessageViewModel(container.chatRepository)
    }
    val uiState by viewModel.uiState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var roomToDelete by remember { mutableStateOf<ChatRoomModel?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadChatRooms()
    }
    DisposableEffect(Unit) {
        viewModel.startPolling()
        onDispose { viewModel.stopPolling() }
    }

    if (showDeleteDialog && roomToDelete != null) {
        SsabreeDialog(
            onDismissRequest = {
                showDeleteDialog = false
                roomToDelete = null
            },
            title = "쪽지 삭제",
            message = "'${roomToDelete?.chatRoomName?.ifEmpty { "익명의 싸용자" }}'님과의 쪽지를 삭제하시겠습니까?\n삭제하면 쪽지 복구가 어렵습니다.",
            confirmText = "삭제",
            dismissText = "취소",
            onConfirm = {
                roomToDelete?.let { viewModel.exitChatRoom(it.roomId, it.postId) }
                showDeleteDialog = false
                roomToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                roomToDelete = null
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "쪽지",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.loadChatRooms() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading && uiState.chatRooms.isEmpty() -> {
                    // 초기 로딩 시에는 PullToRefreshBox의 기본 인디케이터가 표시되므로 Box를 비워둡니다.
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "오류가 발생했습니다",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.loadChatRooms() }) {
                            Text("다시 시도")
                        }
                    }
                }
                uiState.chatRooms.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "쪽지가 없습니다",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(uiState.chatRooms, key = { it.roomId }) { room ->
                            ChatRoomListItem(
                                room = room,
                                onClick = { onMessageClick(room.roomId) },
                                onLongClick = {
                                    roomToDelete = room
                                    showDeleteDialog = true
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatRoomListItem(
    room: ChatRoomModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 12.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            tint = MaterialTheme.colorScheme.outlineVariant
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = room.chatRoomName.ifEmpty { "익명의 싸용자" },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                room.lastMessageAt?.let { time ->
                    Text(
                        text = formatRelativeTime(time),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = room.lastMessage ?: "메시지가 없습니다",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessageScreenPreview() {
    MessageScreen()
}
