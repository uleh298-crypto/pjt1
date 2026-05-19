package com.ssafy.ssabree.features.mygroup.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.ssafy.ssabree.R
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.repository.GroupRepository
import com.ssafy.ssabree.core.repository.MyGroupRepository
import com.ssafy.ssabree.core.repository.model.GroupDetailModel
import com.ssafy.ssabree.core.repository.model.GroupMemberModel
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.core.utils.RetrofitClient
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.mygroup.model.TaskUiModel
import com.ssafy.ssabree.features.mygroup.model.toUiModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskDetailUiState(
    val task: TaskUiModel? = null,
    val authorName: String? = null,
    val authorProfileImageUrl: String? = null,
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class TaskDetailViewModel(
    private val repository: MyGroupRepository,
    private val groupRepository: GroupRepository,
    private val groupKind: GroupKind,
    private val groupId: Long,
    private val taskId: Long
) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskDetailUiState(isLoading = true))
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val tasksDeferred = async { repository.getTasks(groupKind, groupId) }
        val detailDeferred = async {
            if (groupKind == GroupKind.STUDY) {
                groupRepository.getStudyDetail(groupId)
            } else {
                groupRepository.getTeamDetail(groupId)
            }
        }
        val membersDeferred = async {
            if (groupKind == GroupKind.STUDY) {
                groupRepository.getStudyMembers(groupId)
            } else {
                groupRepository.getTeamMembers(groupId)
            }
        }

        val tasksResult = tasksDeferred.await()
        if (tasksResult.isFailure) {
            _uiState.update { it.copy(isLoading = false, errorMessage = tasksResult.exceptionOrNull()?.message) }
            return@launch
        }

        val detail = detailDeferred.await().getOrNull()
        val membersResult = membersDeferred.await()
        if (membersResult.isFailure) {
            _uiState.update { it.copy(isLoading = false, errorMessage = membersResult.exceptionOrNull()?.message) }
            return@launch
        }
        val target = tasksResult.getOrNull()?.firstOrNull { it.id == taskId }?.toUiModel()
        if (target == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "일정 정보를 찾을 수 없습니다.") }
        } else {
            val members = mergeLeader(membersResult.getOrNull().orEmpty(), detail)
            val author = target.creatorId?.let { creatorId ->
                members.firstOrNull { it.id == creatorId }
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    task = target,
                    authorName = author?.name,
                    authorProfileImageUrl = author?.profileImageUrl
                )
            }
        }
    }

    fun deleteTask() = viewModelScope.launch {
        _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
        repository.deleteTask(groupKind, taskId)
            .onSuccess {
                _uiState.update { it.copy(isDeleting = false, successMessage = "일정이 삭제되었습니다.") }
            }
            .onFailure { e ->
                _uiState.update { it.copy(isDeleting = false, errorMessage = e.message) }
            }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun mergeLeader(
        members: List<GroupMemberModel>,
        detail: GroupDetailModel?
    ): List<GroupMemberModel> {
        val leaderId = detail?.leaderId ?: return members
        if (members.any { it.id == leaderId }) return members
        val leader = GroupMemberModel(
            id = leaderId,
            email = detail.leaderEmail,
            name = detail.leaderName,
            mattermostId = detail.leaderMattermostId,
            profileImageUrl = detail.leaderProfileImageUrl
        )
        return members + leader
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    groupKind: GroupKind,
    groupId: Long,
    taskId: Long,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteSuccess: () -> Unit = {}
) {
    val container = LocalAppContainer.current
    val viewModel: TaskDetailViewModel = simpleViewModel(key = "task-detail-${groupKind.routeValue}-$groupId-$taskId") {
        TaskDetailViewModel(container.myGroupRepository, container.groupRepository, groupKind, groupId, taskId)
    }
    val uiState by viewModel.uiState.collectAsState()
    val task = uiState.task
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    if (uiState.errorMessage != null) {
        SsabreeDialog(
            onDismissRequest = { viewModel.clearMessages() },
            title = "오류",
            message = uiState.errorMessage ?: "알 수 없는 오류가 발생했습니다.",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = { viewModel.clearMessages() }
        )
    }

    if (uiState.successMessage != null) {
        SsabreeDialog(
            onDismissRequest = { viewModel.clearMessages() },
            title = "알림",
            message = uiState.successMessage.orEmpty(),
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = {
                viewModel.clearMessages()
                onDeleteSuccess()
            }
        )
    }

    if (showDeleteDialog) {
        SsabreeDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = "삭제 확인",
            message = "일정을 삭제하시겠습니까?",
            confirmText = "삭제",
            dismissText = "취소",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteTask()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("일정 상세", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "더보기")
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.authorProfileImageUrl.isNullOrBlank()) {
                        Icon(
                            painter = painterResource(id = R.drawable.profile),
                            contentDescription = "기본 프로필 이미지",
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            tint = Color.Unspecified
                        )
                    } else {
                        AsyncImage(
                            model = normalizeImageUrl(uiState.authorProfileImageUrl.orEmpty()),
                            contentDescription = "작성자 프로필 이미지",
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                        )
                    }
                    Text(
                        text = "작성자: ${uiState.authorName ?: "-"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextField(
                        value = task?.title.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("제목") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    TextField(
                        value = task?.content.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("상세 내용") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextField(
                            value = task?.startDate.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("시작일") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        TextField(
                            value = task?.endDate.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("종료일") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                    TextField(
                        value = when (task?.status) {
                            "TODO" -> "예정"
                            "IN_PROGRESS" -> "진행"
                            "DONE" -> "완료"
                            else -> task?.status.orEmpty()
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("상태") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
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
