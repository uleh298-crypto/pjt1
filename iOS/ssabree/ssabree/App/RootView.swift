import SwiftUI
import Foundation
import Combine

enum RootScreen {
    case splash
    case login
    case main
}

enum MainTab: Hashable {
    case myGroup
    case group
    case home
    case board
    case message
}

struct RootView: View {
    let container: AppContainer
    @State private var rootScreen: RootScreen = .splash
    @State private var selectedTab: MainTab = .home
    @State private var navPath = NavigationPath()
    @State private var pushEventCancellable: AnyCancellable?

    var body: some View {
        NavigationStack(path: $navPath) {
            Group {
                switch rootScreen {
                case .splash:
                    SplashView(onTimeout: {
                        if container.authRepository.isLoggedIn() {
                            rootScreen = .main
                        } else {
                            rootScreen = .login
                        }
                    })
                case .login:
                    LoginView(
                        viewModel: LoginViewModel(authRepository: container.authRepository),
                        onJoinTap: { navPath.append(AppRoute.join) },
                        onLoginSuccess: { rootScreen = .main },
                        onFindIdPassTap: { navPath.append(AppRoute.findIdPass) }
                    )
                case .main:
                    mainTabView
                }
            }
            .onAppear {
                // Listen for force logout events
                AuthEventBus.shared.addListener { event in
                    if case .forceLogout = event {
                        navPath = NavigationPath()
                        rootScreen = .login
                    }
                }

                // Listen for push notification events (deep link)
                pushEventCancellable = PushEventBus.shared.events
                    .receive(on: DispatchQueue.main)
                    .sink { event in
                        handlePushEvent(event)
                    }

                // 콜드 스타트: 앱이 꺼진 상태에서 알림 탭 시 pendingEvent 처리
                if rootScreen == .main, let pending = PushEventBus.shared.consumePending() {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                        handlePushEvent(pending)
                    }
                }
            }
            .onChange(of: rootScreen) { _, newScreen in
                // 스플래시/로그인 후 메인으로 전환 시 pending 이벤트 처리
                if newScreen == .main, let pending = PushEventBus.shared.consumePending() {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                        handlePushEvent(pending)
                    }
                }
            }
            .navigationDestination(for: AppRoute.self) { route in
                switch route {
                case .splash:
                    SplashView(onTimeout: {
                        if container.authRepository.isLoggedIn() {
                            rootScreen = .main
                        } else {
                            rootScreen = .login
                        }
                    })
                case .login:
                    LoginView(
                        viewModel: LoginViewModel(authRepository: container.authRepository),
                        onJoinTap: { navPath.append(AppRoute.join) },
                        onLoginSuccess: { rootScreen = .main },
                        onFindIdPassTap: { navPath.append(AppRoute.findIdPass) }
                    )
                case .join:
                    JoinView(
                        viewModel: JoinViewModel(
                            authRepository: container.authRepository,
                            campusRepository: container.campusRepository
                        ),
                        onJoinSuccess: {
                            if !navPath.isEmpty { navPath.removeLast() }
                        },
                        onNavigationIconClick: {
                            if !navPath.isEmpty { navPath.removeLast() }
                        }
                    )
                case .findIdPass:
                    FindIdPassView(
                        viewModel: FindIdPassViewModel(authRepository: container.authRepository),
                        onConfirm: {
                            if !navPath.isEmpty { navPath.removeLast() }
                        },
                        onResetConfirm: {
                            if !navPath.isEmpty { navPath.removeLast() }
                        }
                    )
                case .home:
                    HomeView(
                        viewModel: container.homeViewModel,
                        onNotificationTap: { navPath.append(AppRoute.notification) },
                        onMyPageTap: { navPath.append(AppRoute.myPage) },
                        onDdayTap: { navPath.append(AppRoute.ddayDetail) },
                        onProjectTap: { navPath.append(AppRoute.groupList(kind: .project)) },
                        onStudyTap: { navPath.append(AppRoute.groupList(kind: .study)) },
                        onBoardTap: { boardId in
                            // Home 탭에서 게시판 클릭 시 게시판 화면으로 이동 후 해당 게시판 선택
                            Task { @MainActor in
                                await container.boardViewModel.refreshBoardsAndPosts(boardIdToSelect: boardId)
                                selectedTab = .board
                            }
                        }
                    )
                case .board:
                    BoardView(
                        viewModel: container.boardViewModel,
                        onWriteTap: { boardId in navPath.append(AppRoute.boardWrite(boardId: boardId)) },
                        onPostTap: { boardId, postId in navPath.append(AppRoute.boardDetail(boardId: boardId, postId: postId)) },
                        onSearchTap: { navPath.append(AppRoute.boardSearch) }
                    )
                case .boardDetail(let boardId, let postId):
                    BoardDetailView(
                        viewModel: BoardDetailViewModel(
                            postRepository: container.postRepository,
                            commentRepository: container.commentRepository,
                            reportRepository: container.reportRepository
                        ),
                        boardId: boardId,
                        postId: postId,
                        onMessageClick: { postId in
                            navPath.append(AppRoute.newChat(postId: postId))
                        },
                        onEditClick: { postId in
                            navPath.append(AppRoute.boardEdit(postId: postId))
                        },
                        onDeleteSuccess: {
                            if !navPath.isEmpty { navPath.removeLast() }
                        }
                    )
                case .boardWrite(let boardId):
                    BoardWriteView(
                        viewModel: BoardWriteViewModel(
                            postRepository: container.postRepository,
                            boardRepository: container.boardRepository,
                            uploadRepository: container.uploadRepository,
                            initialBoardId: boardId
                        ),
                        onWriteSuccess: {
                            Task { await container.boardViewModel.onRefresh() }
                        }
                    )
                case .boardEdit(let postId):
                    BoardEditView(
                        postId: postId,
                        viewModel: BoardEditViewModel(postRepository: container.postRepository),
                        onEditSuccess: {
                            Task { await container.boardViewModel.onRefresh() }
                        }
                    )
                case .boardSearch:
                    BoardSearchView(
                        viewModel: BoardSearchViewModel(
                            postRepository: container.postRepository,
                            keywordRepository: container.keywordRepository
                        ),
                        onPostTap: { boardId, postId in
                            navPath.append(AppRoute.boardDetail(boardId: boardId, postId: postId))
                        }
                    )
                case .group:
                    GroupView(
                        viewModel: container.groupViewModel,
                        onBackClick: { if !navPath.isEmpty { navPath.removeLast() } },
                        onDetailClick: { id in navPath.append(AppRoute.groupDetail(kind: container.groupViewModel.uiState.groupKind, id: id)) },
                        onFabClick: { kind in navPath.append(AppRoute.groupWrite(kind: kind)) }
                    )
                case .groupList(let kind):
                    let vm = GroupViewModel(
                        groupRepository: container.groupRepository,
                        keywordRepository: container.keywordRepository,
                        myPageRepository: container.myPageRepository,
                        campusRepository: container.campusRepository,
                        initialKind: kind
                    )
                    GroupView(
                        viewModel: vm,
                        onBackClick: { if !navPath.isEmpty { navPath.removeLast() } },
                        onDetailClick: { id in navPath.append(AppRoute.groupDetail(kind: kind, id: id)) },
                        onFabClick: { currentKind in navPath.append(AppRoute.groupWrite(kind: currentKind)) }
                    )
                case .groupDetail(let kind, let id):
                    GroupDetailView(
                        groupKind: kind,
                        groupId: id,
                        viewModel: GroupDetailViewModel(
                            groupRepository: container.groupRepository,
                            authRepository: container.authRepository,
                            groupKind: kind,
                            groupId: id
                        )
                    )
                case .groupWrite(let kind):
                    GroupWriteView(
                        viewModel: GroupWriteViewModel(
                            groupRepository: container.groupRepository,
                            myPageRepository: container.myPageRepository,
                            campusRepository: container.campusRepository
                        ),
                        initialKind: kind
                    )
                case .groupEdit(let kind, let id):
                    GroupEditView(
                        groupKind: kind,
                        groupId: id,
                        viewModel: GroupEditViewModel(
                            groupRepository: container.groupRepository,
                            groupService: container.groupService,
                            myPageRepository: container.myPageRepository,
                            campusRepository: container.campusRepository,
                            groupKind: kind,
                            groupId: id
                        )
                    )
                case .groupApply(let groupId, let groupKind):
                    GroupApplyView(
                        groupId: groupId,
                        groupKind: groupKind,
                        viewModel: GroupApplyViewModel(
                            groupRepository: container.groupRepository,
                            portfolioRepository: container.portfolioRepository,
                            myPageRepository: container.myPageRepository
                        )
                    )
                case .selectGroup:
                    SelectGroupView(onSelect: { kind in
                        navPath.append(AppRoute.groupList(kind: kind))
                    })
                case .myGroup:
                    MyGroupView(
                        studyViewModel: container.myGroupStudyViewModel,
                        projectViewModel: container.myGroupProjectViewModel,
                        onDetailClick: { kind, groupId, isLeader in
                            navPath.append(AppRoute.myGroupDetail(groupId: groupId, groupKind: kind, isLeader: isLeader))
                        },
                        onMyApplicationsClick: { kind in
                            navPath.append(AppRoute.myApplications(groupKind: kind))
                        }
                    )
                case .myApplications(let groupKind):
                    MyApplicationsView(
                        groupKind: groupKind,
                        viewModel: MyApplicationsViewModel(
                            groupRepository: container.groupRepository,
                            groupKind: groupKind
                        )
                    )
                case .myGroupDetail(let groupId, let groupKind, let isLeader):
                    MyGroupDetailView(
                        groupId: groupId,
                        groupKind: groupKind,
                        isLeader: isLeader,
                        viewModel: MyGroupDetailViewModel(
                            groupRepository: container.groupRepository,
                            groupService: container.groupService,
                            groupKind: groupKind,
                            groupId: groupId,
                            isLeader: isLeader
                        )
                    )
                case .groupManage(let groupId, let groupKind):
                    GroupManageView(
                        onMemberManageTap: { navPath.append(AppRoute.memberManage(groupId: groupId, groupKind: groupKind)) },
                        onGroupDetailTap: { _ in navPath.append(AppRoute.groupDetail(kind: groupKind, id: groupId)) }
                    )
                case .memberManage(let groupId, let groupKind):
                    MemberManageView(
                        groupId: groupId,
                        groupKind: groupKind,
                        viewModel: MemberManageViewModel(
                            groupRepository: container.groupRepository,
                            groupService: container.groupService
                        )
                    )
                case .announcements(let groupId, let groupKind, let isLeader):
                    AnnouncementsView(
                        groupId: groupId,
                        groupKind: groupKind,
                        isLeader: isLeader,
                        viewModel: AnnouncementsViewModel(
                            groupService: container.groupService
                        )
                    )
                case .writeAnnouncement(let groupId, let groupKind):
                    WriteAnnouncementView(
                        groupId: groupId,
                        groupKind: groupKind,
                        viewModel: WriteAnnouncementViewModel(
                            groupService: container.groupService
                        )
                    )
                case .editAnnouncement(let groupId, let groupKind, let announcementId):
                    EditAnnouncementView(
                        groupId: groupId,
                        groupKind: groupKind,
                        announcementId: announcementId,
                        viewModel: EditAnnouncementViewModel(
                            groupService: container.groupService
                        )
                    )
                case .addTask(let groupId, let groupKind):
                    AddTaskView(
                        groupId: groupId,
                        groupKind: groupKind,
                        viewModel: AddTaskViewModel(
                            groupService: container.groupService
                        )
                    )
                case .taskDetail(let groupId, let groupKind, let taskId):
                    TaskDetailView(
                        groupId: groupId,
                        groupKind: groupKind,
                        taskId: taskId,
                        viewModel: TaskDetailViewModel(
                            groupService: container.groupService,
                            groupRepository: container.groupRepository,
                            groupKind: groupKind,
                            groupId: groupId,
                            taskId: taskId
                        )
                    )
                case .editTask(let groupId, let groupKind, let taskId):
                    EditTaskView(
                        groupId: groupId,
                        groupKind: groupKind,
                        taskId: taskId,
                        viewModel: EditTaskViewModel(
                            groupService: container.groupService,
                            taskId: taskId
                        )
                    )
                case .myProgress(let id):
                    MyProgressView(
                        groupId: id,
                        viewModel: MyProgressViewModel(myGroupRepository: container.myGroupRepository)
                    )
                case .selectMyGroup:
                    SelectMyGroupView(onSelect: { kind in
                        navPath.append(AppRoute.myGroupList(kind: kind))
                    })
                case .myGroupList(let kind):
                    MyGroupListView(
                        viewModel: kind == .study ? container.myGroupStudyViewModel : container.myGroupProjectViewModel,
                        onBackClick: { if !navPath.isEmpty { navPath.removeLast() } },
                        onDetailClick: { groupId, isLeader in
                            navPath.append(AppRoute.myGroupDetail(groupId: groupId, groupKind: kind, isLeader: isLeader))
                        }
                    )
                case .message:
                    MessageView(viewModel: container.messageViewModel)
                case .messageDetail(let roomId):
                    MessageDetailView(
                        roomId: roomId,
                        viewModel: MessageDetailViewModel(
                            chatRepository: container.chatRepository,
                            authDataStore: container.authDataStore,
                            authRepository: container.authRepository,
                            reportRepository: container.reportRepository
                        )
                    )
                case .newChat(let postId):
                    MessageDetailView(
                        postId: postId,
                        viewModel: MessageDetailViewModel(
                            chatRepository: container.chatRepository,
                            authDataStore: container.authDataStore,
                            authRepository: container.authRepository,
                            reportRepository: container.reportRepository
                        )
                    )
                case .ddayDetail:
                    DdayDetailView(ddayRepository: container.ddayRepository)
                case .myPage:
                    MyPageView(
                        viewModel: MyPageViewModel(
                            myPageRepository: container.myPageRepository,
                            portfolioRepository: container.portfolioRepository,
                            uploadRepository: container.uploadRepository,
                            stackRepository: container.stackRepository
                        ),
                        authRepository: container.authRepository,
                        onLogout: {
                            navPath = NavigationPath()
                            rootScreen = .login
                        },
                        onSettingTap: { navPath.append(AppRoute.setting) },
                        onPortfolioDetailTap: { navPath.append(AppRoute.portfolioDetail) },
                        onMyPostsTap: { navPath.append(AppRoute.myPosts) },
                        onMyCommentsTap: { navPath.append(AppRoute.myComments) },
                        onMyScrapsTap: { navPath.append(AppRoute.myScraps) }
                    )
                case .myPosts:
                    MyPostsView(
                        viewModel: MyPostsViewModel(myPageRepository: container.myPageRepository),
                        onPostTap: { boardId, postId in
                            navPath.append(AppRoute.boardDetail(boardId: boardId, postId: postId))
                        }
                    )
                case .myComments:
                    MyCommentsView(
                        viewModel: MyCommentsViewModel(myPageRepository: container.myPageRepository),
                        onCommentTap: { boardId, postId in
                            navPath.append(AppRoute.boardDetail(boardId: boardId, postId: postId))
                        }
                    )
                case .myScraps:
                    MyScrapsView(
                        viewModel: MyScrapsViewModel(myPageRepository: container.myPageRepository),
                        onPostTap: { boardId, postId in
                            navPath.append(AppRoute.boardDetail(boardId: boardId, postId: postId))
                        }
                    )
                case .portfolioDetail:
                    PortfolioDetailView(
                        viewModel: PortfolioDetailViewModel(
                            portfolioRepository: container.portfolioRepository,
                            projectRepository: container.projectRepository,
                            stackRepository: container.stackRepository,
                            uploadRepository: container.uploadRepository
                        )
                    )
                case .applicantPortfolio(let portfolioId, let applicationId, let groupKind):
                    ApplicantPortfolioView(
                        portfolioId: portfolioId,
                        applicationId: applicationId,
                        groupKind: groupKind,
                        viewModel: ApplicantPortfolioViewModel(
                            portfolioRepository: container.portfolioRepository,
                            projectRepository: container.projectRepository,
                            groupService: container.groupService
                        )
                    )
                case .applicantPortfolioDetail(let portfolioId):
                    ApplicantPortfolioDetailView(
                        portfolioId: portfolioId,
                        viewModel: ApplicantPortfolioViewModel(
                            portfolioRepository: container.portfolioRepository,
                            projectRepository: container.projectRepository
                        )
                    )
                case .projectWrite(let portfolioId, let projectId):
                    ProjectWriteView(
                        portfolioId: portfolioId,
                        projectId: projectId,
                        viewModel: ProjectWriteViewModel(
                            projectRepository: container.projectRepository,
                            uploadRepository: container.uploadRepository
                        ),
                        stackViewModel: StackViewModel(
                            stackRepository: container.stackRepository
                        )
                    )
                case .inquiry:
                    InquiryView(
                        viewModel: InquiryViewModel(inquiryRepository: container.inquiryRepository)
                    )
                case .setting:
                    SettingView(
                        authRepository: container.authRepository,
                        onLogout: {
                            navPath = NavigationPath()
                            rootScreen = .login
                        }
                    )
                case .termsOfService:
                    TermsOfServiceView()
                case .communityRules:
                    CommunityRulesView()
                case .notification:
                    NotificationView(
                        viewModel: NotificationViewModel(notificationRepository: container.notificationRepository),
                        onPostClick: { postId in navPath.append(AppRoute.boardDetail(boardId: 0, postId: postId)) },
                        onChatClick: { roomId in navPath.append(AppRoute.messageDetail(roomId: roomId)) },
                        onGroupApplicationClick: { groupId, kind in navPath.append(AppRoute.memberManage(groupId: groupId, groupKind: kind)) },
                        onApplicationAcceptedClick: { groupId, kind in navPath.append(AppRoute.myGroupDetail(groupId: groupId, groupKind: kind, isLeader: false)) },
                        onSettingsTap: { navPath.append(AppRoute.notificationSettings) }
                    )
                case .notificationSettings:
                    NotificationSettingsView(
                        viewModel: NotificationSettingsViewModel(notificationRepository: container.notificationRepository)
                    )
                case .notificationDetail(let id):
                    NotificationDetailView(id: id)
                case .report:
                    ReportView()
                }
            }
        }
    }

    // MARK: - Main Tab View
    // 안드로이드와 동일한 탭 순서: 내 그룹, 그룹 찾기, HOME, 게시판, 쪽지
    @ViewBuilder
    private var mainTabView: some View {
        TabView(selection: $selectedTab) {
            MyGroupView(
                studyViewModel: container.myGroupStudyViewModel,
                projectViewModel: container.myGroupProjectViewModel,
                onDetailClick: { kind, groupId, isLeader in
                    navPath.append(AppRoute.myGroupDetail(groupId: groupId, groupKind: kind, isLeader: isLeader))
                },
                onMyApplicationsClick: { kind in
                    navPath.append(AppRoute.myApplications(groupKind: kind))
                }
            )
            .tabItem { Label("내 그룹", systemImage: "person.2.fill") }
            .tag(MainTab.myGroup)

            SelectGroupView(onSelect: { kind in
                navPath.append(AppRoute.groupList(kind: kind))
            })
            .tabItem { Label("그룹 찾기", systemImage: "person.3.fill") }
            .tag(MainTab.group)

            HomeView(
                viewModel: container.homeViewModel,
                onNotificationTap: { navPath.append(AppRoute.notification) },
                onMyPageTap: { navPath.append(AppRoute.myPage) },
                onDdayTap: { navPath.append(AppRoute.ddayDetail) },
                onProjectTap: { navPath.append(AppRoute.groupList(kind: .project)) },
                onStudyTap: { navPath.append(AppRoute.groupList(kind: .study)) },
                onBoardTap: { boardId in
                    Task { @MainActor in
                        await container.boardViewModel.refreshBoardsAndPosts(boardIdToSelect: boardId)
                        selectedTab = .board
                    }
                }
            )
            .tabItem { Label("HOME", systemImage: "house.fill") }
            .tag(MainTab.home)

            BoardView(
                viewModel: container.boardViewModel,
                onWriteTap: { boardId in navPath.append(AppRoute.boardWrite(boardId: boardId)) },
                onPostTap: { boardId, postId in navPath.append(AppRoute.boardDetail(boardId: boardId, postId: postId)) },
                onSearchTap: { navPath.append(AppRoute.boardSearch) }
            )
            .tabItem { Label("게시판", systemImage: "list.bullet") }
            .tag(MainTab.board)

            MessageView(viewModel: container.messageViewModel)
                .tabItem { Label("쪽지", systemImage: "envelope.fill") }
                .tag(MainTab.message)
        }
        .tint(AppColors.primary)
    }

    // MARK: - Push Event Handling

    private func handlePushEvent(_ event: PushEvent) {
        // 로그인되어 있지 않으면 무시
        guard rootScreen == .main else { return }

        // 기존 네비게이션 스택 초기화 후 딥링크 화면으로 이동 (중복 방지)
        navPath = NavigationPath()

        switch event {
        case .openChat(let roomId):
            navPath.append(AppRoute.messageDetail(roomId: roomId))

        case .openPost(let postId):
            navPath.append(AppRoute.boardDetail(boardId: 0, postId: postId))

        case .openGroupApplication(let groupId, let groupType):
            let upperType = groupType.uppercased()
            let kind: GroupKind = (upperType == "TEAM" || upperType == "PROJECT" || upperType == "PROJECTS") ? .project : .study
            navPath.append(AppRoute.memberManage(groupId: groupId, groupKind: kind))

        case .openApplicationAccepted(let groupId, let groupType):
            let upperType = groupType.uppercased()
            let kind: GroupKind = (upperType == "TEAM" || upperType == "PROJECT" || upperType == "PROJECTS") ? .project : .study
            navPath.append(AppRoute.myGroupDetail(groupId: groupId, groupKind: kind, isLeader: false))
        }

        // pendingEvent 소비 (중복 처리 방지)
        _ = PushEventBus.shared.consumePending()
    }
}

