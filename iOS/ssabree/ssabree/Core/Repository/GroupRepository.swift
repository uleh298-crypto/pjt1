import Foundation

// MARK: - Group Creation Info

struct GroupCreateInfo {
    let title: String
    let type: String
    let capacity: Int
    let startDate: String
    let endDate: String
    let campusId: Int
    let description: String
}

protocol GroupRepository {
    func getGroups(category: String, page: Int, size: Int) async -> Result<[GroupModel], Error>
    func getGroup(id: Int) async -> Result<GroupModel, Error>
    func createGroup(title: String, content: String, category: String, maxMembers: Int) async -> Result<Void, Error>
    func applyGroup(groupId: Int, introduction: String) async -> Result<Void, Error>
    func createStudy(info: GroupCreateInfo) async -> Result<GroupSummaryModel, Error>
    func createTeam(info: GroupCreateInfo) async -> Result<GroupSummaryModel, Error>

    // MARK: - Study APIs
    func getStudies(campusId: Int?, type: String?) async -> Result<[GroupSummaryModel], Error>
    func getMyStudies() async -> Result<[GroupSummaryModel], Error>
    func getStudyDetail(studyId: Int) async -> Result<GroupDetailModel, Error>
    func getStudyMembers(studyId: Int) async -> Result<[GroupMemberModel], Error>

    // MARK: - Team APIs
    func getTeams(campusId: Int?, type: String?) async -> Result<[GroupSummaryModel], Error>
    func getMyTeams() async -> Result<[GroupSummaryModel], Error>
    func getTeamDetail(teamId: Int) async -> Result<GroupDetailModel, Error>
    func getTeamMembers(teamId: Int) async -> Result<[GroupMemberModel], Error>

    // MARK: - Apply APIs
    func applyStudy(studyId: Int, portfolioId: Int, title: String, message: String, position: String) async -> Result<Void, Error>
    func applyTeam(teamId: Int, portfolioId: Int, title: String, message: String, position: String) async -> Result<Void, Error>

    // MARK: - Delete APIs
    func deleteStudy(studyId: Int) async -> Result<Void, Error>
    func deleteTeam(teamId: Int) async -> Result<Void, Error>

    // MARK: - My Applications APIs
    func getMyStudyApplications() async -> Result<[MyApplicationModel], Error>
    func getMyTeamApplications() async -> Result<[MyApplicationModel], Error>
    func cancelStudyApplication(applicationId: Int) async -> Result<Void, Error>
    func cancelTeamApplication(applicationId: Int) async -> Result<Void, Error>
}

final class GroupRepositoryImpl: GroupRepository {
    private let groupService: GroupService
    
    init(groupService: GroupService) {
        self.groupService = groupService
    }
    
    func getGroups(category: String, page: Int, size: Int) async -> Result<[GroupModel], Error> {
        do {
            let response = try await groupService.getGroups(category: category, page: page, size: size)
            let models = response.content.map { $0.toModel() }
            return .success(models)
        } catch {
            return .failure(error)
        }
    }
    
    func getGroup(id: Int) async -> Result<GroupModel, Error> {
        do {
            let response = try await groupService.getGroup(id: id)
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }
    
    func createGroup(title: String, content: String, category: String, maxMembers: Int) async -> Result<Void, Error> {
        do {
            let request = GroupCreateRequest(title: title, content: content, category: category, maxMembers: maxMembers)
            try await groupService.createGroup(request: request)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func createStudy(info: GroupCreateInfo) async -> Result<GroupSummaryModel, Error> {
        do {
            let request = StudyTeamCreateRequest(
                title: info.title,
                type: info.type,
                capacity: info.capacity,
                startDate: info.startDate,
                endDate: info.endDate,
                campusId: info.campusId,
                description: info.description
            )
            let response = try await groupService.createStudy(request: request)
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }

    func createTeam(info: GroupCreateInfo) async -> Result<GroupSummaryModel, Error> {
        do {
            let request = StudyTeamCreateRequest(
                title: info.title,
                type: info.type,
                capacity: info.capacity,
                startDate: info.startDate,
                endDate: info.endDate,
                campusId: info.campusId,
                description: info.description
            )
            let response = try await groupService.createTeam(request: request)
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }
    
    func applyGroup(groupId: Int, introduction: String) async -> Result<Void, Error> {
        do {
            let request = GroupApplyRequest(groupId: groupId, introduction: introduction)
            try await groupService.applyGroup(request: request)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    // MARK: - Study APIs

    func getStudies(campusId: Int?, type: String?) async -> Result<[GroupSummaryModel], Error> {
        do {
            let response = try await groupService.getStudies(campusId: campusId, type: type)
            return .success(response.map { $0.toModel() })
        } catch {
            return .failure(error)
        }
    }

    func getMyStudies() async -> Result<[GroupSummaryModel], Error> {
        do {
            let response = try await groupService.getMyStudies()
            return .success(response.map { $0.toModel() })
        } catch {
            return .failure(error)
        }
    }

    func getStudyDetail(studyId: Int) async -> Result<GroupDetailModel, Error> {
        do {
            let response = try await groupService.getStudyDetail(studyId: studyId)
            return .success(response.toDetailModel())
        } catch {
            return .failure(error)
        }
    }

    func getStudyMembers(studyId: Int) async -> Result<[GroupMemberModel], Error> {
        do {
            let response = try await groupService.getStudyMembers(studyId: studyId)
            return .success(response.map { $0.toModel() })
        } catch {
            return .failure(error)
        }
    }

    // MARK: - Team APIs

    func getTeams(campusId: Int?, type: String?) async -> Result<[GroupSummaryModel], Error> {
        do {
            let response = try await groupService.getTeams(campusId: campusId, type: type)
            return .success(response.map { $0.toModel() })
        } catch {
            return .failure(error)
        }
    }

    func getMyTeams() async -> Result<[GroupSummaryModel], Error> {
        do {
            let response = try await groupService.getMyTeams()
            return .success(response.map { $0.toModel() })
        } catch {
            return .failure(error)
        }
    }

    func getTeamDetail(teamId: Int) async -> Result<GroupDetailModel, Error> {
        do {
            let response = try await groupService.getTeamDetail(teamId: teamId)
            return .success(response.toDetailModel())
        } catch {
            return .failure(error)
        }
    }

    func getTeamMembers(teamId: Int) async -> Result<[GroupMemberModel], Error> {
        do {
            let response = try await groupService.getTeamMembers(teamId: teamId)
            return .success(response.map { $0.toModel() })
        } catch {
            return .failure(error)
        }
    }

    // MARK: - Apply APIs

    func applyStudy(studyId: Int, portfolioId: Int, title: String, message: String, position: String) async -> Result<Void, Error> {
        do {
            let request = GroupApplicationRequest(portfolioId: portfolioId, title: title, message: message, position: position)
            try await groupService.applyStudy(studyId: studyId, request: request)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func applyTeam(teamId: Int, portfolioId: Int, title: String, message: String, position: String) async -> Result<Void, Error> {
        do {
            let request = GroupApplicationRequest(portfolioId: portfolioId, title: title, message: message, position: position)
            try await groupService.applyTeam(teamId: teamId, request: request)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    // MARK: - Delete APIs

    func deleteStudy(studyId: Int) async -> Result<Void, Error> {
        do {
            try await groupService.deleteStudy(studyId: studyId)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func deleteTeam(teamId: Int) async -> Result<Void, Error> {
        do {
            try await groupService.deleteTeam(teamId: teamId)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    // MARK: - My Applications APIs

    func getMyStudyApplications() async -> Result<[MyApplicationModel], Error> {
        do {
            let response = try await groupService.getMyStudyApplications()
            let models = response.map { resp -> MyApplicationModel in
                // Handle deleted groups
                if resp.groupId == nil || resp.groupTitle == nil {
                    return MyApplicationModel(
                        id: resp.id,
                        groupId: 0,
                        groupTitle: "삭제된 그룹",
                        leaderName: nil,
                        status: "DELETED",
                        position: resp.position,
                        createdAt: resp.createdAt,
                        isGroupDeleted: true
                    )
                }
                return MyApplicationModel(
                    id: resp.id,
                    groupId: resp.groupId!,
                    groupTitle: resp.groupTitle!,
                    leaderName: resp.leaderName,
                    status: resp.status,
                    position: resp.position,
                    createdAt: resp.createdAt,
                    isGroupDeleted: false
                )
            }
            return .success(models)
        } catch {
            return .failure(error)
        }
    }

    func getMyTeamApplications() async -> Result<[MyApplicationModel], Error> {
        do {
            let response = try await groupService.getMyTeamApplications()
            let models = response.map { resp -> MyApplicationModel in
                // Handle deleted groups
                if resp.groupId == nil || resp.groupTitle == nil {
                    return MyApplicationModel(
                        id: resp.id,
                        groupId: 0,
                        groupTitle: "삭제된 그룹",
                        leaderName: nil,
                        status: "DELETED",
                        position: resp.position,
                        createdAt: resp.createdAt,
                        isGroupDeleted: true
                    )
                }
                return MyApplicationModel(
                    id: resp.id,
                    groupId: resp.groupId!,
                    groupTitle: resp.groupTitle!,
                    leaderName: resp.leaderName,
                    status: resp.status,
                    position: resp.position,
                    createdAt: resp.createdAt,
                    isGroupDeleted: false
                )
            }
            return .success(models)
        } catch {
            return .failure(error)
        }
    }

    func cancelStudyApplication(applicationId: Int) async -> Result<Void, Error> {
        do {
            try await groupService.cancelStudyApplication(applicationId: applicationId)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func cancelTeamApplication(applicationId: Int) async -> Result<Void, Error> {
        do {
            try await groupService.cancelTeamApplication(applicationId: applicationId)
            return .success(())
        } catch {
            return .failure(error)
        }
    }
}

extension GroupResponse {
    func toModel() -> GroupModel {
        return GroupModel(
            id: id,
            title: title,
            content: content,
            category: category,
            maxMembers: maxMembers,
            currentMembers: currentMembers,
            dDay: dDay,
            status: status,
            type: nil,
            leaderId: nil,
            startDate: nil,
            endDate: nil
        )
    }
}

final class FakeGroupRepository: GroupRepository {
    func getGroups(category: String, page: Int, size: Int) async -> Result<[GroupModel], Error> {
        return .success([])
    }

    func getGroup(id: Int) async -> Result<GroupModel, Error> {
        return .failure(NSError(domain: "Fake", code: 404, userInfo: nil))
    }

    func createGroup(title: String, content: String, category: String, maxMembers: Int) async -> Result<Void, Error> {
        return .success(())
    }

    func createStudy(info: GroupCreateInfo) async -> Result<GroupSummaryModel, Error> {
        return .success(GroupSummaryModel(id: 999, title: info.title, type: info.type, capacity: info.capacity, currentMembers: 1, status: "OPEN", leaderId: 1, startDate: info.startDate, endDate: info.endDate))
    }

    func createTeam(info: GroupCreateInfo) async -> Result<GroupSummaryModel, Error> {
        return .success(GroupSummaryModel(id: 1000, title: info.title, type: info.type, capacity: info.capacity, currentMembers: 1, status: "OPEN", leaderId: 1, startDate: info.startDate, endDate: info.endDate))
    }

    func applyGroup(groupId: Int, introduction: String) async -> Result<Void, Error> {
        return .success(())
    }

    func getStudies(campusId: Int?, type: String?) async -> Result<[GroupSummaryModel], Error> {
        return .success([])
    }

    func getMyStudies() async -> Result<[GroupSummaryModel], Error> {
        return .success([
            GroupSummaryModel(id: 1, title: "알고리즘 스터디", type: "ALGORITHM", capacity: 10, currentMembers: 5, status: "OPEN", leaderId: 1, startDate: "2024-01-01", endDate: "2024-12-31"),
            GroupSummaryModel(id: 2, title: "CS 면접 대비", type: "CS", capacity: 8, currentMembers: 3, status: "ONGOING", leaderId: 2, startDate: "2024-02-01", endDate: "2024-06-30")
        ])
    }

    func getStudyDetail(studyId: Int) async -> Result<GroupDetailModel, Error> {
        return .success(GroupDetailModel(
            id: studyId,
            title: "알고리즘 스터디",
            description: "매주 알고리즘 문제를 풀고 코드리뷰를 진행합니다.",
            type: "ALGORITHM",
            capacity: 10,
            currentMembers: 5,
            status: "OPEN",
            leaderId: 1,
            leaderName: "홍길동",
            leaderMattermostId: nil,
            leaderProfileImageUrl: nil,
            startDate: "2024-01-01",
            endDate: "2024-12-31"
        ))
    }

    func getStudyMembers(studyId: Int) async -> Result<[GroupMemberModel], Error> {
        return .success([])
    }

    func getTeams(campusId: Int?, type: String?) async -> Result<[GroupSummaryModel], Error> {
        return .success([])
    }

    func getMyTeams() async -> Result<[GroupSummaryModel], Error> {
        return .success([
            GroupSummaryModel(id: 3, title: "싸브리 프로젝트", type: "SSAFY", capacity: 6, currentMembers: 4, status: "ONGOING", leaderId: 1, startDate: "2024-01-15", endDate: "2024-05-30"),
            GroupSummaryModel(id: 4, title: "공모전 팀", type: "CONTEST", capacity: 5, currentMembers: 5, status: "OPEN", leaderId: 3, startDate: "2024-03-01", endDate: "2024-07-31")
        ])
    }

    func getTeamDetail(teamId: Int) async -> Result<GroupDetailModel, Error> {
        return .success(GroupDetailModel(
            id: teamId,
            title: "싸브리 프로젝트",
            description: "SSAFY 프로젝트를 함께 진행합니다.",
            type: "SSAFY",
            capacity: 6,
            currentMembers: 4,
            status: "ONGOING",
            leaderId: 1,
            leaderName: "홍길동",
            leaderMattermostId: nil,
            leaderProfileImageUrl: nil,
            startDate: "2024-01-15",
            endDate: "2024-05-30"
        ))
    }

    func getTeamMembers(teamId: Int) async -> Result<[GroupMemberModel], Error> {
        return .success([])
    }

    func applyStudy(studyId: Int, portfolioId: Int, title: String, message: String, position: String) async -> Result<Void, Error> {
        return .success(())
    }

    func applyTeam(teamId: Int, portfolioId: Int, title: String, message: String, position: String) async -> Result<Void, Error> {
        return .success(())
    }

    func deleteStudy(studyId: Int) async -> Result<Void, Error> {
        return .success(())
    }

    func deleteTeam(teamId: Int) async -> Result<Void, Error> {
        return .success(())
    }

    func getMyStudyApplications() async -> Result<[MyApplicationModel], Error> {
        return .success([])
    }

    func getMyTeamApplications() async -> Result<[MyApplicationModel], Error> {
        return .success([])
    }

    func cancelStudyApplication(applicationId: Int) async -> Result<Void, Error> {
        return .success(())
    }

    func cancelTeamApplication(applicationId: Int) async -> Result<Void, Error> {
        return .success(())
    }
}
