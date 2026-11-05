package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.MonoCatalog
import com.example.data.model.MonoUserProfile
import com.example.ui.theme.AlabasterWhite
import com.example.ui.theme.LocalMonoPreferences
import com.example.ui.theme.MonoObsidian
import com.example.ui.theme.MonoSlateCard
import com.example.ui.theme.MonoSlateElevated
import kotlin.math.roundToLong

// ============================================================================
// 1. MOTION DESIGN SYSTEM (Spring Physics + Calibrated Timings)
// ============================================================================
object MonoMotion {
    const val BUTTON_PRESS_MS = 120
    const val MICRO_INTERACTION_MS = 180
    const val CARD_TRANSITION_MS = 300
    const val SCREEN_TRANSITION_MS = 360
    const val BOTTOM_SHEET_MS = 320
    const val NUMBER_COUNT_MS = 650

    fun <T> buttonSpring(reducedMotion: Boolean = false): FiniteAnimationSpec<T> =
        if (reducedMotion) snap()
        else spring(dampingRatio = 0.68f, stiffness = 650f)

    fun <T> microSpring(reducedMotion: Boolean = false): FiniteAnimationSpec<T> =
        if (reducedMotion) snap()
        else spring(dampingRatio = 0.78f, stiffness = 520f)

    fun <T> cardSpring(reducedMotion: Boolean = false): FiniteAnimationSpec<T> =
        if (reducedMotion) snap()
        else spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)

    fun <T> numberCountSpec(reducedMotion: Boolean = false): FiniteAnimationSpec<T> =
        if (reducedMotion) snap()
        else tween(durationMillis = NUMBER_COUNT_MS, easing = FastOutSlowInEasing)

    fun directionalTabTransition(
        initialIndex: Int,
        targetIndex: Int,
        reducedMotion: Boolean
    ): ContentTransform {
        if (reducedMotion) {
            return fadeIn(snap()) togetherWith fadeOut(snap())
        }
        val forward = targetIndex >= initialIndex
        return (slideInHorizontally(
            animationSpec = tween(SCREEN_TRANSITION_MS, easing = LinearOutSlowInEasing),
            initialOffsetX = { fullWidth -> if (forward) fullWidth / 5 else -fullWidth / 5 }
        ) + fadeIn(tween(240, easing = LinearOutSlowInEasing))) togetherWith
            (slideOutHorizontally(
                animationSpec = tween(260, easing = FastOutLinearInEasing),
                targetOffsetX = { fullWidth -> if (forward) -fullWidth / 7 else fullWidth / 7 }
            ) + fadeOut(tween(180, easing = FastOutLinearInEasing)))
    }
}

// ============================================================================
// 2. ADVANCED NATIVE ANDROID HAPTIC FEEDBACK ENGINE
// ============================================================================
class MonoHapticEngine(
    private val context: Context,
    private val view: View,
    private val enabled: Boolean
) {
    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun isSystemHapticsEnabled(): Boolean {
        return try {
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.HAPTIC_FEEDBACK_ENABLED,
                1
            ) != 0
        } catch (_: Exception) {
            true
        }
    }

    private fun canVibrate(): Boolean = enabled && isSystemHapticsEnabled()

    /**
     * LIGHT HAPTIC: Normal button taps, tab switching, selecting categories, opening menus, calendar dates.
     */
    fun lightTap() {
        if (!canVibrate()) return
        try {
            val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                HapticFeedbackConstants.KEYBOARD_TAP
            } else {
                HapticFeedbackConstants.CLOCK_TICK
            }
            view.performHapticFeedback(constant)
        } catch (_: Exception) {
        }
    }

    /**
     * Chart / Scrubbing tick detent
     */
    fun scrubTick() {
        if (!canVibrate()) return
        try {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        } catch (_: Exception) {
        }
    }

    /**
     * MEDIUM HAPTIC: Saving transactions, confirming budget changes, completing goals, applying filters.
     */
    fun mediumImpact() {
        if (!canVibrate()) return
        try {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        } catch (_: Exception) {
        }
    }

    /**
     * SUCCESS HAPTIC: Successfully adding expense/income, completing savings goal, restoring backup.
     */
    fun success() {
        if (!canVibrate()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0L, 14L, 45L, 22L), intArrayOf(0, 120, 0, 190), -1)
                )
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
        } catch (_: Exception) {
        }
    }

    /**
     * WARNING HAPTIC: Exceeding a budget, deleting a transaction, important financial alerts.
     */
    fun warning() {
        if (!canVibrate()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0L, 24L, 55L, 28L), intArrayOf(0, 180, 0, 160), -1)
                )
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        } catch (_: Exception) {
        }
    }

    /**
     * ERROR HAPTIC: Invalid input, failed save operations, validation errors.
     */
    fun error() {
        if (!canVibrate()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0L, 30L, 40L, 30L, 40L, 35L), -1)
                )
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        } catch (_: Exception) {
        }
    }
}

@Composable
fun rememberMonoHaptics(): MonoHapticEngine {
    val context = LocalContext.current
    val view = LocalView.current
    val prefs = LocalMonoPreferences.current
    return remember(context, view, prefs.hapticsEnabled) {
        MonoHapticEngine(context, view, prefs.hapticsEnabled)
    }
}

// ============================================================================
// 3. PREMIUM FINANCIAL NUMBER ANIMATION COMPOSABLE
// ============================================================================
@Composable
fun AnimatedFinancialAmount(
    targetCents: Long,
    profile: MonoUserProfile,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayLarge,
    color: Color = AlabasterWhite,
    showPlusForPositive: Boolean = false,
    prefix: String = ""
) {
    val prefs = LocalMonoPreferences.current
    var previousTarget by remember { mutableLongStateOf(targetCents) }

    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = MonoMotion.numberCountSpec(prefs.reducedMotion),
        label = "financialNumberCount"
    )

    // Smoothly interpolate between previous settled value and current targetCents
    val animatedFloat by animateFloatAsState(
        targetValue = targetCents.toFloat(),
        animationSpec = MonoMotion.numberCountSpec(prefs.reducedMotion),
        label = "financialCentsInterpolation"
    )

    LaunchedEffect(targetCents) {
        previousTarget = targetCents
    }

    val displayedCents = if (prefs.reducedMotion || progress >= 0.999f) {
        targetCents
    } else {
        animatedFloat.roundToLong()
    }

    val formatted = remember(displayedCents, profile.currencyCode, profile.customCurrencySymbol, showPlusForPositive, prefix) {
        prefix + MonoCatalog.formatMoney(displayedCents, profile, showPlusForPositive)
    }

    Text(
        text = formatted,
        style = style,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
fun AnimatedPercentageText(
    targetPercent: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    color: Color = AlabasterWhite,
    suffix: String = "%"
) {
    val prefs = LocalMonoPreferences.current
    val animatedValue by animateFloatAsState(
        targetValue = targetPercent.toFloat(),
        animationSpec = MonoMotion.numberCountSpec(prefs.reducedMotion),
        label = "animatedPercent"
    )
    val displayInt = if (prefs.reducedMotion) targetPercent else animatedValue.roundToLong().toInt()
    Text(
        text = "$displayInt$suffix",
        style = style,
        color = color,
        modifier = modifier
    )
}

// ============================================================================
// 4. LIGHTWEIGHT SKELETON SHIMMER LOADER (NO SPINNERS)
// ============================================================================
@Composable
fun MonoShimmerSkeleton(
    modifier: Modifier = Modifier,
    height: Dp = 76.dp,
    cornerRadius: Dp = 8.dp
) {
    val prefs = LocalMonoPreferences.current
    if (prefs.reducedMotion) {
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
        ) {
            drawRoundRect(
                color = MonoSlateCard,
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
            )
        }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "monoShimmer")
    val phase by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1250, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "monoShimmerPhase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val startX = width * phase
            val brush = Brush.linearGradient(
                colors = listOf(MonoObsidian, MonoSlateElevated, MonoObsidian),
                start = Offset(startX, 0f),
                end = Offset(startX + width * 0.45f, size.height)
            )
            drawRoundRect(
                brush = brush,
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
            )
        }
    }
}
