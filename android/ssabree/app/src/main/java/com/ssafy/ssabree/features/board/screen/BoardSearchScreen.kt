package com.ssafy.ssabree.features.board.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.ssafy.ssabree.app.FakeAppContainer
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.features.board.component.PostItemView
import com.ssafy.ssabree.features.board.model.PostUiModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BoardSearchScreen(
    onBackClick: () -> Unit = {},
    onPostClick: (PostUiModel) -> Unit = {}
) {
    val container = LocalAppContainer.current
    val viewModel: BoardSearchViewModel = simpleViewModel {
        BoardSearchViewModel(
            postRepository = container.postRepository,
            keywordRepository = container.keywordRepository
        )
    }
    val uiState by viewModel.uiState.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // 인기 검색어 (API 미구현으로 주석 처리)
    // val popularSearches = listOf("점심", "삼성", "싸월급", "자취", "두쪽쿠", "맛집", "SW검정시험", "A형")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 상단 검색바 영역
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            // 커스텀 검색창 (테마 반영)
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = uiState.query,
                        onValueChange = { viewModel.onQueryChange(it) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                viewModel.onSearchSubmit()
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        ),
                        modifier = Modifier
                            .weight(1f),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        decorationBox = { innerTextField ->
                            if (uiState.query.isEmpty()) {
                                Text(
                                    text = "검색어를 입력하세요",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (uiState.query.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onQueryChange("") },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "지우기",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            val isSubmittedQuery =
                uiState.lastSubmittedQuery.isNotEmpty() &&
                    uiState.query.trim() == uiState.lastSubmittedQuery

            if ((!isSubmittedQuery && uiState.recentKeywords.isNotEmpty()) || uiState.showMinLengthError) {
                // 최근 검색어 섹션
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
                                onDelete = {
                                    viewModel.deleteRecentKeyword(keyword)
                                },
                                onClick = {
                                    viewModel.onQuickSearch(keyword)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // 인기 검색어 섹션 (API 미구현으로 주석 처리)
                /*
                Text(
                    text = "인기 검색어",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 한 줄에 여러 개씩 자동 줄바꿈 배치 (예시 데이터)
                val rows = popularSearches.chunked(4)
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { keyword ->
                            PopularSearchChip(
                                text = keyword,
                                onClick = {
                                    triggerSearch(keyword)
                                }
                            )
                        }
                    }
                }
                */

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isSubmittedQuery && !uiState.showMinLengthError) {
                if (uiState.isLoading && uiState.results.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (!uiState.isLoading && uiState.results.isEmpty()) {
                    // 검색 결과가 없을 때
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "검색 결과가 없습니다.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            items = uiState.results,
                            key = { _, item -> item.id }
                        ) { index, post ->
                            viewModel.loadMoreIfNeeded(index)
                            PostItemView(
                                post = post,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                onClick = { onPostClick(post) }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }

                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }

                if (uiState.error != null) {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
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

@Composable
fun PopularSearchChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary, // 테마의 주요 색상 사용
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BoardSearchScreenPreview() {
    MaterialTheme {
        CompositionLocalProvider(
            LocalAppContainer provides FakeAppContainer()
        ) {
            BoardSearchScreen()
        }
    }
}
