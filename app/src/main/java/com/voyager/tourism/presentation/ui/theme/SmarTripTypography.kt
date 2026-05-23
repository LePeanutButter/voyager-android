package com.voyager.tourism.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Escala tipográfica cercana a `index.css` del web (`--font-size-xs` … `--font-size-4xl`).
 * Familia: Plus Jakarta Sans / Inter en web; aquí sans-serif del sistema hasta incorporar fuentes descargables.
 */
object SmarTripTypography {
    private val font = FontFamily.SansSerif

    val typography = Typography(
        displayLarge = TextStyle(fontFamily = font, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 45.sp, letterSpacing = (-0.02).sp),
        displayMedium = TextStyle(fontFamily = font, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 38.sp, letterSpacing = (-0.02).sp),
        headlineLarge = TextStyle(fontFamily = font, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 38.sp, letterSpacing = (-0.02).sp),
        headlineMedium = TextStyle(fontFamily = font, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = (-0.02).sp),
        headlineSmall = TextStyle(fontFamily = font, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 28.sp, letterSpacing = (-0.02).sp),
        titleLarge = TextStyle(fontFamily = font, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 26.sp, letterSpacing = (-0.02).sp),
        titleMedium = TextStyle(fontFamily = font, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
        titleSmall = TextStyle(fontFamily = font, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge = TextStyle(fontFamily = font, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 26.sp),
        bodyMedium = TextStyle(fontFamily = font, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp),
        bodySmall = TextStyle(fontFamily = font, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
        labelLarge = TextStyle(fontFamily = font, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
        labelMedium = TextStyle(fontFamily = font, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
        labelSmall = TextStyle(fontFamily = font, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
    )
}
