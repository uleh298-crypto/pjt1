import Foundation

protocol MyPageRepository {
    func getMyPage() async -> Result<MyPageModel, Error>
    func getMyPosts() async -> Result<[PostModel], Error>
    func getMyComments() async -> Result<[MyCommentModel], Error>
    func getMyScraps() async -> Result<[PostModel], Error>
    func updateProfileImage(_ imageUrl: String) async -> Result<Void, Error>
    func getAnon() async -> Result<AnonModel, Error>
}
