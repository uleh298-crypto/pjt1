package com.ssafy.ssabree.features.board.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ssafy.ssabree.core.designsystem.theme.SsabreeTheme
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.ui.RepoImage
import com.ssafy.ssabree.features.board.model.CommentUiModel
import com.ssafy.ssabree.R
import androidx.compose.ui.res.painterResource

private val ReplyBackgroundColor = Color(0xFFE8F4FD)

@Composable
fun CommentItemView(
    modifier: Modifier = Modifier,
    comment: CommentUiModel,
    isReply: Boolean = false,
    onReplyClick: (CommentUiModel) -> Unit = {},
    onLikeClick: (CommentUiModel) -> Unit = {},
    onReportClick: (CommentUiModel) -> Unit = {},
    onEditClick: (CommentUiModel) -> Unit = {},
    onDeleteClick: (CommentUiModel) -> Unit = {},
    imageRepository: com.ssafy.ssabree.core.repository.ImageRepository = LocalAppContainer.current.imageRepository
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showBlindedContent by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isReply) {
                    Modifier
                        .padding(start = 40.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                } else {
                    Modifier.padding(vertical = 8.dp)
                }
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // 프로필 영역 생략 (기존과 동일)
            if (comment.authorProfileUrl != null) {
                RepoImage(
                    url = comment.authorProfileUrl,
                    imageRepository = imageRepository,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f).padding(top = 4.dp)) {
                Text(
                    text = comment.authorName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 액션 버튼들
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onReplyClick(comment) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.ChatBubbleOutline, contentDescription = "답글", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { onLikeClick(comment) }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (comment.isLiked) Icons.Filled.ThumbUp else Icons.Filled.ThumbUpOffAlt,
                        contentDescription = "좋아요",
                        modifier = Modifier.size(18.dp),
                        tint = if (comment.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // [수정] 더보기 버튼 및 드롭다운 메뉴
                Box {
                    IconButton(onClick = { isMenuExpanded = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "더보기", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        if (comment.isMine) {
                            DropdownMenuItem(
                                text = { Text("수정", fontSize = 14.sp) },
                                onClick = {
                                    isMenuExpanded = false
                                    onEditClick(comment)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("삭제", fontSize = 14.sp, color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    isMenuExpanded = false
                                    onDeleteClick(comment)
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("신고", fontSize = 14.sp) },
                                onClick = {
                                    isMenuExpanded = false
                                    onReportClick(comment)
                                }
                            )
                        }
                    }
                }
            }
        }

        // 내용 및 날짜/좋아요 수 영역
        if (comment.isBlinded && !showBlindedContent) {
            Column(
                modifier = Modifier.padding(start = 42.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "험한 말은 싸피봇이 처리했으니 안심하라구!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = { showBlindedContent = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "내용보기",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.bot),
                    contentDescription = "bot",
                    modifier = Modifier.size(100.dp)
                )
            }
        } else {
            Text(text = comment.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 42.dp))
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.padding(start = 42.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = comment.dateText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (comment.likeCount > 0) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.ThumbUp,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = comment.likeCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 대댓글 영역
        if (!isReply && comment.replies.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            comment.replies.forEach { reply ->
                CommentItemView(
                    comment = reply,
                    isReply = true,
                    onReplyClick = { onReplyClick(comment) },  // 대댓글에서 답글 누르면 부모 댓글에 달리도록
                    onLikeClick = onLikeClick,
                    onReportClick = onReportClick,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick
                )
                if (reply != comment.replies.last()) {
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
