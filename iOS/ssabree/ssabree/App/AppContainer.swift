import Foundation

// MARK: - App Container Protocol

protocol AppContainer {
    var authDataStore: AuthDataStore { get }
    var authRepository: AuthRepository { get }
    var homeRepository: HomeRepository { get }
    var boardRepository: BoardRepository { get }
    var campusRepository: CampusRepository { get }
    var keywordRepository: KeywordRepository { get }
    var postRepository: PostRepository { get }
    var commentRepository: CommentRepository { get }
    var uploadRepository: UploadRepository { get }
    var myPageRepository: MyPageRepository { get }
    var groupRepository: GroupRepository { get }
    var groupService: GroupService { get }
    var messageRepository: MessageRepository { get }
    var chatRepository: ChatRepository { get }
    var myGroupRepository: MyGroupRepository { get }
    var ddayRepository: DdayRepository { get }
    var notificationRepository: NotificationRepository { get }
    var portfolioRepository: PortfolioRepository { get }
    var projectRepository: ProjectRepository { get }
    var reportRepository: ReportRepository { get }
    var inquiryRepository: InquiryRepository { get }
    var stackRepository: StackRepository { get }
    var imageRepository: ImageRepository { get }

    // MARK: - Cached ViewModels (탭에서 사용하는 ViewModel들 캐시)
    var homeViewModel: HomeViewModel { get }
    var boardViewModel: BoardViewModel { get }
    var groupViewModel: GroupViewModel { get }
    var messageViewModel: MessageViewModel { get }
    var myGroupStudyViewModel: MyGroupAllViewModel { get }
    var myGroupProjectViewModel: MyGroupAllViewModel { get }
}

// MARK: - Real App Container

final class RealAppContainer: AppContainer {
    // MARK: - Storage
    let authDataStore: AuthDataStore

    // MARK: - Services
    let groupService: GroupService

    // MARK: - Repositories
    let authRepository: AuthRepository
    let homeRepository: HomeRepository
    let boardRepository: BoardRepository
    let campusRepository: CampusRepository
    let keywordRepository: KeywordRepository
    let postRepository: PostRepository
    let commentRepository: CommentRepository
    let uploadRepository: UploadRepository
    let myPageRepository: MyPageRepository
    let groupRepository: GroupRepository
    let messageRepository: MessageRepository
    let chatRepository: ChatRepository
    let myGroupRepository: MyGroupRepository
    let ddayRepository: DdayRepository
    let notificationRepository: NotificationRepository
    let portfolioRepository: PortfolioRepository
    let projectRepository: ProjectRepository
    let reportRepository: ReportRepository
    let inquiryRepository: InquiryRepository
    let stackRepository: StackRepository
    let imageRepository: ImageRepository

    // MARK: - Cached ViewModels
    lazy var homeViewModel: HomeViewModel = HomeViewModel(homeRepository: homeRepository, myPageRepository: myPageRepository)
    lazy var boardViewModel: BoardViewModel = BoardViewModel(boardRepository: boardRepository, postRepository: postRepository)
    lazy var groupViewModel: GroupViewModel = GroupViewModel(
        groupRepository: groupRepository,
        keywordRepository: keywordRepository,
        myPageRepository: myPageRepository,
        campusRepository: campusRepository
    )
    lazy var messageViewModel: MessageViewModel = MessageViewModel(chatRepository: chatRepository)
    lazy var myGroupStudyViewModel: MyGroupAllViewModel = MyGroupAllViewModel(groupRepository: groupRepository, groupKind: .study, authRepository: authRepository)
    lazy var myGroupProjectViewModel: MyGroupAllViewModel = MyGroupAllViewModel(groupRepository: groupRepository, groupKind: .project, authRepository: authRepository)

    init() {
        // Setup secure storage with Keychain
        let secureStore = KeychainSecureStore()
        let dataStore = AuthDataStore(secureStore: secureStore)
        self.authDataStore = dataStore

        // Configure API Client with auth data store
        APIClient.shared.configure(authDataStore: dataStore)

        // Configure WebSocket Manager with auth data store
        Task { @MainActor in
            ChatWebSocketManager.shared.configure(authDataStore: dataStore)
        }

        // Initialize services
        let authService = AuthServiceImpl()
        let boardService = BoardServiceImpl()
        let campusService = CampusServiceImpl()
        let commentService = CommentServiceImpl()
        let homeService = HomeServiceImpl()
        let postService = PostServiceImpl()
        let uploadService = UploadServiceImpl()
        let myPageService = MyPageServiceImpl()
        let groupServiceImpl = GroupServiceImpl()
        self.groupService = groupServiceImpl
        let messageService = MessageServiceImpl()
        let chatService = ChatServiceImpl()
        let ddayService = DdayServiceImpl()
        let notificationService = NotificationServiceImpl()
        let portfolioService = PortfolioServiceImpl()
        let projectService = ProjectServiceImpl()
        let reportService = ReportServiceImpl()
        let inquiryService = InquiryServiceImpl()
        let stackService = StackServiceImpl()

        // Initialize repositories
        self.authRepository = AuthRepositoryImpl(
            authService: authService,
            authDataStore: dataStore
        )
        // DdayRepository 먼저 초기화 (HomeRepository에서 사용)
        self.ddayRepository = DdayRepositoryImpl(ddayService: ddayService)
        self.homeRepository = HomeRepositoryImpl(homeService: homeService, ddayRepository: ddayRepository)
        self.boardRepository = BoardRepositoryImpl(boardService: boardService)
        self.campusRepository = CampusRepositoryImpl(campusService: campusService)
        self.keywordRepository = KeywordRepositoryImpl()
        self.postRepository = PostRepositoryImpl(postService: postService, authDataStore: dataStore)
        self.commentRepository = CommentRepositoryImpl(commentService: commentService)
        self.uploadRepository = UploadRepositoryImpl(uploadService: uploadService)
        self.myPageRepository = MyPageRepositoryImpl(myPageService: myPageService)
        self.groupRepository = GroupRepositoryImpl(groupService: groupServiceImpl)
        self.messageRepository = MessageRepositoryImpl(messageService: messageService)
        self.chatRepository = ChatRepositoryImpl(chatService: chatService)
        self.myGroupRepository = MyGroupRepositoryImpl(groupService: groupServiceImpl)
        self.notificationRepository = NotificationRepositoryImpl(notificationService: notificationService)
        self.portfolioRepository = PortfolioRepositoryImpl(portfolioService: portfolioService)
        self.projectRepository = ProjectRepositoryImpl(projectService: projectService)
        self.reportRepository = ReportRepositoryImpl(reportService: reportService)
        self.inquiryRepository = InquiryRepositoryImpl(inquiryService: inquiryService)
        self.stackRepository = StackRepositoryImpl(stackService: stackService)
        self.imageRepository = ImageRepositoryImpl.shared
    }
}

// MARK: - Fake App Container (for previews and testing)

final class FakeAppContainer: AppContainer {
    let authDataStore: AuthDataStore
    let authRepository: AuthRepository
    let homeRepository: HomeRepository
    let boardRepository: BoardRepository
    let campusRepository: CampusRepository
    let keywordRepository: KeywordRepository
    let postRepository: PostRepository
    let commentRepository: CommentRepository
    let uploadRepository: UploadRepository
    let myPageRepository: MyPageRepository
    let groupRepository: GroupRepository
    let groupService: GroupService
    let messageRepository: MessageRepository
    let chatRepository: ChatRepository
    let myGroupRepository: MyGroupRepository
    let ddayRepository: DdayRepository
    let notificationRepository: NotificationRepository
    let portfolioRepository: PortfolioRepository
    let projectRepository: ProjectRepository
    let reportRepository: ReportRepository
    let inquiryRepository: InquiryRepository
    let stackRepository: StackRepository
    let imageRepository: ImageRepository

    // MARK: - Cached ViewModels
    lazy var homeViewModel: HomeViewModel = HomeViewModel(homeRepository: homeRepository, myPageRepository: myPageRepository)
    lazy var boardViewModel: BoardViewModel = BoardViewModel(boardRepository: boardRepository, postRepository: postRepository)
    lazy var groupViewModel: GroupViewModel = GroupViewModel(
        groupRepository: groupRepository,
        keywordRepository: keywordRepository,
        myPageRepository: myPageRepository,
        campusRepository: campusRepository
    )
    lazy var messageViewModel: MessageViewModel = MessageViewModel(chatRepository: chatRepository)
    lazy var myGroupStudyViewModel: MyGroupAllViewModel = MyGroupAllViewModel(groupRepository: groupRepository, groupKind: .study, authRepository: authRepository)
    lazy var myGroupProjectViewModel: MyGroupAllViewModel = MyGroupAllViewModel(groupRepository: groupRepository, groupKind: .project, authRepository: authRepository)

    init() {
        let secureStore = InMemorySecureStore()
        self.authDataStore = AuthDataStore(secureStore: secureStore)

        self.authRepository = FakeAuthRepository()
        self.homeRepository = FakeHomeRepository()
        self.boardRepository = FakeBoardRepository()
        self.campusRepository = FakeCampusRepository()
        self.keywordRepository = FakeKeywordRepository()
        self.postRepository = FakePostRepository()
        self.commentRepository = FakeCommentRepository()
        self.uploadRepository = FakeUploadRepository()
        self.myPageRepository = FakeMyPageRepository()
        self.groupRepository = FakeGroupRepository()
        self.groupService = FakeGroupService()
        self.messageRepository = FakeMessageRepository()
        self.chatRepository = FakeChatRepository()
        self.myGroupRepository = FakeMyGroupRepository()
        self.ddayRepository = FakeDdayRepository()
        self.notificationRepository = FakeNotificationRepository()
        self.portfolioRepository = FakePortfolioRepository()
        self.projectRepository = FakeProjectRepository()
        self.reportRepository = FakeReportRepository()
        self.inquiryRepository = FakeInquiryRepository()
        self.stackRepository = FakeStackRepository()
        self.imageRepository = FakeImageRepository()
    }
}
