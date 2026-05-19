package com.ssafy.ssabree.features.join.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.ssabree.R
import com.ssafy.ssabree.core.designsystem.theme.LoginButton

@Composable
internal fun JoinStep2(
    // MM 인증
    mattermostId: String,
    onMattermostIdChange: (String) -> Unit,
    verificationCode: String,
    onVerificationCodeChange: (String) -> Unit,
    selectedGeneration: Int?,
    isCodeSent: Boolean,
    isSendingCode: Boolean,
    isVerifying: Boolean,
    errorMessage: String?,
    onSendCode: () -> Unit,
    onVerify: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(80.dp),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(18.dp),
            tonalElevation = 2.dp
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SSAFY MatterMost 인증",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "싸브리타임 이용을 위해 개인정보를 입력해주세요.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "MatterMost 아이디",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            TextField(
                value = mattermostId,
                onValueChange = onMattermostIdChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                placeholder = {
                    Text(
                        text = "abcdef",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                enabled = !isCodeSent,
                colors = joinFieldColors()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedGeneration != null) {
            Text(
                text = "선택한 기수: ${selectedGeneration}기",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isCodeSent) {
            Button(
                onClick = onSendCode,
                enabled = mattermostId.isNotBlank() && selectedGeneration != null && !isSendingCode,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LoginButton,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = LoginButton.copy(alpha = 0.35f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            ) {
                if (isSendingCode) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("인증번호 발송", fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isCodeSent) {
            Text(
                text = "인증번호 6자리",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = verificationCode,
                onValueChange = onVerificationCodeChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = {
                    Text(
                        text = "인증번호 6자리 입력",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = joinFieldColors()
            )

            // 에러 메시지
            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = msg,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onVerify,
                enabled = verificationCode.length == 6 && !isVerifying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LoginButton,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = LoginButton.copy(alpha = 0.35f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("인증 완료", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 코드 발송 전 에러 메시지
        if (!isCodeSent) {
            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = msg,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun GenerationDropdownField(
    generations: List<Int>,
    selectedGeneration: Int?,
    expanded: Boolean,
    onDropdownClick: () -> Unit,
    onDismiss: () -> Unit,
    onGenerationSelected: (Int) -> Unit
) {
    val focusManager = LocalFocusManager.current
    Box {
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable {
                    focusManager.clearFocus()
                    onDropdownClick()
                }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = selectedGeneration?.let { "${it}기" } ?: "기수",
                color = if (selectedGeneration == null) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(start = 6.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                focusManager.clearFocus()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            generations.forEach { generation ->
                DropdownMenuItem(
                    text = { Text("${generation}기") },
                    onClick = {
                        focusManager.clearFocus()
                        onGenerationSelected(generation)
                    }
                )
            }
        }
    }
}

@Composable
private fun joinFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent
)
