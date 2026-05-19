package com.ssafy.ssabree.features.notification.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.repository.model.NotificationModel
import com.ssafy.ssabree.core.repository.model.NotificationType
import com.ssafy.ssabree.core.ui.simpleViewModel
import kotlinx.coroutines.launch

private fun formatDateText(createdAt: String): String {
    return try {
        val normalized = createdAt.replace("T", " ")
        if (normalized.length >= 16) normalized.take(16) else normalized
    } catch (e: Exception) {
        createdAt.replace("T", " ").take(16)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBackClick: () -> Unit = {},
    onPostClick: (Long) -> Unit = {},
    onChatClick: (Long) -> Unit = {},
    onTeamClick: (Long) -> Unit = {},
    onStudyClick: (Long) -> Unit = {}
) {
    val container = LocalAppContainer.current
    val viewModel: NotificationViewModel = simpleViewModel {
        NotificationViewModel(container.notificationRepository)
    }
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var showDeletedDialog by remember { mutableStateOf(false) }
    var deletedDialogMessage by remember { mutableStateOf("삭제된 정보입니다.") }

    if (showDeletedDialog) {
        SsabreeDialog(
            onDismissRequest = { showDeletedDialog = false },
            title = "알림",
            message = deletedDialogMessage,
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = { showDeletedDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "알림",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "알림을 불러오는데 실패했습니다.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    val notifications = uiState.notifications

                    if (notifications.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "알림이 없습니다.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(notifications, key = { it.id }) { notification ->
                                NotificationListItem(
                                    notification = notification,
                                    onClick = {
                                        viewModel.markAsRead(notification.id)
                                        scope.launch {
                                            handleNotificationClick(
                                                notification = notification,
                                                onPostClick = onPostClick,
                                                onChatClick = onChatClick,
                                                onTeamClick = onTeamClick,
                                                onStudyClick = onStudyClick,
                                                postRepositoryCheck = { id ->
                                                    container.postRepository.getPostDetail(id)
                                                },
                                                chatRoomCheck = { id ->
                                                    container.chatRepository.getChatRoom(id)
                                                },
                                                teamCheck = { id ->
                                                    container.groupRepository.getTeamDetail(id)
                                                },
                                                studyCheck = { id ->
                                                    container.groupRepository.getStudyDetail(id)
                                                },
                                                onDeleted = { message ->
                                                    deletedDialogMessage = message
                                                    showDeletedDialog = true
                                                }
                                            )
                                        }
                                    }
                                )
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun handleNotificationClick(
    notification: NotificationModel,
    onPostClick: (Long) -> Unit,
    onChatClick: (Long) -> Unit,
    onTeamClick: (Long) -> Unit,
    onStudyClick: (Long) -> Unit,
    postRepositoryCheck: suspend (Long) -> Result<*>,
    chatRoomCheck: suspend (Long) -> Result<com.ssafy.ssabree.core.repository.model.ChatRoomModel>,
    teamCheck: suspend (Long) -> Result<*>,
    studyCheck: suspend (Long) -> Result<*>,
    onDeleted: (String) -> Unit
) {
    val relatedUrl = notification.relatedUrl ?: return

    when {
        // COMMENT, REPLY -> /posts/{postId}
        relatedUrl.startsWith("/posts/") -> {
            val postId = relatedUrl.removePrefix("/posts/").toLongOrNull()
            if (postId != null) {
                val deletedMessage = when (notification.type) {
                    NotificationType.COMMENT, NotificationType.REPLY -> "삭제된 댓글입니다."
                    else -> "삭제된 게시글입니다."
                }
                postRepositoryCheck(postId)
                    .onSuccess { onPostClick(postId) }
                    .onFailure { onDeleted(deletedMessage) }
            }
        }
        // MESSAGE -> /chats/{chatRoomId}
        relatedUrl.startsWith("/chats/") -> {
            val roomId = relatedUrl.removePrefix("/chats/").toLongOrNull()
            if (roomId != null) {
                chatRoomCheck(roomId)
                    .onSuccess { room ->
                        if (room.isDeleted) {
                            onDeleted("삭제된 쪽지입니다.")
                        } else {
                            onChatClick(roomId)
                        }
                    }
                    .onFailure { onDeleted("삭제된 쪽지입니다.") }
            }
        }
        // TEAM -> /teams/{teamId}
        relatedUrl.startsWith("/teams/") -> {
            val teamId = relatedUrl.removePrefix("/teams/").toLongOrNull()
            if (teamId != null) {
                teamCheck(teamId)
                    .onSuccess { onTeamClick(teamId) }
                    .onFailure { onDeleted("삭제된 팀입니다.") }
            }
        }
        // STUDY -> /studies/{studyId}
        relatedUrl.startsWith("/studies/") -> {
            val studyId = relatedUrl.removePrefix("/studies/").toLongOrNull()
            if (studyId != null) {
                studyCheck(studyId)
                    .onSuccess { onStudyClick(studyId) }
                    .onFailure { onDeleted("삭제된 스터디입니다.") }
            }
        }
        // 그 외 (HOT_POST, NOTICE, ETC) -> 읽음 처리만 (이미 markAsRead 호출됨)
    }
}

@Composable
fun NotificationListItem(
    notification: NotificationModel,
    onClick: () -> Unit = {}
) {
    val unreadColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)

    val backgroundColor = if (notification.isRead) {
        MaterialTheme.colorScheme.surface
    } else {
        unreadColor
    }

    val typeLabel = when (notification.type) {
        NotificationType.COMMENT -> "댓글"
        NotificationType.REPLY -> "답글"
        NotificationType.MESSAGE -> "쪽지"
        NotificationType.HOT_POST -> "인기글"
        NotificationType.NOTICE -> "공지"
        NotificationType.ETC -> "알림"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Text(
                text = formatDateText(notification.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = notification.content,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
