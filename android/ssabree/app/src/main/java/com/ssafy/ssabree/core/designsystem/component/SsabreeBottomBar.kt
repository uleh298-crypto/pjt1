// core/designsystem/component/SsabreeBottomBar.kt
package com.ssafy.ssabree.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.composed

sealed class BottomNavItem(
    val route: String,
    val label: String
) {
    object GroupManage : BottomNavItem("group_manage", "나의 그룹")
    object Group : BottomNavItem("group", "그룹 찾기")
    object Home : BottomNavItem("home", "HOME")
    object Board : BottomNavItem("board", "게시판")
    object Message : BottomNavItem("message", "쪽지")
}

@Composable
fun SsabreeBottomBar(
    currentRoute: String?,
    onItemSelected: (BottomNavItem) -> Unit
) {
    val items = listOf(
        BottomNavItem.GroupManage,
        BottomNavItem.Group,
        BottomNavItem.Home,
        BottomNavItem.Board,
        BottomNavItem.Message
    )

    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = colorScheme.surface
    ) {
        val iconSize = 30.dp
        val labelSize = 11.sp
        val homeCircleSize = 58.dp

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route ||
                    currentRoute?.startsWith("${item.route}/") == true

                val icon = when (item) {
                    BottomNavItem.GroupManage -> Icons.Default.SupervisorAccount
                    BottomNavItem.Group -> Icons.Default.Groups
                    BottomNavItem.Home -> Icons.Default.Home
                    BottomNavItem.Board -> Icons.Default.List
                    BottomNavItem.Message -> Icons.Default.Email
                }

                val iconColor =
                    if (isSelected) colorScheme.primary
                    else colorScheme.onSurface.copy(alpha = 0.6f)

                val textColor =
                    if (isSelected) colorScheme.primary
                    else colorScheme.onSurface.copy(alpha = 0.6f)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .noRippleClickable { onItemSelected(item) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (item == BottomNavItem.Home && isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(homeCircleSize)
                                    .shadow(4.dp, CircleShape)
                                    .background(
                                        color = colorScheme.primary,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = item.label,
                                        tint = colorScheme.onPrimary,
                                        modifier = Modifier.size(iconSize)
                                    )
                                    Text(
                                        text = item.label,
                                        fontSize = labelSize,
                                        color = colorScheme.onPrimary
                                    )
                                }
                            }
                        } else {
                            Icon(
                                imageVector = icon,
                                contentDescription = item.label,
                                tint = iconColor,
                                modifier = Modifier.size(iconSize)
                            )
                            Text(
                                text = item.label,
                                fontSize = labelSize,
                                color = textColor
                            )
                        }
                    }
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

@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000,
    showSystemUi = false
)
@Composable
fun SsabreeBottomBarPreview_Home() {
    SsabreeBottomBar(
        currentRoute = BottomNavItem.Home.route,
        onItemSelected = {}
    )
}
