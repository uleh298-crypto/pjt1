package com.ssafy.ssabree.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.ssafy.ssabree.core.designsystem.theme.SsabreeTheme
import com.ssafy.ssabree.core.designsystem.theme.ThemeMode
import com.ssafy.ssabree.core.utils.FcmTokenSyncer
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.core.utils.PushEvent
import com.ssafy.ssabree.core.utils.PushEventBus
import com.ssafy.ssabree.core.utils.ThemePreferenceManager
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private lateinit var themePreferenceManager: ThemePreferenceManager
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }
    private var navControllerState: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themePreferenceManager = ThemePreferenceManager(this)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        FcmTokenSyncer.syncIfAuthenticated()
        setContent {
            val navController = rememberNavController()
            navControllerState = navController
            var themeMode by remember { mutableStateOf(themePreferenceManager.getThemeMode()) }
            val deepLinkTarget = intent.toDeepLinkTarget()
            val startRoute = deepLinkTarget?.preferredStartDestination ?: deepLinkTarget?.route

            SsabreeTheme(
                themeMode = themeMode,
                onThemeModeChange = { newMode ->
                    themeMode = newMode
                    themePreferenceManager.saveThemeMode(newMode)
                }
            ) {
                AppRoot(
                    navController = navController,
                    startIntent = intent,
                    startDestinationOverride = startRoute
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        navControllerState?.let { handleDeepLink(it, intent, fromNewIntent = true) }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer is not provided")
}

@Composable
fun AppRoot(
    navController: NavHostController,
    container: AppContainer = RealAppContainer(), // 실제 Repository들 이용
    startIntent: Intent? = null,
    startDestinationOverride: String? = null,
//    container: AppContainer = FakeAppContainer(), // 테스트용 Repository -> 서버에 연결하지 않고 기능 실험용
) {
    CompositionLocalProvider(LocalAppContainer provides container) {
        LaunchedEffect(Unit) {
            PushEventBus.events.collectLatest { event ->
                when (event) {
                    is PushEvent.OpenChat -> {
                        // 포그라운드에서는 자동 내비게이션하지 않고 목록만 새로고침하도록 이벤트만 전달
                    }

                    is PushEvent.OpenPost -> {
                        // 포그라운드에서는 자동 내비게이션하지 않음 (채팅과 동일하게 처리)
                    }

                    is PushEvent.OpenGroupApplication -> {
                        // 포그라운드에서는 자동 내비게이션하지 않음
                    }

                    is PushEvent.OpenApplicationAccepted -> {
                        // 포그라운드에서는 자동 내비게이션하지 않음
                    }
                }
            }
        }

        LaunchedEffect(startIntent) {
            handleDeepLink(navController, startIntent, fromNewIntent = false)
        }

        AppNavGraph(
            navController = navController,
            startDestination = startDestinationOverride ?: Screens.SplashScreen.route
        )
    }
}

private data class DeepLinkTarget(
    val route: String,
    val preferredStartDestination: String? = null
)

private fun Intent?.toDeepLinkTarget(): DeepLinkTarget? {
    val data = this?.data ?: return null
    val pathSegments = data.pathSegments
    return when (data.host) {
        "chat" -> data.lastPathSegment?.toLongOrNull()?.let {
            DeepLinkTarget(
                route = Screens.messageDetailRoute(it),
                preferredStartDestination = Screens.MessageScreen.route
            )
        }
        "post" -> data.lastPathSegment?.toLongOrNull()?.let {
            DeepLinkTarget(
                route = Screens.boardDetailRoute(it),
                preferredStartDestination = Screens.boardRoute(0)
            )
        }
        "group-application" -> {
            // ssabree://group-application/{groupType}/{groupId}
            val groupType = pathSegments.getOrNull(0) ?: "study"
            val groupId = pathSegments.getOrNull(1)?.toLongOrNull() ?: return null
            val groupKind = if (groupType == "project") GroupKind.PROJECT else GroupKind.STUDY
            DeepLinkTarget(
                route = Screens.memberManageRoute(groupKind, groupId),
                preferredStartDestination = Screens.GroupManageScreen.route
            )
        }
        "application-accepted" -> {
            // ssabree://application-accepted/{groupType}/{groupId}
            val groupType = pathSegments.getOrNull(0) ?: "study"
            val groupId = pathSegments.getOrNull(1)?.toLongOrNull() ?: return null
            val groupKind = if (groupType == "project") GroupKind.PROJECT else GroupKind.STUDY
            DeepLinkTarget(
                route = Screens.myGroupDetailRoute(groupKind, groupId, false),
                preferredStartDestination = Screens.GroupManageScreen.route
            )
        }
        else -> null
    }
}

private fun handleDeepLink(
    navController: NavHostController,
    intent: Intent?,
    fromNewIntent: Boolean
) {
    val target = intent.toDeepLinkTarget() ?: return

    // fromNewIntent가 아닌 경우 (앱 시작 시) 네비게이션은 AppNavGraph의 startDestination으로 처리됨
    // 여기서는 fromNewIntent인 경우(foreground/background에서 알림 클릭)만 처리
    if (!fromNewIntent) return

    val startDest = target.preferredStartDestination
    if (startDest != null) {
        // 먼저 부모 화면으로 이동 (백스택 구성)
        navController.navigate(startDest) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // 목표 화면으로 이동
    navController.navigate(target.route) {
        launchSingleTop = true
    }
}
