package com.ssafy.ssabree.features.mygroup.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.utils.toAbsoluteKstText
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.mygroup.model.MyApplicationUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationsScreen(
    groupKind: GroupKind,
    onBackClick: () -> Unit = {},
    onGroupDetailClick: (Long) -> Unit = {}
) {
    val container = LocalAppContainer.current
    val viewModel: MyApplicationsViewModel =
        simpleViewModel(key = "my-applications-${groupKind.routeValue}") {
            MyApplicationsViewModel(container.groupRepository, groupKind)
        }
    val uiState by viewModel.uiState.collectAsState()
    var pendingCancel by remember { mutableStateOf<MyApplicationUiModel?>(null) }

    Scaffold(
        topBar = {
            val title = if (groupKind == GroupKind.STUDY) "내 스터디 지원" else "내 프로젝트 지원"
            TopAppBar(
                title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.load() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (uiState.applications.isEmpty() && !uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("지원 내역이 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(uiState.applications, key = { it.id }) { application ->
                        MyApplicationBox(
                            application = application,
                            onCancelClick = { pendingCancel = application }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }

    pendingCancel?.let { application ->
        SsabreeDialog(
            onDismissRequest = { pendingCancel = null },
            title = "지원 취소",
            message = "${application.groupTitle} 지원을 취소하시겠습니까?",
            confirmText = "취소",
            dismissText = "닫기",
            onConfirm = {
                pendingCancel = null
                viewModel.cancelApplication(application.id)
            },
            onDismiss = { pendingCancel = null }
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
private fun MyApplicationBox(
    application: MyApplicationUiModel,
    onCancelClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = application.groupTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!application.leaderName.isNullOrBlank()) {
                    Text(
                        text = "팀장: ${application.leaderName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val appliedDate = application.createdAt.toAbsoluteKstText("yyyy.MM.dd HH:mm").ifBlank { "-" }
                Text(
                    text = "지원일: $appliedDate",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusColor = when (application.status) {
                    "APPROVED" -> MaterialTheme.colorScheme.primary
                    "REJECTED" -> MaterialTheme.colorScheme.error
                    "DELETED" -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text(
                    text = application.statusMessage,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
                Spacer(modifier = Modifier.weight(1f))
                if (application.isPending && !application.isGroupDeleted) {
                    TextButton(onClick = onCancelClick) {
                        Text(
                            text = "지원 취소",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        }
    }
}
