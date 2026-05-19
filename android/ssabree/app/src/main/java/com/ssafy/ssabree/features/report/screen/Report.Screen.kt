package com.ssafy.ssabree.features.report.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onBackClick: () -> Unit = {},
    onReportSubmit: () -> Unit = {}
) {
    var reportTitle by remember { mutableStateOf("") }
    var reportContent by remember { mutableStateOf("") }
    val maxCharLimit = 300

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "신고하기",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "제목 :",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = reportTitle,
                onValueChange = { reportTitle = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = {
                    Text(
                        text = "신고 제목을 입력해주세요.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = "내용 :",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = reportContent,
                onValueChange = {
                    if (it.length <= maxCharLimit) reportContent = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                placeholder = {
                    Text(
                        text = "신고 내용을 입력해주세요.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = "글자수 제한 (${reportContent.length}/$maxCharLimit)",
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp),
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onReportSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8BA4F9) // 이미지와 비슷한 색상 적용
                )
            ) {
                Text(
                    text = "신고 완료",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportScreenPreview() {
    ReportScreen()
}
