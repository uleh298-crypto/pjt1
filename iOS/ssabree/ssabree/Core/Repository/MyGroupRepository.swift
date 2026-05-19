import Foundation

protocol MyGroupRepository {
    func getMyGroups() async -> Result<[GroupModel], Error>
    func getApplications() async -> Result<[ApplicationModel], Error>
    func getAnnouncements(groupId: Int) async -> Result<[AnnouncementModel], Error>
    func createAnnouncement(groupId: Int, title: String, content: String) async -> Result<Void, Error>
    func updateAnnouncement(groupId: Int, announcementId: Int, title: String, content: String) async -> Result<Void, Error>
    func deleteAnnouncement(groupId: Int, announcementId: Int) async -> Result<Void, Error>
    func getMembers(groupId: Int) async -> Result<[MemberModel], Error>
    func approveMember(groupId: Int, memberId: Int) async -> Result<Void, Error>
    func rejectMember(groupId: Int, memberId: Int) async -> Result<Void, Error>
    func removeMember(groupId: Int, memberId: Int) async -> Result<Void, Error>
    func getProgress(groupId: Int) async -> Result<[ProgressModel], Error>
}

final class MyGroupRepositoryImpl: MyGroupRepository {
    private let groupService: GroupService
    
    init(groupService: GroupService) {
        self.groupService = groupService
    }
    
    func getMyGroups() async -> Result<[GroupModel], Error> {
        do {
            let responses = try await groupService.getMyGroups()
            let models = responses.map { $0.toModel() }
            return .success(models)
        } catch {
            return .failure(error)
        }
    }
    
    func getApplications() async -> Result<[ApplicationModel], Error> {
        do {
            let responses = try await groupService.getApplications()
            let models = responses.map { $0.toModel() }
            return .success(models)
        } catch {
            return .failure(error)
        }
    }
    
    func getAnnouncements(groupId: Int) async -> Result<[AnnouncementModel], Error> {
        do {
            let responses = try await groupService.getAnnouncements(groupId: groupId)
            let models = responses.map { $0.toModel() }
            return .success(models)
        } catch {
            return .failure(error)
        }
    }
    
    func createAnnouncement(groupId: Int, title: String, content: String) async -> Result<Void, Error> {
        do {
            let request = AnnouncementCreateRequest(title: title, content: content)
            try await groupService.createAnnouncement(groupId: groupId, request: request)
            return .success(())
        } catch {
            return .failure(error)
        }
    }
    
    func updateAnnouncement(groupId: Int, announcementId: Int, title: String, content: String) async -> Result<Void, Error> {
        do {
            let request = AnnouncementUpdateRequest(title: title, content: content)
            try await groupService.updateAnnouncement(groupId: groupId, announcementId: announcementId, request: request)
            return .success(())
        } catch {
            return .failure(error)
        }
    }
    
    func deleteAnnouncement(groupId: Int, announcementId: Int) async -> Result<Void, Error> {
        do {
            try await groupService.deleteAnnouncement(groupId: groupId, announcementId: announcementId)
            return .success(())
        } catch {
            return .failure(error)
        }
    }
    
    func getMembers(groupId: Int) async -> Result<[MemberModel], Error> {
        do {
            let responses = try await groupService.getMembers(groupId: groupId)
            let models = responses.map { $0.toModel() }
            return .success(models)
        } catch {
            return .failure(error)
        }
    }
    
    func approveMember(groupId: Int, memberId: Int) async -> Result<Void, Error> {
        do {
            try await groupService.approveMember(groupId: groupId, memberId: memberId)
            return .success(())
        } catch {
            return .failure(error)
        }
    }
    
    func rejectMember(groupId: Int, memberId: Int) async -> Result<Void, Error> {
        do {
            try await groupService.rejectMember(groupId: groupId, memberId: memberId)
            return .success(())
        } catch {
            return .failure(error)
        }
    }
    
    func removeMember(groupId: Int, memberId: Int) async -> Result<Void, Error> {
        do {
            try await groupService.removeMember(groupId: groupId, memberId: memberId)
            return .success(())
        } catch {
            return .failure(error)
        }
    }
    
    func getProgress(groupId: Int) async -> Result<[ProgressModel], Error> {
        do {
            let responses = try await groupService.getProgress(groupId: groupId)
            let models = responses.map { $0.toModel() }
            return .success(models)
        } catch {
            return .failure(error)
        }
    }
}

extension AnnouncementResponse {
    func toModel() -> AnnouncementModel {
        return AnnouncementModel(
            id: id,
            title: title,
            content: content,
            createdAt: createdAt
        )
    }
}

extension ApplicationResponse {
    func toModel() -> ApplicationModel {
        return ApplicationModel(
            id: id,
            groupId: groupId,
            groupTitle: groupTitle,
            status: status,
            appliedAt: appliedAt
        )
    }
}

extension MemberResponse {
    func toModel() -> MemberModel {
        return MemberModel(
            id: id,
            nickname: nickname,
            role: role
        )
    }
}

extension ProgressResponse {
    func toModel() -> ProgressModel {
        return ProgressModel(
            id: id,
            title: title,
            date: date,
            status: status
        )
    }
}

final class FakeMyGroupRepository: MyGroupRepository {
    func getMyGroups() async -> Result<[GroupModel], Error> {
        return .success([])
    }
    
    func getApplications() async -> Result<[ApplicationModel], Error> {
        return .success([])
    }
    
    func getAnnouncements(groupId: Int) async -> Result<[AnnouncementModel], Error> {
        return .success([])
    }
    
    func createAnnouncement(groupId: Int, title: String, content: String) async -> Result<Void, Error> {
        return .success(())
    }
    
    func updateAnnouncement(groupId: Int, announcementId: Int, title: String, content: String) async -> Result<Void, Error> {
        return .success(())
    }
    
    func deleteAnnouncement(groupId: Int, announcementId: Int) async -> Result<Void, Error> {
        return .success(())
    }
    
    func getMembers(groupId: Int) async -> Result<[MemberModel], Error> {
        return .success([])
    }
    
    func approveMember(groupId: Int, memberId: Int) async -> Result<Void, Error> {
        return .success(())
    }
    
    func rejectMember(groupId: Int, memberId: Int) async -> Result<Void, Error> {
        return .success(())
    }
    
    func removeMember(groupId: Int, memberId: Int) async -> Result<Void, Error> {
        return .success(())
    }
    
    func getProgress(groupId: Int) async -> Result<[ProgressModel], Error> {
        return .success([])
    }
}
