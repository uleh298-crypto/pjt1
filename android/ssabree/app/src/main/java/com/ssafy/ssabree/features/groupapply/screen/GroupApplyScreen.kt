package com.ssafy.ssabree.features.groupapply.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import coil.ImageLoader
import coil.decode.SvgDecoder
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.app.FakeAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.repository.model.GroupApplyInfo
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.mypage.screen.InfoCard
import com.ssafy.ssabree.features.mypage.screen.MyPageViewModel
import com.ssafy.ssabree.features.mypage.screen.StackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupApplyScreen(
    groupId: Long,
    groupKind: GroupKind,
    initialTitle: String = "",
    initialContent: String = "",
    initialPosition: String = "",
    shouldRefreshPortfolio: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onSubmitClick: () -> Unit = {},
    onPortfolioDetailClick: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val container = LocalAppContainer.current
    val viewModel: GroupApplyViewModel = simpleViewModel(key = "group-apply-${groupKind.routeValue}-$groupId") {
        GroupApplyViewModel(container.groupRepository, groupKind, groupId)
    }
    val uiState by viewModel.uiState.collectAsState()

    val portfolioViewModel: PortfolioIdViewModel = simpleViewModel(key = "portfolio-id") {
        PortfolioIdViewModel(container.portfolioRepository)
    }
    val portfolioUiState by portfolioViewModel.uiState.collectAsState()

    val myPageViewModel: MyPageViewModel = simpleViewModel(key = "group-apply-mypage") {
        MyPageViewModel(
            container.memberRepository,
            container.portfolioRepository,
            container.projectRepository,
            null
        )
    }
    val myPageUiState by myPageViewModel.uiState.collectAsState()
    val stackViewModel: StackViewModel = simpleViewModel {
        StackViewModel(container.stackRepository)
    }
    val stackUiState by stackViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val svgImageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }
    val stackImageByName = remember(stackUiState.stacks) {
        stackUiState.stacks.associate { it.name to it.imgUrl }
    }

    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var content by rememberSaveable { mutableStateOf(initialContent) }
    var position by rememberSaveable { mutableStateOf(initialPosition) }

    LaunchedEffect(initialTitle, initialContent, initialPosition) {
        if (title.isBlank() && initialTitle.isNotBlank()) title = initialTitle
        if (content.isBlank() && initialContent.isNotBlank()) content = initialContent
        if (position.isBlank() && initialPosition.isNotBlank()) position = initialPosition
    }

    // 포트폴리오 새로고침
    LaunchedEffect(shouldRefreshPortfolio) {
        if (shouldRefreshPortfolio) {
            myPageViewModel.loadMyPage()
            onRefreshConsumed()
        }
    }

    val portfolioIdValue = portfolioUiState.portfolioId
    val isSubmitEnabled =
        title.isNotBlank() && content.isNotBlank() && position.isNotBlank() && (portfolioIdValue ?: 0L) > 0L

    if (uiState.isSuccess) {
        SsabreeDialog(
            onDismissRequest = {},
            title = "알림",
            message = "지원이 완료되었습니다.",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = {
                viewModel.resetResult()
                onSubmitClick()
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

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "지원하기",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val info = GroupApplyInfo(
                                portfolioId = portfolioIdValue ?: 0L,
                                title = title,
                                message = content,
                                position = position
                            )
                            viewModel.submit(info)
                        },
                        enabled = isSubmitEnabled && !uiState.isSubmitting
                    ) {
                        Text(
                            text = "등록",
                            color = if (isSubmitEnabled && !uiState.isSubmitting) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("제목", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("제목을 입력하세요", color = MaterialTheme.colorScheme.onSurfaceVariant) },
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

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("상세 내용", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                TextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("상세 내용을 입력하세요", color = MaterialTheme.colorScheme.onSurfaceVariant) },
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

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("포지션", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                TextField(
                    value = position,
                    onValueChange = { position = it },
                    placeholder = { Text("예: BE", color = MaterialTheme.colorScheme.onSurfaceVariant) },
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

            InfoCard(
                summary = myPageUiState.myPage?.portfolioSummary,
                onPortfolioDetailClick = { onPortfolioDetailClick(title, content, position) },
                stackImageByName = stackImageByName,
                imageLoader = svgImageLoader
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GroupApplyScreenPreview() {
    CompositionLocalProvider(LocalAppContainer provides FakeAppContainer()) {
        GroupApplyScreen(groupId = 1L, groupKind = GroupKind.STUDY)
    }
}
