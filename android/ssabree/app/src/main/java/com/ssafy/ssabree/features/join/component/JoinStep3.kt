package com.ssafy.ssabree.features.join.component

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.ssabree.R
import com.ssafy.ssabree.core.designsystem.theme.LoginButton
import androidx.compose.ui.graphics.Color

@Composable
internal fun JoinStep3(
    email: String,
    onEmailChange: (String) -> Unit,
    isCheckingEmail: Boolean,
    isEmailAvailable: Boolean?,
    onCheckEmailDuplicate: () -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordConfirm: String,
    onPasswordConfirmChange: (String) -> Unit,
    passwordMismatch: Boolean,
    isLoading: Boolean,
    isStep3Valid: Boolean,
    errorMessage: String?,
    onJoin: () -> Unit,
) {
    val scrollState = rememberScrollState()
    var showPassword by remember { mutableStateOf(false) }
    var showPasswordConfirm by remember { mutableStateOf(false) }

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

        Spacer(modifier = Modifier.height(24.dp))

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                JoinLabeledField(
                    label = "이메일",
                    value = email,
                    onValueChange = onEmailChange,
                    placeholder = "user@example.com",
                    keyboardType = KeyboardType.Email
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = onCheckEmailDuplicate,
                enabled = email.isNotBlank() && !isCheckingEmail,
                modifier = Modifier.height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isEmailAvailable == true) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color.Transparent
                )
            ) {
                if (isCheckingEmail) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("중복확인", fontSize = 13.sp)
                }
            }
        }

        when (isEmailAvailable) {
            true -> Text(
                text = "사용 가능한 이메일입니다.",
                fontSize = 11.sp,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(top = 6.dp, start = 2.dp)
            )
            false -> Text(
                text = "이미 사용 중인 이메일입니다.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 6.dp, start = 2.dp)
            )
            null -> Text(
                text = "이메일 형식으로 입력해 주세요",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 6.dp, start = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        JoinLabeledField(
            label = "비밀번호",
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "비밀번호",
            visualTransformation = if (showPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                val icon = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = icon,
                        contentDescription = if (showPassword) "비밀번호 숨기기" else "비밀번호 표시"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        JoinLabeledField(
            label = "비밀번호 확인",
            value = passwordConfirm,
            onValueChange = onPasswordConfirmChange,
            placeholder = "비밀번호 확인",
            visualTransformation = if (showPasswordConfirm) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                val icon = if (showPasswordConfirm) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { showPasswordConfirm = !showPasswordConfirm }) {
                    Icon(
                        imageVector = icon,
                        contentDescription = if (showPasswordConfirm) "비밀번호 숨기기" else "비밀번호 표시"
                    )
                }
            }
        )

        Text(
            text = "8자 이상, 영문, 숫자, 특수문자(@\$!%*#?&) 포함",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 6.dp, start = 2.dp)
        )

        if (passwordMismatch) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "비밀번호가 일치하지 않습니다.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 2.dp)
            )
        }

        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = msg,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onJoin,
            enabled = isStep3Valid,
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
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("가입 완료", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun JoinStep3Preview() {
    JoinStep3(
        email = "", onEmailChange = {},
        isCheckingEmail = false,
        isEmailAvailable = null,
        onCheckEmailDuplicate = {},
        password = "", onPasswordChange = {},
        passwordConfirm = "", onPasswordConfirmChange = {},
        passwordMismatch = false,
        isLoading = false,
        isStep3Valid = false,
        errorMessage = null,
        onJoin = {}
    )
}
