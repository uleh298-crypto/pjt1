package com.ssafy.ssabree.features.group.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.group.model.GroupListItemUiModel
import com.ssafy.ssabree.features.group.model.GroupTypeMapper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GroupScreen(
    groupKind: GroupKind = GroupKind.STUDY,
    shouldRefresh: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onFabClick: (GroupKind) -> Unit = {},
    onDetailClick: (Long) -> Unit = {}
) {
    val container = LocalAppContainer.current
    val viewModel: GroupViewModel = simpleViewModel(key = "group-list-${groupKind.routeValue}") {
        GroupViewModel(
            container.groupRepository,
            container.keywordRepository,
            container.memberRepository,
            container.campusRepository,
            groupKind
        )
    }

    val uiState by viewModel.uiState.collectAsState()

    // 외부에서 새로고침 요청 시 데이터 리로드
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.load()
            onRefreshConsumed()
        }
    }
    
    // 검색 모드 상태 관리
    var isSearchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var committedQuery by remember { mutableStateOf("") }
    
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    fun exitSearchMode() {
        isSearchMode = false
        searchQuery = ""
        committedQuery = ""
        viewModel.clearMinLengthError()
        focusManager.clearFocus()
    }
    BackHandler(enabled = isSearchMode) {
        exitSearchMode()
    }

    val filters = remember(groupKind) {
        if (groupKind == GroupKind.STUDY) {
            GroupTypeMapper.studyFilterLabels()
        } else {
            GroupTypeMapper.teamFilterLabels()
        }
    }

    // 검색어에 따른 필터링 결과 (검색 모드일 때만 검색어 적용)
    val visibleGroups = remember(uiState.filteredGroups, committedQuery, isSearchMode) {
        if (!isSearchMode) {
            uiState.filteredGroups
        } else if (committedQuery.isBlank()) {
            emptyList()
        } else {
            uiState.filteredGroups.filter { it.title.contains(committedQuery, ignoreCase = true) }
        }
    }
    val orderedGroups = remember(visibleGroups) {
        val (active, closed) = visibleGroups.partition { !it.isClosed }
        active + closed
    }

    LaunchedEffect(searchQuery) {
        viewModel.clearMinLengthError()
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { 
                                exitSearchMode()
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                            
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(onSearch = {
                                            viewModel.onSearchSubmit(searchQuery)
                                            committedQuery = searchQuery.trim()
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                        }),
                                        decorationBox = { innerTextField ->
                                            if (searchQuery.isEmpty()) {
                                                Text("검색어를 입력하세요", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 15.sp)
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
                    val title = if (groupKind == GroupKind.STUDY) "스터디" else "프로젝트"
                    TopAppBar(
                        title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)},
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = { isSearchMode = true }) {
                                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                            }
                        },
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
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
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isSearchMode) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                    }

                    if (!isSearchMode) {
                        GroupHeaderRow(groupKind = groupKind)
                        FilterRow(
                            filters = filters,
                            selectedFilter = uiState.selectedFilter,
                            onFilterSelected = viewModel::onFilterSelected
                        )
                    } else {
                    }

                    val isSubmittedQuery =
                        committedQuery.isNotEmpty() && searchQuery.trim() == committedQuery

                    if (isSearchMode && (!isSubmittedQuery || uiState.showMinLengthError)) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            if (uiState.showMinLengthError) {
                                Text(
                                    text = "두 글자 이상 입력해 주세요.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            if (uiState.recentKeywords.isNotEmpty()) {
                                Text(
                                    text = "최근 검색어",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 2,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    uiState.recentKeywords.forEach { keyword ->
                                        RecentSearchChip(
                                            text = keyword,
                                            onDelete = { viewModel.deleteRecentKeyword(keyword) },
                                            onClick = { searchQuery = keyword }
                                        )
                                    }
                                }
                            }
                        }
                    } else if (!isSearchMode || isSubmittedQuery) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding(),
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(orderedGroups, key = { it.id }) { group ->
                                GroupCard(group = group, onDetailClick = onDetailClick)
                            }

                            if (orderedGroups.isEmpty() && isSubmittedQuery) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 40.dp), contentAlignment = Alignment.Center) {
                                        Text("검색 결과가 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!isSearchMode) {
            FloatingActionButton(
                onClick = { onFabClick(groupKind) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 75.dp)
                    .size(64.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(36.dp))
            }
        }
    }
}

@Composable
private fun GroupHeaderRow(groupKind: GroupKind) {
    val title = if (groupKind == GroupKind.STUDY) "스터디" else "프로젝트"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "모집", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FilterRow(filters: List<String>, selectedFilter: String, onFilterSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        filters.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(text = filter, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary),
                border = null,
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

@Composable
fun GroupCard(group: GroupListItemUiModel, onDetailClick: (Long) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onDetailClick(group.id) },
        enabled = !group.isClosed,
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp))
                Column {
                    Text(text = group.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = group.categoryLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = group.dDay, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${group.currentMembers}/${group.maxMembers}명",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            group.closedMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RecentSearchChip(
    text: String,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "삭제",
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onDelete() },
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
