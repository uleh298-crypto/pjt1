import { BrowserRouter, Routes, Route } from 'react-router-dom'
import './App.css'
import TestPage from './TestPage'
import MainLayout from './components/MainLayout'

// Splash & Legacy
import SplashPage from './pages/splash/SplashPage'

// Auth
import LoginPage from './pages/login/LoginPage'
import JoinPage from './pages/join/JoinPage'

// Core Features
import LandingPage from './pages/landing/LandingPage'
import HomePage from './pages/home/HomePage'
import BoardPage from './pages/board/BoardPage'
import GroupPage from './pages/group/GroupPage'
import GroupDetailPage from './pages/group/GroupDetailPage'
import GroupWritePage from './pages/group/GroupWritePage'
import GroupApplyPage from './pages/group/GroupApplyPage'

// My Groups (New Implementation)
import MyGroupsPage from './pages/mygroup/MyGroupsPage'
import { default as MyGroupDetailPageNew } from './pages/mygroup/MyGroupDetailPage'

// Message & Notification & Report
import MessagePage from './pages/message/MessagePage'
import MessageDetailPage from './pages/messagedetail/MessageDetailPage'
import NotificationPage from './pages/notification/NotificationPage'
import NotificationDetailPage from './pages/notification/NotificationDetailPage'
import ReportPage from './pages/report/ReportPage'

// My Group
import MyGroupPage from './pages/mygroup/MyGroupPage'
import MyGroupDetailPage from './pages/mygroup/MyGroupDetailPage'
import SelectMyGroupPage from './pages/mygroup/SelectMyGroupPage'
import AnnouncementsPage from './pages/mygroup/AnnouncementsPage'
import WriteAnnouncementPage from './pages/mygroup/WriteAnnouncementPage'
import EditAnnouncementPage from './pages/mygroup/EditAnnouncementPage'
import GroupManagePage from './pages/mygroup/GroupManagePage'
import MemberManagePage from './pages/mygroup/MemberManagePage'
import MyProgressPage from './pages/mygroup/MyProgressPage'

// My Page
import MyPage from './pages/mypage/MyPagePage'
import CommunityRulesPage from './pages/mypage/CommunityRulesPage'
import InquiryPage from './pages/mypage/InquiryPage'
import MyCommentsPage from './pages/mypage/MyCommentsPage'
import MyPostsPage from './pages/mypage/MyPostsPage'
import MyScrapsPage from './pages/mypage/MyScrapsPage'
import PortfolioDetailPage from './pages/mypage/PortfolioDetailPage'
import PortfolioWritePage from './pages/mypage/PortfolioWritePage'
import ProjectWritePage from './pages/mypage/ProjectWritePage'
import RestrictionHistoryPage from './pages/mypage/RestrictionHistoryPage'
import SettingPage from './pages/mypage/SettingPage'
import TermsOfServicePage from './pages/mypage/TermsOfServicePage'

function App() {
    return (
        <BrowserRouter>
            <Routes>
                {/* Independent Routes */}
                <Route path="/splash" element={<SplashPage />} />

                {/* Auth */}
                <Route path="/login" element={<LoginPage />} />
                <Route path="/join" element={<JoinPage />} />
                <Route path="/" element={<LandingPage />} />
                <Route path="/landing" element={<LandingPage />} />
                {/* <Route path="/login/find_id_pass" element={<FindIdPassPage />} /> */}

                <Route element={<MainLayout />}>
                    {/* Main Tab Routes */}
                    <Route path="/home" element={<HomePage />} />

                    {/* Board */}
                    <Route path="/board" element={<BoardPage />} />
                    <Route path="/board/:boardId" element={<BoardPage />} />
                    <Route path="/board/write" element={<BoardPage />} /> {/* TODO: Create BoardWritePage */}
                    <Route path="/board/detail/:postId" element={<BoardPage />} /> {/* TODO: Create BoardDetailPage */}
                    <Route path="/board/edit/:postId" element={<BoardPage />} /> {/* TODO: Create BoardEditPage */}
                    <Route path="/board/search" element={<BoardPage />} /> {/* TODO: Create BoardSearchPage */}

                    {/* Group (Project/Study) - Legacy */}
                    <Route path="/group/list/:type" element={<GroupPage />} />
                    <Route path="/group/write/:tabIndex/:isEditMode" element={<GroupPage />} />
                    <Route path="/group/detail/:type/:groupId" element={<GroupPage />} />
                    <Route path="/group/apply/:type/:groupId" element={<GroupPage />} />

                    {/* Group (Project/Study) - New Routes */}
                    <Route path="/groups/study" element={<GroupPage />} />
                    <Route path="/groups/team" element={<GroupPage />} />
                    <Route path="/groups/study/:id" element={<GroupDetailPage kind="study" />} />
                    <Route path="/groups/team/:id" element={<GroupDetailPage kind="team" />} />
                    <Route path="/groups/study/write" element={<GroupWritePage kind="study" />} />
                    <Route path="/groups/team/write" element={<GroupWritePage kind="team" />} />
                    <Route path="/groups/study/:id/apply" element={<GroupApplyPage kind="study" />} />
                    <Route path="/groups/team/:id/apply" element={<GroupApplyPage kind="team" />} />

                    {/* Message */}
                    <Route path="/message" element={<MessagePage />} />
                    <Route path="/message/detail/:roomId" element={<MessageDetailPage />} />
                    <Route path="/message/new/:postId/:targetMemberId" element={<MessageDetailPage />} />

                    {/* Notification */}
                    <Route path="/notification" element={<NotificationPage />} />
                    <Route path="/notification/detail" element={<NotificationDetailPage />} />

                    {/* Report */}
                    <Route path="/report" element={<ReportPage />} />

                    {/* My Group Manage */}
                    <Route path="/group_manage" element={<GroupManagePage />} />
                    <Route path="/group_manage/list/:type" element={<MyGroupPage />} />
                    <Route path="/mygroup/detail/:type/:groupId/:isLeader" element={<MyGroupDetailPage kind="study" />} />

                    {/* My Groups (New) */}
                    <Route path="/mygroups/study" element={<MyGroupsPage />} />
                    <Route path="/mygroups/team" element={<MyGroupsPage />} />
                    <Route path="/mygroups/study/:id" element={<MyGroupDetailPageNew kind="study" />} />
                    <Route path="/mygroups/team/:id" element={<MyGroupDetailPageNew kind="team" />} />

                    {/* Member Manage & Progress */}
                    <Route path="/member_manage/:type/:groupId" element={<MemberManagePage />} />
                    <Route path="/group/progress/:type/:groupId" element={<MyProgressPage />} />

                    {/* Announcements */}
                    <Route path="/announcements/:type/:groupId/:isLeader" element={<AnnouncementsPage />} />
                    <Route path="/announcements/write/:type/:groupId" element={<WriteAnnouncementPage />} />
                    <Route path="/announcements/edit/:type/:groupId/:noticeId/*" element={<EditAnnouncementPage />} />

                    {/* My Page */}
                    <Route path="/mypage" element={<MyPage />} />
                    <Route path="/mypage/posts" element={<MyPostsPage />} />
                    <Route path="/mypage/comments" element={<MyCommentsPage />} />
                    <Route path="/mypage/scraps" element={<MyScrapsPage />} />
                    <Route path="/portfolio/:id" element={<PortfolioDetailPage />} />
                    <Route path="/portfolio/detail" element={<PortfolioDetailPage />} />
                    <Route path="/portfolio/new" element={<PortfolioWritePage mode="create" />} />
                    <Route path="/portfolio/edit/:id" element={<PortfolioWritePage mode="edit" />} />
                    <Route path="/portfolio/project/:portfolioId" element={<ProjectWritePage />} />
                    <Route path="/portfolio/project/:portfolioId/:projectId" element={<ProjectWritePage />} />

                    <Route path="/mypage/inquiry" element={<InquiryPage />} />
                    <Route path="/mypage/community_rules" element={<CommunityRulesPage />} />
                    <Route path="/mypage/restriction_history" element={<RestrictionHistoryPage />} />
                    <Route path="/mypage/terms_of_service" element={<TermsOfServicePage />} />
                    <Route path="/setting_screen" element={<SettingPage />} />

                    <Route path="/test" element={<TestPage />} />
                </Route>
            </Routes>
        </BrowserRouter>
    )
}

export default App
