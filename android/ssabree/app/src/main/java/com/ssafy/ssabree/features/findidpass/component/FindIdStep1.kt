package com.ssafy.ssabree.features.findidpass.component

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.ssabree.R
import com.ssafy.ssabree.core.designsystem.theme.LoginButton

@Composable
internal fun FindIdStep1(
    mattermostId: String,
    onMattermostIdChange: (String) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    generations: List<Int>,
    selectedGeneration: Int?,
    isGenerationDropdownExpanded: Boolean,
    onGenerationDropdownClick: () -> Unit,
    onGenerationDropdownDismiss: () -> Unit,
    onGenerationSelected: (Int) -> Unit,
    verificationCode: String,
    onVerificationCodeChange: (String) -> Unit,
    isCodeSent: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onSendCode: () -> Unit,
    onVerify: () -> Unit
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
                contentDescription = "MatterMost 아이콘",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SSAFY MatterMost 인증",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "아이디 찾기를 위해 인증을 완료해 주세요.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "이름",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            TextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                placeholder = {
                    Text(
                        text = "이름",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                colors = findFieldColors()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Mattermost 아이디",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = mattermostId,
                    onValueChange = onMattermostIdChange,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    placeholder = {
                        Text(
                            text = "abcdef",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    singleLine = true,
                    colors = findFieldColors()
                )

                Spacer(modifier = Modifier.width(8.dp))

                GenerationDropdownField(
                    generations = generations,
                    selectedGeneration = selectedGeneration,
                    expanded = isGenerationDropdownExpanded,
                    onDropdownClick = {
                        focusManager.clearFocus()
                        onGenerationDropdownClick()
                    },
                    onDismiss = {
                        focusManager.clearFocus()
                        onGenerationDropdownDismiss()
                    },
                    onGenerationSelected = {
                        focusManager.clearFocus()
                        onGenerationSelected(it)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isCodeSent) {
                Button(
                    onClick = {
                        onSendCode()
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    },
                    enabled = mattermostId.isNotBlank() && selectedGeneration != null && !isLoading,
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
                    Text(text = if (isLoading) "전송 중..." else "인증번호 발송", fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isCodeSent) {
            Text(
                text = "인증번호 6자리",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
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
                colors = findFieldColors()
            )

            errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onVerify,
                enabled = verificationCode.length == 6 && !isLoading,
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
                Text(text = if (isLoading) "조회 중..." else "인증 완료", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (!isCodeSent) {
            errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
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
                modifier = Modifier.align(Alignment.CenterEnd),
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
