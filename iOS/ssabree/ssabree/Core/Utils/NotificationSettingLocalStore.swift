import Foundation

/// 알림 설정 로컬 저장소
/// 주기적 알림 (입실/퇴실, 점심 확인) 설정을 UserDefaults에 저장
final class NotificationSettingLocalStore {
    private let defaults = UserDefaults.standard

    private enum Keys {
        static let scheduledNotificationEnabled = "scheduled_notification_enabled"
    }

    static let shared = NotificationSettingLocalStore()

    private init() {}

    /// 주기적 알림 활성화 여부 (기본값: true)
    func isScheduledNotificationEnabled() -> Bool {
        // 저장된 값이 없으면 기본값 true
        if defaults.object(forKey: Keys.scheduledNotificationEnabled) == nil {
            return true
        }
        return defaults.bool(forKey: Keys.scheduledNotificationEnabled)
    }

    /// 주기적 알림 설정 저장
    func saveScheduledNotificationEnabled(_ enabled: Bool) {
        defaults.set(enabled, forKey: Keys.scheduledNotificationEnabled)
    }
}
