import Foundation

// MARK: - D-Day Item Model (for DdayDetailView)

struct DdayItemModel: Identifiable, Equatable {
    let id: Int
    let title: String
    let targetDate: String  // "yyyy-MM-dd" API 형식
    let dDay: Int           // 남은 일수
    let iconKey: String?

    /// D-Day 표시 문자열 ("D-10", "D-DAY", "D+5")
    var dDayLabel: String {
        DdayLocalStore.formatDdayLabel(days: dDay)
    }

    /// 화면 표시용 날짜 문자열 ("yyyy.MM.dd")
    var displayDate: String {
        // "yyyy-MM-dd" → "yyyy.MM.dd"
        targetDate.replacingOccurrences(of: "-", with: ".")
    }
}

// MARK: - DdayResponse Extension

extension DdayResponse {
    func toItemModel() -> DdayItemModel {
        DdayItemModel(
            id: id,
            title: title,
            targetDate: targetDate,
            dDay: DdayLocalStore.calculateDaysFromApiDate(targetDate),
            iconKey: iconKey
        )
    }
}

// MARK: - LocalDdayItem to DdayItemModel

extension LocalDdayItem {
    func toItemModel() -> DdayItemModel {
        // "yyyy.MM.dd" → "yyyy-MM-dd"
        let apiDate = date.replacingOccurrences(of: ".", with: "-")
        return DdayItemModel(
            id: id,
            title: title,
            targetDate: apiDate,
            dDay: DdayLocalStore.calculateDays(from: date),
            iconKey: iconKey
        )
    }
}
