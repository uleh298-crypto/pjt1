import Foundation

protocol CampusRepository {
    func getCampuses() async -> Result<[Campus], Error>
    func getClasses(campusId: Int) async -> Result<[Ban], Error>
}
