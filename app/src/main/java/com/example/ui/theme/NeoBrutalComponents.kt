package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout. defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.MonoMotion
import com.example.util.rememberMonoHaptics

// ============================================================================
// NEO-BRUTALIST + ACID LIME SURFACE & INTERACTIVE COMPONENTS
// ============================================================================

fun Modifier.neoBrutalSurface(
    backgroundColor: Color = MonoSlateCard,
    borderColor: Color = MonoBorderStructural,
    shadowColor: Color = MonoBlack,
    borderWidth: Dp = 1.5.dp,
    shadowOffset: Dp = 3.dp,
    cornerRadius: Dp = 8.dp
): Modifier = this
    .drawBehind {
        val offsetPx = shadowOffset.toPx()
        val radiusPx = cornerRadius.toPx()
        if (offsetPx > 0f) {
            drawRoundRect(
                color = shadowColor,
                topLeft = Offset(offsetPx, offsetPx),
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )
        }
    }
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor, RoundedCornerShape(cornerRadius))
    .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))

@Composable
fun MonoBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AcidLimeSurface,
    borderColor: Color = AcidLime,
    textColor: Color = AcidLime
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MonoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    backgroundColor: Color = AcidLime,
    contentColor: Color = MonoBlack,
    borderColor: Color = AcidLime,
    enabled: Boolean = true,
    height: Dp = 52.dp,
    useMediumHaptic: Boolean = false
) {
    val prefs = LocalMonoPreferences.current
    val haptics = rememberMonoHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.965f else 1f,
        animationSpec = MonoMotion.buttonSpring(prefs.reducedMotion),
        label = "monoBtnScale"
    )
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) 2.dp else 0.dp,
        animationSpec = tween(MonoMotion.BUTTON_PRESS_MS),
        label = "monoBtnOffset"
    )

    val effectiveBg by animateColorAsState(
        targetValue = if (enabled) backgroundColor else MonoSlateElevated,
        animationSpec = tween(MonoMotion.MICRO_INTERACTION_MS),
        label = "monoBtnBg"
    )
    val effectiveFg by animateColorAsState(
        targetValue = if (enabled) contentColor else DimSteel,
        animationSpec = tween(MonoMotion.MICRO_INTERACTION_MS),
        label = "monoBtnFg"
    )
    val effectiveBorder by animateColorAsState(
        targetValue = if (enabled) borderColor else MonoBorderStructural,
        animationSpec = tween(MonoMotion.MICRO_INTERACTION_MS),
        label = "monoBtnBorder"
    )

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .defaultMinSize(minHeight = height)
            .scale(scale)
            .offset(x = pressOffset, y = pressOffset)
            .neoBrutalSurface(
                backgroundColor = effectiveBg,
                borderColor = effectiveBorder,
                shadowColor = MonoBlack,
                borderWidth = 1.5.dp,
                shadowOffset = (3.dp - pressOffset).coerceAtLeast(0.dp),
                cornerRadius = 8.dp
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = {
                    if (useMediumHaptic) haptics.mediumImpact() else haptics.lightTap()
                    onClick()
                }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = effectiveFg,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = effectiveFg,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MonoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MonoSlateCard,
    borderColor: Color = MonoBorderStructural,
    accentHeaderColor: Color? = null,
    headerTitle: String? = null,
    headerRightText: String? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val prefs = LocalMonoPreferences.current
    val haptics = rememberMonoHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (onClick != null && isPressed) 0.985f else 1f,
        animationSpec = MonoMotion.buttonSpring(prefs.reducedMotion),
        label = "monoCardScale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .neoBrutalSurface(
                backgroundColor = backgroundColor,
                borderColor = if (isPressed && onClick != null) AcidLime.copy(alpha = 0.7f) else borderColor,
                shadowColor = MonoBlack,
                borderWidth = 1.5.dp,
                shadowOffset = 3.dp,
                cornerRadius = 10.dp
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            haptics.lightTap()
                            onClick()
                        }
                    )
                } else {
                    Modifier
                }
            )
            .animateContentSize(animationSpec = MonoMotion.cardSpring(prefs.reducedMotion))
    ) {
        if (headerTitle != null) {
            val stripBg = accentHeaderColor ?: MonoObsidian
            val stripFg = if (stripBg == AcidLime || stripBg == CyberMint || stripBg == VoltageAmber) {
                MonoBlack
            } else {
                AlabasterWhite
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(stripBg)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = headerTitle.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = stripFg,
                    fontWeight = FontWeight.Bold
                )
                if (headerRightText != null) {
                    Text(
                        text = headerRightText.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (stripFg == MonoBlack) MonoBlack else AcidLime,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(borderColor)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun MonoSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs = LocalMonoPreferences.current
    val haptics = rememberMonoHaptics()
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 2.dp,
        animationSpec = MonoMotion.microSpring(prefs.reducedMotion),
        label = "monoSwitchOffset"
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) AcidLime else MonoObsidian,
        animationSpec = tween(MonoMotion.MICRO_INTERACTION_MS),
        label = "monoSwitchTrack"
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) MonoBlack else MutedZinc,
        animationSpec = tween(MonoMotion.MICRO_INTERACTION_MS),
        label = "monoSwitchThumb"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) AcidLime else MonoBorderStrong,
        animationSpec = tween(MonoMotion.MICRO_INTERACTION_MS),
        label = "monoSwitchBorder"
    )

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .width(48.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(trackColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(
                role = Role.Switch,
                onClick = {
                    haptics.lightTap()
                    onCheckedChange(!checked)
                }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(thumbColor)
        )
    }
}

/**
 * Progress bar that animates smoothly from previous value to new value with consistent
 * 45-degree Neo-Brutalist diagonal stripe geometry.
 */
@Composable
fun MonoStripedProgressBar(
    progress: Float, // 0f..1f
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    fillColor: Color = AcidLime,
    trackColor: Color = MonoObsidian,
    stripeColor: Color = MonoBlack.copy(alpha = 0.28f)
) {
    val prefs = LocalMonoPreferences.current
    val clampedTarget = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedTarget,
        animationSpec = MonoMotion.cardSpring(prefs.reducedMotion),
        label = "stripedProgress"
    )
    val animatedFillColor by animateColorAsState(
        targetValue = fillColor,
        animationSpec = tween(MonoMotion.CARD_TRANSITION_MS),
        label = "stripedProgressColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .background(trackColor)
            .border(1.dp, MonoBorderStructural, RoundedCornerShape(4.dp))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val fillWidth = size.width * animatedProgress
            if (fillWidth > 0f) {
                clipRect(left = 0f, top = 0f, right = fillWidth, bottom = size.height) {
                    drawRect(
                        color = animatedFillColor,
                        size = Size(fillWidth, size.height)
                    )
                    // Draw consistent 45-degree diagonal Neo-Brutalist stripes
                    val stripeSpacing = 12.dp.toPx()
                    val stripeThickness = 4.5.dp.toPx()
                    var x = -size.height
                    while (x < fillWidth + size.height) {
                        val path = Path().apply {
                            moveTo(x, size.height)
                            lineTo(x + stripeThickness, size.height)
                            lineTo(x + size.height + stripeThickness, 0f)
                            lineTo(x + size.height, 0f)
                            close()
                        }
                        drawPath(path = path, color = stripeColor)
                        x += stripeSpacing
                    }
                }
            }
        }
    }
}

// Legacy helper wrappers for compatibility if referenced
@Composable
fun NeoBrutalBadge(
    text: String,
    backgroundColor: Color = AcidLime,
    textColor: Color = MonoBlack,
    modifier: Modifier = Modifier
) {
    MonoBadge(
        text = text,
        modifier = modifier,
        backgroundColor = backgroundColor,
        borderColor = backgroundColor,
        textColor = textColor
    )
}

@Composable
fun NeoBrutalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    backgroundColor: Color = AcidLime,
    contentColor: Color = MonoBlack,
    height: Dp = 52.dp,
    shadowOffset: Dp = 3.dp,
    enabled: Boolean = true
) {
    MonoButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        height = height,
        enabled = enabled
    )
}

@Composable
fun NeoBrutalCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MonoSlateCard,
    accentHeaderColor: Color? = null,
    headerTitle: String? = null,
    headerRightText: String? = null,
    shadowOffset: Dp = 3.dp,
    borderWidth: Dp = 1.5.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    MonoCard(
        modifier = modifier,
        backgroundColor = backgroundColor,
        accentHeaderColor = accentHeaderColor,
        headerTitle = headerTitle,
        headerRightText = headerRightText,
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun NeoBrutalIllustratedEmblem(
    glyph: String,
    backgroundColor: Color = AcidLime,
    size: Dp = 44.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(1.5.dp, MonoBlack, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = glyph,
            style = MaterialTheme.typography.titleMedium,
            color = MonoBlack,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
