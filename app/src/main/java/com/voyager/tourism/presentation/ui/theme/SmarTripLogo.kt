package com.voyager.tourism.presentation.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.voyager.tourism.R

/**
 * Logotipo oficial copiado desde `voyager-web-client/public/`:
 * - `logo.png` → [R.drawable.smartrap_logo_light] (tema claro / fondos claros)
 * - `logo-alt.png` → [R.drawable.smartrap_logo_dark] (tema oscuro / fondos oscuros)
 *
 * Misma regla que [Header.jsx]: `theme === 'dark' ? '/logo-alt.png' : '/logo.png'`.
 */
enum class SmarTripLogoVariant {
    /** Variante según [LocalSmarTripDarkTheme] (como el header web con tema app). */
    MatchTheme,

    /** Hero login y superficies claras (`--gradient-hero`). */
    OnLightBackground,

    /** Splash y fondos de marca oscuros (p. ej. `primary` azul). */
    OnDarkBackground,
}

@Composable
fun SmarTripLogo(
    modifier: Modifier = Modifier,
    variant: SmarTripLogoVariant = SmarTripLogoVariant.MatchTheme,
    contentDescription: String? = null,
) {
    val desc = contentDescription ?: stringResource(R.string.app_name)
    val darkApp = LocalSmarTripDarkTheme.current
    val resId = when (variant) {
        SmarTripLogoVariant.MatchTheme ->
            if (darkApp) R.drawable.smartrap_logo_dark else R.drawable.smartrap_logo_light
        SmarTripLogoVariant.OnLightBackground -> R.drawable.smartrap_logo_light
        SmarTripLogoVariant.OnDarkBackground -> R.drawable.smartrap_logo_dark
    }
    Image(
        painter = painterResource(resId),
        contentDescription = desc,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
