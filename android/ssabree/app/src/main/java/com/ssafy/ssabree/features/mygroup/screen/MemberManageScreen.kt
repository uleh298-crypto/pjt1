package com.ssafy.ssabree.features.mygroup.screen

import android.R.attr.data
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ssafy.ssabree.R
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.utils.RetrofitClient
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.mygroup.model.ApplicationUiModel
import com.ssafy.ssabree.features.mygroup.model.MemberUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberManageScreen(
    groupId: Long,
    groupKind: GroupKind,
    isLeader: Boolean = true,
    hasNewApplicant: Boolean = false, // [추가] 지원자 유무 연동
    onBackClick: (hasPendingRequests: Boolean) -> Unit = {},
    onApplicantClick: (Long, Long) -> Unit = { _, _ -> },
    onMemberClick: (Long) -> Unit = {}
) {
    val container = LocalAppContainer.current
    val viewModel: MemberManageViewModel = simpleViewModel(key = "member-manage-${groupKind.routeValue}-$groupId") {
        MemberManageViewModel(
            container.groupRepository,
            container.myGroupRepository,
            container.memberRepository,
            groupKind,
            groupId
        )
    }
    val uiState = viewModel.uiState.collectAsState()
    val currentState = uiState.value
    val pendingApplications = remember(currentState.applications) {
        currentState.applications.filter { it.status == "PENDING" }
    }
    var kickTarget by remember { mutableStateOf<MemberUiModel?>(null) }
    var acceptTarget by remember { mutableStateOf<ApplicationUiModel?>(null) }
    var rejectTarget by remember { mutableStateOf<ApplicationUiModel?>(null) }
    var showFullDialog by remember { mutableStateOf(false) }

    BackHandler { onBackClick(pendingApplications.isNotEmpty()) }

    if (currentState.errorMessage != null) {
        SsabreeDialog(
            onDismissRequest = { viewModel.clearErrorMessage() },
            title = "오류",
            message = currentState.errorMessage ?: "알 수 없는 오류가 발생했습니다.",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = { viewModel.clearErrorMessage() }
        )
    }

    if (currentState.successMessage != null) {
        SsabreeDialog(
            onDismissRequest = { viewModel.clearSuccessMessage() },
            title = "알림",
            message = currentState.successMessage ?: "완료되었습니다.",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = { viewModel.clearSuccessMessage() }
        )
    }

    if (kickTarget != null) {
        val target = kickTarget!!
        SsabreeDialog(
            onDismissRequest = { kickTarget = null },
            title = "멤버 추방",
            message = "${target.name} 님을 추방하시겠습니까?",
            confirmText = "추방",
            dismissText = "취소",
            onConfirm = {
                kickTarget = null
                viewModel.kickMember(target.id)
            },
            onDismiss = { kickTarget = null }
        )
    }

    if (acceptTarget != null) {
        val target = acceptTarget!!
        SsabreeDialog(
            onDismissRequest = { acceptTarget = null },
            title = "수락 확인",
            message = "${target.applicantName ?: target.title} 님의 지원을 수락하시겠습니까?",
            confirmText = "수락",
            dismissText = "취소",
            onConfirm = {
                acceptTarget = null
                viewModel.accept(target.id)
            },
            onDismiss = { acceptTarget = null }
        )
    }

    if (rejectTarget != null) {
        val target = rejectTarget!!
        SsabreeDialog(
            onDismissRequest = { rejectTarget = null },
            title = "거절 확인",
            message = "${target.applicantName ?: target.title} 님의 지원을 거절하시겠습니까?",
            confirmText = "거절",
            dismissText = "취소",
            onConfirm = {
                rejectTarget = null
                viewModel.reject(target.id)
            },
            onDismiss = { rejectTarget = null }
        )
    }

    if (showFullDialog) {
        SsabreeDialog(
            onDismissRequest = { showFullDialog = false },
            title = "알림",
            message = "그룹 인원이 꽉 찼습니다. 그룹 인원을 늘려주시길 바랍니다.",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = { showFullDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("멤버 관리", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { onBackClick(pendingApplications.isNotEmpty()) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Text("멤버 요청", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))
            
            // [실시간 연동] 지원하기를 눌렀을 때만 나타나는 새로운 지원자
            if (hasNewApplicant) {
                MemberRequestItem(
                    data = ApplicationUiModel(
                        id = -1,
                        title = "새 지원자",
                        message = "새로운 지원이 도착했습니다.",
                        position = "지원",
                        status = "PENDING"
                    ),
                    onAccept = {},
                    onReject = {},
                    onPortfolioClick = {}
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            pendingApplications.forEach { app ->
                MemberRequestItem(
                    data = app,
                    onAccept = {
                        val isFull =
                            currentState.capacity > 0 && currentState.memberCount >= currentState.capacity
                        if (isFull) {
                            showFullDialog = true
                        } else {
                            acceptTarget = app
                        }
                    },
                    onReject = { rejectTarget = app },
                    onPortfolioClick = {
                        val portfolioId = app.portfolioId ?: return@MemberRequestItem
                        if (portfolioId <= 0L) return@MemberRequestItem
                        onApplicantClick(portfolioId, app.id)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (!hasNewApplicant && pendingApplications.isEmpty()) {
                Text(
                    "아직 지원자가 없습니다.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("멤버 목록", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column {
                    if (uiState.value.members.isEmpty()) {
                        Text(
                            "멤버 정보가 없습니다.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        uiState.value.members.forEachIndexed { index, member ->
                            MemberManageItem(
                                member = member,
                                isLeader = uiState.value.leaderId != null && uiState.value.leaderId == member.id,
                                showKick = isLeader,
                                onMemberClick = onMemberClick,
                                onKickClick = { kickTarget = member }
                            )
                            if (index != uiState.value.members.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun MemberRequestItem(
    data: ApplicationUiModel,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onPortfolioClick: () -> Unit
) {
    val hasPortfolio = data.portfolioId != null && data.portfolioId > 0L
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = hasPortfolio, onClick = onPortfolioClick)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Display applicant's profile image if available
            if (data.applicantProfileImageUrl.isNullOrBlank()) {
                Icon(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "기본 프로필 이미지",
                    modifier = Modifier.size(50.dp).clip(CircleShape),
                    tint = Color.Unspecified
                )
            } else {
                AsyncImage(
                    model = normalizeImageUrl(data.applicantProfileImageUrl),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Display applicant's name if available, fallback to title
                Text(
                    data.applicantName?.takeIf { it.isNotBlank() } ?: "-",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        data.position,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Row {
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("수락", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("거절", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun MemberManageItem(
    member: MemberUiModel,
    isLeader: Boolean,
    showKick: Boolean,
    onMemberClick: (Long) -> Unit,
    onKickClick: () -> Unit
) {
    val isPortfolioEnabled = member.portfolioId != null && member.portfolioId > 0L
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clickable(enabled = isPortfolioEnabled) {
                member.portfolioId?.let { onMemberClick(it) }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (member.profileImageUrl.isNullOrBlank()) {
            Icon(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "기본 프로필 이미지",
                modifier = Modifier.size(52.dp).clip(CircleShape),
                tint = Color.Unspecified
            )
        } else {
            AsyncImage(
                model = normalizeImageUrl(member.profileImageUrl),
                contentDescription = null,
                modifier = Modifier.size(52.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(member.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                if (isLeader) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp)) {
                        Text("팀장", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                }
            }
            Text(member.mattermostId, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!isLeader && showKick) {
            Button(
                onClick = onKickClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("내보내기", fontSize = 10.sp, color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}


private fun normalizeImageUrl(rawUrl: String?): String {
    val trimmed = rawUrl?.trim().orEmpty()
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
