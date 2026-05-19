import SwiftUI

@Observable
final class ThemeManager {
    static let shared = ThemeManager()

    private let themeKey = "app_theme_mode"

    var themeMode: ThemeMode {
        didSet {
            UserDefaults.standard.set(themeMode.rawValue, forKey: themeKey)
        }
    }

    private init() {
        if let savedTheme = UserDefaults.standard.string(forKey: themeKey),
           let mode = ThemeMode(rawValue: savedTheme) {
            self.themeMode = mode
        } else {
            self.themeMode = .system
        }
    }

    func setTheme(_ mode: ThemeMode) {
        themeMode = mode
    }

    /// 실제로 다크 모드를 사용해야 하는지 결정
    func isDarkMode(systemColorScheme: ColorScheme) -> Bool {
        switch themeMode {
        case .dark:
            return true
        case .light:
            return false
        case .system:
            return systemColorScheme == .dark
        }
    }

    /// SwiftUI의 preferredColorScheme에 사용할 값
    var preferredColorScheme: ColorScheme? {
        switch themeMode {
        case .dark:
            return .dark
        case .light:
            return .light
        case .system:
            return nil // 시스템 설정 따름
        }
    }
}

// MARK: - Environment Key

private struct ThemeManagerKey: EnvironmentKey {
    static let defaultValue = ThemeManager.shared
}

extension EnvironmentValues {
    var themeManager: ThemeManager {
        get { self[ThemeManagerKey.self] }
        set { self[ThemeManagerKey.self] = newValue }
    }
}
