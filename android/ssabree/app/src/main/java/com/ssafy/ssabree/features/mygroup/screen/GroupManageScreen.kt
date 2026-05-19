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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.ssabree.R
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.mygroup.model.MemberUiModel
import com.ssafy.ssabree.features.mygroup.model.NoticeUiModel
import com.ssafy.ssabree.features.mygroup.model.TaskUiModel
import com.ssafy.ssabree.core.utils.RetrofitClient
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupManageScreen(
    groupId: Long,
    groupKind: GroupKind,
    isLeader: Boolean,
    hasNewApplicant: Boolean = false,
    onNotificationShown: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onMemberManageClick: () -> Unit = {},
    onAddProgressClick: () -> Unit = {},
    onAllAnnouncementsClick: () -> Unit = {},
    onTaskClick: (Long) -> Unit = {},
    onEditClick: () -> Unit = {}, // [수정] 이동 연결
    onDeleteClick: () -> Unit = {}  // [수정] 삭제 연결
) {
    val container = LocalAppContainer.current
    val viewModel: MyGroupDetailViewModel = simpleViewModel(key = "group-manage-${groupKind.routeValue}-$groupId") {
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

    val scrollState = rememberScrollState()
    var showPopup by remember { mutableStateOf(hasNewApplicant) }

    if (showPopup && isLeader) {
        SsabreeDialog(
            onDismissRequest = {
                showPopup = false
                onNotificationShown()
            },
            title = "알림",
            message = "새로운 지원자가 있습니다.",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = {
                showPopup = false
                onNotificationShown()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("나의 그룹", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            GroupInfoHeader(
                title = uiState.title,
                memberCountText = uiState.memberCountText,
                applicationsCount = uiState.applicationsCount,
                dDayText = uiState.dDayText,
                isLeader = isLeader,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick
            )

            Spacer(modifier = Modifier.height(32.dp))
            NoticeSection(
                notices = uiState.notices,
                onAllAnnouncementsClick = onAllAnnouncementsClick
            )
            Spacer(modifier = Modifier.height(32.dp))
            MemberListSection(
                members = uiState.members,
                leaderId = uiState.leaderId,
                onMemberManageClick = onMemberManageClick
            )
            Spacer(modifier = Modifier.height(32.dp))
            DailyProgressSection(
                tasks = uiState.tasks,
                onAddProgressClick = onAddProgressClick,
                onTaskClick = onTaskClick
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun GroupInfoHeader(
    title: String,
    memberCountText: String,
    applicationsCount: Int,
    dDayText: String,
    isLeader: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    // [수정] 다크모드/라이트모드 반응형 색상
    val statsBoxBgColor = if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0)
    val statsValueTextColor = if (isDark) Color.White else Color.Black
    val statsLabelTextColor = if (isDark) Color.LightGray else Color.Gray

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
                Text("진행중", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title.ifBlank { "그룹 정보" }, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)

            Spacer(modifier = Modifier.height(16.dp))

            if (isLeader) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.align(Alignment.End)) {
                    Button(
                        onClick = onEditClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) { Text("수정", fontSize = 12.sp, fontWeight = FontWeight.Bold) }

                    Button(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) { Text("삭제", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // [수정] 다크모드 대응 통계 박스
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(statsBoxBgColor, RoundedCornerShape(12.dp))
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoStatItem("멤버", memberCountText, statsLabelTextColor, statsValueTextColor, Modifier.weight(1f))
                VerticalDivider(modifier = Modifier.height(24.dp), color = statsLabelTextColor.copy(alpha = 0.5f))
                InfoStatItem("지원 현황", "${applicationsCount}명", statsLabelTextColor, statsValueTextColor, Modifier.weight(1f))
                VerticalDivider(modifier = Modifier.height(24.dp), color = statsLabelTextColor.copy(alpha = 0.5f))
                InfoStatItem("D-day", dDayText, statsLabelTextColor, statsValueTextColor, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun InfoStatItem(label: String, value: String, labelColor: Color, valueColor: Color, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(label, fontSize = 12.sp, color = labelColor)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun NoticeSection(
    notices: List<NoticeUiModel>,
    onAllAnnouncementsClick: () -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
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
                    NoticeItem("등록된 공지사항이 없습니다.")
                } else {
                    notices.take(2).forEachIndexed { index, notice ->
                        NoticeItem(notice.title)
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
fun NoticeItem(text: String) {
    Row(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun MemberListSection(
    members: List<MemberUiModel>,
    leaderId: Long?,
    onMemberManageClick: () -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("멤버 목록", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = onMemberManageClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("멤버 관리", fontSize = 11.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column {
                if (members.isEmpty()) {
                    Text(
                        "멤버 정보가 없습니다.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    members.forEachIndexed { index, member ->
                        MemberItem(
                            name = member.name,
                            mattermostId = member.mattermostId,
                            profileImageUrl = member.profileImageUrl,
                            isLeader = leaderId != null && leaderId == member.id
                        )
                        if (index != members.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemberItem(
    name: String,
    mattermostId: String,
    profileImageUrl: String?,
    isLeader: Boolean
) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (profileImageUrl.isNullOrBlank()) {
            Icon(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "기본 프로필 이미지",
                modifier = Modifier.size(52.dp).clip(CircleShape),
                tint = Color.Unspecified
            )
        } else {
            AsyncImage(
                model = normalizeImageUrl(profileImageUrl),
                contentDescription = null,
                modifier = Modifier.size(52.dp).clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                if (isLeader) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp)) {
                        Text("팀장", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Text(mattermostId, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
fun DailyProgressSection(
    tasks: List<TaskUiModel>,
    onAddProgressClick: () -> Unit,
    onTaskClick: (Long) -> Unit
) {
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

        if (tasks.isEmpty()) {
            ProgressItem(
                name = "일정 없음",
                author = null,
                description = "등록된 일정이 없습니다.",
                dateRange = "-",
                authorProfileImageUrl = null,
                status = "예정",
                statusColor = MaterialTheme.colorScheme.outlineVariant,
                onClick = null
            )
        } else {
            tasks.take(3).forEachIndexed { index, task ->
                ProgressItem(
                    name = task.title,
                    author = task.authorName ?: "-",
                    description = task.content,
                    dateRange = "${task.startDate} ~ ${task.endDate}",
                    authorProfileImageUrl = task.authorProfileImageUrl,
                    when (task.status) {
                        "TODO" -> "예정"
                        "IN_PROGRESS" -> "진행"
                        "DONE" -> "완료"
                        else -> task.status
                    },
                    when (task.status) {
                        "TODO" -> MaterialTheme.colorScheme.outlineVariant
                        "IN_PROGRESS" -> MaterialTheme.colorScheme.primary
                        "DONE" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.outlineVariant
                    },
                    onClick = { onTaskClick(task.id) }
                )
                if (index != tasks.take(3).lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ProgressItem(
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
            .let { base -> if (onClick == null) base else base.clickable { onClick() } }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (authorProfileImageUrl.isNullOrBlank()) {
                    Icon(
                        painter = painterResource(id = R.drawable.profile),
                        contentDescription = "기본 프로필 이미지",
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        tint = Color.Unspecified
                    )
                } else {
                    AsyncImage(
                        model = normalizeImageUrl(authorProfileImageUrl),
                        contentDescription = "작성자 프로필 이미지",
                        modifier = Modifier.size(40.dp).clip(CircleShape)
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

@Preview(showBackground = true)
@Composable
fun GroupManageScreenPreview() {
    MaterialTheme {
        GroupManageScreen(
            groupId = 1L,
            groupKind = GroupKind.STUDY,
            isLeader = true
        )
    }
}
