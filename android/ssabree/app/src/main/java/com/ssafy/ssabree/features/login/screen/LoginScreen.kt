package com.ssafy.ssabree.features.login.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.ssabree.R
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.designsystem.theme.LoginButton
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.features.login.viewmodel.LoginViewModel

// 로그인 화면입니다.
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,   // 로그인 버튼 클릭 이벤트 전송 (AppNavGraph에서 처리)
    onJoinClick: () -> Unit,     // 회원가입 Text 클릭 이벤트 전송 (AppNavGraph에서 처리)
    onFindIdPassClick: () -> Unit
) {

    val container = LocalAppContainer.current

    val viewModel: LoginViewModel = simpleViewModel {
        LoginViewModel(container.authRepository)
    }

    val uiState = viewModel.uiState
    val context = LocalContext.current

    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            android.widget.Toast
                .makeText(context, "환영합니다 싸용자님!", android.widget.Toast.LENGTH_SHORT)
                .show()
            onLoginSuccess()
        }
    }

    var showPassword by remember { mutableStateOf(value = false) }  // 비밀번호 가리기

    // 라이트모드 다크모드 설정 시 배경 화면 변경을 위한 surface
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(64.dp))

                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
                            .size(104.dp),
                        painter = painterResource(id = R.drawable.login_logo),
                        contentDescription = "싸브리타임 로그인 아이콘"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
                            .size(24.dp),
                        painter = painterResource(id = R.drawable.login_comment),
                        contentDescription = "싸브리타임 로그인 코멘트"
                    )

                    Spacer(modifier = Modifier.height(56.dp))

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 아이디
                            Text(
                                text = "이메일",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextField(
                                value = uiState.email,
                                onValueChange = { viewModel.onEmailChange(it) },
                                placeholder = { Text(text = "이메일") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Next
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // 비밀번호
                            Text(
                                text = "비밀번호",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextField(
                                value = uiState.password,
                                onValueChange = { viewModel.onPasswordChange(it) },
                                placeholder = { Text("비밀번호") },
                                singleLine = true,
                                visualTransformation = if (showPassword) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                trailingIcon = {
                                    val icon =
                                        if (showPassword) Icons.Filled.Visibility
                                        else Icons.Filled.VisibilityOff

                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = if (showPassword) "비밀번호 숨기기" else "비밀번호 표시"
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.login(onLoginSuccess) },
                    enabled = !uiState.isLoading &&
                            uiState.email.isNotBlank() &&
                            uiState.password.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LoginButton,
                        disabledContainerColor = LoginButton.copy(alpha = 0.35f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "로그인",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                uiState.errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = msg,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))


                Text(
                    text = "이메일/비밀번호 찾기",
                    modifier = Modifier.noRippleClickable() { onFindIdPassClick() },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary

                )


                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "싸브리타임이 처음이신가요? ",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "회원가입",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.noRippleClickable() { onJoinClick() }
                    )
                }
            }
        }
    }
}

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember {
            MutableInteractionSource()
        }) {
        onClick()
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    LoginScreen(
        onLoginSuccess = {},
        onJoinClick = {},
        onFindIdPassClick = {}
    )
}
