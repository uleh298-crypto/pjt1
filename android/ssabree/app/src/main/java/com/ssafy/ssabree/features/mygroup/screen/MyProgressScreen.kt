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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.features.group.model.GroupKind

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProgressScreen(
    groupId: Long,
    groupKind: GroupKind,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit = {}
) {
    val container = LocalAppContainer.current
    val viewModel: MyProgressViewModel = simpleViewModel(key = "myprogress-${groupKind.routeValue}-$groupId") {
        MyProgressViewModel(container.myGroupRepository, container.memberRepository, groupKind, groupId)
    }
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var isStatusExpanded by remember { mutableStateOf(false) }

    var newTitle by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }
    var newStartDate by remember { mutableStateOf("") }
    var newEndDate by remember { mutableStateOf("") }
    var newStatus by remember { mutableStateOf("IN_PROGRESS") }
    val canCreateTask =
        newTitle.isNotBlank() && newContent.isNotBlank() && newStartDate.isNotBlank() && newEndDate.isNotBlank()

    val dateFormatter = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.KOREA) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showDateErrorDialog by remember { mutableStateOf(false) }
    var dateErrorMessage by remember { mutableStateOf("") }
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
                            newStartDate = dateFormatter.format(java.util.Date(millis))
                            if (newEndDate.isNotBlank()) {
                                val endMillis = runCatching { dateFormatter.parse(newEndDate)?.time }.getOrNull()
                                if (endMillis != null && endMillis < millis) {
                                    newEndDate = ""
                                }
                            }
                        }
                    }
                    showStartDatePicker = false
                }) {
                    Text("확인", fontWeight = FontWeight.Bold)
                }
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
                        val startMillis = runCatching { dateFormatter.parse(newStartDate)?.time }.getOrNull()
                        if (startMillis != null && millis < startMillis) {
                            dateErrorMessage = "종료일은 시작일 이전으로 설정할 수 없습니다."
                            showDateErrorDialog = true
                        } else {
                            newEndDate = dateFormatter.format(java.util.Date(millis))
                        }
                    }
                    showEndDatePicker = false
                }) {
                    Text("확인", fontWeight = FontWeight.Bold)
                }
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

    if (uiState.isSaved) {
        SsabreeDialog(
            onDismissRequest = {},
            title = "알림",
            message = "저장되었습니다.",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = {
                viewModel.resetSaved()
                onSaveSuccess()
            }
        )
    }

    if (uiState.isCreated) {
        SsabreeDialog(
            onDismissRequest = {},
            title = "알림",
            message = "일정이 등록되었습니다.",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = {
                viewModel.resetCreated()
                onSaveSuccess()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("일정 추가", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.createTask(
                                title = newTitle,
                                content = newContent,
                                startDate = newStartDate,
                                endDate = newEndDate,
                                status = newStatus
                            ) {
                                newTitle = ""
                                newContent = ""
                                newStartDate = ""
                                newEndDate = ""
                                newStatus = "IN_PROGRESS"
                            }
                        },
                        enabled = canCreateTask && !uiState.isCreating
                    ) {
                        if (uiState.isCreating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = "등록",
                                color = if (canCreateTask) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 1. 프로필 카드
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (uiState.myProfileImageUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    } else {
                        AsyncImage(
                            model = uiState.myProfileImageUrl,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            uiState.myName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            buildString {
                                if (uiState.myGeneration != null) {
                                    append("${uiState.myGeneration}기")
                                }
                                if (!uiState.myCampus.isNullOrBlank()) {
                                    if (isNotEmpty()) append(" ")
                                    append("${uiState.myCampus} 캠퍼스")
                                }
                                if (isEmpty()) append("캠퍼스 정보 없음")
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. 일정 추가
            SectionHeader(title = "일정 추가", icon = Icons.AutoMirrored.Filled.Assignment)
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
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
                    value = newContent,
                    onValueChange = { newContent = it },
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
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        onClick = { showStartDatePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (newStartDate.isBlank()) "시작일" else newStartDate,
                                modifier = Modifier.weight(1f),
                                color = if (newStartDate.isBlank())
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (newEndDate.isBlank()) "종료일" else newEndDate,
                                modifier = Modifier.weight(1f),
                                color = if (newEndDate.isBlank())
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
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        onClick = { isStatusExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (newStatus) {
                                    "TODO" -> "예정"
                                    "IN_PROGRESS" -> "진행중"
                                    "DONE" -> "완료"
                                    else -> newStatus
                                },
                                modifier = Modifier.weight(1f),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = isStatusExpanded,
                        onDismissRequest = { isStatusExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        listOf("TODO" to "예정", "IN_PROGRESS" to "진행중", "DONE" to "완료").forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label, fontSize = 14.sp) },
                                onClick = {
                                    focusManager.clearFocus()
                                    newStatus = value
                                    isStatusExpanded = false
                                }
                            )
                        }
                    }
                }
                uiState.errorMessage?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatusChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    
    val bgColor = when {
        isSelected && text == "진행중" -> if (isDark) Color(0xFF004D40) else Color(0xFFE0F2F1)
        isSelected && text == "예정" -> if (isDark) Color(0xFF5F4B00) else Color(0xFFFFFDE7)
        isSelected && text == "완료" -> if (isDark) Color(0xFF1A237E) else Color(0xFFE8EAF6)
        else -> if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF5F5F5)
    }
    val textColor = when {
        isSelected && text == "진행중" -> if (isDark) Color(0xFF80CBC4) else Color(0xFF00897B)
        isSelected && text == "예정" -> if (isDark) Color(0xFFFFF176) else Color(0xFFFBC02D)
        isSelected && text == "완료" -> if (isDark) Color(0xFFC5CAE9) else Color(0xFF5C6BC0)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(90.dp)
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector? = null, showAdd: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else if (showAdd) {
            Icon(
                Icons.AutoMirrored.Filled.Assignment,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun TagItem(tag: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = tag,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
