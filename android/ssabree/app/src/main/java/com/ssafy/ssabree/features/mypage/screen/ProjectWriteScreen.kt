package com.ssafy.ssabree.features.mypage.screen

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.exifinterface.media.ExifInterface
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.filled.Remove
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
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.SvgDecoder
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.core.repository.model.StackModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProjectWriteScreen(
    portfolioId: Long,
    projectId: Long? = null,
    onBackClick: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    val container = LocalAppContainer.current
    val viewModel: ProjectWriteViewModel = simpleViewModel {
        ProjectWriteViewModel(container.projectRepository)
    }
    val uiState by viewModel.uiState.collectAsState()
    val stackViewModel: StackViewModel = simpleViewModel {
        StackViewModel(container.stackRepository)
    }
    val stackUiState by stackViewModel.uiState.collectAsState()
    val uploadRepository = container.uploadRepository
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val svgImageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }

    var title by remember { mutableStateOf("") }
    var introduction by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val stackItems = remember { mutableStateListOf<ProjectStackItem>() }
    val urls = remember { mutableStateListOf<String>() }
    val imageUrls = remember { mutableStateListOf<String>() }
    var isUploadingImages by remember { mutableStateOf(false) }
    var imageUploadError by remember { mutableStateOf<String?>(null) }

    var initialTitle by remember { mutableStateOf("") }
    var initialIntroduction by remember { mutableStateOf("") }
    var initialDescription by remember { mutableStateOf("") }
    var initialStacks by remember { mutableStateOf(listOf<ProjectStackItem>()) }
    var initialUrls by remember { mutableStateOf(listOf<String>()) }
    var initialImageUrls by remember { mutableStateOf(listOf<String>()) }
    var showBackConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(projectId) {
        viewModel.loadProject(portfolioId, projectId)
    }

    LaunchedEffect(uiState.project) {
        uiState.project?.let { project ->
            title = project.title
            introduction = project.introduction.orEmpty()
            description = project.description.orEmpty()
            stackItems.clear()
            stackItems.addAll(project.techStacks.map { ProjectStackItem(stackName = it) })
            urls.clear()
            urls.addAll(project.urls)
            imageUrls.clear()
            imageUrls.addAll(project.imageUrls)
            initialTitle = title
            initialIntroduction = introduction
            initialDescription = description
            initialStacks = stackItems.toList()
            initialUrls = urls.toList()
            initialImageUrls = imageUrls.toList()
        }
    }

    val isSubmitEnabled = title.isNotBlank()
    val topBarTitle = if (projectId == null) "프로젝트 추가" else "프로젝트 수정"
    val hasChanges by remember {
        derivedStateOf {
            title != initialTitle ||
                introduction != initialIntroduction ||
                description != initialDescription ||
                stackItems.toList() != initialStacks ||
                urls.toList() != initialUrls ||
                imageUrls.toList() != initialImageUrls
        }
    }
    val isEmptyProject by remember {
        derivedStateOf {
            title.isBlank() &&
                introduction.isBlank() &&
                description.isBlank() &&
                stackItems.all { it.stackName.isBlank() } &&
                urls.all { it.isBlank() } &&
                imageUrls.isEmpty()
        }
    }

    uiState.errorMessage?.let { message ->
        SsabreeDialog(
            onDismissRequest = {},
            title = "오류",
            message = message,
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = onBackClick
        )
    }

    // 성공 다이얼로그
    uiState.successMessage?.let { message ->
        SsabreeDialog(
            onDismissRequest = {},
            title = "알림",
            message = message,
            confirmText = "확인",
            showDismissButton = false,
            onConfirm = {
                viewModel.clearSuccessMessage()
                onSubmitSuccess()
            }
        )
    }

    BackHandler {
        if (hasChanges && !isEmptyProject) {
            showBackConfirmDialog = true
        } else {
            onBackClick()
        }
    }

    if (showBackConfirmDialog) {
        SsabreeDialog(
            onDismissRequest = { showBackConfirmDialog = false },
            title = "저장 확인",
            message = "저장되지 않은 내용이 있습니다. 저장하시겠습니까?",
            confirmText = "예",
            dismissText = "아니오",
            onConfirm = {
                showBackConfirmDialog = false
                if (isEmptyProject) {
                    onBackClick()
                    return@SsabreeDialog
                }
                viewModel.saveProject(
                    portfolioId = portfolioId,
                    projectId = projectId,
                    title = title,
                    introduction = introduction,
                    description = description,
                    techStacks = stackItems.map { it.stackName }.filter { it.isNotBlank() },
                    urls = urls.map { it.trim() }.filter { it.isNotBlank() },
                    imageUrls = imageUrls.toList(),
                    onSuccess = {}
                )
            },
            onDismiss = {
                showBackConfirmDialog = false
                onBackClick()
            }
        )
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10),
        onResult = { selected ->
            if (selected.isEmpty()) return@rememberLauncherForActivityResult
            val remaining = (10 - imageUrls.size).coerceAtLeast(0)
            val targets = selected.take(remaining)
            if (targets.isEmpty()) return@rememberLauncherForActivityResult
            coroutineScope.launch {
                isUploadingImages = true
                imageUploadError = null
                for (uri in targets) {
                    val fileResult = copyUriToTempFile(context, uri)
                    val file = fileResult.getOrNull()
                    if (file == null) {
                        imageUploadError = fileResult.exceptionOrNull()?.message ?: "이미지를 불러올 수 없습니다."
                        continue
                    }
                    uploadRepository.uploadImage(file)
                        .onSuccess { url ->
                            if (imageUrls.size < 10) imageUrls.add(url)
                        }
                        .onFailure { e ->
                            imageUploadError = e.message ?: "이미지 업로드에 실패했습니다."
                        }
                }
                isUploadingImages = false
            }
        }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) {
                            showBackConfirmDialog = true
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveProject(
                                portfolioId = portfolioId,
                                projectId = projectId,
                                title = title,
                                introduction = introduction,
                                description = description,
                                techStacks = stackItems.map { it.stackName }.filter { it.isNotBlank() },
                                urls = urls.map { it.trim() }.filter { it.isNotBlank() },
                                imageUrls = imageUrls.toList(),
                                onSuccess = {}
                            )
                        },
                        enabled = isSubmitEnabled && !uiState.isSaving
                    ) {
                        Text(
                            text = "저장",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isSubmitEnabled)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
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
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            LabeledTextField(
                label = "프로젝트 제목",
                value = title,
                onValueChange = { title = it },
                placeholder = "프로젝트 제목을 입력하세요"
            )
            LabeledTextField(
                label = "프로젝트 한 줄 소개",
                value = introduction,
                onValueChange = { introduction = it },
                placeholder = "간단 소개를 입력하세요",
                singleLine = false,
                minLines = 3
            )
            LabeledTextField(
                label = "프로젝트 설명",
                value = description,
                onValueChange = { description = it },
                placeholder = "상세 설명을 입력하세요",
                singleLine = false,
                minLines = 4
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("기술 스택", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { stackItems.add(ProjectStackItem()) }) {
                        Icon(Icons.Default.Add, contentDescription = "추가", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = {
                            if (stackItems.isNotEmpty()) stackItems.removeAt(stackItems.lastIndex)
                        }) {
                        Icon(Icons.Default.Remove, contentDescription = "삭제", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                if (stackUiState.stacks.isEmpty()) {
                    Text("불러온 기술 스택이 없습니다.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    stackItems.forEachIndexed { index, item ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StackSearchDropdownProject(
                                modifier = Modifier.fillMaxWidth(),
                                stacks = stackUiState.stacks,
                                selected = item,
                                imageLoader = svgImageLoader,
                                onQueryChange = { query ->
                                    stackItems[index] = item.copy(stackId = null, stackName = query)
                                },
                                onSelected = { selected ->
                                    stackItems[index] = item.copy(
                                        stackId = selected.id,
                                        stackName = selected.name,
                                        stackImageUrl = selected.imgUrl
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("관련 URL", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { urls.add("") }) {
                        Icon(Icons.Default.Add, contentDescription = "추가", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = {
                            if (urls.isNotEmpty()) urls.removeAt(urls.lastIndex)
                        }) {
                        Icon(Icons.Default.Remove, contentDescription = "삭제", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                urls.forEachIndexed { index, url ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        TextField(
                            value = url,
                            onValueChange = { urls[index] = it },
                            placeholder = { Text("https://example.com", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("이미지 (최대 10개)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "이미지 추가",
                            color = if (imageUrls.size < 10 && !isUploadingImages) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(enabled = imageUrls.size < 10 && !isUploadingImages) {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                    }
                    if (isUploadingImages) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    }
                }
                if (imageUploadError != null) {
                    Text(
                        text = imageUploadError ?: "이미지 업로드에 실패했습니다.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
                if (imageUrls.isEmpty()) {
                    Text("이미지가 없습니다.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(imageUrls) { url ->
                            Box {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "프로젝트 이미지",
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { imageUrls.remove(url) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(28.dp)
                                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "삭제",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.isSaving) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
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
    minLines: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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

private suspend fun copyUriToTempFile(context: Context, uri: Uri): Result<File> = withContext(Dispatchers.IO) {
    runCatching {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("이미지를 불러올 수 없습니다.")

        val originalBitmap = BitmapFactory.decodeStream(inputStream)
            ?: throw IllegalArgumentException("이미지를 디코딩할 수 없습니다.")
        inputStream.close()

        // EXIF 회전 정보 읽기
        val rotation = context.contentResolver.openInputStream(uri)?.use { exifStream ->
            val exif = ExifInterface(exifStream)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f

        // 회전 적용
        val rotatedBitmap = if (rotation != 0f) {
            val matrix = Matrix().apply { postRotate(rotation) }
            Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true).also {
                originalBitmap.recycle()
            }
        } else {
            originalBitmap
        }

        val maxSize = 1024 * 1024 // 1MB
        val maxDimension = 1920

        // 이미지 크기 조정
        val scaledBitmap = if (rotatedBitmap.width > maxDimension || rotatedBitmap.height > maxDimension) {
            val scale = minOf(
                maxDimension.toFloat() / rotatedBitmap.width,
                maxDimension.toFloat() / rotatedBitmap.height
            )
            val newWidth = (rotatedBitmap.width * scale).toInt()
            val newHeight = (rotatedBitmap.height * scale).toInt()
            Bitmap.createScaledBitmap(rotatedBitmap, newWidth, newHeight, true).also {
                if (it != rotatedBitmap) rotatedBitmap.recycle()
            }
        } else {
            rotatedBitmap
        }

        val outputFile = File.createTempFile("project_", ".jpg", context.cacheDir)

        // 1MB 이하가 될 때까지 품질 조정
        var quality = 90
        do {
            FileOutputStream(outputFile).use { output ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
            }
            quality -= 10
        } while (outputFile.length() > maxSize && quality > 10)

        scaledBitmap.recycle()
        outputFile
    }
}

private data class ProjectStackItem(
    val stackId: Long? = null,
    val stackName: String = "",
    val stackImageUrl: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StackSearchDropdownProject(
    stacks: List<StackModel>,
    selected: ProjectStackItem,
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
