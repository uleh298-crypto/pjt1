package com.ssafy.ssabree.features.mypage.screen

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.painterResource
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.ssafy.ssabree.app.ApplicationClass
import com.ssafy.ssabree.app.FakeAppContainer
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.datasource.local.AuthDataStore
import com.ssafy.ssabree.core.designsystem.component.SsabreeBottomSheet
import com.ssafy.ssabree.core.designsystem.component.SsabreeBottomSheetIconItem
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.repository.model.MyPageCountsModel
import com.ssafy.ssabree.core.repository.model.MyPagePortfolioSummaryModel
import com.ssafy.ssabree.core.repository.model.MyPageUserModel
import com.ssafy.ssabree.core.utils.AuthLogoutReason
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.R
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(
    onBackClick: () -> Unit,
    onLogout: (AuthLogoutReason) -> Unit,
    onSettingClick: () -> Unit,
    onPortfolioDetailClick: () -> Unit,
    onMyPostsClick: () -> Unit = {},
    onMyCommentsClick: () -> Unit = {},
    onMyScrapsClick: () -> Unit = {},
    refreshSignal: StateFlow<Boolean>? = null,
    onRefreshConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val container = LocalAppContainer.current
    val viewModel: MyPageViewModel = simpleViewModel {
        MyPageViewModel(
            container.memberRepository,
            container.portfolioRepository,
            container.projectRepository,
            container.uploadRepository
        )
    }
    val stackViewModel: StackViewModel = simpleViewModel {
        StackViewModel(container.stackRepository)
    }
    val uiState by viewModel.uiState.collectAsState()
    val stackUiState by stackViewModel.uiState.collectAsState()
    val refreshRequested = refreshSignal?.collectAsState(false)?.value ?: false

    val scope = rememberCoroutineScope()
    val svgImageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }
    val stackImageByName = remember(stackUiState.stacks) {
        stackUiState.stacks.associate { it.name to it.imgUrl }
    }
    val authDataStore = remember {
        AuthDataStore(ApplicationClass.encryptedSharedPrefManager)
    }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(refreshRequested) {
        if (refreshRequested) {
            viewModel.loadMyPage()
            onRefreshConsumed()
        }
    }
    var showImagePickerSheet by remember { mutableStateOf(false) }
    var showFullScreenImage by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // File for camera capture - create new file each time
    var tempPhotoFile by remember { mutableStateOf<File?>(null) }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uriToCompressedFile(context, it)
            file?.let { f -> viewModel.uploadProfileImage(f) }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoFile != null) {
            val compressedFile = compressImageFile(context, tempPhotoFile!!)
            compressedFile?.let { viewModel.uploadProfileImage(it) }
        }
    }

    // Permission launcher for camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Create a new temp file for camera
            tempPhotoFile = File.createTempFile("profile_camera_", ".jpg", context.cacheDir)
            tempPhotoFile?.let { file ->
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                cameraLauncher.launch(uri)
            }
        }
    }

    // Permission launcher for gallery (for older Android versions)
    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        }
    }

    // Bottom Sheet for image picker - 통일된 디자인
    if (showImagePickerSheet) {
        SsabreeBottomSheet(
            onDismissRequest = { showImagePickerSheet = false },
            sheetState = sheetState,
            title = "프로필 사진 설정"
        ) {
            SsabreeBottomSheetIconItem(
                text = "카메라로 촬영",
                icon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                onClick = {
                    showImagePickerSheet = false
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )
            SsabreeBottomSheetIconItem(
                text = "갤러리에서 선택",
                icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                onClick = {
                    showImagePickerSheet = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        galleryLauncher.launch("image/*")
                    } else {
                        galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            )
            // 현재 프로필 삭제 버튼 추가
            if (!uiState.myPage?.user?.profileImageUrl.isNullOrBlank()) {
                SsabreeBottomSheetIconItem(
                    text = "현재 프로필 삭제",
                    icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showImagePickerSheet = false
                        viewModel.deleteProfileImage()
                    }
                )
            }
        }
    }

    // Full screen image viewer
    if (showFullScreenImage && !uiState.myPage?.user?.profileImageUrl.isNullOrBlank()) {
        FullScreenImageDialog(
            imageUrl = uiState.myPage?.user?.profileImageUrl!!,
            onDismiss = { showFullScreenImage = false }
        )
    }

    if (showLogoutDialog) {
        SsabreeDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = "로그아웃",
            message = "로그아웃 하시겠습니까?",
            confirmText = "로그아웃",
            dismissText = "취소",
            onConfirm = {
                showLogoutDialog = false
                scope.launch {
                    authDataStore.clear()
                    onLogout(AuthLogoutReason.USER_LOGOUT)
                }
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("마이페이지", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onSettingClick) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 12.dp))
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }
            }

            ProfileHeader(
                user = uiState.myPage?.user,
                isUploadingImage = uiState.isUploadingImage,
                onImageClick = {
                    if (!uiState.myPage?.user?.profileImageUrl.isNullOrBlank()) {
                        showFullScreenImage = true
                    }
                },
                onCameraClick = { showImagePickerSheet = true }
            )
            Spacer(modifier = Modifier.height(24.dp))
            StatsRow(
                counts = uiState.myPage?.counts,
                onPostsClick = onMyPostsClick,
                onCommentsClick = onMyCommentsClick,
                onScrapsClick = onMyScrapsClick
            )
            Spacer(modifier = Modifier.height(28.dp))

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(28.dp))

            InfoCard(
                summary = uiState.myPage?.portfolioSummary,
                isLoading = uiState.isLoadingPortfolio,
                onPortfolioDetailClick = onPortfolioDetailClick,
                stackImageByName = stackImageByName,
                imageLoader = svgImageLoader
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 로그아웃 버튼/텍스트 영역
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .wrapContentSize()
                        .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                ) {
                    Text("로그아웃", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FullScreenImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = Color.White
                )
            }

            AsyncImage(
                model = imageUrl,
                contentDescription = "프로필 이미지",
                modifier = Modifier
                    .fillMaxWidth()
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
        }
    }
}

@Composable
fun ProfileHeader(
    user: MyPageUserModel?,
    isUploadingImage: Boolean = false,
    onImageClick: () -> Unit = {},
    onCameraClick: () -> Unit = {}
) {
    val name = user?.name ?: "사용자"
    val mattermostId = user?.mattermostId?.let { "@$it" } ?: "@-"
    val campusLabel = when {
        user?.generation != null && user.campus?.isNotBlank() == true ->
            "${user.generation}기 ${user.campus} 캠퍼스"
        user?.generation != null -> "${user.generation}기"
        user?.campus?.isNotBlank() == true -> user.campus ?: "-"
        else -> "캠퍼스 정보 없음"
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(74.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            // Profile image circle
            if (!user?.profileImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = user.profileImageUrl,
                    contentDescription = "프로필 이미지",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = onImageClick),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = onImageClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.profile),
                        contentDescription = "기본 프로필 이미지",
                        modifier = Modifier.size(150.dp),
                        tint = Color.Unspecified
                    )
                }
            }

            // Loading indicator when uploading
            if (isUploadingImage) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            // Camera icon button
            Surface(
                modifier = Modifier
                    .size(26.dp)
                    .clickable(onClick = onCameraClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 2.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "프로필 사진 변경",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(mattermostId, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Text(campusLabel, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun StatsRow(
    counts: MyPageCountsModel?,
    onPostsClick: () -> Unit = {},
    onCommentsClick: () -> Unit = {},
    onScrapsClick: () -> Unit = {}
) {
    val posts = counts?.postCount?.toString() ?: "0"
    val comments = counts?.commentCount?.toString() ?: "0"
    val scraps = counts?.scrapCount?.toString() ?: "0"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(
            label = "작성한 글",
            value = posts,
            onClick = onPostsClick,
            modifier = Modifier.weight(1f)
        )
        VerticalDivider(modifier = Modifier.height(28.dp), color = MaterialTheme.colorScheme.outlineVariant)
        StatItem(
            label = "댓글",
            value = comments,
            onClick = onCommentsClick,
            modifier = Modifier.weight(1f)
        )
        VerticalDivider(modifier = Modifier.height(28.dp), color = MaterialTheme.colorScheme.outlineVariant)
        StatItem(
            label = "스크랩",
            value = scraps,
            onClick = onScrapsClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun InfoCard(
    summary: MyPagePortfolioSummaryModel?,
    isLoading: Boolean = false,
    onPortfolioDetailClick: () -> Unit,
    stackImageByName: Map<String, String?>,
    imageLoader: ImageLoader
) {
    val techStacks = summary?.techStack?.entries?.toList() ?: emptyList()
    val swRating = summary?.ssafySwRating ?: "-"
    val solvedAcRank = summary?.solvedAcRank ?: "-"
    val solvedAcTierName = summary?.solvedAcTierName?.ifBlank { solvedAcRank } ?: solvedAcRank
    val solvedAcTierImageUrl = summary?.solvedAcTierImageUrl
    val solvedAcSolvedCount = summary?.solvedAcSolvedCount?.toString() ?: "-"
    val links = summary?.links ?: emptyList()
    val projects = summary?.projects ?: emptyList()

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("내 포트폴리오", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    if (isLoading) {
                        Spacer(modifier = Modifier.width(12.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
                Surface(modifier = Modifier.clickable { onPortfolioDetailClick() }, shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                    Text("상세보기", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SummarySection(icon = Icons.Default.Code, label = "기술 스택") {
                if (techStacks.isEmpty()) {
                    Text("등록된 기술이 없습니다.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        techStacks
                            .chunked(2)
                            .forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { entry ->
                                        val level = toLevelLabel(entry.value)
                                        val imageUrl = stackImageByName[entry.key]
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
                                                if (!imageUrl.isNullOrBlank()) {
                                                    AsyncImage(
                                                        model = imageUrl,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        imageLoader = imageLoader
                                                    )
                                                }
                                                Text(
                                                    "${entry.key} ($level)",
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

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SummarySection(
                    icon = Icons.Default.Psychology,
                    label = "SW 역량",
                    value = swRating
                )
                SummarySection(
                    icon = Icons.Default.EmojiEvents,
                    label = "Solved.ac"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("티어: ${solvedAcTierName.ifBlank { "-" }}", fontSize = 12.sp)
                            Text("푼 문제: $solvedAcSolvedCount", fontSize = 12.sp)
                        }
                        if (!solvedAcTierImageUrl.isNullOrBlank()) {
                            SolvedAcTierImage(
                                imageUrl = solvedAcTierImageUrl,
                                size = 28.dp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SummarySection(icon = Icons.Default.Link, label = "관련 링크 (블로그, 깃허브 등)") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (links.isEmpty()) {
                        Text("-", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    } else {
                        links.forEach { url ->
                            Text(url, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            SummarySection(icon = Icons.Default.WorkOutline, label = "프로젝트 경험") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (projects.isEmpty()) {
                        Text("-", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    } else {
                        projects.forEachIndexed { index, title ->
                            Text("${index + 1}. $title", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

private fun toLevelLabel(raw: String?): String {
    return when (raw?.trim()?.lowercase()) {
        "high" -> "상"
        "mid" -> "중"
        "low" -> "하"
        "" -> "-"
        null -> "-"
        else -> raw
    }
}

private fun maskSolvedAcHandle(handle: String): String {
    val trimmed = handle.trim()
    if (trimmed.isBlank() || trimmed == "-") return handle
    if (trimmed.length <= 2) return "*".repeat(trimmed.length)
    return trimmed.take(2) + "*".repeat(trimmed.length - 2)
}

@Composable
fun SummarySection(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String? = null,
    content: @Composable (() -> Unit)? = null
) {
    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            if (value != null) {
                Text(value, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
            content?.invoke()
        }
    }
}

/**
 * EXIF 정보를 읽어서 이미지 회전 각도를 반환
 */
private fun getRotationFromExif(filePath: String): Int {
    return try {
        // androidx.exifinterface.media.ExifInterface로 변경
        val exif = androidx.exifinterface.media.ExifInterface(filePath)
        val orientation = exif.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL)
        when (orientation) {
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    } catch (_: Exception) { // 사용되지 않는 변수 e를 _로 변경
        0
    }
}

/**
 * 비트맵을 주어진 각도만큼 회전
 */
private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
    if (degrees == 0) return bitmap
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun uriToCompressedFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null

        // 먼저 임시 파일에 저장하여 EXIF 정보를 읽을 수 있게 함
        val tempRawFile = File.createTempFile("profile_raw_", ".jpg", context.cacheDir)
        tempRawFile.outputStream().use { out ->
            inputStream.copyTo(out)
        }
        inputStream.close()

        // EXIF에서 회전 정보 읽기
        val rotation = getRotationFromExif(tempRawFile.absolutePath)

        var bitmap = BitmapFactory.decodeFile(tempRawFile.absolutePath) ?: return null

        // 회전 적용
        bitmap = rotateBitmap(bitmap, rotation)

        // Compress and resize
        val maxSize = 1024
        val scale = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height, 1f)
        val scaledBitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }

        val tempFile = File.createTempFile("profile_upload_", ".jpg", context.cacheDir)
        FileOutputStream(tempFile).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }

        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
        bitmap.recycle()
        tempRawFile.delete()

        tempFile
    } catch (_: Exception) { // 사용되지 않는 변수 e를 _로 변경
        null
    }
}

private fun compressImageFile(context: Context, file: File): File? {
    return try {
        // EXIF에서 회전 정보 읽기
        val rotation = getRotationFromExif(file.absolutePath)

        var bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null

        // 회전 적용
        bitmap = rotateBitmap(bitmap, rotation)

        // Compress and resize
        val maxSize = 1024
        val scale = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height, 1f)
        val scaledBitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }

        val tempFile = File.createTempFile("profile_compressed_", ".jpg", context.cacheDir)
        FileOutputStream(tempFile).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }

        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
        bitmap.recycle()

        tempFile
    } catch (_: Exception) { // 사용되지 않는 변수 e를 _로 변경
        null
    }
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

@Preview(showBackground = true)
@Composable
fun Preview() {
    CompositionLocalProvider(LocalAppContainer provides FakeAppContainer()) {
        MyPageScreen(
            onBackClick = {},
            onLogout = {},
            onSettingClick = {},
            onPortfolioDetailClick = {}
        )
    }
}
