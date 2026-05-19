package com.ssafy.ssabree.features.home.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.* 
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.Dp
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.repository.ImageRepository
import com.ssafy.ssabree.core.repository.model.BoardThumbModel
import com.ssafy.ssabree.core.repository.model.CampusMealModel
import com.ssafy.ssabree.core.repository.model.DDayModel
import com.ssafy.ssabree.core.repository.model.HomeModel
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.core.ui.RepoImage
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.home.component.TopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

private data class HomeBanner(
    val title: String,
    val subtitle: String,
    val gradientColors: List<Color>,
    val icon: ImageVector,
    val onClick: () -> Unit
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNotificationClick: () -> Unit = {},
    onMyPageClick: () -> Unit = {},
    onDdayClick: () -> Unit = {},
    onGroupClick: (GroupKind) -> Unit = {},
    onBoardClick: (Long) -> Unit = {} // onBoardClick 추가
) {
    val container = LocalAppContainer.current
    val viewModel: HomeViewModel = simpleViewModel {
        HomeViewModel(container.homeRepository, container.memberRepository)
    }
    val imageRepository = container.imageRepository
    val uiState by viewModel.uiState.collectAsState()

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.loadHome() }
    ) {
        HomeContent(
            uiState = uiState,
            onLocationSelected = viewModel::onLocationSelected,
            onMealImageClick = viewModel::onMealImageClick,
            onMealImageDismiss = viewModel::onMealImageDismiss,
            imageRepository = imageRepository,
            onNotificationClick = onNotificationClick,
            onMyPageClick = onMyPageClick,
            onDdayClick = onDdayClick,
            onGroupClick = onGroupClick,
            onBoardClick = onBoardClick // onBoardClick 전달
        )
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onLocationSelected: (String) -> Unit,
    onMealImageClick: (Int) -> Unit,
    onMealImageDismiss: () -> Unit,
    imageRepository: ImageRepository,
    onNotificationClick: () -> Unit = {},
    onMyPageClick: () -> Unit = {},
    onDdayClick: () -> Unit = {},
    onGroupClick: (GroupKind) -> Unit = {},
    onBoardClick: (Long) -> Unit = {} // onBoardClick 추가
) {
    val locationOptions = uiState.locationOptions.ifEmpty { listOf("Seoul") }
    val selectedLocation = uiState.selectedLocation.ifBlank { locationOptions.first() }
    val teamSubtitle = uiState.team?.let { team ->
        "${team.name}\n${team.count}명 모집 중"
    } ?: "모집 중인 팀이 없습니다"
    val studySubtitle = uiState.study?.let { study ->
        "${study.name}\n${study.count}명 모집 중"
    } ?: "모집 중인 스터디가 없습니다"

    val bannerItems = listOf(
        HomeBanner(
            title = "프로젝트",
            subtitle = teamSubtitle,
            gradientColors = listOf(
                Color(0xFF9FD7FF),
                Color(0xFFB59CFF)
            ),
            icon = Icons.Outlined.Groups,
            onClick = { onGroupClick(GroupKind.PROJECT) }
        ),
        HomeBanner(
            title = "스터디",
            subtitle = studySubtitle,
            gradientColors = listOf(
                Color(0xFF8FD5FF),
                Color(0xFF6BA9FF)
            ),
            icon = Icons.Outlined.CollectionsBookmark,
            onClick = { onGroupClick(GroupKind.STUDY) }
        )
    )

    // D-Day 아이템 리스트
    val dDayItems = uiState.dDays.ifEmpty { listOf(DDayModel("D-Day", 0)) }

    // 무한 루프를 위해 아주 큰 페이지 수를 설정하고 중앙 부근에서 시작합니다.
    val pageCount = Int.MAX_VALUE
    val initialPage = pageCount / 2 - (pageCount / 2 % dDayItems.size)
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageCount })
    val scrollState = rememberScrollState()

    // 자동 슬라이드 로직 (5초마다 전환)
    LaunchedEffect(Unit) {
        while (true) {
            yield()
            delay(5000)
            pagerState.animateScrollToPage(pagerState.currentPage + 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                onNotificationClick = onNotificationClick,
                onMyPageClick = onMyPageClick
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // 인사 영역 및 D-Day 슬라이더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "싸용자님, 안녕하세요!",
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "오늘도 싸브리타임과 함께 힘내요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }

                    // D-Day 슬라이드 배너 (HorizontalPager)
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .padding(start = 8.dp)
                            .clickable { onDdayClick() } // 클릭 이벤트 연결
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth(),
                            userScrollEnabled = false
                        ) { page ->
                            val item = dDayItems[page % dDayItems.size]
                            val dDayText = if (item.days >= 0) "D-${item.days}" else "D+${-item.days}"
                            Surface(
                                color = Color(0xFFEEF2FB),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarMonth,
                                        contentDescription = null,
                                        tint = Color(0xFF5B7FFF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.title,
                                            color = Color(0xFF5B7FFF),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = dDayText,
                                            color = Color(0xFF5B7FFF),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 상단 배너 영역
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    bannerItems.forEach { banner ->
                        HomeGradientCard(
                            title = banner.title,
                            subtitle = banner.subtitle,
                            gradientColors = banner.gradientColors,
                            icon = banner.icon,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(onClick = banner.onClick)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 점심 메뉴 섹션
                Text(
                    text = "점심 메뉴",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(10.dp))

                LunchSection(
                    locationOptions = locationOptions,
                    selectedLocation = selectedLocation,
                    onLocationSelected = onLocationSelected,
                    onMealImageClick = onMealImageClick,
                    mealImageUrls = uiState.selectedMealImageUrls,
                    imageRepository = imageRepository,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 전체 게시판 섹션
                Text(
                    text = "전체 게시판",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                BoardListSection(
                    boards = uiState.boards,
                    modifier = Modifier.fillMaxWidth(),
                    onBoardClick = onBoardClick // onBoardClick 전달
                )

                Spacer(modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        EnlargedMealImageOverlay(
            imageUrls = uiState.selectedMealImageUrls,
            startIndex = uiState.enlargedMealImageIndex,
            imageRepository = imageRepository,
            onDismiss = onMealImageDismiss
        )
    }
}

@Composable
private fun HomeGradientCard(
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(gradientColors),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "More",
                    tint = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
private fun BoardListSection(
    boards: List<BoardThumbModel>,
    modifier: Modifier = Modifier,
    onBoardClick: (Long) -> Unit // onBoardClick 추가
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (boards.isEmpty()) {
                BoardListItem(
                    title = "게시판 목록이 없습니다",
                    subtitle = "최근 게시물 없음",
                    showDivider = false
                )
            } else {
                boards.forEachIndexed { index, board ->
                    BoardListItem(
                        title = board.name,
                        subtitle = board.recentPostTitle ?: "최근 게시물이 없습니다.",
                        showDivider = index != boards.lastIndex,
                        boardId = board.id, // boardId 전달
                        onBoardClick = onBoardClick // onBoardClick 전달
                    )
                }
            }
        }
    }
}

@Composable
private fun BoardListItem(
    title: String,
    subtitle: String,
    showDivider: Boolean,
    boardId: Long = 0L, // boardId 추가
    onBoardClick: (Long) -> Unit = {} // onBoardClick 추가
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .clickable { onBoardClick(boardId) } // 클릭 이벤트 연결
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            }
        }
        if (showDivider) {
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun LunchSection(
    locationOptions: List<String>,
    selectedLocation: String,
    onLocationSelected: (String) -> Unit,
    onMealImageClick: (Int) -> Unit,
    mealImageUrls: List<String>,
    imageRepository: ImageRepository,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp)
        ) {
            // 캠퍼스 선택
            Text(
                text = "캠퍼스",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(locationOptions) { option ->
                    FilterChip(
                        selected = selectedLocation == option,
                        onClick = { onLocationSelected(option) },
                        label = {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        border = if (selectedLocation == option) null
                        else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 선택된 캠퍼스 정보
            Text(
                text = "$selectedLocation 캠퍼스 오늘의 점심",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "표시된 메뉴는 실제 식단과 다를 수 있습니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 점심 이미지 가로 스크롤
            if (mealImageUrls.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "등록된 점심 이미지가 없습니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                val listState = rememberLazyListState()
                val indicatorIndex by remember {
                    derivedStateOf {
                        val visible = listState.layoutInfo.visibleItemsInfo
                        if (visible.isEmpty()) 0 else visible.first().index
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LazyRow(
                        state = listState,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScrollbar(listState, knobColor = MaterialTheme.colorScheme.primary)
                    ) {
                        items(mealImageUrls.size) { index ->
                            val imageUrl = mealImageUrls[index]
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (index != 0) {
                                    Box(
                                        modifier = Modifier
                                            .height(260.dp)
                                            .width(1.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                                RepoImage(
                                    url = imageUrl,
                                    imageRepository = imageRepository,
                                    contentDescription = "점심 메뉴 이미지",
                                    modifier = Modifier
                                        .width(160.dp)
                                        .height(300.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onMealImageClick(index) },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.horizontalScrollbar(
    state: androidx.compose.foundation.lazy.LazyListState,
    height: Dp = 6.dp,
    knobColor: Color = Color.Gray,
    trackColor: Color = Color.Transparent
): Modifier = composed {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0.3f
    val duration = if (state.isScrollInProgress) 150 else 500
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration)
    )
    val density = LocalDensity.current
    drawWithContent {
        drawContent()
        val layoutInfo = state.layoutInfo
        val firstVisible = layoutInfo.visibleItemsInfo.firstOrNull() ?: return@drawWithContent
        val itemSizePx = firstVisible.size.toFloat()
        if (itemSizePx <= 0f) return@drawWithContent
        val spacingPx = with(density) { 10.dp.toPx() }
        val totalItems = layoutInfo.totalItemsCount
        if (totalItems <= 1) return@drawWithContent

        val viewportWidth = size.width
        val totalContentWidth = (itemSizePx + spacingPx) * totalItems - spacingPx
        if (totalContentWidth <= 0f) return@drawWithContent

        val thumbWidth = (viewportWidth * (viewportWidth / totalContentWidth))
            .coerceAtLeast(with(density) { 24.dp.toPx() })
        val maxOffset = (viewportWidth - thumbWidth).coerceAtLeast(0f)
        val scrollOffsetPx = state.firstVisibleItemIndex * (itemSizePx + spacingPx) +
            state.firstVisibleItemScrollOffset.toFloat()
        val scrollFraction = (scrollOffsetPx / (totalContentWidth - viewportWidth)).coerceIn(0f, 1f)
        val offsetX = maxOffset * scrollFraction

        val barHeight = with(density) { height.toPx() }
        val topLeft = Offset(0f, size.height - barHeight)
        drawRoundRect(
            color = trackColor,
            topLeft = topLeft,
            size = Size(viewportWidth, barHeight),
            cornerRadius = CornerRadius(barHeight / 2),
            alpha = alpha
        )
        drawRoundRect(
            color = knobColor,
            topLeft = Offset(offsetX, size.height - barHeight),
            size = Size(thumbWidth, barHeight),
            cornerRadius = CornerRadius(barHeight / 2),
            alpha = alpha
        )
    }
}

@Composable
private fun EnlargedMealImageOverlay(
    imageUrls: List<String>,
    startIndex: Int?,
    imageRepository: ImageRepository,
    onDismiss: () -> Unit
) {
    if (startIndex == null || imageUrls.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount = { imageUrls.size }
    )
    val scope = rememberCoroutineScope()

    BackHandler(enabled = true) { onDismiss() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onDismiss() }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            ) { page ->
                RepoImage(
                    url = imageUrls[page],
                    imageRepository = imageRepository,
                    contentDescription = "확대된 점심 이미지",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            IconButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 16.dp)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(
                onClick = {
                    val prev = (pagerState.currentPage - 1).coerceAtLeast(0)
                    if (prev != pagerState.currentPage) {
                        scope.launch { pagerState.animateScrollToPage(prev) }
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NavigateBefore,
                    contentDescription = "이전",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                onClick = {
                    val next = (pagerState.currentPage + 1).coerceAtMost(imageUrls.lastIndex)
                    if (next != pagerState.currentPage) {
                        scope.launch { pagerState.animateScrollToPage(next) }
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NavigateNext,
                    contentDescription = "다음",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewHomeContent() {
    val container = LocalAppContainer.current
    val viewModel: HomeViewModel = simpleViewModel {
        HomeViewModel(container.homeRepository, container.memberRepository)
    }
    HomeContent(
        uiState = HomeUiState(
            selectedLocation = "서울",
            locationOptions = listOf("서울", "대전", "광주", "부산"),
            dDays = HomeModel.sample.dDays,
            team = HomeModel.sample.team,
            study = HomeModel.sample.study,
            campusMeals = HomeModel.sample.campusMeals,
            boards = HomeModel.sample.boards
        ),
        onLocationSelected = {},
        onMealImageClick = {},
        onMealImageDismiss = {},
        imageRepository = container.imageRepository
    )
}
