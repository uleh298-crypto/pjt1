package com.ssafy.ssabree.features.mygroup.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.repository.MyGroupRepository
import com.ssafy.ssabree.core.repository.model.GroupTaskCreateInfo
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.mygroup.model.TaskUiModel
import com.ssafy.ssabree.features.mygroup.model.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskEditUiState(
    val task: TaskUiModel? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class TaskEditViewModel(
    private val repository: MyGroupRepository,
    private val groupKind: GroupKind,
    private val groupId: Long,
    private val taskId: Long
) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskEditUiState(isLoading = true))
    val uiState: StateFlow<TaskEditUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        repository.getTasks(groupKind, groupId)
            .onSuccess { tasks ->
                val target = tasks.firstOrNull { it.id == taskId }?.toUiModel()
                if (target == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "일정 정보를 찾을 수 없습니다.") }
                } else {
                    _uiState.update { it.copy(isLoading = false, task = target) }
                }
            }
            .onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
    }

    fun updateTask(
        title: String,
        content: String,
        startDate: String,
        endDate: String,
        status: String
    ) = viewModelScope.launch {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        repository.updateTask(
            groupKind,
            taskId,
            GroupTaskCreateInfo(
                title = title,
                content = content,
                startDate = startDate,
                endDate = endDate,
                status = status
            )
        ).onSuccess {
            _uiState.update { it.copy(isSaving = false, successMessage = "일정이 수정되었습니다.") }
        }.onFailure { e ->
            _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    groupKind: GroupKind,
    groupId: Long,
    taskId: Long,
    onBackClick: () -> Unit = {},
    onEditSuccess: () -> Unit = {}
) {
    val container = LocalAppContainer.current
    val viewModel: TaskEditViewModel = simpleViewModel(key = "task-edit-${groupKind.routeValue}-$groupId-$taskId") {
        TaskEditViewModel(container.myGroupRepository, groupKind, groupId, taskId)
    }
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("IN_PROGRESS") }
    var isStatusExpanded by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showDateErrorDialog by remember { mutableStateOf(false) }
    var dateErrorMessage by remember { mutableStateOf("") }
    val dateFormatter = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.KOREA) }
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()
    val todayMillis = remember {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        cal.timeInMillis
    }

    LaunchedEffect(uiState.task) {
        val task = uiState.task ?: return@LaunchedEffect
        title = task.title
        content = task.content
        startDate = task.startDate
        endDate = task.endDate
        status = task.status
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = startDatePickerState.selectedDateMillis
                    if (millis != null) {
                        if (millis < todayMillis) {
                            dateErrorMessage = "시작일은 오늘 이전으로 설정할 수 없습니다."
                            showDateErrorDialog = true
                        } else {
                            startDate = dateFormatter.format(java.util.Date(millis))
                            if (endDate.isNotBlank()) {
                                val endMillis = runCatching { dateFormatter.parse(endDate)?.time }.getOrNull()
                                if (endMillis != null && endMillis < millis) {
                                    endDate = ""
                                }
                            }
                        }
                    }
                    showStartDatePicker = false
                }) { Text("확인", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("취소") }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = endDatePickerState.selectedDateMillis
                    if (millis != null) {
                        val startMillis = runCatching { dateFormatter.parse(startDate)?.time }.getOrNull()
                        if (startMillis != null && millis < startMillis) {
                            dateErrorMessage = "종료일은 시작일 이전으로 설정할 수 없습니다."
                            showDateErrorDialog = true
                        } else {
                            endDate = dateFormatter.format(java.util.Date(millis))
                        }
                    }
                    showEndDatePicker = false
                }) { Text("확인", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("취소") }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }

    if (showDateErrorDialog) {
        SsabreeDialog(
            onDismissRequest = { showDateErrorDialog = false },
            title = "알림",
            message = dateErrorMessage,
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = { showDateErrorDialog = false }
        )
    }

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
                onEditSuccess()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("일정 수정", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.updateTask(
                                title = title,
                                content = content,
                                startDate = startDate,
                                endDate = endDate,
                                status = status
                            )
                        },
                        enabled = !uiState.isSaving && title.isNotBlank() && content.isNotBlank()
                    ) {
                        Text(
                            text = "수정",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!uiState.isSaving && title.isNotBlank() && content.isNotBlank()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
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
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("제목", color = MaterialTheme.colorScheme.onSurfaceVariant) },
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
                        value = content,
                        onValueChange = { content = it },
                        placeholder = { Text("상세 내용", color = MaterialTheme.colorScheme.onSurfaceVariant) },
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
                        Surface(
                            onClick = { showStartDatePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (startDate.isBlank()) "시작일" else startDate,
                                    modifier = Modifier.weight(1f),
                                    color = if (startDate.isBlank())
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            onClick = { showEndDatePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (endDate.isBlank()) "종료일" else endDate,
                                    modifier = Modifier.weight(1f),
                                    color = if (endDate.isBlank())
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = isStatusExpanded,
                        onExpandedChange = {
                            focusManager.clearFocus()
                            isStatusExpanded = !isStatusExpanded
                        }
                    ) {
                        TextField(
                            value = when (status) {
                                "TODO" -> "예정"
                                "IN_PROGRESS" -> "진행"
                                "DONE" -> "완료"
                                else -> status
                            },
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("상태", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStatusExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = isStatusExpanded,
                            onDismissRequest = { isStatusExpanded = false }
                        ) {
                            listOf("TODO" to "예정", "IN_PROGRESS" to "진행", "DONE" to "완료").forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        focusManager.clearFocus()
                                        status = value
                                        isStatusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
