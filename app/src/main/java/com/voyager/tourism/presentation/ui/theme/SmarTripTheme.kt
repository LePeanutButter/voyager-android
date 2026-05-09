package com.voyager.tourism.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Colores de marca y superficies alineados con `voyager-web-client/src/index.css`
 * (`:root` y `[data-theme="dark"]`).
 */
object SmarTripColors {
    val BrandRed = Color(0xFFDE1113)
    val BrandCyan = Color(0xFF19B5E9)
    val BrandBlue = Color(0xFF0D83D0)
    val BrandBlueDark = Color(0xFF04459C)
    val BrandInk = Color(0xFF0A0F29)
    val Gray50 = Color(0xFFF7F9FC)
    val Gray100 = Color(0xFFEFF3F8)
    val Gray200 = Color(0xFFDFE7F2)
    val Gray500 = Color(0xFF8A93A5)
    val Gray600 = Color(0xFF5B6475)
    val Success = Color(0xFF0EA56F)
    val Danger = Color(0xFFEF4444)
    val SurfaceDark = Color(0xFF0A0F29)
    val SurfaceCardDark = Color(0xFF111A3A)
    val TextSecondaryDark = Color(0xFFCCD5E8)
    val TextMutedDark = Color(0xFF9AA7C2)
    val OutlineDark = Color(0xFF243156)

    val GradientPrimaryStart = BrandBlueDark
    val GradientPrimaryEnd = BrandBlue
}

private val LightScheme = lightColorScheme(
    primary = SmarTripColors.BrandBlueDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDFF3FF),
    onPrimaryContainer = SmarTripColors.BrandInk,
    secondary = SmarTripColors.Gray600,
    onSecondary = Color.White,
    secondaryContainer = SmarTripColors.Gray100,
    onSecondaryContainer = SmarTripColors.BrandInk,
    tertiary = SmarTripColors.BrandCyan,
    onTertiary = SmarTripColors.BrandInk,
    error = SmarTripColors.Danger,
    onError = Color.White,
    background = Color.White,
    onBackground = SmarTripColors.BrandInk,
    surface = Color.White,
    onSurface = SmarTripColors.BrandInk,
    surfaceVariant = SmarTripColors.Gray50,
    onSurfaceVariant = SmarTripColors.Gray600,
    outline = SmarTripColors.Gray200,
    outlineVariant = SmarTripColors.Gray200,
)

private val DarkScheme = darkColorScheme(
    primary = SmarTripColors.BrandCyan,
    onPrimary = SmarTripColors.BrandInk,
    primaryContainer = Color(0xFF0D3D7A),
    onPrimaryContainer = Color(0xFFE8F4FF),
    secondary = SmarTripColors.TextSecondaryDark,
    onSecondary = SmarTripColors.BrandInk,
    secondaryContainer = SmarTripColors.SurfaceCardDark,
    onSecondaryContainer = Color.White,
    tertiary = SmarTripColors.BrandBlue,
    onTertiary = Color.White,
    error = SmarTripColors.Danger,
    onError = Color.White,
    background = SmarTripColors.SurfaceDark,
    onBackground = Color.White,
    surface = SmarTripColors.SurfaceCardDark,
    onSurface = Color.White,
    surfaceVariant = SmarTripColors.OutlineDark,
    onSurfaceVariant = SmarTripColors.TextMutedDark,
    outline = SmarTripColors.OutlineDark,
    outlineVariant = SmarTripColors.OutlineDark,
)

/** Tema visual actual de la app (ajuste usuario + [SmarTripTheme]); para [SmarTripLogo] en `MatchTheme`. */
val LocalSmarTripDarkTheme = compositionLocalOf { false }

@Composable
fun SmarTripTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = scheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalSmarTripDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = scheme,
            typography = SmarTripTypography.typography,
            content = content,
        )
    }
}
