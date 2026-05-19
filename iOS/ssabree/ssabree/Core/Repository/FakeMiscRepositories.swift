import Foundation

// MARK: - Fake Board Repository

final class FakeBoardRepository: BoardRepository {
    func getBoards() async -> Result<[BoardModel], Error> {
        .success([])
    }

    func getNotice() async -> Result<String?, Error> {
        .success(nil)
    }
}

// MARK: - Fake Campus Repository

final class FakeCampusRepository: CampusRepository {
    func getCampuses() async -> Result<[Campus], Error> {
        .success([
            Campus(id: 1, name: "서울"),
            Campus(id: 2, name: "대전"),
            Campus(id: 3, name: "광주"),
            Campus(id: 4, name: "구미"),
            Campus(id: 5, name: "부울경")
        ])
    }

    func getClasses(campusId: Int) async -> Result<[Ban], Error> {
        return .success([
            Ban(id: 1, name: "1반", campusId: campusId, generation: 14, classNo: 1, trackType: "비전공"),
            Ban(id: 2, name: "2반", campusId: campusId, generation: 14, classNo: 2, trackType: "비전공"),
            Ban(id: 3, name: "3반", campusId: campusId, generation: 14, classNo: 3, trackType: "비전공"),
            Ban(id: 4, name: "4반", campusId: campusId, generation: 15, classNo: 1, trackType: "비전공"),
            Ban(id: 5, name: "5반", campusId: campusId, generation: 15, classNo: 2, trackType: "비전공")
        ])
    }
}

// MARK: - Fake Keyword Repository

final class FakeKeywordRepository: KeywordRepository {
    func getPopularKeywords() -> [String] {
        []
    }

    func getRecentKeywords() -> [String] {
        []
    }

    func addRecentKeyword(keyword: String) {
        // no-op
    }

    func deleteRecentKeyword(keyword: String) {
        // no-op
    }
}

// MARK: - Fake Comment Repository

final class FakeCommentRepository: CommentRepository {
    func like(commentId: Int) async -> Result<CommentLikeModel, Error> {
        .success(CommentLikeModel(liked: true, likeCount: 1))
    }

    func unlike(commentId: Int) async -> Result<CommentLikeModel, Error> {
        .success(CommentLikeModel(liked: false, likeCount: 0))
    }

    func updateComment(commentId: Int, content: String) async -> Result<Void, Error> {
        .success(())
    }

    func deleteComment(commentId: Int) async -> Result<Void, Error> {
        .success(())
    }
}

// MARK: - Fake Upload Repository

final class FakeUploadRepository: UploadRepository {
    func uploadImage(image: Data) async -> Result<String, Error> {
        .success("https://example.com/fake-image.jpg")
    }
}

// MARK: - Fake MyPage Repository

final class FakeMyPageRepository: MyPageRepository {
    func getMyPage() async -> Result<MyPageModel, Error> {
        .success(MyPageModel(
            user: MyPageUserModel(
                userId: 1,
                name: "김싸피",
                mattermostId: "ssafy",
                campus: "서울",
                generation: 13,
                profileImageUrl: nil
            ),
            counts: MyPageCountsModel(postCount: 5, commentCount: 10, scrapCount: 3),
            portfolioSummary: MyPagePortfolioSummaryModel(
                techStack: ["Swift": "high", "Kotlin": "mid"],
                ssafySwRating: "A+",
                solvedAcRank: "Gold",
                solvedAcHandle: "example123",
                solvedAcTierName: "Gold III",
                solvedAcTierImageUrl: nil,
                solvedAcSolvedCount: 150,
                links: ["https://github.com/ssafy"],
                projects: ["SSABREE", "프로젝트2"]
            )
        ))
    }

    func getMyPosts() async -> Result<[PostModel], Error> {
        .success([])
    }

    func getMyComments() async -> Result<[MyCommentModel], Error> {
        .success([])
    }

    func getMyScraps() async -> Result<[PostModel], Error> {
        .success([])
    }

    func updateProfileImage(_ imageUrl: String) async -> Result<Void, Error> {
        .success(())
    }

    func getAnon() async -> Result<AnonModel, Error> {
        .success(AnonModel(name: "익명", isAuthor: false, isMine: false))
    }
}

// MARK: - Fake Group Service

final class FakeGroupService: GroupService {
    func getGroups(category: String, page: Int, size: Int) async throws -> GroupListResponse {
        GroupListResponse(content: [], hasNext: false)
    }

    func getGroup(id: Int) async throws -> GroupResponse {
        GroupResponse(id: id, title: "테스트 그룹", content: "테스트 내용", category: "STUDY", maxMembers: 5, currentMembers: 1, dDay: nil, status: "RECRUITING")
    }

    func getMyGroups() async throws -> [GroupResponse] { [] }
    func getApplications() async throws -> [ApplicationResponse] { [] }
    func getAnnouncements(groupId: Int) async throws -> [AnnouncementResponse] { [] }
    func createGroup(request: GroupCreateRequest) async throws {}
    func applyGroup(request: GroupApplyRequest) async throws {}
    func createAnnouncement(groupId: Int, request: AnnouncementCreateRequest) async throws {}
    func updateAnnouncement(groupId: Int, announcementId: Int, request: AnnouncementUpdateRequest) async throws {}
    func deleteAnnouncement(groupId: Int, announcementId: Int) async throws {}
    func getMembers(groupId: Int) async throws -> [MemberResponse] { [] }
    func approveMember(groupId: Int, memberId: Int) async throws {}
    func rejectMember(groupId: Int, memberId: Int) async throws {}
    func removeMember(groupId: Int, memberId: Int) async throws {}
    func getProgress(groupId: Int) async throws -> [ProgressResponse] { [] }

    // Study APIs
    func getStudies(campusId: Int?, type: String?) async throws -> [GroupSummaryResponse] { [] }
    func getMyStudies() async throws -> [GroupSummaryResponse] { [] }
    func getStudyDetail(studyId: Int) async throws -> GroupDetailResponse {
        GroupDetailResponse(id: studyId, title: "테스트 스터디", description: nil, type: "STUDY", capacity: 5, status: "RECRUITING", leader: nil, startDate: nil, endDate: nil, createdAt: nil, updatedAt: nil)
    }
    func getStudyMembers(studyId: Int) async throws -> [GroupMemberResponse] { [] }
    func createStudy(request: StudyTeamCreateRequest) async throws -> GroupSummaryResponse {
        GroupSummaryResponse(id: 1, title: request.title, type: "STUDY", capacity: request.capacity, status: "RECRUITING", leader: nil, startDate: nil, endDate: nil)
    }
    func updateStudy(studyId: Int, request: GroupUpdateRequest) async throws {}
    func applyStudy(studyId: Int, request: GroupApplicationRequest) async throws {}

    // Team APIs
    func getTeams(campusId: Int?, type: String?) async throws -> [GroupSummaryResponse] { [] }
    func getMyTeams() async throws -> [GroupSummaryResponse] { [] }
    func getTeamDetail(teamId: Int) async throws -> GroupDetailResponse {
        GroupDetailResponse(id: teamId, title: "테스트 팀", description: nil, type: "PROJECT", capacity: 5, status: "RECRUITING", leader: nil, startDate: nil, endDate: nil, createdAt: nil, updatedAt: nil)
    }
    func getTeamMembers(teamId: Int) async throws -> [GroupMemberResponse] { [] }
    func createTeam(request: StudyTeamCreateRequest) async throws -> GroupSummaryResponse {
        GroupSummaryResponse(id: 1, title: request.title, type: "PROJECT", capacity: request.capacity, status: "RECRUITING", leader: nil, startDate: nil, endDate: nil)
    }
    func updateTeam(teamId: Int, request: GroupUpdateRequest) async throws {}
    func applyTeam(teamId: Int, request: GroupApplicationRequest) async throws {}

    // Delete APIs
    func deleteStudy(studyId: Int) async throws {}
    func deleteTeam(teamId: Int) async throws {}

    // My Applications APIs
    func getMyStudyApplications() async throws -> [MyApplicationResponse] { [] }
    func getMyTeamApplications() async throws -> [MyApplicationResponse] { [] }
    func cancelStudyApplication(applicationId: Int) async throws {}
    func cancelTeamApplication(applicationId: Int) async throws {}

    // Study Notice APIs
    func getStudyNotices(studyId: Int) async throws -> [NoticeResponse] { [] }
    func createStudyNotice(studyId: Int, request: NoticeCreateRequest) async throws {}
    func updateStudyNotice(studyId: Int, noticeId: Int, request: NoticeCreateRequest) async throws {}
    func deleteStudyNotice(studyId: Int, noticeId: Int) async throws {}

    // Team Notice APIs
    func getTeamNotices(teamId: Int) async throws -> [NoticeResponse] { [] }
    func createTeamNotice(teamId: Int, request: NoticeCreateRequest) async throws {}
    func updateTeamNotice(teamId: Int, noticeId: Int, request: NoticeCreateRequest) async throws {}
    func deleteTeamNotice(teamId: Int, noticeId: Int) async throws {}

    // Study Task APIs
    func getStudyTasks(studyId: Int) async throws -> [TaskResponse] { [] }
    func createStudyTask(studyId: Int, request: TaskCreateRequest) async throws -> TaskResponse {
        TaskResponse(id: 1, studyId: studyId, teamId: nil, title: request.title, content: request.content, startDate: request.startDate, endDate: request.endDate, status: request.status, creatorId: nil, createdAt: nil, updatedAt: nil)
    }
    func updateStudyTask(taskId: Int, request: TaskCreateRequest) async throws {}
    func updateStudyTaskStatus(taskId: Int, request: TaskStatusRequest) async throws {}
    func deleteStudyTask(taskId: Int) async throws {}

    // Team Task APIs
    func getTeamTasks(teamId: Int) async throws -> [TaskResponse] { [] }
    func createTeamTask(teamId: Int, request: TaskCreateRequest) async throws -> TaskResponse {
        TaskResponse(id: 1, studyId: nil, teamId: teamId, title: request.title, content: request.content, startDate: request.startDate, endDate: request.endDate, status: request.status, creatorId: nil, createdAt: nil, updatedAt: nil)
    }
    func updateTeamTask(taskId: Int, request: TaskCreateRequest) async throws {}
    func updateTeamTaskStatus(taskId: Int, request: TaskStatusRequest) async throws {}
    func deleteTeamTask(taskId: Int) async throws {}

    // Leave Group APIs
    func leaveStudy(studyId: Int) async throws {}
    func leaveTeam(teamId: Int) async throws {}

    // Application Management APIs
    func getStudyApplications(studyId: Int) async throws -> [GroupApplicationDetailResponse] { [] }
    func getStudyApplicationDetail(applicationId: Int) async throws -> GroupApplicationDetailResponse {
        GroupApplicationDetailResponse(id: applicationId, portfolio: nil, title: "지원서", message: "잘 부탁드립니다.", position: "백엔드", status: "PENDING", createdAt: nil, updatedAt: nil, memberId: 1, portfolioId: 1)
    }
    func acceptStudyApplication(applicationId: Int) async throws {}
    func rejectStudyApplication(applicationId: Int) async throws {}
    func removeStudyMember(studyId: Int, memberId: Int) async throws {}
    func getTeamApplications(teamId: Int) async throws -> [GroupApplicationDetailResponse] { [] }
    func getTeamApplicationDetail(applicationId: Int) async throws -> GroupApplicationDetailResponse {
        GroupApplicationDetailResponse(id: applicationId, portfolio: nil, title: "지원서", message: "잘 부탁드립니다.", position: "백엔드", status: "PENDING", createdAt: nil, updatedAt: nil, memberId: 1, portfolioId: 1)
    }
    func acceptTeamApplication(applicationId: Int) async throws {}
    func rejectTeamApplication(applicationId: Int) async throws {}
    func removeTeamMember(teamId: Int, memberId: Int) async throws {}

    func getMemberProfile(memberId: Int) async throws -> MeResponse {
        MeResponse(id: memberId, email: nil, name: nil, studentNo: nil, campus: nil, generation: nil, classNo: nil, mattermostId: nil, profileImageUrl: nil, deletedAt: nil, createdAt: nil, updatedAt: nil)
    }
}
