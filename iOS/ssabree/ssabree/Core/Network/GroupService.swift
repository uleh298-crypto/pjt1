import Foundation

protocol GroupService {
    func getGroups(category: String, page: Int, size: Int) async throws -> GroupListResponse
    func getGroup(id: Int) async throws -> GroupResponse
    func getMyGroups() async throws -> [GroupResponse]
    func getApplications() async throws -> [ApplicationResponse]
    func getAnnouncements(groupId: Int) async throws -> [AnnouncementResponse]
    func createGroup(request: GroupCreateRequest) async throws
    func applyGroup(request: GroupApplyRequest) async throws

    func createAnnouncement(groupId: Int, request: AnnouncementCreateRequest) async throws
    func updateAnnouncement(groupId: Int, announcementId: Int, request: AnnouncementUpdateRequest) async throws
    func deleteAnnouncement(groupId: Int, announcementId: Int) async throws

    func getMembers(groupId: Int) async throws -> [MemberResponse]
    func approveMember(groupId: Int, memberId: Int) async throws
    func rejectMember(groupId: Int, memberId: Int) async throws
    func removeMember(groupId: Int, memberId: Int) async throws
    func getProgress(groupId: Int) async throws -> [ProgressResponse]

    // MARK: - Study APIs
    func getStudies(campusId: Int?, type: String?) async throws -> [GroupSummaryResponse]
    func getMyStudies() async throws -> [GroupSummaryResponse]
    func getStudyDetail(studyId: Int) async throws -> GroupDetailResponse
    func getStudyMembers(studyId: Int) async throws -> [GroupMemberResponse]
    func createStudy(request: StudyTeamCreateRequest) async throws -> GroupSummaryResponse
    func updateStudy(studyId: Int, request: GroupUpdateRequest) async throws
    func applyStudy(studyId: Int, request: GroupApplicationRequest) async throws

    // MARK: - Team APIs
    func getTeams(campusId: Int?, type: String?) async throws -> [GroupSummaryResponse]
    func getMyTeams() async throws -> [GroupSummaryResponse]
    func getTeamDetail(teamId: Int) async throws -> GroupDetailResponse
    func getTeamMembers(teamId: Int) async throws -> [GroupMemberResponse]
    func createTeam(request: StudyTeamCreateRequest) async throws -> GroupSummaryResponse
    func updateTeam(teamId: Int, request: GroupUpdateRequest) async throws
    func applyTeam(teamId: Int, request: GroupApplicationRequest) async throws

    // MARK: - Delete APIs
    func deleteStudy(studyId: Int) async throws
    func deleteTeam(teamId: Int) async throws

    // MARK: - My Applications APIs
    func getMyStudyApplications() async throws -> [MyApplicationResponse]
    func getMyTeamApplications() async throws -> [MyApplicationResponse]
    func cancelStudyApplication(applicationId: Int) async throws
    func cancelTeamApplication(applicationId: Int) async throws

    // MARK: - Study Notice APIs
    func getStudyNotices(studyId: Int) async throws -> [NoticeResponse]
    func createStudyNotice(studyId: Int, request: NoticeCreateRequest) async throws
    func updateStudyNotice(studyId: Int, noticeId: Int, request: NoticeCreateRequest) async throws
    func deleteStudyNotice(studyId: Int, noticeId: Int) async throws

    // MARK: - Team Notice APIs
    func getTeamNotices(teamId: Int) async throws -> [NoticeResponse]
    func createTeamNotice(teamId: Int, request: NoticeCreateRequest) async throws
    func updateTeamNotice(teamId: Int, noticeId: Int, request: NoticeCreateRequest) async throws
    func deleteTeamNotice(teamId: Int, noticeId: Int) async throws

    // MARK: - Study Task APIs
    func getStudyTasks(studyId: Int) async throws -> [TaskResponse]
    func createStudyTask(studyId: Int, request: TaskCreateRequest) async throws -> TaskResponse
    func updateStudyTask(taskId: Int, request: TaskCreateRequest) async throws
    func updateStudyTaskStatus(taskId: Int, request: TaskStatusRequest) async throws
    func deleteStudyTask(taskId: Int) async throws

    // MARK: - Team Task APIs
    func getTeamTasks(teamId: Int) async throws -> [TaskResponse]
    func createTeamTask(teamId: Int, request: TaskCreateRequest) async throws -> TaskResponse
    func updateTeamTask(taskId: Int, request: TaskCreateRequest) async throws
    func updateTeamTaskStatus(taskId: Int, request: TaskStatusRequest) async throws
    func deleteTeamTask(taskId: Int) async throws

    // MARK: - Leave Group APIs
    func leaveStudy(studyId: Int) async throws
    func leaveTeam(teamId: Int) async throws

    // MARK: - Study Application Management APIs
    func getStudyApplications(studyId: Int) async throws -> [GroupApplicationDetailResponse]
    func getStudyApplicationDetail(applicationId: Int) async throws -> GroupApplicationDetailResponse
    func acceptStudyApplication(applicationId: Int) async throws
    func rejectStudyApplication(applicationId: Int) async throws
    func removeStudyMember(studyId: Int, memberId: Int) async throws

    // MARK: - Team Application Management APIs
    func getTeamApplications(teamId: Int) async throws -> [GroupApplicationDetailResponse]
    func getTeamApplicationDetail(applicationId: Int) async throws -> GroupApplicationDetailResponse
    func acceptTeamApplication(applicationId: Int) async throws
    func rejectTeamApplication(applicationId: Int) async throws
    func removeTeamMember(teamId: Int, memberId: Int) async throws

    // MARK: - Member Profile API
    func getMemberProfile(memberId: Int) async throws -> MeResponse
}

final class GroupServiceImpl: GroupService {
    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func getGroups(category: String, page: Int, size: Int) async throws -> GroupListResponse {
        let queryItems = [
            URLQueryItem(name: "category", value: category),
            URLQueryItem(name: "page", value: String(page)),
            URLQueryItem(name: "size", value: String(size))
        ]
        let endpoint = APIEndpoint(path: "/api/groups", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: queryItems)
    }

    func getGroup(id: Int) async throws -> GroupResponse {
        let endpoint = APIEndpoint(path: "/api/groups/\(id)", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getMyGroups() async throws -> [GroupResponse] {
        let endpoint = APIEndpoint(path: "/api/groups/me", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getApplications() async throws -> [ApplicationResponse] {
        let endpoint = APIEndpoint(path: "/api/groups/applications", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getAnnouncements(groupId: Int) async throws -> [AnnouncementResponse] {
        let endpoint = APIEndpoint(path: "/api/groups/\(groupId)/announcements", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func createGroup(request: GroupCreateRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/groups", method: .POST, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    func applyGroup(request: GroupApplyRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/groups/apply", method: .POST, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    func createAnnouncement(groupId: Int, request: AnnouncementCreateRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/groups/\(groupId)/announcements", method: .POST, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    func updateAnnouncement(groupId: Int, announcementId: Int, request: AnnouncementUpdateRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/groups/\(groupId)/announcements/\(announcementId)", method: .PUT, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    func deleteAnnouncement(groupId: Int, announcementId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/groups/\(groupId)/announcements/\(announcementId)", method: .DELETE, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getMembers(groupId: Int) async throws -> [MemberResponse] {
        let endpoint = APIEndpoint(path: "/api/groups/\(groupId)/members", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func approveMember(groupId: Int, memberId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/groups/\(groupId)/members/\(memberId)/approve", method: .POST, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func rejectMember(groupId: Int, memberId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/groups/\(groupId)/members/\(memberId)/reject", method: .POST, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func removeMember(groupId: Int, memberId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/groups/\(groupId)/members/\(memberId)", method: .DELETE, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getProgress(groupId: Int) async throws -> [ProgressResponse] {
        let endpoint = APIEndpoint(path: "/api/groups/\(groupId)/progress", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    // MARK: - Study APIs

    func getStudies(campusId: Int?, type: String?) async throws -> [GroupSummaryResponse] {
        var queryItems: [URLQueryItem] = []
        if let campusId = campusId {
            queryItems.append(URLQueryItem(name: "campusId", value: String(campusId)))
        }
        if let type = type {
            queryItems.append(URLQueryItem(name: "type", value: type))
        }
        let endpoint = APIEndpoint(path: "/api/studies", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: queryItems.isEmpty ? nil : queryItems)
    }

    func getMyStudies() async throws -> [GroupSummaryResponse] {
        let endpoint = APIEndpoint(path: "/api/studies/me", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getStudyDetail(studyId: Int) async throws -> GroupDetailResponse {
        let endpoint = APIEndpoint(path: "/api/studies/\(studyId)", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getStudyMembers(studyId: Int) async throws -> [GroupMemberResponse] {
        let endpoint = APIEndpoint(path: "/api/studies/\(studyId)/members", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func createStudy(request: StudyTeamCreateRequest) async throws -> GroupSummaryResponse {
        let endpoint = APIEndpoint(path: "/api/studies", method: .POST, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }

    func updateStudy(studyId: Int, request: GroupUpdateRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/studies/\(studyId)", method: .PUT, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    func applyStudy(studyId: Int, request: GroupApplicationRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/studies/\(studyId)/applications", method: .POST, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    // MARK: - Team APIs

    func getTeams(campusId: Int?, type: String?) async throws -> [GroupSummaryResponse] {
        var queryItems: [URLQueryItem] = []
        if let campusId = campusId {
            queryItems.append(URLQueryItem(name: "campusId", value: String(campusId)))
        }
        if let type = type {
            queryItems.append(URLQueryItem(name: "type", value: type))
        }
        let endpoint = APIEndpoint(path: "/api/teams", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: queryItems.isEmpty ? nil : queryItems)
    }

    func getMyTeams() async throws -> [GroupSummaryResponse] {
        let endpoint = APIEndpoint(path: "/api/teams/me", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getTeamDetail(teamId: Int) async throws -> GroupDetailResponse {
        let endpoint = APIEndpoint(path: "/api/teams/\(teamId)", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getTeamMembers(teamId: Int) async throws -> [GroupMemberResponse] {
        let endpoint = APIEndpoint(path: "/api/teams/\(teamId)/members", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func createTeam(request: StudyTeamCreateRequest) async throws -> GroupSummaryResponse {
        let endpoint = APIEndpoint(path: "/api/teams", method: .POST, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }

    func updateTeam(teamId: Int, request: GroupUpdateRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/teams/\(teamId)", method: .PUT, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    func applyTeam(teamId: Int, request: GroupApplicationRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/teams/\(teamId)/applications", method: .POST, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    // MARK: - Delete APIs

    func deleteStudy(studyId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/studies/\(studyId)", method: .DELETE, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func deleteTeam(teamId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/teams/\(teamId)", method: .DELETE, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    // MARK: - My Applications APIs

    func getMyStudyApplications() async throws -> [MyApplicationResponse] {
        let endpoint = APIEndpoint(path: "/api/study-applications/me", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getMyTeamApplications() async throws -> [MyApplicationResponse] {
        let endpoint = APIEndpoint(path: "/api/team-applications/me", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func cancelStudyApplication(applicationId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/study-applications/\(applicationId)", method: .DELETE, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func cancelTeamApplication(applicationId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/team-applications/\(applicationId)", method: .DELETE, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    // MARK: - Study Notice APIs

    func getStudyNotices(studyId: Int) async throws -> [NoticeResponse] {
        let endpoint = APIEndpoint(path: "/api/studies/\(studyId)/notices", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func createStudyNotice(studyId: Int, request: NoticeCreateRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/studies/\(studyId)/notices", method: .POST, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    func updateStudyNotice(studyId: Int, noticeId: Int, request: NoticeCreateRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/studies/\(studyId)/notices/\(noticeId)", method: .PUT, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    func deleteStudyNotice(studyId: Int, noticeId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/studies/\(studyId)/notices/\(noticeId)", method: .DELETE, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    // MARK: - Team Notice APIs

    func getTeamNotices(teamId: Int) async throws -> [NoticeResponse] {
        let endpoint = APIEndpoint(path: "/api/teams/\(teamId)/notices", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func createTeamNotice(teamId: Int, request: NoticeCreateRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/teams/\(teamId)/notices", method: .POST, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    func updateTeamNotice(teamId: Int, noticeId: Int, request: NoticeCreateRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/teams/\(teamId)/notices/\(noticeId)", method: .PUT, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    func deleteTeamNotice(teamId: Int, noticeId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/teams/\(teamId)/notices/\(noticeId)", method: .DELETE, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    // MARK: - Study Task APIs

    func getStudyTasks(studyId: Int) async throws -> [TaskResponse] {
        let endpoint = APIEndpoint(path: "/api/studies/\(studyId)/tasks", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func createStudyTask(studyId: Int, request: TaskCreateRequest) async throws -> TaskResponse {
        let endpoint = APIEndpoint(path: "/api/studies/\(studyId)/tasks", method: .POST, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }

    func updateStudyTask(taskId: Int, request: TaskCreateRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/study-tasks/\(taskId)", method: .PUT, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    func updateStudyTaskStatus(taskId: Int, request: TaskStatusRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/study-tasks/\(taskId)/status", method: .PUT, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    func deleteStudyTask(taskId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/study-tasks/\(taskId)", method: .DELETE, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    // MARK: - Team Task APIs

    func getTeamTasks(teamId: Int) async throws -> [TaskResponse] {
        let endpoint = APIEndpoint(path: "/api/teams/\(teamId)/tasks", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func createTeamTask(teamId: Int, request: TaskCreateRequest) async throws -> TaskResponse {
        let endpoint = APIEndpoint(path: "/api/teams/\(teamId)/tasks", method: .POST, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: request, queryItems: nil)
    }

    func updateTeamTask(taskId: Int, request: TaskCreateRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/team-tasks/\(taskId)", method: .PUT, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    func updateTeamTaskStatus(taskId: Int, request: TaskStatusRequest) async throws {
        let endpoint = APIEndpoint(path: "/api/team-tasks/\(taskId)/status", method: .PUT, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: request, queryItems: nil)
    }

    func deleteTeamTask(taskId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/team-tasks/\(taskId)", method: .DELETE, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    // MARK: - Leave Group APIs

    func leaveStudy(studyId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/studies/\(studyId)/leave", method: .DELETE, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func leaveTeam(teamId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/teams/\(teamId)/leave", method: .DELETE, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    // MARK: - Study Application Management APIs

    func getStudyApplications(studyId: Int) async throws -> [GroupApplicationDetailResponse] {
        let endpoint = APIEndpoint(path: "/api/studies/\(studyId)/applications", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getStudyApplicationDetail(applicationId: Int) async throws -> GroupApplicationDetailResponse {
        let endpoint = APIEndpoint(path: "/api/study-applications/\(applicationId)", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func acceptStudyApplication(applicationId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/study-applications/\(applicationId)/accept", method: .POST, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func rejectStudyApplication(applicationId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/study-applications/\(applicationId)/reject", method: .POST, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func removeStudyMember(studyId: Int, memberId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/studies/\(studyId)/members/\(memberId)", method: .DELETE, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    // MARK: - Team Application Management APIs

    func getTeamApplications(teamId: Int) async throws -> [GroupApplicationDetailResponse] {
        let endpoint = APIEndpoint(path: "/api/teams/\(teamId)/applications", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getTeamApplicationDetail(applicationId: Int) async throws -> GroupApplicationDetailResponse {
        let endpoint = APIEndpoint(path: "/api/team-applications/\(applicationId)", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func acceptTeamApplication(applicationId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/team-applications/\(applicationId)/accept", method: .POST, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func rejectTeamApplication(applicationId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/team-applications/\(applicationId)/reject", method: .POST, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func removeTeamMember(teamId: Int, memberId: Int) async throws {
        let endpoint = APIEndpoint(path: "/api/teams/\(teamId)/members/\(memberId)", method: .DELETE, requiresAuth: true)
        try await apiClient.requestEmpty(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }

    func getMemberProfile(memberId: Int) async throws -> MeResponse {
        let endpoint = APIEndpoint(path: "/api/members/\(memberId)", method: .GET, requiresAuth: true)
        return try await apiClient.request(endpoint: endpoint, body: nil as String?, queryItems: nil)
    }
}

// MARK: - New DTOs for Study/Team APIs

/// Leader info nested in group responses (matches backend Member entity fields)
struct GroupLeaderInfoResponse: Decodable {
    let id: Int
    let name: String?
    let email: String?
    let mattermostId: String?
    let profileImageUrl: String?
}

struct GroupSummaryResponse: Decodable {
    let id: Int
    let title: String
    let type: String
    let capacity: Int
    let status: String?
    let leader: GroupLeaderInfoResponse?
    let startDate: String?
    let endDate: String?
    let currentMemberCount: Int

    private enum CodingKeys: String, CodingKey {
        case id, title, type, capacity, status, leader, startDate, endDate, members
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decode(Int.self, forKey: .id)
        title = try container.decode(String.self, forKey: .title)
        type = try container.decode(String.self, forKey: .type)
        capacity = try container.decode(Int.self, forKey: .capacity)
        status = try container.decodeIfPresent(String.self, forKey: .status)
        leader = try container.decodeIfPresent(GroupLeaderInfoResponse.self, forKey: .leader)
        startDate = try container.decodeIfPresent(String.self, forKey: .startDate)
        endDate = try container.decodeIfPresent(String.self, forKey: .endDate)
        let memberCount: Int
        if container.contains(.members),
           let membersContainer = try? container.nestedUnkeyedContainer(forKey: .members) {
            memberCount = membersContainer.count ?? 0
        } else {
            memberCount = 0
        }
        currentMemberCount = memberCount + (leader != nil ? 1 : 0)
    }

    init(id: Int, title: String, type: String, capacity: Int, status: String?, leader: GroupLeaderInfoResponse?, startDate: String?, endDate: String?, currentMemberCount: Int = 0) {
        self.id = id
        self.title = title
        self.type = type
        self.capacity = capacity
        self.status = status
        self.leader = leader
        self.startDate = startDate
        self.endDate = endDate
        self.currentMemberCount = currentMemberCount
    }

    func toModel() -> GroupSummaryModel {
        GroupSummaryModel(
            id: id,
            title: title,
            type: type,
            capacity: capacity,
            currentMembers: currentMemberCount,
            status: status,
            leaderId: leader?.id,
            startDate: startDate,
            endDate: endDate
        )
    }
}

struct GroupDetailResponse: Decodable {
    let id: Int
    let title: String
    let description: String?
    let type: String
    let capacity: Int
    let status: String?
    let leader: GroupLeaderInfoResponse?
    let startDate: String?
    let endDate: String?
    let createdAt: String?
    let updatedAt: String?

    func toDetailModel() -> GroupDetailModel {
        GroupDetailModel(
            id: id,
            title: title,
            description: description,
            type: type,
            capacity: capacity,
            currentMembers: nil,
            status: status,
            leaderId: leader?.id,
            leaderName: leader?.name,
            leaderMattermostId: leader?.mattermostId,
            leaderProfileImageUrl: leader?.profileImageUrl,
            startDate: startDate,
            endDate: endDate,
            createdAt: createdAt,
            updatedAt: updatedAt
        )
    }
}

struct GroupMemberResponse: Decodable {
    let id: Int
    let memberId: Int
    // Backend returns memberName (not nickname)
    let memberName: String?
    let memberEmail: String?
    // Backend returns memberProfileImageUrl (not profileImageUrl)
    let memberProfileImageUrl: String?
    let role: String?
    let status: String?
    let portfolioId: Int?

    func toModel() -> GroupMemberModel {
        GroupMemberModel(
            id: id,
            memberId: memberId,
            nickname: memberName,
            profileImageUrl: memberProfileImageUrl,
            role: role,
            status: status,
            mattermostId: nil,
            portfolioId: portfolioId
        )
    }
}

struct GroupApplicationRequest: Encodable {
    let portfolioId: Int
    let title: String
    let message: String
    let position: String
}

// MARK: - My Application Response

struct MyApplicationResponse: Decodable {
    let id: Int
    let study: MyApplicationStudyInfo?
    let team: MyApplicationTeamInfo?
    let title: String
    let message: String
    let status: String
    let position: String
    let createdAt: String?
    let updatedAt: String?

    var groupId: Int? {
        study?.id ?? team?.id
    }

    var groupTitle: String? {
        study?.title ?? team?.title
    }

    var leaderName: String? {
        study?.leader?.name ?? team?.leaderName
    }
}

struct MyApplicationStudyInfo: Decodable {
    let id: Int
    let title: String
    let leader: GroupLeaderInfoResponse?
}

struct MyApplicationTeamInfo: Decodable {
    let id: Int
    let title: String
    let leaderId: Int?
    let leaderName: String?
}

// MARK: - Notice Response

struct NoticeResponse: Decodable {
    let id: Int
    let studyId: Int?
    let teamId: Int?
    let title: String
    let content: String
    let isPinned: Bool?
    let createdAt: String?
    let updatedAt: String?
}

// MARK: - Task Response

struct TaskResponse: Decodable {
    let id: Int
    let studyId: Int?
    let teamId: Int?
    let title: String
    let content: String
    let startDate: String?
    let endDate: String?
    let status: String
    let creatorId: Int?
    let createdAt: String?
    let updatedAt: String?
}

// MARK: - Notice/Task Request

struct NoticeCreateRequest: Encodable {
    let title: String
    let content: String
    let isPinned: Bool
}

struct TaskCreateRequest: Encodable {
    let title: String
    let content: String
    let startDate: String
    let endDate: String
    let status: String
}

struct TaskStatusRequest: Encodable {
    let status: String
}

// MARK: - Application Detail Response

struct GroupApplicationDetailResponse: Decodable {
    let id: Int
    let portfolio: ApplicationPortfolioInfo?
    let title: String
    let message: String
    let position: String
    let status: String
    let createdAt: String?
    let updatedAt: String?
    let memberId: Int?
    let portfolioId: Int?

    func toApplicantModel() -> GroupMemberModel {
        GroupMemberModel(
            id: id,
            memberId: portfolio?.memberId ?? memberId ?? 0,
            nickname: portfolio?.memberName,
            profileImageUrl: portfolio?.memberProfileImageUrl,
            role: position,
            status: status,
            mattermostId: nil,
            portfolioId: portfolio?.id ?? portfolioId
        )
    }
}

struct ApplicationPortfolioInfo: Decodable {
    let id: Int
    let title: String?
    let memberId: Int
    let memberName: String
    let memberEmail: String?
    let memberProfileImageUrl: String?
    let introduction: String?
    let bojHandle: String?
    let solvedacRank: String?
    let swTestRank: String?
}

// MARK: - Group Update Request

struct GroupUpdateRequest: Encodable {
    let title: String?
    let type: String?
    let capacity: Int?
    let startDate: String?
    let endDate: String?
    let campusId: Int?
    let description: String?
    let status: String?
}
