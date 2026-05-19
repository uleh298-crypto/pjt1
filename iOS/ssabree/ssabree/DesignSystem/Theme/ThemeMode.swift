import SwiftUI

enum ThemeMode: String, CaseIterable {
    case dark = "dark"
    case light = "light"
    case system = "system"

    var displayName: String {
        switch self {
        case .dark: return "다크 모드"
        case .light: return "라이트 모드"
        case .system: return "시스템 모드"
        }
    }

    static func fromDisplayName(_ name: String) -> ThemeMode {
        return ThemeMode.allCases.first { $0.displayName == name } ?? .system
    }
}
