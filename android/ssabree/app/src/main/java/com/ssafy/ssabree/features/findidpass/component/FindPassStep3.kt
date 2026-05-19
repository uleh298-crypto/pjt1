package com.ssafy.ssabree.features.findidpass.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.ssabree.R
import com.ssafy.ssabree.core.designsystem.theme.LoginButton

@Composable
internal fun FindPassStep3(
    newPassword: String,
    newPasswordCheck: String,
    onNewPasswordChange: (String) -> Unit,
    onNewPasswordCheckChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onResetConfirm: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isResetEnabled = newPassword.isNotBlank() && newPasswordCheck.isNotBlank() && !isLoading

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
            text = "비밀번호를 재설정해주세요.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        FindLabeledField(
            label = "비밀번호",
            value = newPassword,
            onValueChange = onNewPasswordChange,
            placeholder = "비밀번호",
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        )

        Spacer(modifier = Modifier.height(10.dp))

        FindLabeledField(
            label = "비밀번호 확인",
            value = newPasswordCheck,
            onValueChange = onNewPasswordCheckChange,
            placeholder = "비밀번호 확인",
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        )

        Text(
            text = "6~20자의 영문, 숫자, 특수문자 중 2개 이상 조합",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 6.dp, start = 2.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        errorMessage?.let { message ->
            Text(
                text = message,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onResetConfirm,
            enabled = isResetEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LoginButton,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = LoginButton.copy(alpha = 0.35f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
        ) {
            Text(text = if (isLoading) "재설정 중..." else "재설정 완료", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
