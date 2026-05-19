package com.ssafy.ssabree.features.groupdetail.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ssafy.ssabree.R
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.core.utils.RetrofitClient
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.group.model.GroupDetailUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: Long,
    groupKind: GroupKind,
    onBackClick: () -> Unit = {},
    onApplyClick: () -> Unit = {},
    onEditClick: (Long, GroupKind) -> Unit = { _, _ -> },
    onDeleteSuccess: () -> Unit = {}
) {
    val container = LocalAppContainer.current
    val viewModel: GroupDetailViewModel = simpleViewModel(key = "group-detail-${groupKind.routeValue}-$groupId") {
        GroupDetailViewModel(container.groupRepository, container.memberRepository, groupKind, groupId)
    }
    val uiState by viewModel.uiState.collectAsState()
    var showAlreadyMemberDialog by remember { mutableStateOf(false) }
    var showPendingDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("상세보기", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isLeader) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "더보기")
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("수정") },
                                    onClick = {
                                        showMenu = false
                                        onEditClick(groupId, groupKind)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("삭제", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val detail = uiState.detail
            if (detail != null) {
                GroupDetailContent(
                    detail = detail,
                    modifier = Modifier.weight(1f)
                )
            } else if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            ) {
                Button(
                    onClick = {
                        if (uiState.isMember) {
                            showAlreadyMemberDialog = true
                        } else if (uiState.hasPendingApplication) {
                            showPendingDialog = true
                        } else {
                            onApplyClick()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 8.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("지원하기", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showAlreadyMemberDialog) {
        SsabreeDialog(
            onDismissRequest = { showAlreadyMemberDialog = false },
            title = "알림",
            message = "이미 속한 그룹입니다.",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = { showAlreadyMemberDialog = false }
        )
    }

    if (showPendingDialog) {
        SsabreeDialog(
            onDismissRequest = { showPendingDialog = false },
            title = "알림",
            message = "승인 대기중인 그룹입니다.",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = { showPendingDialog = false }
        )
    }

    if (showDeleteDialog) {
        SsabreeDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = "삭제 확인",
            message = "해당 모집글을 삭제하시겠습니까?",
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
private fun GroupDetailContent(detail: GroupDetailUiModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = detail.title,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (detail.leaderProfileImageUrl.isNullOrBlank()) {
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = normalizeImageUrl(detail.leaderProfileImageUrl),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = detail.leaderName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Text(
                    text = detail.leaderMattermostId,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Text(text = "팀장", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoSmallCard(
                icon = Icons.Default.CalendarMonth,
                label = "모집 종료일",
                value = detail.endDateDisplay,
                subValue = detail.dDay,
                modifier = Modifier.weight(1f)
            )
            InfoSmallCard(
                icon = Icons.Default.Groups,
                label = "그룹 인원",
                value = detail.memberCountInfo,
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = "상세 설명", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = detail.description,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
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
fun InfoSmallCard(
    icon: ImageVector,
    label: String,
    value: String,
    subValue: String? = null,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.height(100.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (subValue != null) {
                    Text(text = subValue, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
