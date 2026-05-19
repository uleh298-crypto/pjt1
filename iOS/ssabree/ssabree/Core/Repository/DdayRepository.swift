import Foundation

// MARK: - DdayRepository Protocol

protocol DdayRepository {
    /// 원격 D-Day만 조회 (DdayDetailScreen에서 사용)
    func fetchDdays() async -> Result<[DdayItemModel], Error>

    /// 모든 D-Day 통합 조회 (원격 + 로컬 + 월급날) - HomeScreen에서 사용
    func getAllDdays() async -> Result<[DDayModel], Error>
}

// MARK: - DdayRepositoryImpl

final class DdayRepositoryImpl: DdayRepository {
    private let ddayService: DdayService
    private let localStore: DdayLocalStore

    init(ddayService: DdayService, localStore: DdayLocalStore = .shared) {
        self.ddayService = ddayService
        self.localStore = localStore
    }

    func fetchDdays() async -> Result<[DdayItemModel], Error> {
        do {
            let response = try await ddayService.getDdays()
            let items = response.items.map { $0.toItemModel() }
            #if DEBUG
            print("[DdayRepository] fetchDdays success: count=\(items.count)")
            #endif
            return .success(items)
        } catch {
            #if DEBUG
            print("[DdayRepository] fetchDdays failed: \(error)")
            #endif
            return .failure(error)
        }
    }

    func getAllDdays() async -> Result<[DDayModel], Error> {
        var allDdays: [DDayModel] = []

        // 1. 원격 D-Day
        if case .success(let remoteDdays) = await fetchDdays() {
            for item in remoteDdays {
                allDdays.append(DDayModel(title: item.title, days: item.dDay))
            }
        }

        // 2. 로컬 D-Day (showOnHome=true인 것만)
        let localItems = localStore.load()
        for item in localItems where item.showOnHome {
            let days = DdayLocalStore.calculateDays(from: item.date)
            // 아직 지나지 않은 D-day만 (음수면 이미 지남)
            if days >= 0 {
                allDdays.append(DDayModel(title: item.title, days: days))
            }
        }

        // 3. 월급날
        let salaryDday = DdayLocalStore.getNextSalaryDday()
        allDdays.append(DDayModel(title: "월급날", days: salaryDday))

        // D-day가 가까운 순으로 정렬
        allDdays.sort { $0.days < $1.days }

        #if DEBUG
        print("[DdayRepository] getAllDdays: count=\(allDdays.count)")
        for dday in allDdays {
            print("  - \(dday.title): D-\(dday.days)")
        }
        #endif

        return .success(allDdays)
    }
}

// MARK: - FakeDdayRepository

final class FakeDdayRepository: DdayRepository {
    func fetchDdays() async -> Result<[DdayItemModel], Error> {
        .success([
            DdayItemModel(id: 1, title: "SSAFY 수료", targetDate: "2025-06-01", dDay: 30, iconKey: nil),
            DdayItemModel(id: 2, title: "프로젝트 마감", targetDate: "2025-05-10", dDay: 8, iconKey: nil)
        ])
    }

    func getAllDdays() async -> Result<[DDayModel], Error> {
        .success([
            DDayModel(title: "SSAFY 수료", days: 30),
            DDayModel(title: "프로젝트 마감", days: 8),
            DDayModel(title: "월급날", days: 5)
        ])
    }
}
