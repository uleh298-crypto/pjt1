package com.ssafy.ssabree.features.mypage.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.repository.model.InquiryModel
import com.ssafy.ssabree.core.ui.simpleViewModel
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private val CardShape = RoundedCornerShape(24.dp)
private val ButtonShape = RoundedCornerShape(12.dp)

private data class InquiryItem(
    val id: Long,
    val title: String,
    val content: String,
    val date: String,
    val answer: String? = null
)

enum class InquiryScreenState { MAIN, WRITE, DETAIL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InquiryScreen(onBackClick: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: InquiryViewModel = simpleViewModel {
        InquiryViewModel(container.inquiryRepository)
    }
    val uiState by viewModel.uiState.collectAsState()

    var screenState by remember { mutableStateOf(InquiryScreenState.MAIN) }
    var selectedInquiry by remember { mutableStateOf<InquiryItem?>(null) }
    var inquiryTitle by remember { mutableStateOf("") }
    var inquiryContent by remember { mutableStateOf("") }

    fun handleBack() {
        when (screenState) {
            InquiryScreenState.MAIN -> onBackClick()
            else -> {
                screenState = InquiryScreenState.MAIN
                selectedInquiry = null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (screenState) {
                            InquiryScreenState.WRITE -> "문의 작성"
                            InquiryScreenState.DETAIL -> "상세 문의내역"
                            else -> "문의사항"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { handleBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            if (screenState == InquiryScreenState.MAIN && uiState.inquiries.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { screenState = InquiryScreenState.WRITE },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "작성하기")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (screenState) {
                InquiryScreenState.MAIN -> {
                    when {
                        uiState.isLoading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        uiState.errorMessage != null -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = uiState.errorMessage ?: "Unknown error",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        uiState.inquiries.isEmpty() -> {
                            InquiryEmptyView(onWriteClick = { screenState = InquiryScreenState.WRITE })
                        }
                        else -> {
                            val items = uiState.inquiries.map { it.toUiModel() }
                            InquiryListView(
                                items = items,
                                onItemClick = { item ->
                                    selectedInquiry = item
                                    screenState = InquiryScreenState.DETAIL
                                }
                            )
                        }
                    }
                }
                InquiryScreenState.WRITE -> {
                    InquiryWriteView(
                        title = inquiryTitle,
                        content = inquiryContent,
                        onTitleChange = { inquiryTitle = it },
                        onContentChange = { if (it.length <= 1000) inquiryContent = it },
                        isSubmitting = uiState.isSubmitting,
                        onSubmit = {
                            val combined = buildInquiryContent(inquiryTitle, inquiryContent)
                            viewModel.submitInquiry(combined) {
                                inquiryTitle = ""
                                inquiryContent = ""
                                screenState = InquiryScreenState.MAIN
                            }
                        }
                    )
                }
                InquiryScreenState.DETAIL -> {
                    selectedInquiry?.let { item ->
                        InquiryDetailView(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun InquiryEmptyView(onWriteClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "아직 등록된 문의가 없어요",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        Button(
            onClick = onWriteClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp)
                .fillMaxWidth()
                .height(56.dp),
            shape = ButtonShape
        ) { Text("문의 작성하기", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun InquiryListView(items: List<InquiryItem>, onItemClick: (InquiryItem) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            Card(
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().clickable { onItemClick(item) }
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (item.answer != null) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (item.answer != null) "답변 완료" else "답변 대기",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.answer != null) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(item.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun InquiryWriteView(
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    isSubmitting: Boolean,
    onSubmit: () -> Unit
) {
    Column(modifier = Modifier.padding(20.dp).fillMaxSize()) {
        Card(
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = {
                    Text(
                        "제목을 입력하세요",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                TextField(
                    value = content,
                    onValueChange = onContentChange,
                    placeholder = {
                        Text(
                            "문의 내용을 자세히 적어주세요.\n답변은 영업일 기준 1~3일 내에 달립니다.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    "${content.length}/1000",
                    modifier = Modifier.align(Alignment.BottomEnd),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = ButtonShape,
            enabled = content.isNotEmpty() && !isSubmitting
        ) {
            Text("제출하기", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InquiryDetailView(item: InquiryItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Card(
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            "Q",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("나의 문의", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(item.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(item.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(12.dp))
                Text(item.content, fontSize = 15.sp, lineHeight = 24.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }
        }

        Card(
            shape = CardShape,
            colors = CardDefaults.cardColors(
                containerColor = if (item.answer != null) {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (item.answer != null) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "A",
                            color = MaterialTheme.colorScheme.surface,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("관리자 답변", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (item.answer != null) {
                    Text(item.answer, fontSize = 15.sp, lineHeight = 24.sp, color = MaterialTheme.colorScheme.onSurface)
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("아직 답변이 등록되지 않았습니다.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "조금만 기다려주세요!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

private fun buildInquiryContent(title: String, content: String): String {
    val trimmedTitle = title.trim()
    val trimmedContent = content.trim()
    return if (trimmedTitle.isNotEmpty()) {
        "$trimmedTitle\n$trimmedContent"
    } else {
        trimmedContent
    }
}

private fun InquiryModel.toUiModel(): InquiryItem {
    val (title, body) = splitTitleAndBody(content)
    val displayDate = createdAt?.let { formatInquiryDate(it) }
        ?: LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
    return InquiryItem(
        id = id,
        title = title,
        content = body,
        date = displayDate,
        answer = answer
    )
}

private fun splitTitleAndBody(content: String): Pair<String, String> {
    val lines = content.lines()
    val title = lines.firstOrNull()?.takeIf { it.isNotBlank() } ?: content.take(40)
    val body = if (lines.size > 1) {
        lines.drop(1).joinToString("\n").trim()
    } else {
        content
    }
    return title to body
}

private fun formatInquiryDate(raw: String): String {
    return runCatching {
        OffsetDateTime.parse(raw).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
    }.getOrElse {
        runCatching {
            LocalDateTime.parse(raw).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
        }.getOrElse { raw }
    }
}
