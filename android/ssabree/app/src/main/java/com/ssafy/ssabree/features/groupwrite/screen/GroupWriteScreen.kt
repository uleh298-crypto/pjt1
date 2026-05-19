package com.ssafy.ssabree.features.groupwrite.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.app.FakeAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.repository.model.GroupCreateInfo
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.group.model.GroupTypeMapper
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupWriteScreen(
    initialTab: Int = 0,
    isEditMode: Boolean = false,
    groupId: Long? = null,
    onBackClick: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    val container = LocalAppContainer.current
    val groupKind = if (initialTab == 0) GroupKind.STUDY else GroupKind.PROJECT
    val viewModel: GroupWriteViewModel = simpleViewModel(key = "group-write-${groupKind.routeValue}") {
        GroupWriteViewModel(
            container.groupRepository,
            container.memberRepository,
            container.campusRepository,
            groupKind
        )
    }
    val uiState by viewModel.uiState.collectAsState()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var memberCount by remember { mutableIntStateOf(3) }
    var selectedCategory by remember {
        mutableStateOf(
            if (groupKind == GroupKind.STUDY) "알고리즘" else "싸피"
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    // 시작일은 오늘 날짜로 기본 설정
    val todayMillis = remember {
        // DatePicker는 UTC 기준 millis를 사용하므로, 로컬 "오늘"을 UTC 자정 millis로 변환한다.
        val local = Calendar.getInstance()
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, local.get(Calendar.YEAR))
            set(Calendar.MONTH, local.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, local.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        utc.timeInMillis
    }
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = todayMillis
    )
    val sdf = remember { SimpleDateFormat("yy/MM/dd", Locale.KOREA) }

    val dateDisplayText = remember(dateRangePickerState.selectedStartDateMillis, dateRangePickerState.selectedEndDateMillis) {
        val start = dateRangePickerState.selectedStartDateMillis
        val end = dateRangePickerState.selectedEndDateMillis
        if (start != null && end != null) {
            "${sdf.format(Date(start))} ~ ${sdf.format(Date(end))}"
        } else if (start != null) {
            "${sdf.format(Date(start))} ~ 종료일 선택"
        } else {
            "기간 선택"
        }
    }

    val dDayText = remember(dateRangePickerState.selectedEndDateMillis) {
        val end = dateRangePickerState.selectedEndDateMillis
        if (end != null) {
            val diff = end - System.currentTimeMillis()
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            if (days >= 0) " (D-$days)" else " (진행중)"
        } else {
            ""
        }
    }

    val topBarTitle = if (isEditMode) {
        "수정하기"
    } else {
        if (initialTab == 0) "스터디 모집 글 작성" else "프로젝트 모집 글 작성"
    }
    val recruitmentFields = if (groupKind == GroupKind.STUDY) {
        listOf("알고리즘", "CS", "자격증", "기타")
    } else {
        listOf("싸피", "공모전", "자유")
    }

    // 등록 가능 여부 (제목과 내용이 모두 입력되어야 함)
    val isSubmitEnabled =
        title.isNotBlank() &&
            content.isNotBlank() &&
            dateRangePickerState.selectedStartDateMillis != null &&
            dateRangePickerState.selectedEndDateMillis != null &&
            uiState.campusId != null

    LaunchedEffect(isEditMode, groupId) {
        if (!isEditMode || groupId == null) return@LaunchedEffect
        val detailResult = if (groupKind == GroupKind.STUDY) {
            container.groupRepository.getStudyDetail(groupId)
        } else {
            container.groupRepository.getTeamDetail(groupId)
        }
        detailResult.onSuccess { detail ->
            title = detail.title
            content = detail.description
            memberCount = detail.capacity
            selectedCategory = if (groupKind == GroupKind.STUDY) {
                GroupTypeMapper.studyTypeToLabel(detail.type)
            } else {
                GroupTypeMapper.teamTypeToLabel(detail.type)
            }
            val startMillis = parseDateMillis(detail.startDate)
            val endMillis = parseDateMillis(detail.endDate)
            if (startMillis != null && endMillis != null) {
                dateRangePickerState.setSelection(startMillis, endMillis)
            }
        }
    }

    if (uiState.isSuccess) {
        SsabreeDialog(
            onDismissRequest = {},
            title = "알림",
            message = if (isEditMode) "수정이 완료되었습니다." else "등록이 완료되었습니다.",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = {
                viewModel.resetResult()
                onSubmitSuccess()
            }
        )
    }

    uiState.errorMessage?.let { message ->
        SsabreeDialog(
            onDismissRequest = { viewModel.resetResult() },
            title = "오류",
            message = message,
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = { viewModel.resetResult() }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("확인", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = { Text("모집 기간 선택", modifier = Modifier.padding(16.dp)) },
                headline = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        Text("${sdf.format(Date(start))} - ${sdf.format(Date(end))}", modifier = Modifier.padding(horizontal = 16.dp))
                    } else if (start != null) {
                        Text("${sdf.format(Date(start))} - 종료일을 선택하세요", modifier = Modifier.padding(horizontal = 16.dp))
                    } else {
                        Text("종료일을 선택하세요", modifier = Modifier.padding(horizontal = 16.dp))
                    }
                },
                showModeToggle = false,
                modifier = Modifier.weight(1f)
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = topBarTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val startMillis = dateRangePickerState.selectedStartDateMillis
                            val endMillis = dateRangePickerState.selectedEndDateMillis
                            if (startMillis != null && endMillis != null) {
                                val campusId = uiState.campusId ?: return@TextButton
                                val apiSdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
                                val startDate = apiSdf.format(Date(startMillis))
                                val endDate = apiSdf.format(Date(endMillis))
                                val type = if (groupKind == GroupKind.STUDY) {
                                    GroupTypeMapper.studyLabelToApi(selectedCategory)
                                } else {
                                    GroupTypeMapper.teamLabelToApi(selectedCategory)
                                }
                                val info = GroupCreateInfo(
                                    title = title,
                                    type = type,
                                    capacity = memberCount,
                                    startDate = startDate,
                                    endDate = endDate,
                                    campusId = campusId,
                                    description = content
                                )
                                if (isEditMode && groupId != null) {
                                    val updateInfo = com.ssafy.ssabree.core.repository.model.GroupUpdateInfo(
                                        title = info.title,
                                        type = info.type,
                                        capacity = info.capacity,
                                        startDate = info.startDate,
                                        endDate = info.endDate,
                                        campusId = info.campusId,
                                        description = info.description
                                    )
                                    viewModel.update(groupId, updateInfo)
                                } else {
                                    viewModel.submit(info)
                                }
                            }
                        },
                        enabled = isSubmitEnabled && !uiState.isSubmitting
                    ) {
                        Text(
                            text = if (isEditMode) "저장" else "등록",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isSubmitEnabled)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
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
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 제목 섹션
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("제목", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("제목을 입력하세요", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            // 모집 분야 섹션
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("모집 분야", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    recruitmentFields.forEach { field ->
                        val isSelected = selectedCategory == field
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(40.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { selectedCategory = field }
                                )
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    text = field,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // 모집 기간 & 그룹 인원
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("모집 기간", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { showDatePicker = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                                Text(
                                    text = dateDisplayText,
                                    color = if (dateDisplayText == "기간 선택") Color.Gray else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                                if (dDayText.isNotEmpty() && dateDisplayText != "기간 선택") {
                                    Text(
                                        text = dDayText.trim().removeSurrounding("(", ")"),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("그룹 인원", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(onClick = { if (memberCount > 1) memberCount-- }) {
                                Text("—", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Text("$memberCount", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            TextButton(onClick = { memberCount++ }) {
                                Text("+", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }

            // 상세 내용 섹션
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("상세 내용", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                TextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("프로젝트 소개, 진행 방식, 커리큘럼 등 상세 내용을 입력해 주세요", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GroupWriteScreenPreview() {
    CompositionLocalProvider(LocalAppContainer provides FakeAppContainer()) {
        GroupWriteScreen()
    }
}

private fun parseDateMillis(raw: String): Long? {
    return runCatching {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        input.parse(raw)?.time
    }.getOrNull()
}
