@file:OptIn(ExperimentalMaterial3Api::class)

package com.ssafy.ssabree.features.board.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.ssabree.core.designsystem.theme.SsabreeTheme
import com.ssafy.ssabree.app.FakeAppContainer
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.features.board.component.PostItemView
import com.ssafy.ssabree.features.board.model.PostUiModel


@Composable
fun BoardScreen(
    modifier: Modifier = Modifier,
    initialBoardId: Long? = null, // 초기 boardId를 받을 수 있도록 수정
    shouldRefresh: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
    onWriteClick: () -> Unit = {},
    onPostClick: (PostUiModel) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val container = LocalAppContainer.current
    val viewModel: BoardViewModel = simpleViewModel {
        BoardViewModel(
            postRepository = container.postRepository,
            boardRepository = container.boardRepository,
            initialBoardId = initialBoardId // initialBoardId 전달
        )
    }

    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // 게시글 작성/수정 후 새로고침 트리거
    LaunchedEffect(shouldRefresh, initialBoardId) {
        if (shouldRefresh || initialBoardId != null) { // initialBoardId가 변경될 때도 새로고침
            viewModel.onRefresh(initialBoardId)
            onRefreshConsumed()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        ),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "게시판", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onWriteClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "글쓰기")
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.onRefresh() },
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    PaddingValues(
                        start = padding.calculateStartPadding(LayoutDirection.Ltr),
                        end = padding.calculateEndPadding(LayoutDirection.Ltr),
                        top = padding.calculateTopPadding(),
                        bottom = 0.dp
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HotChipButton(
                        selected = uiState.isHotSelected,
                        onClick = viewModel::onHotSelected
                    )
                    Box {
                        FilterChipButton(
                            text = uiState.selectedFilter.label,
                            selected = !uiState.isHotSelected,
                            onClick = viewModel::onFilterChipClick
                        )
                        DropdownMenu(
                            expanded = uiState.filterMenuExpanded,
                            onDismissRequest = {
                                focusManager.clearFocus()
                                viewModel.onFilterMenuDismiss()
                            },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        ) {
                            uiState.filterOptions.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.label, fontSize = 14.sp) },
                                    onClick = {
                                        focusManager.clearFocus()
                                        viewModel.onFilterSelected(item)
                                    },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    uiState.noticeContent?.takeIf { it.isNotBlank() }?.let { notice ->
                        item {
                            AdminNoticeCard(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                                title = "관리자 공지사항",
                                body = notice
                            )
                        }
                    }

                    itemsIndexed(
                        items = uiState.posts,
                        key = { _, post -> post.id }
                    ) { index, post ->
                        viewModel.onListEndReached(index)

                        PostItemView(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            post = post,
                            onClick = { onPostClick(post) }
                        )
                        if (post != uiState.posts.last()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }
                    }

                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HotChipButton(
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = background,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .defaultMinSize(minHeight = 36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Whatshot,
                contentDescription = null,
                tint = contentColor
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Hot",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            )
        }
    }
}

@Composable
private fun FilterChipButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = background,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .defaultMinSize(minHeight = 36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = if (selected) contentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AdminNoticeCard(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

@Preview
@Composable
fun BoardScreenPreview() {
    CompositionLocalProvider(LocalAppContainer provides FakeAppContainer()) {
        SsabreeTheme {
            BoardScreen(
                modifier = Modifier,
                onWriteClick = {},
                onPostClick = {}
            )
        }
    }
}
