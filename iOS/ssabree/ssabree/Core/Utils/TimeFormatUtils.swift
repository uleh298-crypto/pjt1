import Foundation

// MARK: - Time Format Utilities
/// Android TimeFormatUtils.kt 대응

private let seoulTimeZone = TimeZone(identifier: "Asia/Seoul")!

private let dateOutputFormatter: DateFormatter = {
    let formatter = DateFormatter()
    formatter.dateFormat = "yyyy.MM.dd"
    formatter.timeZone = seoulTimeZone
    return formatter
}()

private let dateTimeOutputFormatter: DateFormatter = {
    let formatter = DateFormatter()
    formatter.dateFormat = "yyyy.MM.dd HH:mm"
    formatter.timeZone = seoulTimeZone
    return formatter
}()

private let dateTimeNoYearFormatter: DateFormatter = {
    let formatter = DateFormatter()
    formatter.dateFormat = "MM.dd HH:mm"
    formatter.timeZone = seoulTimeZone
    return formatter
}()

private let timeOnlyFormatter: DateFormatter = {
    let formatter = DateFormatter()
    formatter.dateFormat = "HH:mm"
    formatter.timeZone = seoulTimeZone
    return formatter
}()

// MARK: - Parse ISO8601 String to Date

/// ISO8601 문자열을 Date로 파싱 (Offset/Timezone 없으면 KST로 간주)
private func parseToDate(_ dateTimeString: String) -> Date? {
    let formatters: [DateFormatter] = {
        // ISO8601 with timezone
        let withTimezone = DateFormatter()
        withTimezone.dateFormat = "yyyy-MM-dd'T'HH:mm:ssXXXXX"
        withTimezone.locale = Locale(identifier: "en_US_POSIX")

        // ISO8601 with Z (서버가 KST이므로 KST로 간주)
        let withZ = DateFormatter()
        withZ.dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        withZ.timeZone = seoulTimeZone
        withZ.locale = Locale(identifier: "en_US_POSIX")

        // ISO8601 with milliseconds and Z (서버가 KST이므로 KST로 간주)
        let withMillisZ = DateFormatter()
        withMillisZ.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        withMillisZ.timeZone = seoulTimeZone
        withMillisZ.locale = Locale(identifier: "en_US_POSIX")

        // ISO8601 without timezone (서버가 KST로 보내므로 KST로 간주)
        let noTimezone = DateFormatter()
        noTimezone.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
        noTimezone.timeZone = seoulTimeZone
        noTimezone.locale = Locale(identifier: "en_US_POSIX")

        // ISO8601 with milliseconds without timezone (KST로 간주)
        let withMillis = DateFormatter()
        withMillis.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS"
        withMillis.timeZone = seoulTimeZone
        withMillis.locale = Locale(identifier: "en_US_POSIX")

        return [withTimezone, withZ, withMillisZ, noTimezone, withMillis]
    }()

    for formatter in formatters {
        if let date = formatter.date(from: dateTimeString) {
            return date
        }
    }
    return nil
}

// MARK: - Format Relative Time

/// 주어진 날짜 문자열을 KST 기준 상대시간 문자열로 변환
///
/// 규칙:
/// - 1분 미만: "지금"
/// - 1시간 미만: "N분 전"
/// - 1일 미만: "N시간 전"
/// - 7일 미만: "N일 전"
/// - 7일 이상: "yyyy.MM.dd"
///
/// 입력 문자열이 Offset/Timezone 정보가 없으면 KST 기준으로 간주
func formatRelativeTime(_ dateTimeString: String?) -> String {
    guard let dateTimeString = dateTimeString, !dateTimeString.isEmpty else {
        return ""
    }

    guard let targetDate = parseToDate(dateTimeString) else {
        return dateTimeString
    }

    let now = Date()
    let diff = now.timeIntervalSince(targetDate)

    // 미래 시간이거나 1분 미만
    if diff < 0 || diff < 60 {
        return "지금"
    }

    let minutes = Int(diff / 60)
    if minutes < 60 {
        return "\(minutes)분 전"
    }

    let hours = Int(diff / 3600)
    if hours < 24 {
        return "\(hours)시간 전"
    }

    let days = Int(diff / 86400)
    if days < 7 {
        return "\(days)일 전"
    }

    return dateOutputFormatter.string(from: targetDate)
}

// MARK: - Format Absolute KST

/// 절대 시각 표기 (기본: yyyy.MM.dd HH:mm, KST 기준)
/// 입력에 Offset/Timezone 없으면 KST로 간주
func formatAbsoluteKst(_ dateTimeString: String?, pattern: String = "yyyy.MM.dd HH:mm") -> String {
    guard let dateTimeString = dateTimeString, !dateTimeString.isEmpty else {
        return ""
    }

    guard let date = parseToDate(dateTimeString) else {
        return dateTimeString
    }

    if pattern == "yyyy.MM.dd HH:mm" {
        return dateTimeOutputFormatter.string(from: date)
    }

    let formatter = DateFormatter()
    formatter.dateFormat = pattern
    formatter.timeZone = seoulTimeZone
    return formatter.string(from: date)
}

// MARK: - Format Adaptive KST

/// 올해이면 연도 생략(MM.dd HH:mm), 그 외에는 yyyy.MM.dd HH:mm으로 포맷
func formatAdaptiveKst(_ dateTimeString: String?) -> String {
    guard let dateTimeString = dateTimeString, !dateTimeString.isEmpty else {
        return ""
    }

    guard let date = parseToDate(dateTimeString) else {
        return dateTimeString
    }

    let calendar = Calendar.current
    let currentYear = calendar.component(.year, from: Date())

    // Convert date to Seoul timezone for year comparison
    var seoulCalendar = Calendar.current
    seoulCalendar.timeZone = seoulTimeZone
    let dateYear = seoulCalendar.component(.year, from: date)

    if dateYear == currentYear {
        return dateTimeNoYearFormatter.string(from: date)
    } else {
        return dateTimeOutputFormatter.string(from: date)
    }
}

// MARK: - Format Chat Time

/// 채팅 전용 시각 포맷:
/// - 오늘: HH:mm
/// - 올해: MM.dd HH:mm
/// - 그 외: yyyy.MM.dd HH:mm
func formatChatTime(_ dateTimeString: String?) -> String {
    guard let dateTimeString = dateTimeString, !dateTimeString.isEmpty else {
        return ""
    }

    guard let date = parseToDate(dateTimeString) else {
        return dateTimeString
    }

    var seoulCalendar = Calendar.current
    seoulCalendar.timeZone = seoulTimeZone

    let now = Date()
    let currentYear = seoulCalendar.component(.year, from: now)
    let dateYear = seoulCalendar.component(.year, from: date)

    // 오늘인지 확인
    if seoulCalendar.isDateInToday(date) {
        return timeOnlyFormatter.string(from: date)
    }

    // 올해인지 확인
    if dateYear == currentYear {
        return dateTimeNoYearFormatter.string(from: date)
    }

    // 그 외
    return dateTimeOutputFormatter.string(from: date)
}

// MARK: - String Extensions

extension String {
    /// Post/Comment 등에서 바로 쓸 수 있는 확장 함수 (상대 시간)
    func toRelativeTimeText() -> String {
        formatRelativeTime(self)
    }

    /// 절대 시각 표기 (KST)
    func toAbsoluteKstText(pattern: String = "yyyy.MM.dd HH:mm") -> String {
        formatAbsoluteKst(self, pattern: pattern)
    }

    /// 올해면 연도 생략, 그 외 전체 표시 (KST)
    func toAdaptiveKstText() -> String {
        formatAdaptiveKst(self)
    }

    /// 채팅 전용 시각 표기 (오늘: HH:mm, 올해: MM.dd HH:mm, 그 외: yyyy.MM.dd HH:mm)
    func toChatTimeText() -> String {
        formatChatTime(self)
    }
}

extension Optional where Wrapped == String {
    /// Post/Comment 등에서 바로 쓸 수 있는 확장 함수 (상대 시간)
    func toRelativeTimeText() -> String {
        formatRelativeTime(self)
    }

    /// 절대 시각 표기 (KST)
    func toAbsoluteKstText(pattern: String = "yyyy.MM.dd HH:mm") -> String {
        formatAbsoluteKst(self, pattern: pattern)
    }

    /// 올해면 연도 생략, 그 외 전체 표시 (KST)
    func toAdaptiveKstText() -> String {
        formatAdaptiveKst(self)
    }

    /// 채팅 전용 시각 표기 (오늘: HH:mm, 올해: MM.dd HH:mm, 그 외: yyyy.MM.dd HH:mm)
    func toChatTimeText() -> String {
        formatChatTime(self)
    }
}
