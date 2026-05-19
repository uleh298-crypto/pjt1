import Foundation

final class CampusRepositoryImpl: CampusRepository {
    private let campusService: CampusService

    init(campusService: CampusService) {
        self.campusService = campusService
    }

    func getCampuses() async -> Result<[Campus], Error> {
        do {
            let response = try await campusService.getCampuses()
            return .success(response)
        } catch {
            return .failure(error)
        }
    }

    func getClasses(campusId: Int) async -> Result<[Ban], Error> {
        do {
            let response = try await campusService.getClasses(campusId: campusId)
            return .success(response)
        } catch {
            return .failure(error)
        }
    }
}
