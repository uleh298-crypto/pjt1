package com.ssafy.ssabree.app

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.ssafy.ssabree.core.designsystem.component.BottomNavItem
import com.ssafy.ssabree.core.designsystem.component.SsabreeBottomBar
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.utils.AuthEvent
import com.ssafy.ssabree.core.utils.AuthEventBus
import com.ssafy.ssabree.core.utils.AuthLogoutReason

import com.ssafy.ssabree.features.board.screen.BoardDetailScreen
import com.ssafy.ssabree.features.board.screen.BoardEditScreen
import com.ssafy.ssabree.features.board.screen.BoardScreen
import com.ssafy.ssabree.features.board.screen.BoardWriteScreen
import com.ssafy.ssabree.features.board.screen.BoardSearchScreen
import com.ssafy.ssabree.features.dday.screen.DdayDetailScreen
import com.ssafy.ssabree.features.findidpass.screen.FindIdPassScreen
import com.ssafy.ssabree.features.group.screen.GroupScreen
import com.ssafy.ssabree.features.group.screen.SelectGroupScreen
import com.ssafy.ssabree.features.group.model.GroupKind
import com.ssafy.ssabree.features.groupwrite.screen.GroupWriteScreen
import com.ssafy.ssabree.features.home.screen.HomeScreen
import com.ssafy.ssabree.features.join.screen.JoinScreen
import com.ssafy.ssabree.features.login.screen.LoginScreen
import com.ssafy.ssabree.features.message.screen.MessageScreen
import com.ssafy.ssabree.features.messagedetail.screen.MessageDetailScreen
import com.ssafy.ssabree.features.notification.screen.NotificationScreen
import com.ssafy.ssabree.features.splash.SplashScreen
import com.ssafy.ssabree.features.groupdetail.screen.GroupDetailScreen
import com.ssafy.ssabree.features.groupapply.screen.GroupApplyScreen
import com.ssafy.ssabree.features.mypage.screen.MyPageScreen
import com.ssafy.ssabree.features.mypage.screen.MyPostsScreen
import com.ssafy.ssabree.features.mypage.screen.MyCommentsScreen
import com.ssafy.ssabree.features.mypage.screen.MyScrapsScreen
import com.ssafy.ssabree.features.mypage.screen.SettingScreen
import com.ssafy.ssabree.features.mypage.screen.PortfolioDetailScreen
import com.ssafy.ssabree.features.mypage.screen.ProjectWriteScreen
import com.ssafy.ssabree.features.notification.screen.NotificationDetailScreen
import com.ssafy.ssabree.features.report.screen.ReportScreen
import com.ssafy.ssabree.features.mypage.screen.InquiryScreen
import com.ssafy.ssabree.features.mypage.screen.CommunityRulesScreen
import com.ssafy.ssabree.features.mypage.screen.RestrictionHistoryScreen
import com.ssafy.ssabree.features.mypage.screen.TermsOfServiceScreen

import com.ssafy.ssabree.features.mygroup.screen.MyGroupScreen
import com.ssafy.ssabree.features.mygroup.screen.MyGroupAllScreen
import com.ssafy.ssabree.features.mygroup.screen.GroupManageScreen
import com.ssafy.ssabree.features.mygroup.screen.MemberManageScreen
import com.ssafy.ssabree.features.mygroup.screen.MyGroupDetailScreen
import com.ssafy.ssabree.features.mygroup.screen.MyProgressScreen
import com.ssafy.ssabree.features.mygroup.screen.TaskDetailScreen
import com.ssafy.ssabree.features.mygroup.screen.TaskEditScreen
import com.ssafy.ssabree.features.mygroup.screen.ApplicantPortfolioScreen
import com.ssafy.ssabree.features.mygroup.screen.ApplicantPortfolioDetailScreen
import com.ssafy.ssabree.features.mygroup.screen.AnnouncementsScreen
import com.ssafy.ssabree.features.mygroup.screen.WriteAnnouncementScreen
import com.ssafy.ssabree.features.mygroup.screen.EditAnnouncementScreen
import com.ssafy.ssabree.features.mygroup.screen.MyApplicationsScreen
import kotlinx.coroutines.flow.collectLatest

import java.net.URLDecoder
import java.net.URLEncoder

data class Route(val route: String, val args: List<String> = emptyList())

object Screens {
    val SplashScreen = Route("splash")
    val LoginScreen = Route("login")
    val JoinScreen = Route("login/join")
    val FindIdPassScreen = Route("login/find_id_pass")
    val HomeScreen = Route("home")
    val GroupManageScreen = Route("group_manage")
    val SelectGroupScreen = Route("group/select")
    val GroupScreen = Route("group/list/{type}", listOf("type"))
    val GroupWriteScreen = Route(
        "group/write/{tabIndex}/{isEditMode}?groupId={groupId}",
        listOf("tabIndex", "isEditMode", "groupId")
    )
    val GroupDetailScreen = Route("group/detail/{type}/{groupId}", listOf("type", "groupId"))
    val GroupApplyScreen = Route("group/apply/{type}/{groupId}", listOf("type", "groupId"))
    val BoardScreen = Route("board/{boardId}", listOf("boardId"))
    val BoardWriteScreen = Route("board/write")
    val BoardDetailScreen = Route("board/detail/{postId}", listOf("postId"))
    val BoardEditScreen = Route("board/edit/{postId}", listOf("postId"))
    val BoardSearchScreen = Route("board/search")
    val MessageScreen = Route("message")
    val MessageDetailScreen = Route("message/detail/{roomId}", listOf("roomId"))
    val NewChatScreen = Route("message/new/{postId}", listOf("postId"))

    fun newChatRoute(postId: Long) = "message/new/$postId"
    val NotificationScreen = Route("notification")
    val MyPageScreen = Route("mypage")
    val MyPostsScreen = Route("mypage/posts")
    val MyCommentsScreen = Route("mypage/comments")
    val MyScrapsScreen = Route("mypage/scraps")
    val PortfolioDetailScreen = Route("portfolio/detail")
    val ProjectWriteScreen = Route("portfolio/project/{portfolioId}", listOf("portfolioId"))
    val ProjectEditScreen = Route("portfolio/project/{portfolioId}/{projectId}", listOf("portfolioId", "projectId"))
    val ApplicantPortfolioScreen = Route(
        "portfolio/applicant/{portfolioId}?applicationId={applicationId}&groupKind={groupKind}",
        listOf("portfolioId", "applicationId", "groupKind")
    )
    val ApplicantPortfolioDetailScreen = Route("portfolio/applicant/detail/{portfolioId}", listOf("portfolioId"))
    val DdayDetailScreen = Route("dday/detail")
    val MemberManageScreen = Route("member_manage/{type}/{groupId}", listOf("type", "groupId"))
    val MyProgressScreen = Route("group/progress/{type}/{groupId}", listOf("type", "groupId"))
    val TaskDetailScreen = Route("group/task/{type}/{groupId}/{taskId}", listOf("type", "groupId", "taskId"))
    val TaskEditScreen = Route("group/task/edit/{type}/{groupId}/{taskId}", listOf("type", "groupId", "taskId"))
    val SettingScreen = Route("setting_screen")
    val NotificationDetailScreen = Route("notification/detail")
    val ReportScreen = Route("report")

    val InquiryScreen = Route("mypage/inquiry")
    val CommunityRulesScreen = Route("mypage/community_rules")
    val RestrictionHistoryScreen = Route("mypage/restriction_history")
    val AnnouncementsScreen = Route("announcements/{type}/{groupId}/{isLeader}", listOf("type", "groupId", "isLeader"))
    val WriteAnnouncementScreen = Route("announcements/write/{type}/{groupId}", listOf("type", "groupId"))
    val EditAnnouncementScreen = Route(
        "announcements/edit/{type}/{groupId}/{noticeId}/{title}/{content}/{isPinned}",
        listOf("type", "groupId", "noticeId", "title", "content", "isPinned")
    )
    val TermsOfServiceScreen = Route("mypage/terms_of_service")
    val MyGroupScreen = Route("group_manage/list/{type}", listOf("type"))
    val MyGroupDetailScreen = Route("mygroup/detail/{type}/{groupId}/{isLeader}", listOf("type", "groupId", "isLeader"))
    val MyApplicationsScreen = Route("mygroup/applications/{type}", listOf("type"))
    fun boardDetailRoute(postId: Long) = "board/detail/$postId"
    fun boardEditRoute(postId: Long) = "board/edit/$postId"
    fun applicantPortfolioRoute(
        portfolioId: Long,
        applicationId: Long? = null,
        groupKind: GroupKind? = null
    ): String {
        val base = "portfolio/applicant/$portfolioId"
        if (applicationId == null && groupKind == null) return base
        val appId = applicationId ?: -1L
        val kind = groupKind?.routeValue.orEmpty()
        return "$base?applicationId=$appId&groupKind=$kind"
    }
    fun applicantPortfolioDetailRoute(portfolioId: Long) = "portfolio/applicant/detail/$portfolioId"
    fun myGroupDetailRoute(kind: GroupKind, groupId: Long, isLeader: Boolean) =
        "mygroup/detail/${kind.routeValue}/$groupId/$isLeader"
    fun myApplicationsRoute(kind: GroupKind) = "mygroup/applications/${kind.routeValue}"
    fun groupWriteRoute(tabIndex: Int, isEditMode: Boolean = false, groupId: Long? = null): String {
        return if (groupId == null) {
            "group/write/$tabIndex/$isEditMode"
        } else {
            "group/write/$tabIndex/$isEditMode?groupId=$groupId"
        }
    }
    fun groupDetailRoute(kind: GroupKind, groupId: Long) = "group/detail/${kind.routeValue}/$groupId"
    fun groupApplyRoute(kind: GroupKind, groupId: Long) = "group/apply/${kind.routeValue}/$groupId"
    fun groupListRoute(type: GroupKind) = "group/list/${type.routeValue}"
    fun myGroupRoute(type: GroupKind) = "group_manage/list/${type.routeValue}"
    fun boardRoute(boardId: Long) = "board/$boardId"

    fun editAnnouncementRoute(
        kind: GroupKind,
        groupId: Long,
        noticeId: Long,
        title: String,
        content: String,
        isPinned: Boolean
    ): String {
        val encodedTitle = URLEncoder.encode(if (title.isEmpty()) " " else title, "UTF-8")
        val encodedContent = URLEncoder.encode(if (content.isEmpty()) " " else content, "UTF-8")
        return "announcements/edit/${kind.routeValue}/$groupId/$noticeId/$encodedTitle/$encodedContent/$isPinned"
    }

    fun memberManageRoute(kind: GroupKind, groupId: Long) =
        "member_manage/${kind.routeValue}/$groupId"

    fun myProgressRoute(kind: GroupKind, groupId: Long) =
        "group/progress/${kind.routeValue}/$groupId"
    fun taskDetailRoute(kind: GroupKind, groupId: Long, taskId: Long) =
        "group/task/${kind.routeValue}/$groupId/$taskId"
    fun taskEditRoute(kind: GroupKind, groupId: Long, taskId: Long) =
        "group/task/edit/${kind.routeValue}/$groupId/$taskId"

    fun announcementsRoute(kind: GroupKind, groupId: Long, isLeader: Boolean) =
        "announcements/${kind.routeValue}/$groupId/$isLeader"

    fun writeAnnouncementRoute(kind: GroupKind, groupId: Long) =
        "announcements/write/${kind.routeValue}/$groupId"

    fun messageDetailRoute(roomId: Long) = "message/detail/$roomId"
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screens.SplashScreen.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    var lastBackPressedAt by remember { mutableStateOf(0L) }
    var hasNewApplicant by remember { mutableStateOf(false) }
    var shouldRefreshBoardList by remember { mutableStateOf(false) }
    var logoutReason by remember { mutableStateOf<AuthLogoutReason?>(null) }
    var hideBottomBar by remember { mutableStateOf(false) }
    LaunchedEffect(currentRoute) {
        hideBottomBar = false
    }
    val showBottomBar =
        currentRoute != Screens.LoginScreen.route &&
            currentRoute != Screens.JoinScreen.route &&
            currentRoute != Screens.SplashScreen.route &&
            currentRoute != Screens.FindIdPassScreen.route &&
            currentRoute != Screens.NotificationScreen.route &&
            currentRoute != Screens.BoardWriteScreen.route &&
            currentRoute != Screens.BoardDetailScreen.route &&
            currentRoute != Screens.BoardEditScreen.route &&
            currentRoute != Screens.BoardSearchScreen.route &&
            currentRoute?.startsWith("group/write") != true &&
            currentRoute != Screens.MyPageScreen.route &&
            currentRoute != Screens.MyPostsScreen.route &&
            currentRoute != Screens.MyCommentsScreen.route &&
            currentRoute != Screens.MyScrapsScreen.route &&
            currentRoute != Screens.PortfolioDetailScreen.route &&
            currentRoute?.startsWith("portfolio/project") != true &&
            currentRoute?.startsWith("portfolio/applicant") != true &&
            currentRoute != Screens.MessageDetailScreen.route &&
            currentRoute?.startsWith("message/new") != true &&
            currentRoute != Screens.DdayDetailScreen.route &&
            currentRoute?.startsWith("member_manage") != true &&
            currentRoute?.startsWith("group/progress") != true &&
            currentRoute?.startsWith("group/task") != true &&
            currentRoute != Screens.SettingScreen.route &&
            currentRoute != Screens.NotificationDetailScreen.route &&
            currentRoute != Screens.ReportScreen.route &&
            currentRoute != Screens.InquiryScreen.route &&
            currentRoute != Screens.CommunityRulesScreen.route &&
            currentRoute != Screens.RestrictionHistoryScreen.route &&
            currentRoute?.startsWith("announcements/") != true &&
            currentRoute != Screens.TermsOfServiceScreen.route &&
            currentRoute?.startsWith("group/list") != true &&
            currentRoute?.startsWith("group_manage/list") != true &&
            currentRoute?.startsWith("group_manage/") != true &&
            currentRoute?.startsWith("mygroup/detail") != true &&
            currentRoute?.startsWith("mygroup/applications") != true &&
            currentRoute?.startsWith("announcements/edit") != true &&
            currentRoute?.startsWith("group/detail") != true &&
            currentRoute?.startsWith("group/apply") != true &&
            !hideBottomBar

    if (showBottomBar) {
        BackHandler {
            val now = System.currentTimeMillis()
            if (now - lastBackPressedAt <= 2000L) {
                (context as? Activity)?.finishAffinity()
            } else {
                lastBackPressedAt = now
                Toast.makeText(context, "종료하시려면 한 번 더 눌러주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        ),
        bottomBar = {
            if (showBottomBar) {
                SsabreeBottomBar(
                        currentRoute = currentRoute,
                        onItemSelected = { item ->
                            val targetRoute = when (item) {
                                BottomNavItem.Home -> Screens.HomeScreen.route
                                BottomNavItem.GroupManage -> Screens.GroupManageScreen.route
                                BottomNavItem.Group -> Screens.SelectGroupScreen.route
                                BottomNavItem.Board -> Screens.boardRoute(0)
                                BottomNavItem.Message -> Screens.MessageScreen.route
                            }

                            // 현재 탭과 목적지 탭이 같은지 확인
                        val isAlreadyInTargetTab = when (item) {
                            BottomNavItem.Board -> currentRoute?.startsWith("board/") == true
                            else -> currentRoute == targetRoute
                        }

                        if (!isAlreadyInTargetTab) {
                            navController.navigate(targetRoute) {
                                // 탭 전환 시의 표준 백스택 관리
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        LaunchedEffect(Unit) {
            AuthEventBus.events.collectLatest { event ->
                if (event is AuthEvent.Logout) {
                    logoutReason = event.reason
                }
            }
        }

        logoutReason?.let { reason ->
            val (title, message) = when (reason) {
                AuthLogoutReason.RETRY_EXCEEDED -> "로그인 만료" to "인증이 반복 실패하여 로그아웃되었습니다.\n다시 로그인해 주세요."
                AuthLogoutReason.REFRESH_TOKEN_MISSING -> "로그인 만료" to "로그인 정보가 없어 로그아웃되었습니다.\n다시 로그인해 주세요."
                AuthLogoutReason.REFRESH_FAILED -> "로그인 만료" to "세션 갱신에 실패하여 로그아웃되었습니다.\n다시 로그인해 주세요."
                AuthLogoutReason.USER_LOGOUT -> "로그아웃" to "사용자 요청으로 로그아웃되었습니다."
                AuthLogoutReason.USER_WITHDRAW -> "회원 탈퇴" to "회원 탈퇴가 완료되어 로그아웃되었습니다."
            }
            SsabreeDialog(
                onDismissRequest = {},
                title = title,
                message = message,
                confirmText = "확인",
                showDismissButton = false,
                onConfirm = {
                    logoutReason = null
                    navController.navigate(Screens.LoginScreen.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable(Screens.SplashScreen.route) {
                SplashScreen(onSplashFinished = { isLoggedIn ->
                    val nextRoute =
                        if (isLoggedIn) Screens.HomeScreen.route else Screens.LoginScreen.route
                    navController.navigate(nextRoute) {
                        popUpTo(Screens.SplashScreen.route) { inclusive = true }
                    }
                })
            }

            composable(Screens.LoginScreen.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screens.HomeScreen.route) {
                            popUpTo(Screens.LoginScreen.route) { inclusive = true }
                        }
                    },
                    onJoinClick = { navController.navigate(Screens.JoinScreen.route) },
                    onFindIdPassClick = { navController.navigate(Screens.FindIdPassScreen.route) }
                )
            }

            composable(Screens.JoinScreen.route) {
                JoinScreen(
                    onBackClick = { navController.popBackStack() },
                    onJoinCompleted = { navController.popBackStack() })
            }

            composable(Screens.FindIdPassScreen.route) {
                FindIdPassScreen(
                    onBackClick = { navController.popBackStack() },
                    onConfirm = {
                        navController.navigate(Screens.LoginScreen.route) {
                            popUpTo(Screens.FindIdPassScreen.route) { inclusive = true }
                        }
                    },
                    onResetConfirm = {
                        navController.navigate(Screens.LoginScreen.route) {
                            popUpTo(Screens.FindIdPassScreen.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screens.HomeScreen.route) {
                HomeScreen(
                    onNotificationClick = { navController.navigate(Screens.NotificationScreen.route) },
                    onMyPageClick = { navController.navigate(Screens.MyPageScreen.route) },
                    onDdayClick = { navController.navigate(Screens.DdayDetailScreen.route) },
                    onGroupClick = { kind ->
                        navController.navigate(Screens.groupListRoute(kind))
                    },
                    onBoardClick = { boardId -> 
                        val targetRoute = Screens.boardRoute(boardId)
                        navController.navigate(targetRoute) {
                            // 홈에서 게시판으로 넘어갈 때 탭 전환 처리
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(
                route = Screens.GroupScreen.route,
                arguments = listOf(navArgument("type") { type = NavType.StringType })
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type")
                val groupKind = GroupKind.fromRoute(type)
                val refreshFlow = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.getStateFlow("group_list_refresh", false)
                GroupScreen(
                    groupKind = groupKind,
                    shouldRefresh = refreshFlow?.collectAsState(false)?.value ?: false,
                    onRefreshConsumed = {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("group_list_refresh", false)
                    },
                    onBackClick = { navController.popBackStack() },
                    onFabClick = { selectedKind ->
                        val tabIndex = if (selectedKind == GroupKind.STUDY) 0 else 1
                        navController.navigate(Screens.groupWriteRoute(tabIndex))
                    },
                    onDetailClick = { groupId ->
                        navController.navigate(Screens.groupDetailRoute(groupKind, groupId))
                    }
                )
            }

            composable(
                route = Screens.GroupDetailScreen.route,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("groupId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type")
                val groupKind = GroupKind.fromRoute(type)
                val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
                GroupDetailScreen(
                    groupId = groupId,
                    groupKind = groupKind,
                    onBackClick = { navController.popBackStack() },
                    onApplyClick = { navController.navigate(Screens.groupApplyRoute(groupKind, groupId)) },
                    onEditClick = { id, kind ->
                        val tabIndex = if (kind == GroupKind.STUDY) 0 else 1
                        navController.navigate(
                            Screens.groupWriteRoute(tabIndex, isEditMode = true, groupId = id)
                        )
                    },
                    onDeleteSuccess = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("group_list_refresh", true)
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screens.GroupApplyScreen.route,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("groupId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type")
                val groupKind = GroupKind.fromRoute(type)
                val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
                GroupApplyScreen(
                    groupId = groupId,
                    groupKind = groupKind,
                    shouldRefreshPortfolio = navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.getStateFlow("mypage_refresh", false)
                        ?.collectAsState(false)
                        ?.value ?: false,
                    onRefreshConsumed = {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("mypage_refresh", false)
                    },
                    onBackClick = { navController.popBackStack() },
                    onPortfolioDetailClick = { _, _, _ ->
                        navController.navigate(Screens.PortfolioDetailScreen.route)
                    },
                    onSubmitClick = {
                        hasNewApplicant = true
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screens.GroupWriteScreen.route,
                arguments = listOf(
                    navArgument("tabIndex") { type = NavType.IntType },
                    navArgument("isEditMode") { type = NavType.BoolType },
                    navArgument("groupId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { backStackEntry ->
                val tabIndex = backStackEntry.arguments?.getInt("tabIndex") ?: 0
                val isEditMode = backStackEntry.arguments?.getBoolean("isEditMode") ?: false
                val groupIdArg = backStackEntry.arguments?.getLong("groupId") ?: -1L
                val groupId = groupIdArg.takeIf { it > 0 }
                GroupWriteScreen(
                    initialTab = tabIndex,
                    isEditMode = isEditMode,
                    groupId = groupId,
                    onBackClick = { navController.popBackStack() },
                    onSubmitSuccess = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("group_list_refresh", true)
                        runCatching {
                            navController.getBackStackEntry(Screens.MyGroupDetailScreen.route)
                                .savedStateHandle
                                .set("mygroup_detail_refresh", true)
                        }
                        navController.popBackStack()
                    }
                )
            }

            composable(Screens.SelectGroupScreen.route) {
                SelectGroupScreen(
                    onSelect = { kind -> navController.navigate(Screens.groupListRoute(kind)) }
                )
            }

            composable(Screens.GroupManageScreen.route) {
                val refreshFlow = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.getStateFlow("mygroup_list_refresh", false)
                MyGroupAllScreen(
                    shouldRefresh = refreshFlow?.collectAsState(false)?.value ?: false,
                    onRefreshConsumed = {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("mygroup_list_refresh", false)
                    },
                    onBackClick = { navController.popBackStack() },
                    onMyApplicationsClick = { kind ->
                        navController.navigate(Screens.myApplicationsRoute(kind))
                    },
                    onSearchModeChange = { isSearchMode -> hideBottomBar = isSearchMode },
                    onDetailClick = { kind, groupId, isLeader ->
                        navController.navigate(Screens.myGroupDetailRoute(kind, groupId, isLeader))
                    }
                )
            }

            composable(
                route = Screens.MyGroupScreen.route,
                arguments = listOf(navArgument("type") { type = NavType.StringType })
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type")
                val groupKind = GroupKind.fromRoute(type)
                val refreshFlow = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.getStateFlow("mygroup_list_refresh", false)
                MyGroupScreen(
                    groupKind = groupKind,
                    shouldRefresh = refreshFlow?.collectAsState(false)?.value ?: false,
                    onRefreshConsumed = {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("mygroup_list_refresh", false)
                    },
                    onBackClick = { navController.popBackStack() },
                    onMyApplicationsClick = {
                        navController.navigate(Screens.myApplicationsRoute(groupKind))
                    },
                    onSearchModeChange = { isSearchMode -> hideBottomBar = isSearchMode },
                    onDetailClick = { groupId, isLeader ->
                        navController.navigate(Screens.myGroupDetailRoute(groupKind, groupId, isLeader))
                    }
                )
            }

            composable(
                route = Screens.MyApplicationsScreen.route,
                arguments = listOf(navArgument("type") { type = NavType.StringType })
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type")
                val groupKind = GroupKind.fromRoute(type)
                MyApplicationsScreen(
                    groupKind = groupKind,
                    onBackClick = { navController.popBackStack() },
                    onGroupDetailClick = { groupId ->
                        navController.navigate(Screens.groupDetailRoute(groupKind, groupId))
                    }
                )
            }

            composable(
                route = "${Screens.GroupManageScreen.route}/{type}/{groupId}/{isLeader}",
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("groupId") { type = NavType.LongType },
                    navArgument("isLeader") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type")
                val groupKind = GroupKind.fromRoute(type)
                val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
                val isLeader = backStackEntry.arguments?.getBoolean("isLeader") ?: false

                GroupManageScreen(
                    groupId = groupId,
                    groupKind = groupKind,
                    isLeader = isLeader,
                    hasNewApplicant = hasNewApplicant,
                    onNotificationShown = { hasNewApplicant = false },
                    onBackClick = { navController.popBackStack() },
                    onMemberManageClick = { navController.navigate(Screens.memberManageRoute(groupKind, groupId)) },
                    onAddProgressClick = { navController.navigate(Screens.myProgressRoute(groupKind, groupId)) },
                    onAllAnnouncementsClick = { navController.navigate(Screens.announcementsRoute(groupKind, groupId, isLeader)) },
                    onTaskClick = { taskId -> navController.navigate(Screens.taskDetailRoute(groupKind, groupId, taskId)) },
                    onEditClick = {
                        val tabIndex = if (groupKind == GroupKind.STUDY) 0 else 1
                        navController.navigate(
                            Screens.groupWriteRoute(tabIndex, isEditMode = true, groupId = groupId)
                        )
                    },
                    onDeleteClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screens.MyGroupDetailScreen.route,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("groupId") { type = NavType.LongType },
                    navArgument("isLeader") { type = NavType.BoolType; defaultValue = false }
                ),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "ssabree://application-accepted/{type}/{groupId}/{isLeader}" }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type")
                val groupKind = GroupKind.fromRoute(type)
                val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
                val isLeader = backStackEntry.arguments?.getBoolean("isLeader") ?: false
                val refreshFlow = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.getStateFlow("mygroup_detail_refresh", false)

                MyGroupDetailScreen(
                    groupId = groupId,
                    groupKind = groupKind,
                    isLeader = isLeader,
                    hasNewApplicant = hasNewApplicant,
                    shouldRefresh = refreshFlow?.collectAsState(false)?.value ?: false,
                    onRefreshConsumed = {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("mygroup_detail_refresh", false)
                    },
                    onNotificationShown = { hasNewApplicant = false },
                    onBackClick = { navController.popBackStack() },
                    onEditClick = {
                        val tabIndex = if (groupKind == GroupKind.STUDY) 0 else 1
                        navController.navigate(
                            Screens.groupWriteRoute(tabIndex, isEditMode = true, groupId = groupId)
                        )
                    },
                    onDeleteSuccess = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("mygroup_list_refresh", true)
                        navController.popBackStack()
                    },
                    onLeaveSuccess = {
                        runCatching {
                            navController.getBackStackEntry(Screens.GroupManageScreen.route)
                                .savedStateHandle
                                .set("mygroup_list_refresh", true)
                        }
                        runCatching {
                            navController.getBackStackEntry(Screens.myGroupRoute(groupKind))
                                .savedStateHandle
                                .set("mygroup_list_refresh", true)
                        }
                        navController.popBackStack()
                    },
                    onMemberManageClick = { navController.navigate(Screens.memberManageRoute(groupKind, groupId)) },
                    onAddProgressClick = { navController.navigate(Screens.myProgressRoute(groupKind, groupId)) },
                    onAllAnnouncementsClick = { navController.navigate(Screens.announcementsRoute(groupKind, groupId, isLeader)) },
                    onTaskClick = { taskId -> navController.navigate(Screens.taskDetailRoute(groupKind, groupId, taskId)) },
                    onMemberClick = { portfolioId -> navController.navigate(Screens.applicantPortfolioRoute(portfolioId)) }
                )
            }

            composable(
                route = Screens.TaskDetailScreen.route,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("groupId") { type = NavType.LongType },
                    navArgument("taskId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type")
                val groupKind = GroupKind.fromRoute(type)
                val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
                val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
                TaskDetailScreen(
                    groupKind = groupKind,
                    groupId = groupId,
                    taskId = taskId,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = {
                        navController.navigate(Screens.taskEditRoute(groupKind, groupId, taskId))
                    },
                    onDeleteSuccess = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("mygroup_detail_refresh", true)
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screens.TaskEditScreen.route,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("groupId") { type = NavType.LongType },
                    navArgument("taskId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type")
                val groupKind = GroupKind.fromRoute(type)
                val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
                val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
                TaskEditScreen(
                    groupKind = groupKind,
                    groupId = groupId,
                    taskId = taskId,
                    onBackClick = { navController.popBackStack() },
                    onEditSuccess = {
                        runCatching {
                            navController.getBackStackEntry(Screens.MyGroupDetailScreen.route)
                                .savedStateHandle
                                .set("mygroup_detail_refresh", true)
                        }
                        navController.popBackStack()
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screens.BoardScreen.route,
                arguments = listOf(navArgument("boardId") { type = NavType.LongType; defaultValue = -1L })
            ) { backStackEntry ->
                val boardId = backStackEntry.arguments?.getLong("boardId") ?: -1L
                val refreshFlow = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.getStateFlow("board_list_refresh", false)
                BoardScreen(
                    initialBoardId = if (boardId != -1L && boardId != 0L) boardId else null,
                    shouldRefresh = refreshFlow?.collectAsState(false)?.value ?: shouldRefreshBoardList,
                    onRefreshConsumed = {
                        shouldRefreshBoardList = false
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("board_list_refresh", false)
                    },
                    onWriteClick = { navController.navigate(Screens.BoardWriteScreen.route) },
                    onPostClick = { post -> navController.navigate(Screens.boardDetailRoute(post.id)) },
                    onSearchClick = { navController.navigate(Screens.BoardSearchScreen.route) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screens.BoardWriteScreen.route) {
                BoardWriteScreen(
                    onCancel = { navController.popBackStack() },
                    onSubmitSuccess = {
                        shouldRefreshBoardList = true
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screens.BoardDetailScreen.route,
                arguments = listOf(navArgument("postId") { type = NavType.LongType }),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "ssabree://post/{postId}" }
                )
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getLong("postId") ?: 0L
                val refreshFlow = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.getStateFlow("board_detail_refresh", false)
                BoardDetailScreen(
                    postId = postId,
                    shouldRefresh = refreshFlow?.collectAsState(false)?.value ?: false,
                    onRefreshConsumed = {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("board_detail_refresh", false)
                    },
                    onBackClick = {
                        shouldRefreshBoardList = true
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("board_list_refresh", true)
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("mypage_list_refresh", true)
                        navController.popBackStack()
                    },
                    onMessageClick = { targetPostId ->
                        navController.navigate(Screens.newChatRoute(targetPostId))
                    },
                    onEditClick = { id -> navController.navigate(Screens.boardEditRoute(id)) },
                    onDeleteSuccess = {
                        shouldRefreshBoardList = true
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("board_list_refresh", true)
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("mypage_list_refresh", true)
                    }
                )
            }

            composable(
                route = Screens.BoardEditScreen.route,
                arguments = listOf(navArgument("postId") { type = NavType.LongType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getLong("postId") ?: 0L
                BoardEditScreen(
                    postId = postId,
                    onCancel = { navController.popBackStack() },
                    onSubmitSuccess = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("board_detail_refresh", true)
                        shouldRefreshBoardList = true
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("mypage_list_refresh", true)
                        navController.popBackStack()
                    }
                )
            }

            composable(Screens.BoardSearchScreen.route) {
                BoardSearchScreen(
                    onBackClick = { navController.popBackStack() },
                    onPostClick = { post -> navController.navigate(Screens.boardDetailRoute(post.id)) }
                )
            }

            composable(Screens.MessageScreen.route) {
                MessageScreen(
                    onMessageClick = { roomId ->
                        navController.navigate(Screens.messageDetailRoute(roomId))
                    }
                )
            }

            composable(
                route = Screens.MessageDetailScreen.route,
                arguments = listOf(navArgument("roomId") { type = NavType.LongType }),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "ssabree://chat/{roomId}" }
                )
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
                MessageDetailScreen(
                    roomId = roomId,
                    onBackClick = { navController.popBackStack() },
                    onExitSuccess = { navController.popBackStack() }
                )
            }

            composable(
                route = Screens.NewChatScreen.route,
                arguments = listOf(
                    navArgument("postId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getLong("postId") ?: 0L
                MessageDetailScreen(
                    roomId = 0,
                    postId = postId,
                    onBackClick = { navController.popBackStack() },
                    onExitSuccess = { navController.popBackStack() }
                )
            }

            composable(Screens.ReportScreen.route) {
                ReportScreen(
                    onBackClick = { navController.popBackStack() },
                    onReportSubmit = { navController.popBackStack() })
            }

            composable(Screens.MyPageScreen.route) {
                val refreshFlow = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.getStateFlow("mypage_refresh", false)
                MyPageScreen(
                    onBackClick = { navController.popBackStack() },
                    onLogout = { reason -> AuthEventBus.send(AuthEvent.Logout(reason)) },
                    onSettingClick = { navController.navigate(Screens.SettingScreen.route) },
                    onPortfolioDetailClick = { navController.navigate(Screens.PortfolioDetailScreen.route) },
                    onMyPostsClick = { navController.navigate(Screens.MyPostsScreen.route) },
                    onMyCommentsClick = { navController.navigate(Screens.MyCommentsScreen.route) },
                    onMyScrapsClick = { navController.navigate(Screens.MyScrapsScreen.route) },
                    refreshSignal = refreshFlow,
                    onRefreshConsumed = {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("mypage_refresh", false)
                    }
                )
            }

            composable(Screens.MyPostsScreen.route) {
                val refreshFlow = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.getStateFlow("mypage_list_refresh", false)
                MyPostsScreen(
                    shouldRefresh = refreshFlow?.collectAsState(false)?.value ?: false,
                    onRefreshConsumed = {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("mypage_list_refresh", false)
                    },
                    onBackClick = { navController.popBackStack() },
                    onPostClick = { postId -> navController.navigate(Screens.boardDetailRoute(postId)) }
                )
            }

            composable(Screens.MyCommentsScreen.route) {
                val refreshFlow = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.getStateFlow("mypage_list_refresh", false)
                MyCommentsScreen(
                    shouldRefresh = refreshFlow?.collectAsState(false)?.value ?: false,
                    onRefreshConsumed = {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("mypage_list_refresh", false)
                    },
                    onBackClick = { navController.popBackStack() },
                    onCommentClick = { postId -> navController.navigate(Screens.boardDetailRoute(postId)) }
                )
            }

            composable(Screens.MyScrapsScreen.route) {
                val refreshFlow = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.getStateFlow("mypage_list_refresh", false)
                MyScrapsScreen(
                    shouldRefresh = refreshFlow?.collectAsState(false)?.value ?: false,
                    onRefreshConsumed = {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("mypage_list_refresh", false)
                    },
                    onBackClick = { navController.popBackStack() },
                    onPostClick = { postId -> navController.navigate(Screens.boardDetailRoute(postId)) }
                )
            }

            composable(Screens.PortfolioDetailScreen.route) {
                val refreshFlow = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.getStateFlow("portfolio_refresh", false)
                PortfolioDetailScreen(
                    onBackClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("mypage_refresh", true)
                        navController.popBackStack()
                    },
                    onProjectCreateClick = { portfolioId ->
                        navController.navigate("portfolio/project/$portfolioId")
                    },
                    onProjectEditClick = { portfolioId, projectId ->
                        navController.navigate("portfolio/project/$portfolioId/$projectId")
                    },
                    refreshSignal = refreshFlow,
                    onRefreshConsumed = {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("portfolio_refresh", false)
                    }
                )
            }

            composable(
                route = Screens.ApplicantPortfolioScreen.route,
                arguments = listOf(
                    navArgument("portfolioId") { type = NavType.LongType },
                    navArgument("applicationId") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("groupKind") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val portfolioId = backStackEntry.arguments?.getLong("portfolioId") ?: 0L
                val applicationId = backStackEntry.arguments?.getLong("applicationId") ?: -1L
                val groupKind = backStackEntry.arguments?.getString("groupKind")
                    ?.takeIf { it.isNotBlank() }
                    ?.let { GroupKind.fromRoute(it) }
                ApplicantPortfolioScreen(
                    portfolioId = portfolioId,
                    applicationId = applicationId,
                    groupKind = groupKind,
                    onBackClick = { navController.popBackStack() },
                    onDetailClick = { navController.navigate(Screens.applicantPortfolioDetailRoute(portfolioId)) }
                )
            }

            composable(
                route = Screens.ApplicantPortfolioDetailScreen.route,
                arguments = listOf(navArgument("portfolioId") { type = NavType.LongType })
            ) { backStackEntry ->
                val portfolioId = backStackEntry.arguments?.getLong("portfolioId") ?: 0L
                ApplicantPortfolioDetailScreen(
                    portfolioId = portfolioId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = Screens.ProjectWriteScreen.route,
                arguments = listOf(navArgument("portfolioId") { type = NavType.LongType })
            ) { backStackEntry ->
                val portfolioId = backStackEntry.arguments?.getLong("portfolioId") ?: 0L
                ProjectWriteScreen(
                    portfolioId = portfolioId,
                    onBackClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("portfolio_refresh", true)
                        navController.popBackStack()
                    },
                    onSubmitSuccess = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("portfolio_refresh", true)
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screens.ProjectEditScreen.route,
                arguments = listOf(
                    navArgument("portfolioId") { type = NavType.LongType },
                    navArgument("projectId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val portfolioId = backStackEntry.arguments?.getLong("portfolioId") ?: 0L
                val projectId = backStackEntry.arguments?.getLong("projectId")
                ProjectWriteScreen(
                    portfolioId = portfolioId,
                    projectId = projectId,
                    onBackClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("portfolio_refresh", true)
                        navController.popBackStack()
                    },
                    onSubmitSuccess = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("portfolio_refresh", true)
                        navController.popBackStack()
                    }
                )
            }

            composable(Screens.SettingScreen.route) {
                SettingScreen(
                    onBackClick = { navController.popBackStack() },
                    onNotificationDetailClick = { navController.navigate(Screens.NotificationDetailScreen.route) },
                    onInquiryClick = { navController.navigate(Screens.InquiryScreen.route) },
                    onCommunityRulesClick = { navController.navigate(Screens.CommunityRulesScreen.route) },
                    onTermsClick = { navController.navigate(Screens.TermsOfServiceScreen.route) },
                    onLogout = { reason -> AuthEventBus.send(AuthEvent.Logout(reason)) }
                )
            }

            composable(Screens.TermsOfServiceScreen.route) {
                TermsOfServiceScreen(onBackClick = { navController.popBackStack() })
            }

            composable(Screens.NotificationDetailScreen.route) {
                NotificationDetailScreen(onBackClick = { navController.popBackStack() })
            }

            composable(
                route = Screens.AnnouncementsScreen.route,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("groupId") { type = NavType.LongType },
                    navArgument("isLeader") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type")
                val groupKind = GroupKind.fromRoute(type)
                val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
                val isLeader = backStackEntry.arguments?.getBoolean("isLeader") ?: false
                val refreshFlow = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.getStateFlow("announcements_refresh", false)
                AnnouncementsScreen(
                    groupId = groupId,
                    groupKind = groupKind,
                    isLeader = isLeader,
                    shouldRefresh = refreshFlow?.collectAsState(false)?.value ?: false,
                    onRefreshConsumed = {
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("announcements_refresh", false)
                    },
                    onBackClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("mygroup_detail_refresh", true)
                        navController.popBackStack()
                    },
                    onNoticeChanged = {
                        runCatching {
                            navController.getBackStackEntry(Screens.MyGroupDetailScreen.route)
                                .savedStateHandle
                                .set("mygroup_detail_refresh", true)
                        }
                    },
                    onWriteAnnouncementClick = { isEdit, noticeId, title, content, isPinned ->
                        if (isEdit) {
                            navController.navigate(
                                Screens.editAnnouncementRoute(groupKind, groupId, noticeId, title, content, isPinned)
                            )
                        } else {
                            navController.navigate(Screens.writeAnnouncementRoute(groupKind, groupId))
                        }
                    }
                )
            }

            composable(
                route = Screens.WriteAnnouncementScreen.route,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("groupId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type")
                val groupKind = GroupKind.fromRoute(type)
                val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
                WriteAnnouncementScreen(
                    groupId = groupId,
                    groupKind = groupKind,
                    onBackClick = { navController.popBackStack() },
                    onCompleteClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("announcements_refresh", true)
                        runCatching {
                            navController.getBackStackEntry(Screens.MyGroupDetailScreen.route)
                                .savedStateHandle
                                .set("mygroup_detail_refresh", true)
                        }
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screens.EditAnnouncementScreen.route,
                arguments = Screens.EditAnnouncementScreen.args.map { arg ->
                    navArgument(arg) {
                        type = when (arg) {
                            "groupId", "noticeId" -> NavType.LongType
                            "isPinned" -> NavType.BoolType
                            else -> NavType.StringType
                        }
                    }
                }
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type")
                val groupKind = GroupKind.fromRoute(type)
                val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
                val noticeId = backStackEntry.arguments?.getLong("noticeId") ?: 0L
                val title = URLDecoder.decode(backStackEntry.arguments?.getString("title") ?: "", "UTF-8")
                val content = URLDecoder.decode(backStackEntry.arguments?.getString("content") ?: "", "UTF-8")
                val isPinned = backStackEntry.arguments?.getBoolean("isPinned") ?: false
                EditAnnouncementScreen(
                    groupId = groupId,
                    groupKind = groupKind,
                    noticeId = noticeId,
                    initialTitle = title,
                    initialContent = content,
                    initialPinned = isPinned,
                    onBackClick = { navController.popBackStack() },
                    onCompleteClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("announcements_refresh", true)
                        runCatching {
                            navController.getBackStackEntry(Screens.MyGroupDetailScreen.route)
                                .savedStateHandle
                                .set("mygroup_detail_refresh", true)
                        }
                        navController.popBackStack()
                    }
                )
            }

            composable(Screens.InquiryScreen.route) {
                InquiryScreen(onBackClick = { navController.popBackStack() })
            }

            composable(Screens.CommunityRulesScreen.route) {
                CommunityRulesScreen(onBackClick = { navController.popBackStack() })
            }
            
            composable(Screens.RestrictionHistoryScreen.route) {
                RestrictionHistoryScreen(onBackClick = { navController.popBackStack() })
            }

            composable(Screens.DdayDetailScreen.route) { DdayDetailScreen(onBackClick = { navController.popBackStack() }) }
            composable(Screens.NotificationScreen.route) {
                NotificationScreen(
                    onBackClick = { navController.popBackStack() },
                    onPostClick = { postId -> navController.navigate(Screens.boardDetailRoute(postId)) },
                    onChatClick = { roomId -> navController.navigate(Screens.messageDetailRoute(roomId)) },
                    onTeamClick = { teamId -> navController.navigate(Screens.groupDetailRoute(GroupKind.PROJECT, teamId)) },
                    onStudyClick = { studyId -> navController.navigate(Screens.groupDetailRoute(GroupKind.STUDY, studyId)) }
                )
            }

            composable(
                route = Screens.MemberManageScreen.route,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("groupId") { type = NavType.LongType }
                ),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "ssabree://group-application/{type}/{groupId}" }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type")
                val groupKind = GroupKind.fromRoute(type)
                val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
                MemberManageScreen(
                    groupId = groupId,
                    groupKind = groupKind,
                    hasNewApplicant = hasNewApplicant,
                    onBackClick = { hasPendingRequests ->
                        if (!hasPendingRequests) {
                            hasNewApplicant = false
                        }
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("mygroup_detail_refresh", true)
                        navController.popBackStack()
                    },
                    onApplicantClick = { portfolioId, applicationId ->
                        navController.navigate(
                            Screens.applicantPortfolioRoute(
                                portfolioId = portfolioId,
                                applicationId = applicationId,
                                groupKind = groupKind
                            )
                        )
                    },
                    onMemberClick = { portfolioId ->
                        navController.navigate(Screens.applicantPortfolioRoute(portfolioId))
                    }
                )
            }

            composable(
                route = Screens.MyProgressScreen.route,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("groupId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type")
                val groupKind = GroupKind.fromRoute(type)
                val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
                MyProgressScreen(
                    groupId = groupId,
                    groupKind = groupKind,
                    onBackClick = { navController.popBackStack() },
                    onSaveSuccess = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("mygroup_detail_refresh", true)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
