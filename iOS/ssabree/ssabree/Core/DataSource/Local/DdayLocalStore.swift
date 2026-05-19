import Foundation

// MARK: - Local D-Day Item Model

struct LocalDdayItem: Codable, Identifiable, Equatable {
    let id: Int
    let title: String
    let date: String  // "yyyy.MM.dd" 형식
    var showOnHome: Bool
    let iconKey: String?

    init(id: Int, title: String, date: String, showOnHome: Bool = true, iconKey: String? = nil) {
        self.id = id
        self.title = title
        self.date = date
        self.showOnHome = showOnHome
        self.iconKey = iconKey
    }
}

// MARK: - D-Day Local Store

final class DdayLocalStore {
    private let defaults: UserDefaults
    private let key = "dday_items"

    static let shared = DdayLocalStore()

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    func load() -> [LocalDdayItem] {
        guard let data = defaults.data(forKey: key) else {
            return []
        }

        do {
            return try JSONDecoder().decode([LocalDdayItem].self, from: data)
        } catch {
            #if DEBUG
            print("[DdayLocalStore] Failed to decode items: \(error)")
            #endif
            return []
        }
    }

    func save(_ items: [LocalDdayItem]) {
        do {
            let data = try JSONEncoder().encode(items)
            defaults.set(data, forKey: key)
        } catch {
            #if DEBUG
            print("[DdayLocalStore] Failed to encode items: \(error)")
            #endif
        }
    }

    func add(_ item: LocalDdayItem) {
        var items = load()
        items.append(item)
        save(items)
    }

    func update(_ item: LocalDdayItem) {
        var items = load()
        if let index = items.firstIndex(where: { $0.id == item.id }) {
            items[index] = item
            save(items)
        }
    }

    func delete(id: Int) {
        var items = load()
        items.removeAll { $0.id == id }
        save(items)
    }

    func getNextId() -> Int {
        let items = load()
        let maxId = items.map { $0.id }.max() ?? 0
        return maxId + 1
    }

    func clear() {
        defaults.removeObject(forKey: key)
    }
}

// MARK: - D-Day Calculation Utilities

extension DdayLocalStore {
    /// 로컬 날짜 문자열("yyyy.MM.dd")로부터 D-Day 일수 계산
    static func calculateDays(from dateString: String) -> Int {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy.MM.dd"
        formatter.locale = Locale(identifier: "ko_KR")

        guard let targetDate = formatter.date(from: dateString) else {
            return Int.max
        }

        let calendar = Calendar.current
        let today = calendar.startOfDay(for: Date())
        let target = calendar.startOfDay(for: targetDate)

        let components = calendar.dateComponents([.day], from: today, to: target)
        return components.day ?? Int.max
    }

    /// API 날짜 문자열("yyyy-MM-dd")로부터 D-Day 일수 계산
    static func calculateDaysFromApiDate(_ dateString: String) -> Int {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        formatter.locale = Locale(identifier: "ko_KR")

        guard let targetDate = formatter.date(from: dateString) else {
            return Int.max
        }

        let calendar = Calendar.current
        let today = calendar.startOfDay(for: Date())
        let target = calendar.startOfDay(for: targetDate)

        let components = calendar.dateComponents([.day], from: today, to: target)
        return components.day ?? Int.max
    }

    /// D-Day 표시 문자열 생성 ("D-10", "D-DAY", "D+5")
    static func formatDdayLabel(days: Int) -> String {
        if days == 0 {
            return "D-DAY"
        } else if days > 0 {
            return "D-\(days)"
        } else {
            return "D+\(abs(days))"
        }
    }
}

// MARK: - Salary Day Calculation

extension DdayLocalStore {
    /// 다음 월급날까지 남은 일수 계산
    static func getNextSalaryDday() -> Int {
        let calendar = Calendar.current
        let today = Date()
        let components = calendar.dateComponents([.year, .month, .day], from: today)

        guard let year = components.year,
              let month = components.month,
              let dayOfMonth = components.day else {
            return 0
        }

        let salaryDayThisMonth = getSalaryDay(year: year, month: month)

        if dayOfMonth <= salaryDayThisMonth {
            // 이번 달 월급날이 아직 안 지남
            return salaryDayThisMonth - dayOfMonth
        } else {
            // 다음 달 월급날 계산
            let nextMonth = month == 12 ? 1 : month + 1
            let nextYear = month == 12 ? year + 1 : year
            let salaryDayNextMonth = getSalaryDay(year: nextYear, month: nextMonth)

            // 다음 달 월급날 날짜 생성
            var nextSalaryComponents = DateComponents()
            nextSalaryComponents.year = nextYear
            nextSalaryComponents.month = nextMonth
            nextSalaryComponents.day = salaryDayNextMonth

            guard let nextSalaryDate = calendar.date(from: nextSalaryComponents) else {
                return 0
            }

            let todayStart = calendar.startOfDay(for: today)
            let nextSalaryStart = calendar.startOfDay(for: nextSalaryDate)

            let diff = calendar.dateComponents([.day], from: todayStart, to: nextSalaryStart)
            return diff.day ?? 0
        }
    }

    /// 특정 년/월의 월급날 계산 (15일 기준, 주말/공휴일이면 다음 평일)
    static func getSalaryDay(year: Int, month: Int) -> Int {
        let calendar = Calendar.current

        var components = DateComponents()
        components.year = year
        components.month = month
        components.day = 15

        guard var date = calendar.date(from: components) else {
            return 15
        }

        // 주말이나 공휴일이면 다음 날로
        while isWeekendOrHoliday(date) {
            guard let nextDay = calendar.date(byAdding: .day, value: 1, to: date) else {
                break
            }
            date = nextDay
        }

        return calendar.component(.day, from: date)
    }

    /// 주말 또는 공휴일인지 확인
    static func isWeekendOrHoliday(_ date: Date) -> Bool {
        let calendar = Calendar.current
        let weekday = calendar.component(.weekday, from: date)

        // 주말 체크 (1: 일요일, 7: 토요일)
        if weekday == 1 || weekday == 7 {
            return true
        }

        let year = calendar.component(.year, from: date)
        let month = calendar.component(.month, from: date)
        let day = calendar.component(.day, from: date)

        // 고정 공휴일 (매년 동일)
        let fixedHolidays: [(month: Int, day: Int)] = [
            (1, 1),   // 신정
            (3, 1),   // 삼일절
            (5, 5),   // 어린이날
            (6, 6),   // 현충일
            (8, 15),  // 광복절
            (10, 3),  // 개천절
            (10, 9),  // 한글날
            (12, 25)  // 크리스마스
        ]

        if fixedHolidays.contains(where: { $0.month == month && $0.day == day }) {
            return true
        }

        // 변동 공휴일 (음력 기반 + 대체공휴일, 2025~2030)
        if let yearHolidays = variableHolidays[year] {
            return yearHolidays.contains(where: { $0.month == month && $0.day == day })
        }

        return false
    }

    /// 연도별 변동 공휴일 (음력 기반 설날/추석/부처님오신날 + 대체공휴일)
    private static let variableHolidays: [Int: [(month: Int, day: Int)]] = [
        2025: [
            // 설날 연휴: 1/27(월)~1/30(목) - 1/27 대체공휴일 포함
            (1, 27), (1, 28), (1, 29), (1, 30),
            // 삼일절 대체공휴일: 3/3(월) - 3/1이 토요일
            (3, 3),
            // 부처님오신날: 5/5(월) - 어린이날과 겹침
            // 어린이날 대체공휴일: 5/6(화)
            (5, 6),
            // 추석 연휴: 10/5(일)~10/7(화) + 대체공휴일 10/8(수)
            (10, 5), (10, 6), (10, 7), (10, 8),
        ],
        2026: [
            // 설날 연휴: 2/16(월)~2/18(수)
            (2, 16), (2, 17), (2, 18),
            // 부처님오신날: 5/24(일) + 대체공휴일 5/25(월)
            (5, 24), (5, 25),
            // 추석 연휴: 9/24(목)~9/26(토)
            (9, 24), (9, 25), (9, 26),
        ],
        2027: [
            // 설날 연휴: 2/6(토)~2/8(월) + 대체공휴일 2/9(화)
            (2, 6), (2, 7), (2, 8), (2, 9),
            // 부처님오신날: 5/13(목)
            (5, 13),
            // 추석 연휴: 9/14(화)~9/16(목)
            (9, 14), (9, 15), (9, 16),
            // 한글날 대체공휴일: 10/11(월) - 10/9가 토요일
            (10, 11),
        ],
        2028: [
            // 설날 연휴: 1/25(화)~1/27(목)
            (1, 25), (1, 26), (1, 27),
            // 부처님오신날: 5/2(화)
            (5, 2),
            // 추석 연휴: 10/2(월)~10/4(수) - 10/3 개천절과 겹침
            (10, 2), (10, 3), (10, 4),
            // 추석-개천절 겹침 대체공휴일: 10/5(목)
            (10, 5),
        ],
        2029: [
            // 설날 연휴: 2/12(월)~2/14(수)
            (2, 12), (2, 13), (2, 14),
            // 부처님오신날: 5/20(일) + 대체공휴일 5/21(월)
            (5, 20), (5, 21),
            // 추석 연휴: 9/21(금)~9/23(일) + 대체공휴일 9/24(월)
            (9, 21), (9, 22), (9, 23), (9, 24),
        ],
        2030: [
            // 설날 연휴: 2/2(토)~2/4(월) + 대체공휴일 2/5(화)
            (2, 2), (2, 3), (2, 4), (2, 5),
            // 부처님오신날: 5/9(목)
            (5, 9),
            // 추석 연휴: 9/11(수)~9/13(금)
            (9, 11), (9, 12), (9, 13),
        ],
    ]

    /// 특정 날짜가 월급날인지 확인
    static func isSalaryDay(year: Int, month: Int, day: Int) -> Bool {
        return getSalaryDay(year: year, month: month) == day
    }
}
