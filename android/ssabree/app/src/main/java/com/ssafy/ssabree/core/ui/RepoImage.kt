package com.ssafy.ssabree.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import coil.compose.AsyncImage
import com.ssafy.ssabree.core.repository.ImageRepository
import com.ssafy.ssabree.core.utils.RetrofitClient

@Composable
fun RepoImage(
    url: String,
    imageRepository: ImageRepository,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null,
    placeholderColor: Color = Color(0xFFE9E9E9),
    errorColor: Color = Color(0xFFCCCCCC)
) {
    val normalizedUrl = remember(url) { normalizeImageUrl(url) }
    val imageData by produceState<ByteArray?>(initialValue = null, key1 = url) {
        value = imageRepository.load(normalizedUrl).getOrNull()
    }

    AsyncImage(
        model = imageData ?: normalizedUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = ColorPainter(placeholderColor),
        error = ColorPainter(errorColor)
    )
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
