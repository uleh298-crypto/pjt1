import Foundation

final class HomeRepositoryImpl: HomeRepository {
    private let homeService: HomeService
    private let ddayRepository: DdayRepository

    init(homeService: HomeService, ddayRepository: DdayRepository) {
        self.homeService = homeService
        self.ddayRepository = ddayRepository
    }

    func fetchHome() async -> Result<HomeModel, Error> {
        do {
            var homeModel = try await homeService.getHome().toModel()

            // DdayRepository에서 모든 D-day 소스 (원격 + 로컬 + 월급날) 가져오기
            if case .success(let allDdays) = await ddayRepository.getAllDdays() {
                homeModel = HomeModel(
                    dDays: allDdays,
                    boards: homeModel.boards,
                    team: homeModel.team,
                    study: homeModel.study,
                    lunchMenu: homeModel.lunchMenu
                )
            }

            return .success(homeModel)
        } catch {
            return .failure(error)
        }
    }
}

extension HomeResponse {
    func toModel() -> HomeModel {
        HomeModel(
            dDays: dDays.map { $0.toModel() },
            boards: boardsList.map { $0.toModel() },
            team: teamThumbnail?.toTeamModel(),
            study: studyThumbnail?.toStudyModel(),
            lunchMenu: campusMeals.map { $0.toModel() }
        )
    }
}

extension HomeDDayResponse {
    func toModel() -> DDayModel {
        DDayModel(title: title, days: days)
    }
}

extension HomeBoardThumbResponse {
    func toModel() -> HomeBoardModel {
        HomeBoardModel(
            id: boardId,
            name: name,
            recentPostTitle: recentPostTitle
        )
    }
}

extension HomeRecruitThumbResponse {
    func toTeamModel() -> TeamRecruitModel {
        TeamRecruitModel(name: name, count: count)
    }

    func toStudyModel() -> StudyRecruitModel {
        StudyRecruitModel(name: name, count: count)
    }
}

extension HomeCampusMealResponse {
    func toModel() -> CampusMealModel {
        CampusMealModel(
            campusId: campusId,
            campusName: campusName,
            imageUrls: imageUrls
        )
    }
}

final class FakeHomeRepository: HomeRepository {
    func fetchHome() async -> Result<HomeModel, Error> {
        .success(HomeModel.sample)
    }
}
