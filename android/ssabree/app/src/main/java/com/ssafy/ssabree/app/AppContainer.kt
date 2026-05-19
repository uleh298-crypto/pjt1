package com.ssafy.ssabree.app

import com.ssafy.ssabree.core.datasource.local.DdayLocalStore
import com.ssafy.ssabree.core.repository.AuthRepository
import com.ssafy.ssabree.core.repository.AuthRepositoryImpl
import com.ssafy.ssabree.core.repository.CampusRepository
import com.ssafy.ssabree.core.repository.CampusRepositoryImpl
import com.ssafy.ssabree.core.repository.CommentRepository
import com.ssafy.ssabree.core.repository.CommentRepositoryImpl
import com.ssafy.ssabree.core.repository.DdayRepository
import com.ssafy.ssabree.core.repository.DdayRepositoryImpl
import com.ssafy.ssabree.core.repository.GroupRepository
import com.ssafy.ssabree.core.repository.GroupRepositoryImpl
import com.ssafy.ssabree.core.repository.MyGroupRepository
import com.ssafy.ssabree.core.repository.MyGroupRepositoryImpl
import com.ssafy.ssabree.core.repository.HomeRepository
import com.ssafy.ssabree.core.repository.HomeRepositoryImpl
import com.ssafy.ssabree.core.repository.BoardRepository
import com.ssafy.ssabree.core.repository.BoardRepositoryImpl
import com.ssafy.ssabree.core.repository.UploadRepository
import com.ssafy.ssabree.core.repository.UploadRepositoryImpl
import com.ssafy.ssabree.core.repository.KeywordRepository
import com.ssafy.ssabree.core.repository.KeywordRepositoryImpl
import com.ssafy.ssabree.core.repository.InquiryRepository
import com.ssafy.ssabree.core.repository.InquiryRepositoryImpl
import com.ssafy.ssabree.core.repository.MemberRepository
import com.ssafy.ssabree.core.repository.MemberRepositoryImpl
import com.ssafy.ssabree.core.repository.PostRepository
import com.ssafy.ssabree.core.repository.PostRepositoryImpl
import com.ssafy.ssabree.core.repository.PortfolioRepository
import com.ssafy.ssabree.core.repository.PortfolioRepositoryImpl
import com.ssafy.ssabree.core.repository.ProjectRepository
import com.ssafy.ssabree.core.repository.ProjectRepositoryImpl
import com.ssafy.ssabree.core.repository.ChatRepository
import com.ssafy.ssabree.core.repository.ChatRepositoryImpl
import com.ssafy.ssabree.core.repository.ReportRepository
import com.ssafy.ssabree.core.repository.ReportRepositoryImpl
import com.ssafy.ssabree.core.repository.StackRepository
import com.ssafy.ssabree.core.repository.StackRepositoryImpl
import com.ssafy.ssabree.core.repository.test.FakeAuthRepository
import com.ssafy.ssabree.core.repository.test.FakeCampusRepository
import com.ssafy.ssabree.core.repository.test.FakeCommentRepository
import com.ssafy.ssabree.core.repository.test.FakeGroupRepository
import com.ssafy.ssabree.core.repository.test.FakeInquiryRepository
import com.ssafy.ssabree.core.repository.test.FakeMemberRepository
import com.ssafy.ssabree.core.repository.test.FakeMyGroupRepository
import com.ssafy.ssabree.core.repository.test.FakeHomeRepository
import com.ssafy.ssabree.core.repository.test.FakeBoardRepository
import com.ssafy.ssabree.core.repository.test.FakeDdayRepository
import com.ssafy.ssabree.core.repository.test.FakeUploadRepository
import com.ssafy.ssabree.core.repository.test.FakePostRepository
import com.ssafy.ssabree.core.repository.test.FakeKeywordRepository
import com.ssafy.ssabree.core.repository.test.FakePortfolioRepository
import com.ssafy.ssabree.core.repository.test.FakeProjectRepository
import com.ssafy.ssabree.core.repository.test.FakeChatRepository
import com.ssafy.ssabree.core.repository.test.FakeReportRepository
import com.ssafy.ssabree.core.repository.test.FakeStackRepository
import com.ssafy.ssabree.core.repository.NotificationRepository
import com.ssafy.ssabree.core.repository.NotificationRepositoryImpl
import com.ssafy.ssabree.core.repository.test.FakeNotificationRepository
import com.ssafy.ssabree.core.repository.ImageRepository
import com.ssafy.ssabree.core.repository.ImageRepositoryImpl
import com.ssafy.ssabree.core.repository.test.FakeImageRepository
import com.ssafy.ssabree.core.datasource.local.image.ImageCacheDatabase
import com.ssafy.ssabree.core.utils.RetrofitClient

interface AppContainer {
    val authRepository: AuthRepository
    val campusRepository: CampusRepository
    val commentRepository: CommentRepository
    val ddayRepository: DdayRepository
    val groupRepository: GroupRepository
    val myGroupRepository: MyGroupRepository
    val homeRepository: HomeRepository
    val boardRepository: BoardRepository
    val uploadRepository: UploadRepository
    val postRepository: PostRepository
    val keywordRepository: KeywordRepository
    val memberRepository: MemberRepository
    val inquiryRepository: InquiryRepository
    val portfolioRepository: PortfolioRepository
    val projectRepository: ProjectRepository
    val chatRepository: ChatRepository
    val reportRepository: ReportRepository
    val stackRepository: StackRepository
    val notificationRepository: NotificationRepository
    val imageRepository: ImageRepository
}

class RealAppContainer : AppContainer {
    private val ddayLocalStore = DdayLocalStore(ApplicationClass.appContext)

    override val authRepository: AuthRepository = AuthRepositoryImpl()
    override val campusRepository: CampusRepository = CampusRepositoryImpl()
    override val commentRepository: CommentRepository = CommentRepositoryImpl()
    override val ddayRepository: DdayRepository = DdayRepositoryImpl(ddayLocalStore)
    override val groupRepository: GroupRepository = GroupRepositoryImpl()
    override val myGroupRepository: MyGroupRepository = MyGroupRepositoryImpl()
    override val homeRepository: HomeRepository = HomeRepositoryImpl(ddayRepository)
    override val boardRepository: BoardRepository = BoardRepositoryImpl()
    override val uploadRepository: UploadRepository = UploadRepositoryImpl()
    override val postRepository: PostRepository = PostRepositoryImpl()
    override val keywordRepository: KeywordRepository = KeywordRepositoryImpl()
    override val memberRepository: MemberRepository = MemberRepositoryImpl()
    override val inquiryRepository: InquiryRepository = InquiryRepositoryImpl()
    override val portfolioRepository: PortfolioRepository = PortfolioRepositoryImpl()
    override val projectRepository: ProjectRepository = ProjectRepositoryImpl()
    override val chatRepository: ChatRepository = ChatRepositoryImpl()
    override val reportRepository: ReportRepository = ReportRepositoryImpl()
    override val stackRepository: StackRepository = StackRepositoryImpl()
    override val notificationRepository: NotificationRepository = NotificationRepositoryImpl()
    private val imageCacheDb = ImageCacheDatabase.getInstance(ApplicationClass.appContext)
    override val imageRepository: ImageRepository =
        ImageRepositoryImpl(
            dao = imageCacheDb.imageCacheDao(),
            client = RetrofitClient.client.newBuilder()
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()
        )
}

class FakeAppContainer : AppContainer {
    override val authRepository: AuthRepository = FakeAuthRepository()
    override val campusRepository: CampusRepository = FakeCampusRepository()
    override val commentRepository: CommentRepository = FakeCommentRepository()
    override val ddayRepository: DdayRepository = FakeDdayRepository()
    override val groupRepository: GroupRepository = FakeGroupRepository()
    override val myGroupRepository: MyGroupRepository = FakeMyGroupRepository()
    override val homeRepository: HomeRepository = FakeHomeRepository()
    override val boardRepository: BoardRepository = FakeBoardRepository()
    override val uploadRepository: UploadRepository = FakeUploadRepository()
    override val postRepository: PostRepository = FakePostRepository()
    override val keywordRepository: KeywordRepository = FakeKeywordRepository()
    override val memberRepository: MemberRepository = FakeMemberRepository()
    override val inquiryRepository: InquiryRepository = FakeInquiryRepository()
    override val portfolioRepository: PortfolioRepository = FakePortfolioRepository()
    override val projectRepository: ProjectRepository = FakeProjectRepository()
    override val chatRepository: ChatRepository = FakeChatRepository()
    override val reportRepository: ReportRepository = FakeReportRepository()
    override val stackRepository: StackRepository = FakeStackRepository()
    override val notificationRepository: NotificationRepository = FakeNotificationRepository()
    override val imageRepository: ImageRepository = FakeImageRepository()
}
