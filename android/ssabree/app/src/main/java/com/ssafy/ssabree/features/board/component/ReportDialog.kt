package com.ssafy.ssabree.features.board.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog

enum class ReportReason(val code: String, val displayName: String) {
    ABUSE("ABUSE", "욕설/비방"),
    SPAM("SPAM", "스팸/광고"),
    INAPPROPRIATE("INAPPROPRIATE", "부적절한 내용"),
    OTHER("OTHER", "기타")
}

@Composable
fun ReportDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (reason: String, detail: String?) -> Unit,
    title: String = "신고하기"
) {
    var selectedReason by remember { mutableStateOf<ReportReason?>(null) }
    var detailText by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // 신고 확인 다이얼로그
    if (showConfirmDialog) {
        SsabreeDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = "신고 확인",
            message = "정말로 신고하시겠습니까?\n신고 사유: ${selectedReason?.displayName}",
            confirmText = "신고하기",
            dismissText = "취소",
            onConfirm = {
                showConfirmDialog = false
                selectedReason?.let { reason ->
                    onConfirm(reason.code, detailText.ifBlank { null })
                }
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "신고 사유를 선택해주세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                // 신고 사유 선택
                ReportReason.entries.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (selectedReason == reason)
                                Icons.Filled.RadioButtonChecked
                            else
                                Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (selectedReason == reason)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = reason.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 상세 내용 입력
                TextField(
                    value = detailText,
                    onValueChange = { detailText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("상세 내용을 입력해주세요 (선택)", color = Color.Gray) },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(24.dp))

                // 버튼들
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("취소")
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = { showConfirmDialog = true },
                        enabled = selectedReason != null
                    ) {
                        Text(
                            text = "신고하기",
                            color = if (selectedReason != null)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
