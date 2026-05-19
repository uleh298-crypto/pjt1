package com.ssafy.ssabree.features.mygroup.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeBottomSheet
import com.ssafy.ssabree.core.designsystem.component.SsabreeBottomSheetItem
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.group.model.GroupTypeMapper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.HorizontalDivider

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MyGroupAllScreen(
    shouldRefresh: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onMyApplicationsClick: (GroupKind) -> Unit = {},
    onSearchModeChange: (Boolean) -> Unit = {},
    onDetailClick: (GroupKind, Long, Boolean) -> Unit = { _, _, _ -> }
) {
    val container = LocalAppContainer.current
    val studyViewModel: MyGroupViewModel = simpleViewModel(key = "mygroup-all-study") {
        MyGroupViewModel(container.groupRepository, container.keywordRepository, container.memberRepository, GroupKind.STUDY)
    }
    val projectViewModel: MyGroupViewModel = simpleViewModel(key = "mygroup-all-project") {
        MyGroupViewModel(container.groupRepository, container.keywordRepository, container.memberRepository, GroupKind.PROJECT)
    }

    val studyState by studyViewModel.uiState.collectAsState()
    val projectState by projectViewModel.uiState.collectAsState()

    var isSearchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var committedQuery by remember { mutableStateOf("") }
    var showMyApplicationsSheet by remember { mutableStateOf(false) }
    val applicationsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            studyViewModel.load()
            projectViewModel.load()
            onRefreshConsumed()
        }
    }

    val normalizedQuery = committedQuery.trim()
    val isSubmittedQuery = normalizedQuery.isNotBlank()
    val isValidQuery = normalizedQuery.length >= 2
    val showMinLengthMessage = isSearchMode && (searchQuery.isNotBlank() || committedQuery.isNotBlank()) && !isValidQuery
    val recentKeywords = studyState.recentKeywords

    val visibleProjectGroups = remember(projectState.filteredGroups, normalizedQuery, isSearchMode) {
        if (!isSearchMode) {
            projectState.filteredGroups
        } else if (!isValidQuery) {
            emptyList()
        } else {
            projectState.filteredGroups.filter { it.title.contains(normalizedQuery, ignoreCase = true) }
        }
    }
    val visibleStudyGroups = remember(studyState.filteredGroups, normalizedQuery, isSearchMode) {
        if (!isSearchMode) {
            studyState.filteredGroups
        } else if (!isValidQuery) {
            emptyList()
        } else {
            studyState.filteredGroups.filter { it.title.contains(normalizedQuery, ignoreCase = true) }
        }
    }

    fun exitSearchMode() {
        isSearchMode = false
        searchQuery = ""
        committedQuery = ""
    }

    BackHandler(enabled = isSearchMode) {
        exitSearchMode()
    }

    LaunchedEffect(isSearchMode) {
        onSearchModeChange(isSearchMode)
    }
    DisposableEffect(Unit) {
        onDispose { onSearchModeChange(false) }
    }

    val isRefreshing = studyState.isLoading || projectState.isLoading

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (isSearchMode) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { exitSearchMode() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = {
                                        committedQuery = searchQuery.trim()
                                        studyViewModel.onSearchSubmit(searchQuery)
                                    }),
                                    decorationBox = { innerTextField ->
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                "검색어를 입력하세요",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                fontSize = 15.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                        Icon(Icons.Default.Close, "Clear", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            } else {
                TopAppBar(
                    title = { Text(text = "나의 그룹", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    actions = {
                        IconButton(onClick = { isSearchMode = true }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showMyApplicationsSheet = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Assignment,
                                contentDescription = "My Applications"
                            )
                        }
                    },
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                studyViewModel.load()
                projectViewModel.load()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (isSearchMode) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                    if (recentKeywords.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Text(
                                text = "최근 검색어",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                recentKeywords.forEach { keyword ->
                                    RecentSearchChip(
                                        text = keyword,
                                        onDelete = { studyViewModel.deleteRecentKeyword(keyword) },
                                        onClick = { searchQuery = keyword }
                                    )
                                }
                            }
                        }
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (showMinLengthMessage) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = "두 글자 이상 입력해 주세요.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            GroupSectionHeader(title = "프로젝트")
                        }
                    if (!isSearchMode) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            GroupFilterRow(
                                filters = GroupTypeMapper.teamFilterLabels(),
                                selectedFilter = projectState.selectedFilter,
                                onFilterSelected = projectViewModel::onFilterSelected
                            )
                        }
                    }
                    if (visibleProjectGroups.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyGroupText()
                        }
                    } else {
                        items(visibleProjectGroups, key = { it.id }) { group ->
                            MyGroupCard(group = group) { groupId, isLeader ->
                                onDetailClick(GroupKind.PROJECT, groupId, isLeader)
                            }
                        }
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(modifier = Modifier.height(8.dp))
                        GroupSectionHeader(title = "스터디")
                    }
                    if (!isSearchMode) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            GroupFilterRow(
                                filters = GroupTypeMapper.studyFilterLabels(),
                                selectedFilter = studyState.selectedFilter,
                                onFilterSelected = studyViewModel::onFilterSelected
                            )
                        }
                    }
                    if (visibleStudyGroups.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyGroupText()
                        }
                    } else {
                        items(visibleStudyGroups, key = { it.id }) { group ->
                            MyGroupCard(group = group) { groupId, isLeader ->
                                onDetailClick(GroupKind.STUDY, groupId, isLeader)
                            }
                        }
                    }
                    }
                }
            }
        }
    }

    if (showMyApplicationsSheet) {
        SsabreeBottomSheet(
            onDismissRequest = { showMyApplicationsSheet = false },
            sheetState = applicationsSheetState,
            title = "내 지원 내역"
        ) {
            SsabreeBottomSheetItem(
                text = "프로젝트 지원 내역",
                showRadioButton = false,
                onClick = {
                    showMyApplicationsSheet = false
                    onMyApplicationsClick(GroupKind.PROJECT)
                }
            )
            SsabreeBottomSheetItem(
                text = "스터디 지원 내역",
                showRadioButton = false,
                onClick = {
                    showMyApplicationsSheet = false
                    onMyApplicationsClick(GroupKind.STUDY)
                }
            )
        }
    }
}

@Composable
private fun GroupSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    )
}

@Composable
private fun GroupFilterRow(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filters.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(text = filter, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = null
                )
            }
        }
    }
}

@Composable
private fun EmptyGroupText() {
    Text(
        text = "아직 가입한 그룹이 없습니다.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
