import SwiftUI
import UIKit

// MARK: - Color Definitions (Android와 동일한 색상값)

private enum LightColors {
    static let background = Color(red: 0xFB/255, green: 0xFB/255, blue: 0xFB/255)      // #FBFBFB
    static let surface = Color(red: 0xFF/255, green: 0xFF/255, blue: 0xFF/255)          // #FFFFFF
    static let onBackground = Color(red: 0x1F/255, green: 0x1B/255, blue: 0x17/255)     // #1F1B17
    static let onSurface = Color(red: 0x1F/255, green: 0x1B/255, blue: 0x17/255)        // #1F1B17
    static let field = Color(red: 0xF1/255, green: 0xEE/255, blue: 0xE9/255)            // #F1EEE9
    static let error = Color(red: 0xB3/255, green: 0x26/255, blue: 0x1E/255)            // #B3261E
}

private enum DarkColors {
    static let background = Color(red: 0x15/255, green: 0x12/255, blue: 0x14/255)       // #151214
    static let surface = Color(red: 0x1F/255, green: 0x1B/255, blue: 0x1D/255)          // #1F1B1D
    static let onBackground = Color(red: 0xE9/255, green: 0xE3/255, blue: 0xDC/255)     // #E9E3DC
    static let onSurface = Color(red: 0xE9/255, green: 0xE3/255, blue: 0xDC/255)        // #E9E3DC
    static let field = Color(red: 0x2A/255, green: 0x26/255, blue: 0x2B/255)            // #2A262B
    static let error = Color(red: 0xF2/255, green: 0xB8/255, blue: 0xB5/255)            // #F2B8B5
}

// MARK: - App Colors (Dynamic - Light/Dark 자동 전환)

enum AppColors {
    // Primary colors (테마에 관계없이 동일)
    static let primary = Color(red: 0x64/255, green: 0x95/255, blue: 0xEB/255)          // #6495EB
    static let loginButton = primary
    static let loginButtonDisabled = primary.opacity(0.35)
    static let onPrimary = Color.white

    // Dynamic colors (Light/Dark 자동 전환)
    static var background: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(DarkColors.background)
                : UIColor(LightColors.background)
        })
    }

    static var surface: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(DarkColors.surface)
                : UIColor(LightColors.surface)
        })
    }

    static var surfaceVariant: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(DarkColors.field)
                : UIColor(LightColors.field)
        })
    }

    static var onBackground: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(DarkColors.onBackground)
                : UIColor(LightColors.onBackground)
        })
    }

    static var onSurface: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(DarkColors.onSurface)
                : UIColor(LightColors.onSurface)
        })
    }

    static var primaryContainer: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(DarkColors.field)
                : UIColor(LightColors.field)
        })
    }

    static var error: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(DarkColors.error)
                : UIColor(LightColors.error)
        })
    }

    static var placeholder: Color {
        onSurface.opacity(0.6)
    }

    static var onSurfaceVariant: Color {
        onSurface.opacity(0.6)
    }

    static var outlineVariant: Color {
        surfaceVariant
    }

    static var surfaceElevated: Color {
        surface
    }
}

// MARK: - UIColor Extension for SwiftUI Color

private extension UIColor {
    convenience init(_ color: Color) {
        let components = color.cgColor?.components ?? [0, 0, 0, 1]
        let red = components.count > 0 ? components[0] : 0
        let green = components.count > 1 ? components[1] : 0
        let blue = components.count > 2 ? components[2] : 0
        let alpha = components.count > 3 ? components[3] : 1
        self.init(red: red, green: green, blue: blue, alpha: alpha)
    }
}
