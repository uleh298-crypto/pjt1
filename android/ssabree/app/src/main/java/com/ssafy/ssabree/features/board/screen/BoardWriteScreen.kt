@file:OptIn(ExperimentalMaterial3Api::class)

package com.ssafy.ssabree.features.board.screen

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import coil.compose.AsyncImage
import com.ssafy.ssabree.app.FakeAppContainer
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.ui.simpleViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun BoardWriteScreen(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {},
) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val viewModel: BoardWriteViewModel = simpleViewModel {
        BoardWriteViewModel(
            postRepository = container.postRepository,
            boardRepository = container.boardRepository,
            uploadRepository = container.uploadRepository,
            appContext = context.applicationContext
        )
    }

    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // 다이얼로그 상태
    var showCancelDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    // 성공 시 다이얼로그 표시
    LaunchedEffect(uiState.isSubmitSuccess) {
        if (uiState.isSubmitSuccess) {
            showSuccessDialog = true
        }
    }

    // 실패 시 다이얼로그 표시
    LaunchedEffect(uiState.submitError) {
        if (uiState.submitError != null) {
            showErrorDialog = true
        }
    }

    // 뒤로 가기 처리
    BackHandler {
        if (uiState.hasContent) {
            showCancelDialog = true
        } else {
            onCancel()
        }
    }

    // 작성 중단 확인 다이얼로그
    if (showCancelDialog) {
        SsabreeDialog(
            onDismissRequest = { showCancelDialog = false },
            title = "게시글 작성 중단",
            message = "작성 중인 내용이 있습니다. 게시글 작성을 중단하시겠습니까?",
            confirmText = "중단",
            dismissText = "계속 작성",
            onConfirm = {
                showCancelDialog = false
                onCancel()
            },
            onDismiss = { showCancelDialog = false }
        )
    }

    // 등록 성공 다이얼로그
    if (showSuccessDialog) {
        SsabreeDialog(
            onDismissRequest = { },
            title = "알림",
            message = "게시글을 등록했습니다.",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = {
                showSuccessDialog = false
                viewModel.clearSubmitSuccess()
                onSubmitSuccess()
            }
        )
    }

    // 등록 실패 다이얼로그
    if (showErrorDialog) {
        SsabreeDialog(
            onDismissRequest = {
                showErrorDialog = false
                viewModel.clearSubmitError()
            },
            title = "오류",
            message = "게시글 작성에 실패했습니다.\n${uiState.submitError ?: ""}",
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = {
                showErrorDialog = false
                viewModel.clearSubmitError()
            }
        )
    }

    // 갤러리 런처
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5),
        onResult = { uris -> viewModel.onImagesAttached(uris) }
    )

    var tempPhotoFile by remember { mutableStateOf<File?>(null) }

    // 카메라로 사진 촬영
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempPhotoFile?.let { file ->
                    normalizeCapturedImage(file)?.let { uri ->
                        viewModel.onImagesAttached(listOf(uri))
                    }
                }
            }
        }
    )

    fun launchCamera() {
        tempPhotoFile = File.createTempFile("camera_", ".jpg", context.cacheDir)
        tempPhotoFile?.let { file ->
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            cameraLauncher.launch(uri)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                launchCamera()
            } else {
                Toast.makeText(context, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "글 작성",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.hasContent) {
                            showCancelDialog = true
                        } else {
                            onCancel()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.onSubmit() },
                        enabled = uiState.isSubmitEnabled
                    ) {
                        Text(
                            text = "등록",
                            color = if (uiState.isSubmitEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
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
                .imePadding()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 게시판 선택
            ExposedDropdownMenuBox(
                expanded = uiState.isBoardMenuExpanded,
                onExpandedChange = {
                    focusManager.clearFocus()
                    viewModel.onToggleBoardMenu(!uiState.isBoardMenuExpanded)
                }
            ) {
                OutlinedTextField(
                    value = uiState.selectedBoardName,
                    onValueChange = {},
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    label = { Text("게시판") },
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.isBoardMenuExpanded)
                    },
                    enabled = uiState.boardOptions.isNotEmpty()
                )
                ExposedDropdownMenu(
                    expanded = uiState.isBoardMenuExpanded,
                    onDismissRequest = { viewModel.onToggleBoardMenu(false) }
                ) {
                    uiState.boardOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.name) },
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.onBoardSelected(option.id)
                            }
                        )
                    }
                }
            }

            // 1. 제목 입력
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("제목", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                TextField(
                    value = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                    placeholder = { Text("제목을 입력하세요", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }

            // 2. 내용 입력
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("내용", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                TextField(
                    value = uiState.content,
                    onValueChange = viewModel::onContentChange,
                    placeholder = { Text("내용을 입력하세요", color = Color.Gray) },
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

            // 3. 첨부 이미지
            if (uiState.attachedImages.isNotEmpty()) {
                AttachedImageList(
                    images = uiState.attachedImages,
                    onRemove = viewModel::onRemoveImage
                )
            }

            // 4. 투표 섹션 (활성화 시 표시)
            if (uiState.isVoteEnabled) {
                VoteSection(
                    voteTitle = uiState.voteTitle,
                    onVoteTitleChange = viewModel::onVoteTitleChange,
                    voteOptions = uiState.voteOptions,
                    onVoteOptionChange = viewModel::onVoteOptionChange,
                    onAddOption = viewModel::onAddVoteOption,
                    onRemoveOption = viewModel::onRemoveVoteOption
                )
            }

            // 5. 기능 추가 카드 (버튼 형식으로 통일)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column {
                    // 사진 첨부 버튼
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                        .clickable {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                        Spacer(Modifier.width(12.dp))
                        Text("사진 첨부", fontSize = 15.sp)
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))

                    // 카메라 촬영 버튼
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    launchCamera()
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Gray)
                        Spacer(Modifier.width(12.dp))
                        Text("카메라 촬영", fontSize = 15.sp)
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))

                    // 투표 기능 버튼 (스위치 제거 -> 버튼으로 변경)
                    val voteButtonColor = if (uiState.isVoteEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                    val voteButtonText = if (uiState.isVoteEnabled) "투표 삭제" else "투표 추가"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onToggleVote() } // 클릭 시 토글
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.HowToVote, contentDescription = null, tint = voteButtonColor)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = voteButtonText,
                            fontSize = 15.sp,
                            color = if (uiState.isVoteEnabled) MaterialTheme.colorScheme.primary else Color.Unspecified
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// --- 하위 컴포넌트 ---

@Composable
private fun AttachedImageList(
    images: List<Uri>,
    onRemove: (Uri) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        images.forEach { uri ->
            Box(modifier = Modifier.size(100.dp)) {
                AsyncImage(
                    model = uri,
                    contentDescription = "첨부 이미지",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { onRemove(uri) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "삭제",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun VoteSection(
    voteTitle: String,
    onVoteTitleChange: (String) -> Unit,
    voteOptions: List<VoteOption>,
    onVoteOptionChange: (Int, String) -> Unit,
    onAddOption: () -> Unit,
    onRemoveOption: (Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "투표 설정",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            TextField(
                value = voteTitle,
                onValueChange = onVoteTitleChange,
                placeholder = { Text("투표 제목을 입력하세요", fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
            )

            voteOptions.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextField(
                        value = option.text,
                        onValueChange = { onVoteOptionChange(option.id, it) },
                        placeholder = { Text("항목 ${voteOptions.indexOf(option) + 1}", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                    )
                    if (voteOptions.size > 2) {
                        IconButton(onClick = { onRemoveOption(option.id) }) {
                            Icon(
                                imageVector = Icons.Default.RemoveCircleOutline,
                                contentDescription = "삭제",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (voteOptions.size < 5) {
                TextButton(
                    onClick = onAddOption,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("항목 추가")
                }
            }
        }
    }
}

private fun getRotationFromExif(filePath: String): Int {
    return try {
        val exif = ExifInterface(filePath)
        when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    } catch (e: Exception) {
        0
    }
}

private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
    if (degrees == 0) return bitmap
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun normalizeCapturedImage(file: File): Uri? {
    return try {
        val rotation = getRotationFromExif(file.absolutePath)
        if (rotation == 0) return Uri.fromFile(file)

        val original = BitmapFactory.decodeFile(file.absolutePath) ?: return Uri.fromFile(file)
        val rotated = rotateBitmap(original, rotation)
        FileOutputStream(file).use { out ->
            rotated.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        if (rotated != original) rotated.recycle()
        original.recycle()
        Uri.fromFile(file)
    } catch (e: IOException) {
        null
    }
}
