package com.ssafy.ssabree.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 통일된 디자인의 다이얼로그 컴포넌트
 * 알림 설정 다이얼로그 디자인을 기반으로 함
 */
@Composable
fun SsabreeDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "확인",
    dismissText: String = "취소",
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    showDismissButton: Boolean = true
) {
    if (message.contains("HTTP 401")) return
    val displayMessage = if (
        message.contains("502", ignoreCase = true) ||
        message.contains("bad gateway", ignoreCase = true)
    ) {
        "서버와의 연결이 끊겼습니다."
    } else {
        message
    }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = displayMessage,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                val confirmColor = when (confirmText) {
                    "확인", "수락" -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.error
                }
                Text(
                    text = confirmText,
                    color = confirmColor,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = if (showDismissButton) {
            {
                TextButton(onClick = { onDismiss?.invoke() ?: onDismissRequest() }) {
                    Text(
                        text = dismissText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else null,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * 통일된 디자인의 ModalBottomSheet 컴포넌트
 * 알림 설정 페이지의 BottomSheet 디자인을 기반으로 함
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SsabreeBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

/**
 * BottomSheet 내의 선택 항목 (RadioButton 포함)
 */
@Composable
fun SsabreeBottomSheetItem(
    text: String,
    selected: Boolean = false,
    showRadioButton: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        if (showRadioButton) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontSize = 16.sp
        )
    }
}

/**
 * BottomSheet 내의 아이콘이 포함된 항목
 */
@Composable
fun SsabreeBottomSheetIconItem(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp)
    ) {
        icon()
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 16.sp
        )
    }
}
