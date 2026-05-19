package com.ssafy.ssabree.features.mygroup.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.ssabree.app.FakeAppContainer
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.R
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.core.utils.RetrofitClient
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.group.model.GroupTypeMapper
import com.ssafy.ssabree.features.mygroup.model.MyGroupItemUiModel
import coil.compose.AsyncImage
import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MyGroupScreen(
    groupKind: GroupKind = GroupKind.STUDY,
    shouldRefresh: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onMyApplicationsClick: () -> Unit = {},
    onSearchModeChange: (Boolean) -> Unit = {},
    onDetailClick: (Long, Boolean) -> Unit = { _, _ -> }
) {
    val container = LocalAppContainer.current
    val viewModel: MyGroupViewModel = simpleViewModel(key = "mygroup-${groupKind.routeValue}") {
        MyGroupViewModel(container.groupRepository, container.keywordRepository, container.memberRepository, groupKind)
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
    LaunchedEffect(isSearchMode) {
        onSearchModeChange(isSearchMode)
    }
    DisposableEffect(Unit) {
        onDispose { onSearchModeChange(false) }
    }

    val filters = remember(groupKind) {
        if (groupKind == GroupKind.STUDY) {
            GroupTypeMapper.studyFilterLabels()
        } else {
            GroupTypeMapper.teamFilterLabels()
        }
    }

    val visibleGroups = remember(uiState.filteredGroups, committedQuery, isSearchMode) {
        if (!isSearchMode) {
            uiState.filteredGroups
        } else if (committedQuery.isBlank()) {
            emptyList()
        } else {
            uiState.filteredGroups.filter { it.title.contains(committedQuery, ignoreCase = true) }
        }
    }

    LaunchedEffect(searchQuery) {
        viewModel.clearMinLengthError()
    }

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
                val title = if (groupKind == GroupKind.STUDY) "나의 스터디" else "나의 프로젝트"
                TopAppBar(
                    title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchMode = true }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = onMyApplicationsClick) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Assignment, contentDescription = "My Applications")
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
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (isSearchMode) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                }

                if (!isSearchMode) {
                    MyGroupHeaderRow(groupKind = groupKind)
                    MyGroupFilterRow(
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
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(visibleGroups, key = { it.id }) { group ->
                            MyGroupCard(group = group, onDetailClick = onDetailClick)
                        }
                        
                        if (visibleGroups.isEmpty() && isSubmittedQuery) {
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
}

@Composable
private fun MyGroupHeaderRow(groupKind: GroupKind) {
    val title = if (groupKind == GroupKind.STUDY) "스터디" else "프로젝트"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "참여 중",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MyGroupFilterRow(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
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
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = null,
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

@Composable
fun MyGroupCard(group: MyGroupItemUiModel, onDetailClick: (Long, Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onDetailClick(group.id, group.isLeader) },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = group.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (group.isLeader) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = group.role,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (group.isLeader) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = group.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${group.currentMembers}명",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val profileItems = remember(group.memberProfileImageUrls, group.currentMembers) {
                val maxCount = minOf(group.currentMembers.coerceAtLeast(1), 4)
                val urls = group.memberProfileImageUrls.take(maxCount)
                urls + List(maxCount - urls.size) { "" }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                profileItems.forEach { profileUrl ->
                    if (profileUrl.isBlank()) {
                        Icon(
                            painter = painterResource(id = R.drawable.profile),
                            contentDescription = "기본 프로필 이미지",
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape),
                            tint = Color.Unspecified
                        )
                    } else {
                        AsyncImage(
                            model = normalizeImageUrl(profileUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Preview(showBackground = true)
@Composable
fun MyGroupScreenPreview() {
    CompositionLocalProvider(LocalAppContainer provides FakeAppContainer()) {
        MyGroupScreen(groupKind = GroupKind.STUDY)
    }
}
