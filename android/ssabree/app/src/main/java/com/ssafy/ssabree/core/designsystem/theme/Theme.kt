package com.ssafy.ssabree.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext

enum class ThemeMode(val displayName: String) {
    DARK("다크 모드"),
    LIGHT("라이트 모드"),
    SYSTEM("시스템 모드");

    companion object {
        fun fromDisplayName(name: String): ThemeMode {
            return entries.find { it.displayName == name } ?: SYSTEM
        }
    }
}

val LocalThemeMode = compositionLocalOf { ThemeMode.SYSTEM }
val LocalOnThemeModeChange = compositionLocalOf<(ThemeMode) -> Unit> { {} }

private val DarkColorScheme = darkColorScheme(
    primary = LoginButton,
    secondary = DarkOnSurfaceColor,
    tertiary = DarkOnSurfaceColor,
    primaryContainer = DarkFieldColor,
    background = DarkBackgroundColor,
    onBackground = DarkOnBackgroundColor,
    surface = DarkSurfaceColor,
    onSurface = DarkOnSurfaceColor,
    onPrimary = LightSurfaceColor,
    error = ErrorDark
)

    private val LightColorScheme = lightColorScheme(
    primary = LoginButton,
    secondary = LightOnSurfaceColor,
    tertiary = LightOnSurfaceColor,
    primaryContainer = LightFieldColor,
    background = LightBackgroundColor,
    onBackground = LightOnBackgroundColor,
    surface = LightSurfaceColor,
    onSurface = LightOnSurfaceColor,
    onPrimary = LightSurfaceColor,
    error = ErrorLight

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun SsabreeTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemDark
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalThemeMode provides themeMode,
        LocalOnThemeModeChange provides onThemeModeChange
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
