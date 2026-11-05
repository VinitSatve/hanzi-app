package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized Neo-Brutalist Hex Color Palette for HANZI:
 * - Off-White (#FDFBF4)
 * - Electric Yellow (#FFE600)
 * - Cobalt Blue (#1E40FF)
 * - Vivid Orange (#FF5416)
 * - Acid Mint (#00E575)
 * - Pure Black (#0B0B0B)
 */
@Immutable
data class HanziBrutalColors(
    val offWhite: Color = BrutalOffWhite,
    val cream: Color = BrutalCream,
    val pureWhite: Color = BrutalWhite,
    val pureBlack: Color = BrutalBlack,
    val electricYellow: Color = BrutalYellow,
    val cobaltBlue: Color = BrutalCobalt,
    val vividOrange: Color = BrutalOrange,
    val acidMint: Color = BrutalMint,
    val background: Color = BrutalOffWhite,
    val surface: Color = BrutalWhite,
    val surfaceVariant: Color = BrutalCream,
    val onBackground: Color = BrutalBlack,
    val onSurface: Color = BrutalBlack,
    val borderBlack: Color = BrutalBlack,
    val shadowBlack: Color = BrutalBlack,
    val isDark: Boolean = false
)

/**
 * Centralized Neo-Brutalist Border Tokens:
 * 3px / 3dp solid black borders and sharp rectangular corners.
 */
@Immutable
data class HanziBrutalBorders(
    val strokeWidthPx: Float = 3f,
    val defaultWidth: Dp = 3.dp,
    val heroWidth: Dp = 3.5.dp,
    val thinWidth: Dp = 2.dp,
    val cornerRadius: Dp = 2.dp,
    val borderColor: Color = BrutalBlack,
    val solidBlackBorder: BorderStroke = BorderStroke(3.dp, BrutalBlack)
)

/**
 * Centralized Neo-Brutalist Hard Offset Shadow Tokens (Zero Blur):
 */
@Immutable
data class HanziBrutalShadows(
    val defaultOffset: Dp = 4.dp,
    val cardOffset: Dp = 5.dp,
    val heroOffset: Dp = 6.dp,
    val chipOffset: Dp = 3.dp,
    val badgeOffset: Dp = 2.dp,
    val pressedOffset: Dp = 1.dp,
    val blurRadius: Dp = 0.dp,
    val shadowColor: Color = BrutalBlack
)

val LocalHanziColors = staticCompositionLocalOf { HanziBrutalColors() }
val LocalHanziBorders = staticCompositionLocalOf { HanziBrutalBorders() }
val LocalHanziShadows = staticCompositionLocalOf { HanziBrutalShadows() }

// Sharp rectangular corners with minimal rounding per Neo-Brutalism specification
val BrutalShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(3.dp),
    large = RoundedCornerShape(3.dp),
    extraLarge = RoundedCornerShape(4.dp)
)

private val HanziBrutalLightColorScheme = lightColorScheme(
    primary = BrutalOrange,
    onPrimary = Color.White,
    primaryContainer = BrutalYellow,
    onPrimaryContainer = BrutalBlack,
    secondary = BrutalCobalt,
    onSecondary = Color.White,
    secondaryContainer = BrutalCobaltSoft,
    onSecondaryContainer = BrutalBlack,
    tertiary = BrutalMint,
    onTertiary = BrutalBlack,
    tertiaryContainer = BrutalMintSoft,
    onTertiaryContainer = BrutalBlack,
    background = BrutalOffWhite,
    onBackground = BrutalBlack,
    surface = BrutalWhite,
    onSurface = BrutalBlack,
    surfaceVariant = BrutalCream,
    onSurfaceVariant = BrutalMutedInk,
    outline = BrutalBlack,
    outlineVariant = BrutalBlack,
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFFD6D6),
    onErrorContainer = BrutalBlack
)

private val HanziBrutalDarkColorScheme = darkColorScheme(
    primary = BrutalYellow,
    onPrimary = BrutalBlack,
    primaryContainer = BrutalOrange,
    onPrimaryContainer = Color.White,
    secondary = BrutalCobalt,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = BrutalYellow,
    tertiary = BrutalMint,
    onTertiary = BrutalBlack,
    background = BrutalDarkBg,
    onBackground = BrutalDarkText,
    surface = BrutalDarkSurface,
    onSurface = BrutalDarkText,
    surfaceVariant = BrutalDarkCard,
    onSurfaceVariant = BrutalDarkMuted,
    outline = BrutalBlack,
    outlineVariant = BrutalYellow,
    error = BrutalOrange,
    onError = Color.White,
    errorContainer = Color(0xFF451A1C),
    onErrorContainer = Color(0xFFFFD6D6)
)

/**
 * Centralized `HanziTheme` object for Jetpack Compose that defines the Neo-Brutalist design system:
 * - 3px / 3dp solid black borders (`HanziTheme.borders`)
 * - Specific hex color palette: off-white (`#FDFBF4`), electric yellow (`#FFE600`),
 *   cobalt blue (`#1E40FF`), vivid orange (`#FF5416`), and pure black (`#0B0B0B`) (`HanziTheme.colors`)
 * - Hard offset shadows with zero blur (`HanziTheme.shadows`)
 * - Integrated with MaterialTheme (`colorScheme`, `typography`, `shapes`)
 */
object HanziTheme {
    // Direct static hex palette constants
    val OffWhite: Color = Color(0xFFFDFBF4)
    val ElectricYellow: Color = Color(0xFFFFE600)
    val CobaltBlue: Color = Color(0xFF1E40FF)
    val VividOrange: Color = Color(0xFFFF5416)
    val AcidMint: Color = Color(0xFF00E575)
    val PureBlack: Color = Color(0xFF0B0B0B)
    val PureWhite: Color = Color(0xFFFFFFFF)

    // 3px / 3dp solid black border & hard offset shadow constants
    const val BorderStrokePx: Float = 3f
    val BorderWidth: Dp = 3.dp
    val HardShadowOffset: Dp = 4.dp
    val CardShadowOffset: Dp = 5.dp
    val HeroShadowOffset: Dp = 6.dp
    val CornerRadius: Dp = 2.dp

    val colors: HanziBrutalColors
        @Composable
        @ReadOnlyComposable
        get() = LocalHanziColors.current

    val borders: HanziBrutalBorders
        @Composable
        @ReadOnlyComposable
        get() = LocalHanziBorders.current

    val shadows: HanziBrutalShadows
        @Composable
        @ReadOnlyComposable
        get() = LocalHanziShadows.current

    val colorScheme: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes
}

/**
 * Primary Neo-Brutalist Theme Composable applying `HanziTheme` tokens
 * and configuring `MaterialTheme`.
 */
@Composable
fun HanziTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val brutalColors = if (darkTheme) {
        HanziBrutalColors(
            background = BrutalDarkBg,
            surface = BrutalDarkSurface,
            surfaceVariant = BrutalDarkCard,
            onBackground = BrutalDarkText,
            onSurface = BrutalDarkText,
            isDark = true
        )
    } else {
        HanziBrutalColors(
            background = BrutalOffWhite,
            surface = BrutalWhite,
            surfaceVariant = BrutalCream,
            onBackground = BrutalBlack,
            onSurface = BrutalBlack,
            isDark = false
        )
    }
    val brutalBorders = HanziBrutalBorders()
    val brutalShadows = HanziBrutalShadows()
    val materialColorScheme = if (darkTheme) HanziBrutalDarkColorScheme else HanziBrutalLightColorScheme

    CompositionLocalProvider(
        LocalHanziColors provides brutalColors,
        LocalHanziBorders provides brutalBorders,
        LocalHanziShadows provides brutalShadows
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            shapes = BrutalShapes,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    HanziTheme(
        darkTheme = darkTheme,
        content = content
    )
}
