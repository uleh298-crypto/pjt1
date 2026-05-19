package com.ssafy.ssabree.features.board.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ssafy.ssabree.features.board.model.PostUiModel
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.repository.ImageRepository
import com.ssafy.ssabree.core.ui.RepoImage
import com.ssafy.ssabree.R
import androidx.compose.ui.res.painterResource


@Composable
fun PostItemView(
    modifier: Modifier = Modifier,
    post: PostUiModel,
    onClick: () -> Unit = {},
    imageRepository: ImageRepository = LocalAppContainer.current.imageRepository
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            // 좌상단: 게시판 표시 + (옵션) 뱃지

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min), // ✅ 이 Row 높이를 콘텐츠 높이에 맞춤
                verticalAlignment = Alignment.Top
            ) {
                // ✅ 텍스트 영역은 남는 폭만 사용
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BoardPill(text = post.boardName)
                        if (!post.badge.isNullOrBlank()) {
                            Spacer(Modifier.width(8.dp))
                            BadgePill(text = post.badge)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    if (post.isBlinded) {
                        Column {
                            Text(
                                text = "험한 말은 싸피봇이 처리했으니 안심하라구!",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(8.dp))
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.bot),
                                contentDescription = "bot",
                                modifier = Modifier.size(100.dp)
                            )
                        }
                    } else {
                        Text(
                            text = post.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = post.preview,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (!post.isBlinded && !post.imageUrl.isNullOrBlank()) {
                    Spacer(Modifier.width(12.dp))
                    RepoImage(
                        url = post.imageUrl,
                        imageRepository = imageRepository,
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                }
            }



            Spacer(Modifier.height(12.dp))

            // 하단: 좌측 날짜 / 우측 좋아요, 댓글
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.dateText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.weight(1f))

                CountChip(
                    icon = { androidx.compose.material3.Icon(Icons.Filled.Visibility, null) },
                    count = post.viewCount
                )
                Spacer(Modifier.width(10.dp))
                CountChip(
                    icon = { androidx.compose.material3.Icon(Icons.Filled.ThumbUp, null) },
                    count = post.likeCount
                )
                Spacer(Modifier.width(10.dp))
                CountChip(
                    icon = { androidx.compose.material3.Icon(Icons.Filled.ChatBubbleOutline, null) },
                    count = post.commentCount
                )
            }
        }
    }
}


@Composable
private fun BoardPill(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BadgePill(text: String) {
    // HOT/BEST 색은 테마에 맞춰 더 세게 주고 싶으면 여기만 조정하면 된다.
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun CountChip(
    icon: @Composable () -> Unit,
    count: Int,
) {

    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(Modifier.width(6.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview
@Composable
fun PostItemViewPreview() {
    PostItemView(
        post = PostUiModel.mock()
    )
}
