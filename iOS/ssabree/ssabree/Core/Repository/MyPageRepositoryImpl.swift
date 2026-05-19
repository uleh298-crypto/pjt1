import Foundation

final class MyPageRepositoryImpl: MyPageRepository {
    private let myPageService: MyPageService

    init(myPageService: MyPageService) {
        self.myPageService = myPageService
    }

    func getMyPage() async -> Result<MyPageModel, Error> {
        do {
            let response = try await myPageService.getMyPage()
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }

    func getMyPosts() async -> Result<[PostModel], Error> {
        do {
            let response = try await myPageService.getMyPosts()
            let models = response.map { $0.toModel() }
            return .success(models)
        } catch {
            return .failure(error)
        }
    }

    func getMyComments() async -> Result<[MyCommentModel], Error> {
        do {
            let response = try await myPageService.getMyComments()
            let models = response.map { $0.toModel() }
            return .success(models)
        } catch {
            return .failure(error)
        }
    }

    func getMyScraps() async -> Result<[PostModel], Error> {
        do {
            let response = try await myPageService.getMyScraps()
            let models = response.map { $0.toModel() }
            return .success(models)
        } catch {
            return .failure(error)
        }
    }

    func updateProfileImage(_ imageUrl: String) async -> Result<Void, Error> {
        do {
            _ = try await myPageService.updateProfile(request: UpdateProfileRequest(profileImageUrl: imageUrl))
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    func getAnon() async -> Result<AnonModel, Error> {
        do {
            let response = try await myPageService.getAnon()
            return .success(response.toModel())
        } catch {
            return .failure(error)
        }
    }
}

extension AnonResponse {
    func toModel() -> AnonModel {
        AnonModel(name: name, isAuthor: isAuthor, isMine: isMine)
    }
}
