@file:OptIn(ExperimentalMaterial3Api::class)

package com.ssafy.ssabree.features.board.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.ssabree.core.designsystem.theme.SsabreeTheme
import com.ssafy.ssabree.app.FakeAppContainer
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.core.ui.RepoImage
import com.ssafy.ssabree.features.board.component.CommentItemView
import com.ssafy.ssabree.features.board.component.ReportDialog
import com.ssafy.ssabree.features.board.model.CommentUiModel
import com.ssafy.ssabree.features.board.model.PostDetailUiModel

@Composable
fun BoardDetailScreen(
    postId: Long = 1L,
    shouldRefresh: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onMessageClick: (postId: Long) -> Unit = {},
    onEditClick: (Long) -> Unit = {},
    onDeleteSuccess: () -> Unit = {},
) {

    val container = LocalAppContainer.current
    val viewModel: BoardDetailViewModel = simpleViewModel {
        BoardDetailViewModel(container.postRepository, container.commentRepository, container.reportRepository)
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
    }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.loadPost(postId)
            onRefreshConsumed()
        }
    }

    // 삭제 성공 시 새로고침 트리거 후 뒤로 가기
    LaunchedEffect(uiState.isDeleteSuccess) {
        if (uiState.isDeleteSuccess) {
            onDeleteSuccess()
            onBackClick()
        }
    }

    // [추가] 삭제 확인 다이얼로그 상태 관리
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 댓글 삭제 확인 다이얼로그 상태
    var showCommentDeleteDialog by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<CommentUiModel?>(null) }

    // 신고 다이얼로그 상태
    var showPostReportDialog by remember { mutableStateOf(false) }
    var showCommentReportDialog by remember { mutableStateOf(false) }
    var commentToReport by remember { mutableStateOf<CommentUiModel?>(null) }

    // 스낵바 상태
    val snackbarHostState = remember { SnackbarHostState() }

    // [추가] 삭제 확인 다이얼로그
    if (showDeleteDialog) {
        SsabreeDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = "게시글 삭제",
            message = "정말로 삭제하시겠습니까?",
            confirmText = "삭제",
            dismissText = "취소",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deletePost()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // 댓글 삭제 확인 다이얼로그
    if (showCommentDeleteDialog && commentToDelete != null) {
        SsabreeDialog(
            onDismissRequest = {
                showCommentDeleteDialog = false
                commentToDelete = null
            },
            title = "댓글 삭제",
            message = "정말로 삭제하시겠습니까?",
            confirmText = "삭제",
            dismissText = "취소",
            onConfirm = {
                commentToDelete?.let { viewModel.deleteComment(it) }
                showCommentDeleteDialog = false
                commentToDelete = null
            },
            onDismiss = {
                showCommentDeleteDialog = false
                commentToDelete = null
            }
        )
    }

    // 댓글 수정 다이얼로그
    if (uiState.editingComment != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelEditComment() },
            title = { Text("댓글 수정") },
            text = {
                OutlinedTextField(
                    value = uiState.editCommentText,
                    onValueChange = viewModel::onEditCommentTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("댓글 내용을 입력하세요") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.submitEditComment() },
                    enabled = uiState.editCommentText.isNotBlank()
                ) {
                    Text("수정")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelEditComment() }) {
                    Text("취소")
                }
            }
        )
    }

    // 게시글 신고 다이얼로그
    if (showPostReportDialog) {
        ReportDialog(
            onDismissRequest = { showPostReportDialog = false },
            onConfirm = { reason, detail ->
                viewModel.reportPost(reason, detail)
                showPostReportDialog = false
            },
            title = "게시글 신고"
        )
    }

    // 댓글 신고 다이얼로그
    if (showCommentReportDialog && commentToReport != null) {
        ReportDialog(
            onDismissRequest = {
                showCommentReportDialog = false
                commentToReport = null
            },
            onConfirm = { reason, detail ->
                commentToReport?.let { viewModel.reportComment(it.id, reason, detail) }
                showCommentReportDialog = false
                commentToReport = null
            },
            title = "댓글 신고"
        )
    }

    // 신고 성공 알림
    LaunchedEffect(uiState.isReportSuccess) {
        if (uiState.isReportSuccess) {
            snackbarHostState.showSnackbar("신고가 접수되었습니다.")
            viewModel.clearReportSuccess()
        }
    }

    // 에러 다이얼로그 (댓글 작성 실패 등)
    if (uiState.error != null) {
        SsabreeDialog(
            onDismissRequest = { viewModel.clearError() },
            title = "오류",
            message = uiState.error ?: "작업에 실패했습니다.",
            confirmText = "확인",
            onConfirm = { viewModel.clearError() },
            showDismissButton = false
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    // 내 글이 아니면 쪽지 버튼 표시
                    if (uiState.post != null && !uiState.post!!.isAuthor) {
                        IconButton(onClick = {
                            uiState.post?.let { post ->
                                onMessageClick(post.id)
                            }
                        }) {
                            Icon(Icons.Filled.Email, contentDescription = "쪽지")
                        }
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            CommentInputBar(
                value = uiState.commentText,
                onValueChange = viewModel::onCommentTextChange,
                onSubmit = viewModel::onSubmitComment,
                isSubmitting = uiState.isCommentSubmitting,
                replyTargetName = uiState.replyTargetComment?.authorName,
                onCancelReply = viewModel::cancelReply
            )
        }
    ) { padding ->
        val post = uiState.post

        if (post != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // 게시글 본문
                item {
                    PostDetailContent(
                        post = post,
                        onLikeClick = viewModel::onLikePost,
                        onBookmarkClick = viewModel::onBookmarkPost,
                        onReportClick = { showPostReportDialog = true },
                        onVoteClick = viewModel::onVote,
                        onEditClick = { onEditClick(post.id) },
                        onDeleteClick = { showDeleteDialog = true }
                    )
                }

                // 구분선
                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                // 댓글 목록
                items(
                    items = post.comments,
                    key = { it.id }
                ) { comment ->
                    CommentItemView(
                        comment = comment,
                        onReplyClick = viewModel::onReplyComment,
                        onLikeClick = viewModel::onLikeComment,
                        onReportClick = {
                            commentToReport = it
                            showCommentReportDialog = true
                        },
                        onEditClick = { viewModel.startEditComment(it) },
                        onDeleteClick = {
                            commentToDelete = it
                            showCommentDeleteDialog = true
                        }
                    )
                    if (comment != post.comments.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        } else if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun PostDetailContent(
    post: PostDetailUiModel,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onReportClick: () -> Unit,
    onVoteClick: (Long) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Column {
        // 작성자 정보
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (post.authorProfileUrl != null) {
                RepoImage(
                    url = post.authorProfileUrl,
                    imageRepository = LocalAppContainer.current.imageRepository,
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.authorName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = post.dateText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(onClick = { isMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "더보기",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false }
                ) {
                    if (post.isAuthor) {
                        DropdownMenuItem(
                            text = { Text("수정", fontSize = 14.sp) },
                            onClick = {
                                isMenuExpanded = false
                                onEditClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("삭제", fontSize = 14.sp, color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                isMenuExpanded = false
                                onDeleteClick()
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("신고", fontSize = 14.sp) },
                            onClick = {
                                isMenuExpanded = false
                                onReportClick()
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 제목
        Text(
            text = post.title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(Modifier.height(8.dp))

        // 내용
        Text(
            text = post.content,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        // 투표
        post.poll?.let { poll ->
            Spacer(Modifier.height(16.dp))
            PollCard(poll = poll, onVoteClick = onVoteClick)
        }

        if (post.imageUrls.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            post.imageUrls.forEach { imageUrl ->
                RepoImage(
                    url = imageUrl,
                    imageRepository = LocalAppContainer.current.imageRepository,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.FillWidth
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            // 좋아요 버튼
            IconButton(onClick = onLikeClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (post.isLiked) Icons.Filled.ThumbUp else Icons.Filled.ThumbUpOffAlt,
                    contentDescription = "좋아요",
                    modifier = Modifier.size(18.dp),
                    tint = if (post.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(text = post.likeCount.toString(), style = MaterialTheme.typography.labelLarge, color = if (post.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.width(8.dp))

            // 댓글 수 (클릭 불가)
            Icon(imageVector = Icons.Filled.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(4.dp))
            Text(text = post.commentCount.toString(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.width(8.dp))

            // 스크랩 버튼
            IconButton(onClick = onBookmarkClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (post.isBookmarked) Icons.Filled.Star else Icons.Filled.StarOutline,
                    contentDescription = "스크랩",
                    modifier = Modifier.size(18.dp),
                    tint = if (post.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(text = post.bookmarkCount.toString(), style = MaterialTheme.typography.labelLarge, color = if (post.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CommentInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isSubmitting: Boolean = false,
    replyTargetName: String? = null,
    onCancelReply: () -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(), // 키보드 높이만큼 함께 올라오도록 처리
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding()
        ) {
            // 대댓글 모드 표시
            if (replyTargetName != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${replyTargetName}님에게 답글 작성 중",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = onCancelReply,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "취소",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f).height(48.dp),
                    enabled = !isSubmitting,
                    placeholder = {
                        Text(
                            text = if (replyTargetName != null) "답글을 입력하세요" else "댓글을 입력하세요",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant, focusedBorderColor = MaterialTheme.colorScheme.primary),
                    textStyle = MaterialTheme.typography.bodySmall,
                    singleLine = true,
                    trailingIcon = {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(onClick = onSubmit) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "댓글 작성",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PollCard(
    poll: com.ssafy.ssabree.features.board.model.PollUiModel,
    onVoteClick: (Long) -> Unit
) {
    val totalVotes = poll.totalVotes.coerceAtLeast(0)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("투표", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            poll.options.forEach { option ->
                val ratio = if (totalVotes == 0) 0f else option.voteCount / totalVotes.toFloat()
                val isSelected = poll.myVotedOptionId == option.optionId
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onVoteClick(option.optionId) }
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option.text,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "${option.voteCount}표",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            }
            Text(
                text = "총 ${poll.totalVotes}표",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BoardDetailScreenPreview() {
    CompositionLocalProvider(LocalAppContainer provides FakeAppContainer()) {
        SsabreeTheme {
            BoardDetailScreen()
        }
    }
}
