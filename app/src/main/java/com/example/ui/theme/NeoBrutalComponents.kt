package com.example.ui.theme

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Core Neo-Brutalist surface modifier powered by centralized `HanziTheme`:
 * Draws a zero-blur solid black offset shadow behind a sharp-cornered container
 * with a 3px / 3dp pure black border (`HanziTheme.BorderWidth`).
 */
fun Modifier.neoBrutalSurface(
    backgroundColor: Color = HanziTheme.PureWhite,
    borderColor: Color = HanziTheme.PureBlack,
    shadowColor: Color = HanziTheme.PureBlack,
    borderWidth: Dp = HanziTheme.BorderWidth,
    shadowOffset: Dp = HanziTheme.HardShadowOffset,
    cornerRadius: Dp = HanziTheme.CornerRadius
): Modifier = this
    .padding(end = shadowOffset, bottom = shadowOffset)
    .drawBehind {
        val rPx = cornerRadius.toPx()
        val offPx = shadowOffset.toPx()
        // Solid hard offset shadow (no blur)
        drawRoundRect(
            color = shadowColor,
            topLeft = Offset(offPx, offPx),
            size = size,
            cornerRadius = CornerRadius(rPx, rPx)
        )
    }
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor, RoundedCornerShape(cornerRadius))
    .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))

/**
 * Chunky Neo-Brutalist Card powered by `HanziTheme` (3px/3dp solid black border,
 * hard offset shadow, optional bold header stripe, and tactile press feedback).
 */
@Composable
fun NeoBrutalCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = HanziTheme.colors.surface,
    accentHeaderColor: Color? = null,
    headerTitle: String? = null,
    headerRightText: String? = null,
    shadowOffset: Dp = HanziTheme.shadows.cardOffset,
    borderWidth: Dp = HanziTheme.borders.defaultWidth,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedPressOffset by animateDpAsState(
        targetValue = if (isPressed && onClick != null) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 80),
        label = "cardPressOffset"
    )
    val effectiveShadow = (shadowOffset - animatedPressOffset).coerceAtLeast(HanziTheme.shadows.pressedOffset)

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .offset(x = animatedPressOffset, y = animatedPressOffset)
            .neoBrutalSurface(
                backgroundColor = backgroundColor,
                borderColor = HanziTheme.colors.borderBlack,
                shadowColor = HanziTheme.shadows.shadowColor,
                borderWidth = borderWidth,
                shadowOffset = effectiveShadow,
                cornerRadius = HanziTheme.borders.cornerRadius
            )
            .then(clickModifier)
    ) {
        if (accentHeaderColor != null || headerTitle != null) {
            val headerBg = accentHeaderColor ?: HanziTheme.colors.electricYellow
            val isDarkHeader = headerBg == HanziTheme.colors.cobaltBlue ||
                headerBg == HanziTheme.colors.pureBlack ||
                headerBg == HanziTheme.colors.vividOrange
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg)
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (headerTitle ?: "").uppercase(),
                    style = HanziTheme.typography.labelLarge,
                    color = if (isDarkHeader) Color.White else HanziTheme.colors.pureBlack,
                    fontWeight = FontWeight.Bold
                )
                if (!headerRightText.isNullOrBlank()) {
                    Text(
                        text = headerRightText.uppercase(),
                        style = HanziTheme.typography.labelSmall,
                        color = if (isDarkHeader) HanziTheme.colors.electricYellow else HanziTheme.colors.pureBlack,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HanziTheme.borders.defaultWidth)
                    .background(HanziTheme.colors.borderBlack)
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

/**
 * Tactile Neo-Brutalist Button powered by `HanziTheme` (3px/3dp solid black border,
 * hard offset shadow, and physical push-down press animation).
 */
@Composable
fun NeoBrutalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = HanziTheme.colors.electricYellow,
    contentColor: Color = HanziTheme.colors.pureBlack,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 52.dp,
    shadowOffset: Dp = HanziTheme.shadows.defaultOffset
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 70),
        label = "btnPressOffset"
    )
    val activeShadow = if (enabled) (shadowOffset - pressOffset).coerceAtLeast(HanziTheme.shadows.pressedOffset) else 2.dp
    val bg = if (enabled) backgroundColor else Color(0xFFD4D4D8)
    val fg = if (enabled) contentColor else Color(0xFF71717A)

    Box(
        modifier = modifier
            .height(height + shadowOffset)
            .offset(x = pressOffset, y = pressOffset)
            .neoBrutalSurface(
                backgroundColor = bg,
                borderColor = HanziTheme.colors.borderBlack,
                shadowColor = HanziTheme.shadows.shadowColor,
                borderWidth = HanziTheme.borders.defaultWidth,
                shadowOffset = activeShadow,
                cornerRadius = HanziTheme.borders.cornerRadius
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = fg,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text.uppercase(),
                style = HanziTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Square/Rectangular Neo-Brutalist Icon Button (minimum 48dp touch target)
 */
@Composable
fun NeoBrutalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = HanziTheme.colors.electricYellow,
    contentColor: Color = HanziTheme.colors.pureBlack,
    size: Dp = 48.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed) 2.5.dp else 0.dp,
        animationSpec = tween(durationMillis = 70),
        label = "iconBtnPress"
    )
    val shadow = (HanziTheme.shadows.defaultOffset - pressOffset).coerceAtLeast(HanziTheme.shadows.pressedOffset)

    Box(
        modifier = modifier
            .size(size)
            .offset(x = pressOffset, y = pressOffset)
            .neoBrutalSurface(
                backgroundColor = backgroundColor,
                borderColor = HanziTheme.colors.borderBlack,
                shadowColor = HanziTheme.shadows.shadowColor,
                borderWidth = HanziTheme.borders.defaultWidth,
                shadowOffset = shadow,
                cornerRadius = HanziTheme.borders.cornerRadius
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Editorial Neo-Brutalist Stamp / Tag
 */
@Composable
fun NeoBrutalBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = HanziTheme.colors.electricYellow,
    textColor: Color = HanziTheme.colors.pureBlack
) {
    Box(
        modifier = modifier
            .neoBrutalSurface(
                backgroundColor = backgroundColor,
                borderColor = HanziTheme.colors.borderBlack,
                shadowColor = HanziTheme.shadows.shadowColor,
                borderWidth = HanziTheme.borders.thinWidth,
                shadowOffset = HanziTheme.shadows.badgeOffset,
                cornerRadius = HanziTheme.borders.cornerRadius
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = HanziTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

/**
 * Tactile Neo-Brutalist Filter / Selection Chip
 */
@Composable
fun NeoBrutalChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = HanziTheme.colors.electricYellow,
    unselectedColor: Color = HanziTheme.colors.surface
) {
    val bg = if (selected) selectedColor else unselectedColor
    val fg = if (selected) {
        if (selectedColor == HanziTheme.colors.cobaltBlue ||
            selectedColor == HanziTheme.colors.pureBlack ||
            selectedColor == HanziTheme.colors.vividOrange
        ) Color.White else HanziTheme.colors.pureBlack
    } else {
        HanziTheme.colors.onSurface
    }
    Box(
        modifier = modifier
            .height(42.dp)
            .neoBrutalSurface(
                backgroundColor = bg,
                borderColor = HanziTheme.colors.borderBlack,
                shadowColor = HanziTheme.shadows.shadowColor,
                borderWidth = HanziTheme.borders.defaultWidth,
                shadowOffset = if (selected) HanziTheme.shadows.chipOffset else HanziTheme.shadows.badgeOffset,
                cornerRadius = HanziTheme.borders.cornerRadius
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = HanziTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

/**
 * Minimal, intentional Neo-Brutalist Segmented Tab Bar inspired by Teenage Engineering & Nothing OS:
 * Uses crisp black-on-white geometry with a single Signal Orange (#FF5416) LED indicator on the active tab.
 */
@Composable
fun NeoBrutalTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    tagPrefix: String = "brutal_tab"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HanziTheme.colors.background)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(tabs) { idx, title ->
                val isSelected = selectedIndex == idx
                val activeBg = HanziTheme.colors.pureBlack
                val activeFg = HanziTheme.colors.pureWhite

                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .neoBrutalSurface(
                            backgroundColor = if (isSelected) activeBg else HanziTheme.colors.surface,
                            borderColor = HanziTheme.colors.borderBlack,
                            shadowColor = HanziTheme.shadows.shadowColor,
                            borderWidth = HanziTheme.borders.thinWidth,
                            shadowOffset = if (isSelected) 3.dp else 2.dp,
                            cornerRadius = HanziTheme.borders.cornerRadius
                        )
                        .clickable { onSelect(idx) }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(
                                    if (isSelected) HanziTheme.colors.vividOrange
                                    else HanziTheme.colors.borderBlack.copy(alpha = 0.2f)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title.uppercase(),
                            style = HanziTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) activeFg else HanziTheme.colors.onSurface
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HanziTheme.borders.thinWidth)
                .background(HanziTheme.colors.borderBlack)
        )
    }
}

/**
 * Chunky Segmented Neo-Brutalist Progress Bar with 3dp black border and tick grid lines
 */
@Composable
fun NeoBrutalProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    fillColor: Color = HanziTheme.colors.electricYellow,
    trackColor: Color = HanziTheme.colors.pureWhite,
    height: Dp = 22.dp,
    segments: Int = 10
) {
    val clamped = progress.coerceIn(0f, 1f)
    val tickColor = HanziTheme.colors.borderBlack
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height + 3.dp)
            .neoBrutalSurface(
                backgroundColor = trackColor,
                borderColor = HanziTheme.colors.borderBlack,
                shadowColor = HanziTheme.shadows.shadowColor,
                borderWidth = HanziTheme.borders.defaultWidth,
                shadowOffset = HanziTheme.shadows.chipOffset,
                cornerRadius = HanziTheme.borders.cornerRadius
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(clamped)
                .background(fillColor)
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = size.width / segments
            for (i in 1 until segments) {
                val x = step * i
                drawLine(
                    color = tickColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

/**
 * Visually distinctive Neo-Brutalist Input Field with bold top badge label,
 * 3dp black border, and hard offset shadow.
 */
@Composable
fun NeoBrutalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    accentColor: Color = HanziTheme.colors.electricYellow,
    leadingIcon: ImageVector? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotBlank()) {
            Box(
                modifier = Modifier
                    .background(HanziTheme.colors.pureBlack)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = label.uppercase(),
                    style = HanziTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neoBrutalSurface(
                    backgroundColor = HanziTheme.colors.surface,
                    borderColor = HanziTheme.colors.borderBlack,
                    shadowColor = HanziTheme.shadows.shadowColor,
                    borderWidth = HanziTheme.borders.defaultWidth,
                    shadowOffset = HanziTheme.shadows.defaultOffset,
                    cornerRadius = HanziTheme.borders.cornerRadius
                )
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = label,
                        tint = HanziTheme.colors.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            style = HanziTheme.typography.bodyLarge,
                            color = HanziTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = singleLine,
                        textStyle = HanziTheme.typography.bodyLarge.copy(
                            color = HanziTheme.colors.onSurface,
                            fontWeight = FontWeight.Bold
                        ),
                        cursorBrush = SolidColor(HanziTheme.colors.vividOrange),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Custom Canvas-illustrated geometric Neo-Brutalist Emblem Icon
 */
@Composable
fun NeoBrutalIllustratedEmblem(
    glyph: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp
) {
    val fg = if (backgroundColor == HanziTheme.colors.cobaltBlue ||
        backgroundColor == HanziTheme.colors.pureBlack ||
        backgroundColor == HanziTheme.colors.vividOrange
    ) {
        Color.White
    } else {
        HanziTheme.colors.pureBlack
    }
    Box(
        modifier = modifier
            .size(size)
            .neoBrutalSurface(
                backgroundColor = backgroundColor,
                borderColor = HanziTheme.colors.borderBlack,
                shadowColor = HanziTheme.shadows.shadowColor,
                borderWidth = HanziTheme.borders.defaultWidth,
                shadowOffset = HanziTheme.shadows.defaultOffset,
                cornerRadius = HanziTheme.borders.cornerRadius
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val s = 6.dp.toPx()
            drawLine(fg.copy(alpha = 0.35f), Offset(0f, s), Offset(s, s), 1.5.dp.toPx())
            drawLine(fg.copy(alpha = 0.35f), Offset(s, 0f), Offset(s, s), 1.5.dp.toPx())
        }
        Text(
            text = glyph,
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.42f).sp,
            color = fg
        )
    }
}
