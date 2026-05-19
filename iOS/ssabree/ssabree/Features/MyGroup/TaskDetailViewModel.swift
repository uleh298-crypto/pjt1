import Foundation
import Observation

struct TaskDetailUiState {
    var task: TaskUiModel? = nil
    var authorName: String? = nil
    var authorProfileImageUrl: String? = nil
    var isLoading: Bool = false
    var isDeleting: Bool = false
    var errorMessage: String? = nil
    var deleteSuccess: Bool = false
}

@Observable
@MainActor
final class TaskDetailViewModel {
    private let groupService: GroupService
    private let groupRepository: GroupRepository
    private let groupKind: GroupKind
    private let groupId: Int
    private let taskId: Int

    var uiState = TaskDetailUiState()

    init(
        groupService: GroupService,
        groupRepository: GroupRepository,
        groupKind: GroupKind,
        groupId: Int,
        taskId: Int
    ) {
        self.groupService = groupService
        self.groupRepository = groupRepository
        self.groupKind = groupKind
        self.groupId = groupId
        self.taskId = taskId
    }

    func load() async {
        uiState.isLoading = true
        uiState.errorMessage = nil

        async let tasksResult = loadTasks()
        async let detailResult = loadDetail()
        async let membersResult = loadMembers()

        let (tasks, detail, members) = await (tasksResult, detailResult, membersResult)

        guard let task = tasks.first(where: { $0.id == taskId }) else {
            uiState.isLoading = false
            uiState.errorMessage = "일정 정보를 찾을 수 없습니다."
            return
        }

        // Merge leader into members list if not present
        var allMembers = members
        if let leaderId = detail?.leaderId, !members.contains(where: { $0.memberId == leaderId }) {
            allMembers.append(MemberUiModel(
                id: leaderId,
                memberId: leaderId,
                name: detail?.leaderName ?? "-",
                mattermostId: "-",
                profileImageUrl: detail?.leaderProfileImageUrl,
                portfolioId: nil
            ))
        }

        // Find author
        if let creatorId = task.creatorId,
           let author = allMembers.first(where: { $0.memberId == creatorId }) {
            uiState.authorName = author.name
            uiState.authorProfileImageUrl = author.profileImageUrl
        }

        uiState.task = task
        uiState.isLoading = false
    }

    func deleteTask() async {
        uiState.isDeleting = true
        uiState.errorMessage = nil

        do {
            if groupKind == .study {
                try await groupService.deleteStudyTask(taskId: taskId)
            } else {
                try await groupService.deleteTeamTask(taskId: taskId)
            }
            uiState.deleteSuccess = true
        } catch {
            uiState.errorMessage = error.localizedDescription
        }
        uiState.isDeleting = false
    }

    func clearError() {
        uiState.errorMessage = nil
    }

    // MARK: - Private

    private func loadTasks() async -> [TaskUiModel] {
        do {
            let responses: [TaskResponse]
            if groupKind == .study {
                responses = try await groupService.getStudyTasks(studyId: groupId)
            } else {
                responses = try await groupService.getTeamTasks(teamId: groupId)
            }
            return responses.map { response in
                TaskUiModel(
                    id: response.id,
                    title: response.title,
                    content: response.content,
                    startDate: response.startDate ?? "",
                    endDate: response.endDate ?? "",
                    status: response.status,
                    creatorId: response.creatorId
                )
            }
        } catch {
            return []
        }
    }

    private func loadDetail() async -> GroupDetailModel? {
        let result: Result<GroupDetailModel, Error>
        if groupKind == .study {
            result = await groupRepository.getStudyDetail(studyId: groupId)
        } else {
            result = await groupRepository.getTeamDetail(teamId: groupId)
        }
        return try? result.get()
    }

    private func loadMembers() async -> [MemberUiModel] {
        let result: Result<[GroupMemberModel], Error>
        if groupKind == .study {
            result = await groupRepository.getStudyMembers(studyId: groupId)
        } else {
            result = await groupRepository.getTeamMembers(teamId: groupId)
        }
        guard let members = try? result.get() else { return [] }
        return members.map { member in
            MemberUiModel(
                id: member.id,
                memberId: member.memberId,
                name: member.nickname ?? "-",
                mattermostId: member.mattermostId ?? "-",
                profileImageUrl: member.profileImageUrl,
                portfolioId: member.portfolioId
            )
        }
    }
}
