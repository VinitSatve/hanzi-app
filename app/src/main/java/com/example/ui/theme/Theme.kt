package com.example.ui.theme

import android.app.Activity
import android.provider.Settings
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val MonoDarkColorScheme = darkColorScheme(
    primary = AcidLime,
    onPrimary = MonoBlack,
    primaryContainer = AcidLimeSurface,
    onPrimaryContainer = AcidLime,
    secondary = CyberMint,
    onSecondary = MonoBlack,
    secondaryContainer = CyberMintSurface,
    onSecondaryContainer = CyberMint,
    tertiary = VoltageAmber,
    onTertiary = MonoBlack,
    background = MonoBlack,
    onBackground = AlabasterWhite,
    surface = MonoObsidian,
    onSurface = AlabasterWhite,
    surfaceVariant = MonoSlateCard,
    onSurfaceVariant = MutedZinc,
    outline = MonoBorderStructural,
    error = SignalCrimson,
    onError = AlabasterWhite
)

val MonoShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(10.dp),
    extraLarge = RoundedCornerShape(12.dp)
)

data class MonoInteractionPreferences(
    val reducedMotion: Boolean = false,
    val hapticsEnabled: Boolean = true
)

val LocalMonoPreferences = staticCompositionLocalOf { MonoInteractionPreferences() }

@Composable
fun MonoTheme(
    userReducedMotion: Boolean = false,
    hapticsEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemReducedMotion = remember(context) {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
            scale == 0f
        } catch (_: Exception) {
            false
        }
    }

    val effectiveReducedMotion = userReducedMotion || systemReducedMotion
    val prefs = remember(effectiveReducedMotion, hapticsEnabled) {
        MonoInteractionPreferences(
            reducedMotion = effectiveReducedMotion,
            hapticsEnabled = hapticsEnabled
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = MonoBlack.toArgb()
                window.navigationBarColor = MonoBlack.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    CompositionLocalProvider(LocalMonoPreferences provides prefs) {
        MaterialTheme(
            colorScheme = MonoDarkColorScheme,
            typography = MonoTypography,
            shapes = MonoShapes,
            content = content
        )
    }
}
