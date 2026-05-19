package com.ssafy.ssabree.features.board.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.ssabree.core.repository.BoardRepository
import com.ssafy.ssabree.core.repository.PostRepository
import com.ssafy.ssabree.core.repository.UploadRepository
import com.ssafy.ssabree.core.repository.model.PollCreateInfo
import com.ssafy.ssabree.core.repository.model.PostCreateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

private const val TAG = "BoardWriteViewModel"

data class VoteOption(
    val id: Int,
    val text: String,
)

data class BoardOption(
    val id: Long,
    val name: String
)

data class BoardWriteUiState(
    val title: String = "",
    val content: String = "",
    val attachedImages: List<Uri> = emptyList(),
    val boardOptions: List<BoardOption> = emptyList(),
    val selectedBoardId: Long? = null,
    val isBoardMenuExpanded: Boolean = false,
    val isVoteEnabled: Boolean = false,
    val voteTitle: String = "",
    val voteOptions: List<VoteOption> = listOf(
        VoteOption(id = 0, text = ""),
        VoteOption(id = 1, text = ""),
    ),
    val isSubmitting: Boolean = false,
    val isSubmitSuccess: Boolean = false,
    val submitError: String? = null,
) {
    val isSubmitEnabled: Boolean
        get() = title.isNotBlank() &&
            content.isNotBlank() &&
            selectedBoardId != null &&
            isPollValid &&
            !isSubmitting

    val hasContent: Boolean
        get() = title.isNotBlank() || content.isNotBlank() || attachedImages.isNotEmpty()

    val selectedBoardName: String
        get() = boardOptions.firstOrNull { it.id == selectedBoardId }?.name ?: "게시판 선택"

    private val isPollValid: Boolean
        get() = !isVoteEnabled || (voteTitle.isNotBlank() && voteOptions.count { it.text.isNotBlank() } >= 2)
}

class BoardWriteViewModel(
    private val postRepository: PostRepository,
    private val boardRepository: BoardRepository,
    private val uploadRepository: UploadRepository,
    private val appContext: Context
) : ViewModel() {

    companion object {
        private const val MAX_IMAGE_DIMENSION = 1280
        private const val MAX_IMAGE_BYTES = 1_000_000 // ~1MB
        private const val MAX_IMAGE_COUNT = 5
    }

    private val _uiState = MutableStateFlow(BoardWriteUiState())
    val uiState: StateFlow<BoardWriteUiState> = _uiState.asStateFlow()

    init {
        loadBoards()
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun onContentChange(value: String) {
        _uiState.update { it.copy(content = value) }
    }

    fun onImagesAttached(uris: List<Uri>) {
        if (uris.isEmpty()) return
        _uiState.update { state ->
            val remaining = MAX_IMAGE_COUNT - state.attachedImages.size
            if (remaining <= 0) return@update state
            val toAdd = uris.take(remaining)
            state.copy(attachedImages = state.attachedImages + toAdd)
        }
    }

    fun onRemoveImage(uri: Uri) {
        _uiState.update { state ->
            state.copy(attachedImages = state.attachedImages - uri)
        }
    }

    fun onToggleBoardMenu(expanded: Boolean) {
        _uiState.update { it.copy(isBoardMenuExpanded = expanded) }
    }

    fun onBoardSelected(boardId: Long) {
        _uiState.update {
            it.copy(
                selectedBoardId = boardId,
                isBoardMenuExpanded = false
            )
        }
    }

    fun onToggleVote() {
        _uiState.update { state ->
            state.copy(
                isVoteEnabled = !state.isVoteEnabled,
                voteTitle = if (state.isVoteEnabled) "" else state.voteTitle,
                voteOptions = if (state.isVoteEnabled) listOf(
                    VoteOption(id = 0, text = ""),
                    VoteOption(id = 1, text = ""),
                ) else state.voteOptions,
            )
        }
    }

    fun onVoteTitleChange(value: String) {
        _uiState.update { it.copy(voteTitle = value) }
    }

    fun onVoteOptionChange(id: Int, value: String) {
        _uiState.update { state ->
            state.copy(
                voteOptions = state.voteOptions.map {
                    if (it.id == id) it.copy(text = value) else it
                }
            )
        }
    }

    fun onAddVoteOption() {
        _uiState.update { state ->
            if (state.voteOptions.size >= 5) return@update state
            val nextId = (state.voteOptions.maxOfOrNull { it.id } ?: -1) + 1
            state.copy(voteOptions = state.voteOptions + VoteOption(id = nextId, text = ""))
        }
    }

    fun onRemoveVoteOption(id: Int) {
        _uiState.update { state ->
            if (state.voteOptions.size <= 2) return@update state
            state.copy(voteOptions = state.voteOptions.filter { it.id != id })
        }
    }

    fun onSubmit() {
        if (!_uiState.value.isSubmitEnabled) return
        val selectedBoardId = _uiState.value.selectedBoardId
        if (selectedBoardId == null) {
            _uiState.update { it.copy(submitError = "게시판을 선택해주세요.") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            val state = _uiState.value
            val imageUrlsResult = runCatching {
                if (state.attachedImages.isEmpty()) {
                    emptyList()
                } else {
                    state.attachedImages.map { uri ->
                        val file = prepareUploadFile(uri).getOrThrow()
                        uploadRepository.uploadImage(file).getOrThrow()
                    }
                }
            }

            val pollInfo = buildPollInfo(state)

            imageUrlsResult
                .onFailure { e ->
                    Log.d(TAG, "image upload failed (${e.message})")
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submitError = e.message ?: "이미지 업로드에 실패했습니다."
                        )
                    }
                }
                .onSuccess { imageUrls ->
                    postRepository.createPost(
                        PostCreateInfo(
                            title = state.title,
                            content = state.content,
                            boardId = selectedBoardId,
                            imageUrls = imageUrls,
                            poll = pollInfo
                        )
                    ).onFailure { e ->
                        Log.d(TAG, "onSubmit: failed (${e.message})")
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                submitError = e.message ?: "알 수 없는 오류가 발생했습니다."
                            )
                        }
                    }.onSuccess {
                        Log.d(TAG, "onSubmit: succeed")
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                isSubmitSuccess = true
                            )
                        }
                    }
                }
        }
    }

    fun clearSubmitError() {
        _uiState.update { it.copy(submitError = null) }
    }

    fun clearSubmitSuccess() {
        _uiState.update { it.copy(isSubmitSuccess = false) }
    }

    private fun loadBoards() {
        viewModelScope.launch {
            boardRepository.getBoards()
                .onSuccess { boards ->
                    val options = boards.map { BoardOption(it.id, it.name) }
                    val defaultId = options.firstOrNull()?.id
                    _uiState.update {
                        it.copy(
                            boardOptions = options,
                            selectedBoardId = it.selectedBoardId ?: defaultId
                        )
                    }
                }
                .onFailure { e ->
                    Log.d(TAG, "loadBoards: failed (${e.message})")
                    _uiState.update {
                        it.copy(submitError = it.submitError ?: "게시판 목록을 불러오지 못했습니다.")
                    }
                }
        }
    }

    private fun buildPollInfo(state: BoardWriteUiState): PollCreateInfo? {
        if (!state.isVoteEnabled) return null
        val title = state.voteTitle.trim()
        val options = state.voteOptions.map { it.text.trim() }.filter { it.isNotBlank() }
        return if (title.isNotBlank() && options.size >= 2) {
            PollCreateInfo(title = title, options = options)
        } else {
            null
        }
    }

    private suspend fun prepareUploadFile(uri: Uri): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = appContext.contentResolver
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }

            options.inSampleSize = calculateInSampleSize(options, MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION)
            options.inJustDecodeBounds = false

            val rotation = readRotationDegrees(uri)

            val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                ?: throw IllegalArgumentException("이미지를 불러올 수 없습니다.")

            val rotatedBitmap = rotateBitmapIfNeeded(bitmap, rotation)
            val scaledBitmap = scaleBitmap(rotatedBitmap, MAX_IMAGE_DIMENSION)
            val compressed = compressBitmapToSize(scaledBitmap, MAX_IMAGE_BYTES)

            val outputFile = File.createTempFile("post_", ".jpg", appContext.cacheDir)
            outputFile.outputStream().use { it.write(compressed) }

            if (scaledBitmap != rotatedBitmap) scaledBitmap.recycle()
            if (rotatedBitmap != bitmap) rotatedBitmap.recycle()
            bitmap.recycle()

            outputFile
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val largestSide = max(bitmap.width, bitmap.height).toFloat()
        if (largestSide <= maxDimension) return bitmap
        val scale = maxDimension / largestSide
        val targetWidth = (bitmap.width * scale).roundToInt().coerceAtLeast(1)
        val targetHeight = (bitmap.height * scale).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    private fun compressBitmapToSize(bitmap: Bitmap, maxBytes: Int): ByteArray {
        var quality = 90
        var compressed: ByteArray
        do {
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos)
            compressed = bos.toByteArray()
            quality -= 10
        } while (compressed.size > maxBytes && quality > 30)
        if (compressed.size > maxBytes) {
            throw IllegalArgumentException("이미지 용량이 너무 큽니다. 1MB 이하로 줄여주세요.")
        }
        return compressed
    }

    private fun readRotationDegrees(uri: Uri): Int {
        return try {
            appContext.contentResolver.openInputStream(uri)?.use { input ->
                ExifInterface(input).let { exif ->
                    when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
