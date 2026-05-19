package com.ssafy.ssabree.features.mygroup.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ssafy.ssabree.R
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.core.utils.toRelativeTimeText
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.mygroup.model.MemberUiModel
import com.ssafy.ssabree.core.utils.RetrofitClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyGroupDetailScreen(
    groupId: Long,
    groupKind: GroupKind,
    isLeader: Boolean = false,
    hasNewApplicant: Boolean = false,
    shouldRefresh: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
    onNotificationShown: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteSuccess: () -> Unit = {},
    onLeaveSuccess: () -> Unit = {},
    onMemberManageClick: () -> Unit = {},
    onAddProgressClick: () -> Unit = {},
    onAllAnnouncementsClick: () -> Unit = {},
    onTaskClick: (Long) -> Unit = {},
    onMemberClick: (Long) -> Unit = {}
) {
    val container = LocalAppContainer.current
    val viewModel: MyGroupDetailViewModel = simpleViewModel(key = "mygroup-detail-${groupKind.routeValue}-$groupId") {
        MyGroupDetailViewModel(
            container.groupRepository,
            container.myGroupRepository,
            container.memberRepository,
            groupKind,
            groupId,
            isLeader
        )
    }
    val uiState by viewModel.uiState.collectAsState()

    // 새로고침 신호 처리
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.load()
            onRefreshConsumed()
        }
    }

    val scrollState = rememberScrollState()
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showApplicantPopup by remember { mutableStateOf(hasNewApplicant) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    val hasApplicantBadge = isLeader && (hasNewApplicant || uiState.applicationsCount > 0)

    // 새 지원자 알림 팝업
    if (showApplicantPopup && isLeader) {
        SsabreeDialog(
            onDismissRequest = {
                showApplicantPopup = false
                onNotificationShown()
            },
            title = "알림",
            message = "새로운 지원자가 있습니다.",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = {
                showApplicantPopup = false
                onNotificationShown()
            }
        )
    }

    // 삭제 확인 다이얼로그
    if (showDeleteDialog) {
        SsabreeDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = "그룹 삭제",
            message = "정말로 이 그룹을 삭제하시겠습니까?\n삭제된 그룹은 복구할 수 없습니다.",
            confirmText = "삭제",
            dismissText = "취소",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteGroup {
                    onDeleteSuccess()
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showLeaveDialog) {
        SsabreeDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = "알림",
            message = "정말 그룹을 나가시겠습니까?",
            confirmText = "나가기",
            dismissText = "취소",
            onConfirm = {
                showLeaveDialog = false
                viewModel.leaveGroup { onLeaveSuccess() }
            },
            onDismiss = { showLeaveDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("그룹 상세", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isLeader) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Box {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                                    if (hasApplicantBadge) {
                                        SmallBadgeDot(modifier = Modifier.align(Alignment.TopStart))
                                    }
                                }
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                offset = DpOffset(x = (-20).dp, y = 0.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("수정") },
                                    onClick = {
                                        showMenu = false
                                        onEditClick()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("멤버 관리") },
                                    onClick = {
                                        showMenu = false
                                        onMemberManageClick()
                                    },
                                    leadingIcon = {
                                        Box {
                                            Icon(Icons.Default.ManageAccounts, contentDescription = null)
                                            if (uiState.applicationsCount > 0) {
                                                SmallBadgeCount(
                                                    count = uiState.applicationsCount,
                                                    modifier = Modifier.align(Alignment.TopStart)
                                                )
                                            }
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("삭제", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    } else {
                        TextButton(onClick = { showLeaveDialog = true }) {
                            Text(
                                text = "나가기",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.load() },
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // 그룹 정보 헤더
                DetailGroupInfoHeader(
                    title = uiState.title,
                    members = uiState.members,
                    leaderId = uiState.leaderId,
                    onMemberClick = onMemberClick
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 공지사항 섹션
                DetailNoticeSection(
                    notices = uiState.notices,
                    onAllAnnouncementsClick = onAllAnnouncementsClick
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 일일 진행 현황 섹션
                DetailDailyProgressSection(
                    tasks = uiState.tasks,
                    onAddProgressClick = onAddProgressClick,
                    onTaskClick = onTaskClick
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    uiState.errorMessage?.let { message ->
        SsabreeDialog(
            onDismissRequest = { viewModel.clearError() },
            title = "오류",
            message = message,
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = { viewModel.clearError() }
        )
    }
}

@Composable
private fun DetailGroupInfoHeader(
    title: String,
    members: List<MemberUiModel>,
    leaderId: Long?,
    onMemberClick: (Long) -> Unit
) {
    val orderedMembers = remember(members, leaderId) {
        if (leaderId == null) {
            members
        } else {
            val leader = members.firstOrNull { it.id == leaderId }
            val others = members.filterNot { it.id == leaderId }
            if (leader != null) listOf(leader) + others else members
        }
    }
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Text(
                    "진행중",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)

            Spacer(modifier = Modifier.height(20.dp))

            // 멤버 목록
            Text("멤버 목록", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            if (members.isEmpty()) {
                Text("멤버 정보가 없습니다.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                orderedMembers.forEachIndexed { index, member ->
                    DetailMemberItem(
                        name = member.name,
                        profileImageUrl = member.profileImageUrl,
                        mattermostId = member.mattermostId,
                        isLeader = leaderId != null && member.id == leaderId,
                        onClick = member.portfolioId?.let { { onMemberClick(it) } }
                    )
                    if (index != orderedMembers.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailNoticeSection(
    notices: List<com.ssafy.ssabree.features.mygroup.model.NoticeUiModel>,
    onAllAnnouncementsClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("공지사항", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "전체보기",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { onAllAnnouncementsClick() }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column {
                if (notices.isEmpty()) {
                    DetailNoticeItem("등록된 공지사항이 없습니다.", null)
                } else {
                    notices.take(2).forEachIndexed { index, notice ->
                        DetailNoticeItem(notice.title, notice.createdAt)
                        if (index != notices.take(2).lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailNoticeItem(text: String, createdAt: String?) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Campaign,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (createdAt != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = createdAt.toRelativeTimeText(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailMemberItem(
    name: String,
    profileImageUrl: String?,
    mattermostId: String,
    isLeader: Boolean,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth()
            .let { base -> if (onClick == null) base else base.clickable { onClick() } },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (profileImageUrl.isNullOrBlank()) {
            Icon(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "기본 프로필 이미지",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                tint = Color.Unspecified
            )
        } else {
            AsyncImage(
                model = normalizeImageUrl(profileImageUrl),
                contentDescription = null,
                modifier = Modifier.size(36.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (isLeader) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "팀장",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            Text(mattermostId, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DetailDailyProgressSection(
    tasks: List<com.ssafy.ssabree.features.mygroup.model.TaskUiModel>,
    onAddProgressClick: () -> Unit,
    onTaskClick: (Long) -> Unit
) {
    var selectedTaskFilter by remember { mutableStateOf("ALL") }
    val filteredTasks = remember(tasks, selectedTaskFilter) {
        when (selectedTaskFilter) {
            "TODO" -> tasks.filter { it.status == "TODO" }
            "IN_PROGRESS" -> tasks.filter { it.status == "IN_PROGRESS" }
            "DONE" -> tasks.filter { it.status == "DONE" }
            else -> tasks
        }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("일정", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onAddProgressClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add Progress",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TabRow(
            selectedTabIndex = when (selectedTaskFilter) {
                "ALL" -> 0
                "TODO" -> 1
                "IN_PROGRESS" -> 2
                else -> 3
            },
            containerColor = Color.Transparent
        ) {
            listOf(
                "ALL" to "전체",
                "TODO" to "예정",
                "IN_PROGRESS" to "진행",
                "DONE" to "완료"
            ).forEachIndexed { index, (value, label) ->
                Tab(
                    selected = when (selectedTaskFilter) {
                        "ALL" -> index == 0
                        "TODO" -> index == 1
                        "IN_PROGRESS" -> index == 2
                        else -> index == 3
                    },
                    onClick = { selectedTaskFilter = value },
                    text = { Text(label, fontSize = 13.sp) }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (filteredTasks.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "일정 없음",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "등록된 일정이 없습니다.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            filteredTasks.take(3).forEachIndexed { index, task ->
                DetailProgressItem(
                    name = task.title,
                    author = task.authorName ?: "-",
                    description = task.content,
                    dateRange = "${task.startDate} ~ ${task.endDate}",
                    authorProfileImageUrl = task.authorProfileImageUrl,
                    status = when (task.status) {
                        "TODO" -> "예정"
                        "IN_PROGRESS" -> "진행"
                        "DONE" -> "완료"
                        else -> task.status
                    },
                    statusColor = when (task.status) {
                        "TODO" -> MaterialTheme.colorScheme.outlineVariant
                        "IN_PROGRESS" -> MaterialTheme.colorScheme.primary
                        "DONE" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.outlineVariant
                    },
                    onClick = { onTaskClick(task.id) }
                )
                if (index != filteredTasks.take(3).lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailProgressItem(
    name: String,
    author: String?,
    description: String,
    dateRange: String,
    authorProfileImageUrl: String?,
    status: String,
    statusColor: Color,
    onClick: (() -> Unit)?
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .let { base ->
                if (onClick == null) base else base.clickable { onClick() }
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (authorProfileImageUrl.isNullOrBlank()) {
                    Icon(
                        painter = painterResource(id = R.drawable.profile),
                        contentDescription = "기본 프로필 이미지",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        tint = Color.Unspecified
                    )
                } else {
                    AsyncImage(
                        model = normalizeImageUrl(authorProfileImageUrl),
                        contentDescription = "작성자 프로필 이미지",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (author != null) {
                        Text("작성자: $author", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.5f)
                ) {
                    Text(
                        status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(6.dp))
            Text(dateRange, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun normalizeImageUrl(rawUrl: String): String {
    val trimmed = rawUrl.trim()
    if (trimmed.isBlank()) return ""
    if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
        return trimmed
    }
    val normalized = trimmed.replace("\\", "/")
    val uploadsIndex = normalized.indexOf("/uploads/")
    if (uploadsIndex >= 0) {
        val relative = normalized.substring(uploadsIndex)
        return RetrofitClient.SERVER_URL.trimEnd('/') + relative
    }
    return RetrofitClient.SERVER_URL.trimEnd('/') + "/uploads/" + normalized.trimStart('/')
}

@Composable
private fun SmallBadgeDot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.error)
    )
}

@Composable
private fun SmallBadgeCount(count: Int, modifier: Modifier = Modifier) {
    val display = if (count > 99) "99+" else count.toString()
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.error
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 1.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = display,
                color = MaterialTheme.colorScheme.onError,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

