package com.ssafy.ssabree.features.mygroup.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.core.utils.toRelativeTimeText
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.mygroup.model.NoticeUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsScreen(
    groupId: Long,
    groupKind: GroupKind,
    isLeader: Boolean = false,
    shouldRefresh: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onNoticeChanged: () -> Unit = {},
    onWriteAnnouncementClick: (Boolean, Long, String, String, Boolean) -> Unit = { _, _, _, _, _ -> }
) {
    val container = LocalAppContainer.current
    val viewModel: AnnouncementsViewModel = simpleViewModel(key = "announcements-${groupKind.routeValue}-$groupId") {
        AnnouncementsViewModel(container.myGroupRepository, groupKind, groupId)
    }
    val uiState by viewModel.uiState.collectAsState()

    // 새로고침 신호 처리
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.load()
            onRefreshConsumed()
        }
    }

    var showOldAnnouncements by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var noticeToDelete by remember { mutableStateOf<NoticeUiModel?>(null) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    val filteredNotices = remember(uiState.notices, searchText.text) {
        val query = searchText.text.trim()
        if (query.isBlank()) uiState.notices
        else uiState.notices.filter {
            it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("공지사항", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.load() },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            if (filteredNotices.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 28.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (searchText.text.isBlank()) "공지사항이 없습니다." else "검색 결과가 없습니다.",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            filteredNotices.take(5).forEachIndexed { index, data ->
                                AnnouncementListItem(
                                    data = data,
                                    canManage = isLeader,
                                    onEditClick = {
                                        onWriteAnnouncementClick(true, data.id, data.title, data.content, data.isPinned)
                                    },
                                    onDeleteClick = {
                                        noticeToDelete = data
                                        showDeleteConfirmDialog = true
                                    }
                                )
                                if (index < 4 && index < filteredNotices.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 20.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            if (filteredNotices.size > 5) {
                                AnimatedVisibility(
                                    visible = showOldAnnouncements,
                                    enter = fadeIn() + expandVertically()
                                ) {
                                    Column {
                                        filteredNotices.drop(5).forEach { data ->
                                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                            AnnouncementListItem(
                                                data = data,
                                                canManage = isLeader,
                                                onEditClick = {
                                                    onWriteAnnouncementClick(true, data.id, data.title, data.content, data.isPinned)
                                                },
                                                onDeleteClick = {
                                                    noticeToDelete = data
                                                    showDeleteConfirmDialog = true
                                                }
                                            )
                                        }
                                    }
                                }

                                if (!showOldAnnouncements) {
                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    TextButton(
                                        onClick = { showOldAnnouncements = true },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                    ) {
                                        Text("이전 공지사항 더보기", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }

            if (isLeader) {
                FloatingActionButton(
                    onClick = { onWriteAnnouncementClick(false, 0L, "", "", false) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    if (showSearchDialog) {
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            title = { Text("공지사항 검색") },
            text = {
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    singleLine = true,
                    placeholder = { Text("제목/내용 검색") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = { showSearchDialog = false }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    searchText = TextFieldValue("")
                    showSearchDialog = false
                }) {
                    Text("초기화")
                }
            }
        )
    }

    // 삭제 확인 다이얼로그
    if (showDeleteConfirmDialog && noticeToDelete != null) {
        SsabreeDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                noticeToDelete = null
            },
            title = "삭제 확인",
            message = "공지사항을 삭제하시겠습니까?",
            confirmText = "삭제",
            dismissText = "취소",
            showDismissButton = true,
            onConfirm = {
                noticeToDelete?.let { viewModel.deleteNotice(it.id) }
                showDeleteConfirmDialog = false
                noticeToDelete = null
            },
            onDismiss = {
                showDeleteConfirmDialog = false
                noticeToDelete = null
            }
        )
    }

    // 삭제 성공 다이얼로그
    if (uiState.isDeleteSuccess) {
        SsabreeDialog(
            onDismissRequest = {},
            title = "알림",
            message = "공지사항이 삭제되었습니다.",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = {
                viewModel.resetDeleteSuccess()
                viewModel.load()
                onNoticeChanged()
            }
        )
    }

    // 오류 다이얼로그
    uiState.errorMessage?.let { message ->
        SsabreeDialog(
            onDismissRequest = { viewModel.resetError() },
            title = "오류",
            message = message,
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = { viewModel.resetError() }
        )
    }
}

@Composable
fun AnnouncementListItem(
    data: NoticeUiModel,
    canManage: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = if (data.isPinned) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = if (data.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = data.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (data.isPinned) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "고정됨",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = data.createdAt.toRelativeTimeText(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            if (canManage) {
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "더보기",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("수정") },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("삭제", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Text(
                text = data.content,
                modifier = Modifier.padding(top = 16.dp, start = 60.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnnouncementsScreenPreview() {
    androidx.compose.runtime.CompositionLocalProvider(LocalAppContainer provides com.ssafy.ssabree.app.FakeAppContainer()) {
        AnnouncementsScreen(groupId = 1L, groupKind = GroupKind.STUDY, isLeader = true)
    }
}
