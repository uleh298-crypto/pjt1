package com.ssafy.ssabree.features.mypage.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.app.FakeAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.core.repository.model.PortfolioImageUpdateInfo
import com.ssafy.ssabree.core.repository.model.PortfolioStackUpdateInfo
import com.ssafy.ssabree.core.repository.model.PortfolioUrlUpdateInfo
import com.ssafy.ssabree.core.repository.model.ProjectModel
import com.ssafy.ssabree.core.repository.model.StackModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.ssafy.ssabree.core.utils.RetrofitClient
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PortfolioDetailScreen(
    onBackClick: () -> Unit = {},
    onProjectCreateClick: (Long) -> Unit = { _ -> },
    onProjectEditClick: (Long, Long) -> Unit = { _, _ -> },
    refreshSignal: StateFlow<Boolean>? = null,
    onRefreshConsumed: () -> Unit = {}
) {
    val container = LocalAppContainer.current
    val viewModel: PortfolioDetailViewModel = simpleViewModel {
        PortfolioDetailViewModel(container.portfolioRepository, container.projectRepository)
    }
    val uiState by viewModel.uiState.collectAsState()
    val portfolio = uiState.portfolio

    val stackViewModel: StackViewModel = simpleViewModel {
        StackViewModel(container.stackRepository)
    }
    val stackUiState by stackViewModel.uiState.collectAsState()
    val stackImageByName = remember(stackUiState.stacks) {
        stackUiState.stacks.associate { it.name to it.imgUrl }
    }
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val uploadRepository = container.uploadRepository
    val coroutineScope = rememberCoroutineScope()
    val svgImageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current

    var isEditMode by rememberSaveable { mutableStateOf(false) }
    var showBackConfirmDialog by remember { mutableStateOf(false) }

    // 포트폴리오 데이터 상태
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var introduction by rememberSaveable { mutableStateOf("") }
    var solvedacRank by rememberSaveable { mutableStateOf("") }
    var solvedacTierName by rememberSaveable { mutableStateOf("") }
    var solvedacTierImageUrl by rememberSaveable { mutableStateOf("") }
    var solvedacSolvedCount by rememberSaveable { mutableStateOf("") }
    var solvedacHandle by rememberSaveable { mutableStateOf("") }
    var solvedacVerifyError by rememberSaveable { mutableStateOf<String?>(null) }
    var isSolvedacVerifying by rememberSaveable { mutableStateOf(false) }
    var swTestRank by rememberSaveable { mutableStateOf("") }
    var isVisible by rememberSaveable { mutableStateOf(true) }
    val stacks = rememberSaveable(saver = stackListSaver) { mutableStateListOf<StackItem>() }
    val urls = rememberSaveable(saver = urlListSaver) { mutableStateListOf<UrlItem>() }
    val images = rememberSaveable(saver = imageListSaver) { mutableStateListOf<ImageItem>() }
    var isUploadingImage by remember { mutableStateOf(false) }
    var imageUploadError by remember { mutableStateOf<String?>(null) }

    // 수정을 취소할 때를 대비한 백업 상태
    var backupTitle by rememberSaveable { mutableStateOf("") }
    var backupDescription by rememberSaveable { mutableStateOf("") }
    var backupIntroduction by rememberSaveable { mutableStateOf("") }
    var backupSolvedacHandle by rememberSaveable { mutableStateOf("") }
    var backupSolvedacRank by rememberSaveable { mutableStateOf("") }
    var backupSolvedacTierName by rememberSaveable { mutableStateOf("") }
    var backupSolvedacTierImageUrl by rememberSaveable { mutableStateOf("") }
    var backupSolvedacSolvedCount by rememberSaveable { mutableStateOf("") }
    var backupSwTestRank by rememberSaveable { mutableStateOf("") }
    var backupIsVisible by rememberSaveable { mutableStateOf(true) }
    var backupStacks by rememberSaveable(saver = stackItemListStateSaver) { mutableStateOf(listOf<StackItem>()) }
    var backupUrls by rememberSaveable(saver = urlItemListStateSaver) { mutableStateOf(listOf<UrlItem>()) }
    var backupImages by rememberSaveable(saver = imageItemListStateSaver) { mutableStateOf(listOf<ImageItem>()) }

    val hasEditChanges by remember {
        derivedStateOf {
            title != backupTitle ||
                description != backupDescription ||
                introduction != backupIntroduction ||
                solvedacHandle != backupSolvedacHandle ||
                solvedacRank != backupSolvedacRank ||
                solvedacTierName != backupSolvedacTierName ||
                solvedacTierImageUrl != backupSolvedacTierImageUrl ||
                solvedacSolvedCount != backupSolvedacSolvedCount ||
                swTestRank != backupSwTestRank ||
                isVisible != backupIsVisible ||
                stacks.toList() != backupStacks ||
                urls.toList() != backupUrls ||
                images.toList() != backupImages
        }
    }

    val refreshRequested = refreshSignal?.collectAsState(false)?.value ?: false
    LaunchedEffect(refreshRequested) {
        if (refreshRequested) {
            portfolio?.id?.let { viewModel.refreshProjects(it) }
            if (!isEditMode && !hasEditChanges) {
                viewModel.loadMyPortfolio()
            }
            onRefreshConsumed()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (!isEditMode && !hasEditChanges) {
                    viewModel.loadMyPortfolio()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(portfolio?.id) {
        if (isEditMode || hasEditChanges) return@LaunchedEffect
        portfolio?.let {
            title = it.title
            description = it.description
            introduction = it.introduction
            solvedacHandle = it.bojHandle.orEmpty()
            solvedacRank = it.solvedacRank.orEmpty()
            solvedacTierName = it.solvedAcInfo?.tierName.orEmpty()
            solvedacTierImageUrl = it.solvedAcInfo?.tierImageUrl.orEmpty()
            solvedacSolvedCount = it.solvedAcInfo?.solvedCount?.toString().orEmpty()
            swTestRank = it.swTestRank.orEmpty()
            isVisible = it.isVisible
            stacks.clear()
            stacks.addAll(
                it.stacks.map { stack ->
                    StackItem(
                        stackId = stack.stackId,
                        stackName = stack.stackName,
                        stackImageUrl = stack.stackImgUrl,
                        expertLevelLabel = toExpertLevelLabel(stack.expertLevel)
                    )
                }
            )
            urls.clear()
            urls.addAll(
                it.urls.map { url ->
                    UrlItem(
                        url = url.url
                    )
                }
            )
            images.clear()
            images.addAll(
                it.images.map { image ->
                    ImageItem(
                        imageUrl = image.imageUrl,
                        orders = image.orders.toString()
                    )
                }
            )
        }
    }

    // 뒤로가기 동작 정의
    val handleBackAction = {
        if (isEditMode) {
            if (hasEditChanges) {
                showBackConfirmDialog = true
            } else {
                // 변경 사항이 없으면 바로 뒤로 이동
                title = backupTitle
                description = backupDescription
                introduction = backupIntroduction
                solvedacHandle = backupSolvedacHandle
                solvedacRank = backupSolvedacRank
                solvedacTierName = backupSolvedacTierName
                solvedacTierImageUrl = backupSolvedacTierImageUrl
                solvedacSolvedCount = backupSolvedacSolvedCount
                swTestRank = backupSwTestRank
                isVisible = backupIsVisible
                stacks.clear()
                stacks.addAll(backupStacks)
                urls.clear()
                urls.addAll(backupUrls)
                images.clear()
                images.addAll(backupImages)
                isEditMode = false
            }
        } else {
            onBackClick()
        }
    }

    // 시스템 뒤로가기 버튼 대응
    BackHandler {
        handleBackAction()
    }

    // 저장 확인 다이얼로그
    if (showBackConfirmDialog) {
        SsabreeDialog(
            onDismissRequest = { showBackConfirmDialog = false },
            title = "저장 확인",
            message = "저장되지 않은 내용이 있습니다. 저장하시겠습니까?",
            confirmText = "예",
            dismissText = "아니오",
            onConfirm = {
                showBackConfirmDialog = false
                viewModel.savePortfolio(
                    title = title,
                    description = description,
                    introduction = introduction,
                    bojHandle = solvedacHandle.ifBlank { null },
                    solvedacRank = solvedacRank.ifBlank { null },
                    swTestRank = swTestRank.ifBlank { null },
                    isVisible = isVisible,
                    stacks = stacks.toStackUpdateInfos(),
                    urls = urls.toUrlUpdateInfos(),
                    images = images.toImageUpdateInfos()
                ) {
                    isEditMode = false
                }
            },
            onDismiss = {
                showBackConfirmDialog = false
                // 백업 데이터로 복구 (저장 안함)
                title = backupTitle
                description = backupDescription
                introduction = backupIntroduction
                solvedacHandle = backupSolvedacHandle
                solvedacRank = backupSolvedacRank
                solvedacTierName = backupSolvedacTierName
                swTestRank = backupSwTestRank
                isVisible = backupIsVisible
                stacks.clear()
                stacks.addAll(backupStacks)
                urls.clear()
                urls.addAll(backupUrls)
                images.clear()
                images.addAll(backupImages)
                isEditMode = false
            }
        )
    }
    if (previewImageUrl != null) {
        ImagePreviewDialog(imageUrl = normalizeImageUrl(previewImageUrl!!), onDismiss = { previewImageUrl = null })
    }

    // 성공 다이얼로그
    uiState.successMessage?.let { message ->
        SsabreeDialog(
            onDismissRequest = { viewModel.clearSuccessMessage() },
            title = "알림",
            message = message,
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = { viewModel.clearSuccessMessage() }
        )
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { selected ->
                coroutineScope.launch {
                    isUploadingImage = true
                    imageUploadError = null
                    val fileResult = copyUriToTempFile(context, selected)
                    val file = fileResult.getOrNull()
                    if (file == null) {
                        imageUploadError = fileResult.exceptionOrNull()?.message ?: "이미지를 불러올 수 없습니다."
                        isUploadingImage = false
                        return@launch
                    }
                    uploadRepository.uploadImage(file)
                        .onSuccess { url ->
                            val nextOrder = (images.mapNotNull { it.orders.toIntOrNull() }.maxOrNull() ?: 0) + 1
                            images.add(ImageItem(imageUrl = url, orders = nextOrder.toString()))
                        }
                        .onFailure { e ->
                            imageUploadError = e.message ?: "이미지 업로드에 실패했습니다."
                        }
                    isUploadingImage = false
                }
            }
        }
    )

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "포트폴리오 수정" else "포트폴리오 상세",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBackAction) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (!isEditMode) {
                            // 수정 시작 시 현재 데이터 백업
                            backupTitle = title
                            backupDescription = description
                            backupIntroduction = introduction
                            backupSolvedacHandle = solvedacHandle
                            backupSolvedacRank = solvedacRank
                            backupSolvedacTierName = solvedacTierName
                            backupSolvedacTierImageUrl = solvedacTierImageUrl
                            backupSolvedacSolvedCount = solvedacSolvedCount
                            backupSwTestRank = swTestRank
                            backupIsVisible = isVisible
                            backupStacks = stacks.toList()
                            backupUrls = urls.toList()
                            backupImages = images.toList()
                            solvedacVerifyError = null
                            imageUploadError = null
                            isEditMode = true
                            coroutineScope.launch { scrollState.animateScrollTo(0) }
                        } else {
                viewModel.savePortfolio(
                    title = title,
                    description = description,
                    introduction = introduction,
                    bojHandle = solvedacHandle.ifBlank { null },
                    solvedacRank = solvedacRank.ifBlank { null },
                    swTestRank = swTestRank.ifBlank { null },
                    isVisible = isVisible,
                    stacks = stacks.toStackUpdateInfos(),
                    urls = urls.toUrlUpdateInfos(),
                                images = images.toImageUpdateInfos()
                            ) {
                                isEditMode = false
                            }
                        }
                    }, enabled = !uiState.isSaving) {
                        Text(
                            text = if (isEditMode) "저장" else "수정",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
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
                .imePadding()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }
            if (!uiState.isLoading && portfolio == null) {
                Text(
                    text = "등록된 포트폴리오가 없습니다.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }

            val portfolioTextStyle = MaterialTheme.typography.bodyMedium

            PortfolioInfoCard {
                PortfolioSection(title = "제목", icon = Icons.Default.Badge) {
                    if (isEditMode) {
                        LabeledTextField(
                            label = "제목",
                            value = title,
                            onValueChange = { title = it },
                            placeholder = "예) 백엔드 개발자 포트폴리오",
                            showLabel = false
                        )
                    } else {
                        Text(text = if (title.isBlank()) "-" else title, style = portfolioTextStyle)
                    }
                }
            }

            PortfolioInfoCard {
                PortfolioSection(title = "한 줄 소개", icon = Icons.Default.Subject) {
                    if (isEditMode) {
                        LabeledTextField(
                            label = "한 줄 소개",
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "한 줄 소개를 입력하세요",
                            showLabel = false
                        )
                    } else {
                        Text(text = if (description.isBlank()) "-" else description, style = portfolioTextStyle)
                    }
                }
            }

            PortfolioInfoCard {
                PortfolioSection(title = "자기소개", icon = Icons.Default.Person) {
                    if (isEditMode) {
                        LabeledTextField(
                            label = "자기소개",
                            value = introduction,
                            onValueChange = { introduction = it },
                            placeholder = "안녕하세요.",
                            singleLine = false,
                            minLines = 3,
                            showLabel = false
                        )
                    } else {
                        Text(text = if (introduction.isBlank()) "-" else introduction, style = portfolioTextStyle)
                    }
                }
            }

            PortfolioInfoCard {
                PortfolioSection(title = "기술 스택", icon = Icons.Default.Code) {
                if (isEditMode) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("기술 스택", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { stacks.add(StackItem()) }) {
                                Icon(Icons.Default.Add, contentDescription = "추가", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = {
                                    if (stacks.isNotEmpty()) stacks.removeAt(stacks.lastIndex)
                                }) {
                                Icon(Icons.Default.Remove, contentDescription = "삭제", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        stacks.forEachIndexed { index, item ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StackSearchDropdown(
                                    modifier = Modifier.fillMaxWidth(),
                                    stacks = stackUiState.stacks,
                                    selected = item,
                                    imageLoader = svgImageLoader,
                                    onQueryChange = { query ->
                                        stacks[index] = item.copy(stackId = null, stackName = query)
                                    },
                                    onSelected = { selected ->
                                        stacks[index] = item.copy(
                                            stackId = selected.id,
                                            stackName = selected.name,
                                            stackImageUrl = selected.imgUrl
                                        )
                                    }
                                )
                                ExpertLevelDropdown(
                                    modifier = Modifier.fillMaxWidth(),
                                    selected = item.expertLevelLabel,
                                    onSelected = { level ->
                                        stacks[index] = item.copy(expertLevelLabel = level)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    if (stacks.isEmpty()) {
                        Text("-", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            stacks
                                .chunked(2)
                                .forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowItems.forEach { stack ->
                                            Surface(
                                                modifier = Modifier.weight(1f),
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    if (!stack.stackImageUrl.isNullOrBlank()) {
                                                        AsyncImage(
                                                            model = stack.stackImageUrl,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp),
                                                            imageLoader = svgImageLoader
                                                        )
                                                    }
                                                    Text(
                                                        "${stack.stackName} (${stack.expertLevelLabel})",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                        if (rowItems.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                        }
                    }
                }
                }
            }

            PortfolioInfoCard {
                PortfolioSection(title = "SW 역량", icon = Icons.Default.Psychology) {
                    if (isEditMode) {
                        SwTestRankDropdown(
                            selected = swTestRank,
                            onSelected = { swTestRank = it }
                        )
                    } else {
                        Text(text = if (swTestRank.isBlank()) "-" else swTestRank, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            PortfolioInfoCard {
                PortfolioSection(title = "Solved.ac 티어", icon = Icons.Default.EmojiEvents) {
                    if (isEditMode) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Solved.ac 아이디", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                TextButton(
                                    onClick = {
                                        solvedacHandle = ""
                                        solvedacRank = ""
                                        solvedacTierName = ""
                                        solvedacTierImageUrl = ""
                                        solvedacSolvedCount = ""
                                        solvedacVerifyError = null
                                    },
                                    enabled = solvedacHandle.isNotBlank()
                                ) {
                                    Text("삭제", color = MaterialTheme.colorScheme.error)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                TextField(
                                    value = solvedacHandle,
                                    onValueChange = {
                                        solvedacHandle = it
                                        solvedacVerifyError = null
                                        if (it.isBlank()) {
                                            solvedacRank = ""
                                            solvedacTierName = ""
                                            solvedacTierImageUrl = ""
                                            solvedacSolvedCount = ""
                                        }
                                    },
                                    placeholder = { Text("아이디 입력", color = Color.Gray) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    )
                                )
                                Button(
                                    onClick = {
                                        val handle = solvedacHandle.trim()
                                        if (handle.isNotBlank()) {
                                            isSolvedacVerifying = true
                                            solvedacVerifyError = null
                                            viewModel.verifySolvedacHandle(
                                                handle = handle,
                                                onSuccess = { info ->
                                                    val tierLabel = solvedacTierLabel(info.tier)
                                                    solvedacRank = tierLabel
                                                    solvedacTierName = tierLabel
                                                    solvedacTierImageUrl = solvedacTierImage(info.tier)
                                                    solvedacSolvedCount = info.solvedCount.toString()
                                                    isSolvedacVerifying = false
                                                },
                                                onFailure = { message ->
                                                    solvedacVerifyError = message
                                                    isSolvedacVerifying = false
                                                }
                                            )
                                        }
                                    },
                                    enabled = solvedacHandle.isNotBlank() && !isSolvedacVerifying,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("등록")
                                }
                            }
                            if (isSolvedacVerifying) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            }
                            if (!solvedacTierName.isBlank()) {
                                Text(
                                    text = "현재 티어: $solvedacTierName",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (solvedacHandle.isNotBlank()) {
                                val displayTier = solvedacTierName.ifBlank { solvedacRank }
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("티어: ${if (displayTier.isBlank()) "-" else displayTier}", fontSize = 12.sp)
                                    Text("푼 문제: ${if (solvedacSolvedCount.isBlank()) "-" else solvedacSolvedCount}", fontSize = 12.sp)
                                    if (solvedacTierImageUrl.isNotBlank()) {
                                        SolvedAcTierImage(
                                            imageUrl = solvedacTierImageUrl,
                                            size = 28.dp
                                        )
                                    }
                                }
                            }
                            if (solvedacVerifyError != null) {
                                Text(
                                    text = solvedacVerifyError ?: "아이디를 확인할 수 없습니다.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else {
                        val displayTier = solvedacTierName.ifBlank { solvedacRank }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("티어: ${if (displayTier.isBlank()) "-" else displayTier}", fontSize = 13.sp)
                            Text("푼 문제: ${if (solvedacSolvedCount.isBlank()) "-" else solvedacSolvedCount}", fontSize = 13.sp)
                            if (solvedacTierImageUrl.isNotBlank()) {
                                SolvedAcTierImage(
                                    imageUrl = solvedacTierImageUrl,
                                    size = 28.dp
                                )
                            }
                        }
                    }
                }
            }

            PortfolioInfoCard {
                PortfolioSection(title = "관련 링크 (블로그, 깃허브 등)", icon = Icons.Default.Link) {
                    if (isEditMode) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("링크", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = { urls.add(UrlItem()) }) {
                                    Icon(Icons.Default.Add, contentDescription = "추가", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = {
                                        if (urls.isNotEmpty()) urls.removeAt(urls.lastIndex)
                                    }) {
                                    Icon(Icons.Default.Remove, contentDescription = "삭제", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            urls.forEachIndexed { index, item ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    LabeledTextField(
                                        label = "URL",
                                        value = item.url,
                                        onValueChange = { urls[index] = item.copy(url = it) },
                                        modifier = Modifier.weight(1f),
                                        placeholder = "https://example.com"
                                    )
                                }
                            }
                        }
                    } else {
                        if (urls.isEmpty()) {
                            Text("-", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                urls.forEach { url ->
                                    val displayUrl = normalizeUrl(url.url)
                                    Text(
                                        text = if (displayUrl.isBlank()) "-" else displayUrl,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable {
                                            if (displayUrl.isNotBlank()) {
                                                uriHandler.openUri(displayUrl)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            PortfolioSection(
                title = "프로젝트 경험",
                icon = Icons.Default.WorkOutline,
                trailing = {
                    if (isEditMode) {
                        TextButton(
                            onClick = {
                                val portfolioId = portfolio?.id ?: return@TextButton
                                onProjectCreateClick(portfolioId)
                            }
                        ) {
                            Text("프로젝트 추가")
                        }
                    }
                },
                contentPaddingStart = 0.dp
            ) {
                var projectToDelete by remember { mutableStateOf<ProjectModel?>(null) }
                if (projectToDelete != null && portfolio != null) {
                    val target = projectToDelete!!
                    SsabreeDialog(
                        onDismissRequest = { projectToDelete = null },
                        title = "프로젝트 삭제",
                        message = "'${target.title}' 프로젝트를 삭제하시겠습니까?",
                        confirmText = "삭제",
                        dismissText = "취소",
                        onConfirm = {
                            viewModel.deleteProject(portfolio.id, target.id)
                            projectToDelete = null
                        },
                        onDismiss = { projectToDelete = null }
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.projects.isEmpty()) {
                        Text("-", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        uiState.projects.forEach { project ->
                            ProjectExperienceCard(
                                project = project,
                                onOpenUrl = { uriHandler.openUri(it) },
                                onImageClick = { previewImageUrl = it },
                                stackImageByName = stackImageByName,
                                svgImageLoader = svgImageLoader,
                                onEditClick = if (isEditMode && portfolio != null) {
                                    { onProjectEditClick(portfolio.id, project.id) }
                                } else null,
                                onDeleteClick = if (isEditMode) {
                                    { projectToDelete = project }
                                } else null,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // 하단 패딩 축소 적용
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PortfolioSection(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    contentPaddingStart: Dp = 28.dp,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.weight(1f))
            trailing?.invoke()
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = contentPaddingStart)
        ) {
            content()
        }
    }
}

@Composable
private fun PortfolioInfoCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            content()
        }
    }
}

@Composable
private fun ProjectExperienceCard(
    project: ProjectModel,
    onOpenUrl: (String) -> Unit,
    onImageClick: (String) -> Unit,
    stackImageByName: Map<String, String?>,
    svgImageLoader: ImageLoader,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.WorkOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "제목",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (onEditClick != null || onDeleteClick != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (onEditClick != null) {
                            TextButton(onClick = onEditClick) { Text("수정") }
                        }
                        if (onDeleteClick != null) {
                            TextButton(
                                onClick = onDeleteClick,
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) { Text("삭제") }
                        }
                    }
                }
            }

            Text(
                text = project.title.ifBlank { "-" },
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )

            ProjectField(
                icon = Icons.Default.Subject,
                label = "한 줄 소개",
                value = project.introduction?.ifBlank { "-" } ?: "-"
            )
            ProjectField(
                icon = Icons.Default.Description,
                label = "상세 내용",
                value = project.description?.ifBlank { "-" } ?: "-"
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "사용된 기술 스택",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (project.techStacks.isEmpty()) {
                    Text("-", fontSize = 14.sp, fontWeight = FontWeight.Normal)
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        project.techStacks.forEach { stack ->
                            val imageUrl = stackImageByName[stack]
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (!imageUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            imageLoader = svgImageLoader
                                        )
                                    }
                                    Text(stack, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("링크", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                if (project.urls.isEmpty()) {
                    Text("-", fontSize = 14.sp, fontWeight = FontWeight.Normal)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        project.urls.forEach { url ->
                            val target = normalizeUrl(url)
                            Text(
                                text = if (target.isBlank()) "-" else target,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    if (target.isNotBlank()) {
                                        onOpenUrl(target)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("이미지", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                if (project.imageUrls.isEmpty()) {
                    Text("-", fontSize = 14.sp, fontWeight = FontWeight.Normal)
                } else {
                    val filtered = project.imageUrls.filter { it.isNotBlank() }.map { normalizeImageUrl(it) }
                    if (filtered.isEmpty()) {
                        Text("-", fontSize = 14.sp, fontWeight = FontWeight.Normal)
                    } else {
                        ImageCarousel(
                            imageUrls = filtered,
                            onImageClick = onImageClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageCarousel(
    imageUrls: List<String>,
    onImageClick: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val currentIndex by remember {
        derivedStateOf {
            val visible = listState.layoutInfo.visibleItemsInfo
            if (visible.isEmpty()) {
                0
            } else {
                val first = visible.first()
                val next = visible.getOrNull(1)
                val offset = listState.firstVisibleItemScrollOffset
                if (next != null && offset > first.size / 2) {
                    next.index
                } else {
                    first.index
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(imageUrls) { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "이미지",
                    modifier = Modifier
                        .width(220.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onImageClick(imageUrl) }
                )
            }
        }
        CarouselIndicator(count = imageUrls.size, currentIndex = currentIndex)
    }
}

@Composable
private fun CarouselIndicator(
    count: Int,
    currentIndex: Int
) {
    if (count <= 1) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(count) { index ->
            val isSelected = index == currentIndex
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (isSelected) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}

@Composable
private fun ProjectField(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Normal)
        }
    }
}

private data class StackItem(
    val stackId: Long? = null,
    val stackName: String = "",
    val stackImageUrl: String? = null,
    val expertLevelLabel: String = "중"
)

private data class UrlItem(
    val url: String = ""
)

private data class ImageItem(
    val imageUrl: String = "",
    val orders: String = ""
)

private val stackListSaver = listSaver<SnapshotStateList<StackItem>, Any>(
    save = { list ->
        list.flatMap { item ->
            listOf(item.stackId ?: -1L, item.stackName, item.stackImageUrl ?: "", item.expertLevelLabel)
        }
    },
    restore = { restored ->
        mutableStateListOf<StackItem>().apply {
            var index = 0
            while (index + 3 < restored.size) {
                val id = restored[index] as Long
                val name = restored[index + 1] as String
                val imageUrl = restored[index + 2] as String
                val level = restored[index + 3] as String
                add(
                    StackItem(
                        stackId = id.takeIf { it != -1L },
                        stackName = name,
                        stackImageUrl = imageUrl.ifBlank { null },
                        expertLevelLabel = level
                    )
                )
                index += 4
            }
        }
    }
)

private val urlListSaver = listSaver<SnapshotStateList<UrlItem>, Any>(
    save = { list -> list.map { it.url } },
    restore = { restored ->
        mutableStateListOf<UrlItem>().apply {
            restored.forEach { url -> add(UrlItem(url = url as String)) }
        }
    }
)

private val imageListSaver = listSaver<SnapshotStateList<ImageItem>, Any>(
    save = { list -> list.flatMap { item -> listOf(item.imageUrl, item.orders) } },
    restore = { restored ->
        mutableStateListOf<ImageItem>().apply {
            var index = 0
            while (index + 1 < restored.size) {
                val url = restored[index] as String
                val orders = restored[index + 1] as String
                add(ImageItem(imageUrl = url, orders = orders))
                index += 2
            }
        }
    }
)

private val stackItemListStateSaver = androidx.compose.runtime.saveable.Saver<MutableState<List<StackItem>>, List<Any>>(
    save = { state ->
        state.value.flatMap { item ->
            listOf(item.stackId ?: -1L, item.stackName, item.stackImageUrl ?: "", item.expertLevelLabel)
        }
    },
    restore = { restored ->
        val list = buildList {
            var index = 0
            while (index + 3 < restored.size) {
                val id = restored[index] as Long
                val name = restored[index + 1] as String
                val imageUrl = restored[index + 2] as String
                val level = restored[index + 3] as String
                add(
                    StackItem(
                        stackId = id.takeIf { it != -1L },
                        stackName = name,
                        stackImageUrl = imageUrl.ifBlank { null },
                        expertLevelLabel = level
                    )
                )
                index += 4
            }
        }
        mutableStateOf(list)
    }
)

private val urlItemListStateSaver = androidx.compose.runtime.saveable.Saver<MutableState<List<UrlItem>>, List<Any>>(
    save = { state -> state.value.map { it.url } },
    restore = { restored ->
        val list = restored.map { url -> UrlItem(url = url as String) }
        mutableStateOf(list)
    }
)

private val imageItemListStateSaver = androidx.compose.runtime.saveable.Saver<MutableState<List<ImageItem>>, List<Any>>(
    save = { state -> state.value.flatMap { item -> listOf(item.imageUrl, item.orders) } },
    restore = { restored ->
        val list = buildList {
            var index = 0
            while (index + 1 < restored.size) {
                val url = restored[index] as String
                val orders = restored[index + 1] as String
                add(ImageItem(imageUrl = url, orders = orders))
                index += 2
            }
        }
        mutableStateOf(list)
    }
)

private fun List<StackItem>.toStackUpdateInfos(): List<PortfolioStackUpdateInfo> {
    return mapNotNull { item ->
        val id = item.stackId ?: return@mapNotNull null
        val level = toExpertLevelValue(item.expertLevelLabel)
        if (level.isBlank()) return@mapNotNull null
        PortfolioStackUpdateInfo(stackId = id, expertLevel = level)
    }
}

private fun List<UrlItem>.toUrlUpdateInfos(): List<PortfolioUrlUpdateInfo> {
    return mapNotNull { item ->
        val url = item.url.trim()
        if (url.isBlank()) return@mapNotNull null
        PortfolioUrlUpdateInfo(url = url)
    }
}

private fun List<ImageItem>.toImageUpdateInfos(): List<PortfolioImageUpdateInfo> {
    return mapNotNull { item ->
        val url = item.imageUrl.trim()
        val orders = item.orders.toIntOrNull() ?: return@mapNotNull null
        if (url.isBlank()) return@mapNotNull null
        PortfolioImageUpdateInfo(imageUrl = url, orders = orders)
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    showLabel: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier) {
        if (showLabel) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            minLines = minLines,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StackSearchDropdown(
    stacks: List<StackModel>,
    selected: StackItem,
    imageLoader: ImageLoader,
    onQueryChange: (String) -> Unit,
    onSelected: (StackModel) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val query = selected.stackName.trim()
    val filtered = if (query.isBlank()) {
        stacks
    } else {
        stacks.filter { it.name.startsWith(query, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = it
        },
        modifier = modifier
    ) {
        TextField(
            value = selected.stackName,
            onValueChange = {
                onQueryChange(it)
                expanded = true
            },
            label = { Text("기술 스택") },
            placeholder = { Text("기술 입력", color = Color.Gray) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            filtered.forEach { stack ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!stack.imgUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = stack.imgUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    imageLoader = imageLoader
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(stack.name)
                        }
                    },
                    onClick = {
                        focusManager.clearFocus()
                        onSelected(stack)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpertLevelDropdown(
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val options = listOf("상", "중", "하")
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            focusManager.clearFocus()
            expanded = it
        },
        modifier = modifier
    ) {
        TextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("숙련도") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                focusManager.clearFocus()
                expanded = false
            }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        focusManager.clearFocus()
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwTestRankDropdown(
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val options = listOf("IM", "A", "A+", "B")
    val displayValue = if (selected.isBlank()) "선택" else selected
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            focusManager.clearFocus()
            expanded = it
        },
        modifier = modifier
    ) {
        TextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            label = { Text("SW 역량") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                focusManager.clearFocus()
                expanded = false
            }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        focusManager.clearFocus()
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun toExpertLevelValue(label: String): String {
    return when (label) {
        "상" -> "high"
        "중" -> "mid"
        "하" -> "low"
        else -> label
    }
}

private fun toExpertLevelLabel(value: String?): String {
    return when (value) {
        "high" -> "상"
        "mid" -> "중"
        "low" -> "하"
        else -> "중"
    }
}

private fun solvedacTierLabel(tier: Int): String {
    return when {
        tier <= 0 -> "Unrated"
        tier in 1..5 -> "Bronze ${6 - tier}"
        tier in 6..10 -> "Silver ${11 - tier}"
        tier in 11..15 -> "Gold ${16 - tier}"
        tier in 16..20 -> "Platinum ${21 - tier}"
        tier in 21..25 -> "Diamond ${26 - tier}"
        tier in 26..30 -> "Ruby ${31 - tier}"
        tier == 31 -> "Master"
        else -> "Unknown"
    }
}

private fun solvedacTierImage(tier: Int): String {
    if (tier <= 0) return ""
    return "https://static.solved.ac/tier_small/$tier.svg"
}

private fun normalizeUrl(rawUrl: String): String {
    val trimmed = rawUrl.trim()
    if (trimmed.isBlank()) return ""
    val hasScheme = trimmed.startsWith("http://", ignoreCase = true) ||
        trimmed.startsWith("https://", ignoreCase = true)
    return if (hasScheme) trimmed else "https://$trimmed"
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
    if (normalized.startsWith("uploads/") || normalized.startsWith("static/uploads/")) {
        return RetrofitClient.SERVER_URL.trimEnd('/') + "/" + normalized
    }
    // Fallback: treat as relative under /uploads
    return RetrofitClient.SERVER_URL.trimEnd('/') + "/uploads/" + normalized.trimStart('/')
}

@Composable
private fun SolvedAcTierImage(
    imageUrl: String,
    size: Dp
) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        imageLoader = imageLoader,
        contentDescription = "Solved.ac 티어",
        modifier = Modifier.size(size)
    )
}

private suspend fun copyUriToTempFile(context: Context, uri: Uri): Result<File> = withContext(Dispatchers.IO) {
    runCatching {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("이미지를 불러올 수 없습니다.")
        val outputFile = File.createTempFile("portfolio_", ".jpg", context.cacheDir)
        inputStream.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
        outputFile
    }
}

@Composable
private fun ImagePreviewDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(12.dp)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "이미지 미리보기",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 3f)
                            if (scale > 1f) {
                                offsetX += pan.x
                                offsetY += pan.y
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    },
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PortfolioDetailScreenPreview() {
    CompositionLocalProvider(LocalAppContainer provides FakeAppContainer()) {
        PortfolioDetailScreen(
            onBackClick = {},
            onProjectCreateClick = {},
            onProjectEditClick = { _, _ -> }
        )
    }
}
