package com.ssafy.ssabree.features.join.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.ssabree.R
import com.ssafy.ssabree.core.datasource.remote.model.Ban
import com.ssafy.ssabree.core.datasource.remote.model.Campus
import com.ssafy.ssabree.core.designsystem.theme.LoginButton

@Composable
internal fun JoinStep1(
    name: String,
    onNameChange: (String) -> Unit,
    studentId: String,
    onStudentIdChange: (String) -> Unit,
    // 기수 드롭다운
    generations: List<Int>,
    selectedGeneration: Int?,
    isGenerationDropdownExpanded: Boolean,
    onGenerationDropdownClick: () -> Unit,
    onGenerationDropdownDismiss: () -> Unit,
    onGenerationSelected: (Int) -> Unit,
    // 캠퍼스 드롭다운
    campuses: List<Campus>,
    selectedCampus: Campus?,
    isCampusDropdownExpanded: Boolean,
    isLoadingCampuses: Boolean,
    onCampusDropdownClick: () -> Unit,
    onCampusDropdownDismiss: () -> Unit,
    onCampusSelected: (Campus) -> Unit,
    // 반 드롭다운
    classes: List<Ban>,
    selectedClass: Ban?,
    isClassDropdownExpanded: Boolean,
    isLoadingClasses: Boolean,
    onClassDropdownClick: () -> Unit,
    onClassDropdownDismiss: () -> Unit,
    onClassSelected: (Ban) -> Unit,
    // 스텝 완료
    isStep1Valid: Boolean,
    onStepCompleted: () -> Unit
) {
    val scrollState = rememberScrollState()

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

        JoinLabeledField(
            label = "이름",
            value = name,
            onValueChange = onNameChange,
            placeholder = "이름"
        )

        Spacer(modifier = Modifier.height(12.dp))

        JoinLabeledField(
            label = "학번",
            value = studentId,
            onValueChange = onStudentIdChange,
            placeholder = "숫자만 입력",
            keyboardType = KeyboardType.Number
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 기수 드롭다운
        JoinDropdownField(
            label = "기수",
            selectedText = selectedGeneration?.toString() ?: "",
            placeholder = "기수를 선택",
            expanded = isGenerationDropdownExpanded,
            isLoading = false,
            onDropdownClick = onGenerationDropdownClick,
            onDismiss = onGenerationDropdownDismiss,
            items = generations,
            itemText = { "${it}기" },
            onItemSelected = onGenerationSelected
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 캠퍼스 드롭다운
        JoinDropdownField(
            label = "캠퍼스",
            selectedText = selectedCampus?.name ?: "",
            placeholder = "캠퍼스를 선택",
            expanded = isCampusDropdownExpanded,
            isLoading = isLoadingCampuses,
            onDropdownClick = onCampusDropdownClick,
            onDismiss = onCampusDropdownDismiss,
            items = campuses,
            itemText = { it.name },
            onItemSelected = onCampusSelected
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 반 드롭다운
        JoinDropdownField(
            label = "1학기 반",
            selectedText = selectedClass?.classNo?.toString()
                ?: selectedClass?.name.orEmpty(),
            placeholder = when {
                selectedGeneration == null -> "기수를 먼저 선택하세요"
                selectedCampus == null -> "캠퍼스를 먼저 선택하세요"
                else -> "반을 선택"
            },
            expanded = isClassDropdownExpanded,
            isLoading = isLoadingClasses,
            enabled = selectedCampus != null && selectedGeneration != null,
            onDropdownClick = onClassDropdownClick,
            onDismiss = onClassDropdownDismiss,
            items = classes,
            itemText = { ban ->
                ban.classNo?.toString() ?: ban.name
            },
            onItemSelected = onClassSelected
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onStepCompleted,
            enabled = isStep1Valid,
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
            Text("다음", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun <T> JoinDropdownField(
    label: String,
    selectedText: String,
    placeholder: String,
    expanded: Boolean,
    isLoading: Boolean,
    enabled: Boolean = true,
    onDropdownClick: () -> Unit,
    onDismiss: () -> Unit,
    items: List<T>,
    itemText: (T) -> String,
    onItemSelected: (T) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                    .clickable(enabled = enabled && !isLoading) {
                        focusManager.clearFocus()
                        onDropdownClick()
                    }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = selectedText.ifEmpty { placeholder },
                        color = if (selectedText.isEmpty()) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
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
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(itemText(item)) },
                        onClick = {
                            focusManager.clearFocus()
                            onItemSelected(item)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun JoinStep1Preview() {
    JoinStep1(
        name = "", onNameChange = {},
        studentId = "", onStudentIdChange = {},
        generations = listOf(14, 15),
        selectedGeneration = 14,
        isGenerationDropdownExpanded = false,
        onGenerationDropdownClick = {},
        onGenerationDropdownDismiss = {},
        onGenerationSelected = {},
        campuses = listOf(Campus(1, "서울"), Campus(2, "대전")),
        selectedCampus = null,
        isCampusDropdownExpanded = false,
        isLoadingCampuses = false,
        onCampusDropdownClick = {},
        onCampusDropdownDismiss = {},
        onCampusSelected = {},
        classes = listOf(
            Ban(
                id = 3,
                name = "1반",
                campus = Campus(1, "구미"),
                generation = 10,
                classNo = 1,
                trackType = "JAVA_BACKEND",
                createdAt = null,
                deletedAt = null,
                updatedAt = null
            )
        ),
        selectedClass = null,
        isClassDropdownExpanded = false,
        isLoadingClasses = false,
        onClassDropdownClick = {},
        onClassDropdownDismiss = {},
        onClassSelected = {},
        isStep1Valid = false,
        onStepCompleted = {}
    )
}
