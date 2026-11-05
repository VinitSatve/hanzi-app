package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.MonoCatalog
import com.example.data.model.MonoUserProfile
import com.example.ui.theme.AcidLime
import com.example.ui.theme.AcidLimeSurface
import com.example.ui.theme.AlabasterWhite
import com.example.ui.theme.CyberMint
import com.example.ui.theme.LocalMonoPreferences
import com.example.ui.theme.MonoBadge
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorderStructural
import com.example.ui.theme.MonoButton
import com.example.ui.theme.MonoCard
import com.example.ui.theme.MonoObsidian
import com.example.ui.theme.MonoSlateCard
import com.example.ui.theme.MonoStripedProgressBar
import com.example.ui.theme.MutedZinc
import com.example.ui.theme.SignalCrimson
import com.example.ui.theme.VoltageAmber
import com.example.ui.viewmodel.CategoryBreakdownItem
import com.example.ui.viewmodel.DailyChartPoint
import com.example.ui.viewmodel.DateRangeFilter
import com.example.util.AnimatedFinancialAmount
import com.example.util.AnimatedPercentageText
import com.example.util.MonoMotion
import com.example.util.rememberMonoHaptics
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@Composable
fun AnalyticsScreen(
    profile: MonoUserProfile,
    dailyChartPoints: List<DailyChartPoint>,
    categoryBreakdown: List<CategoryBreakdownItem>,
    selectedDateFilter: DateRangeFilter,
    monthLabel: String,
    onSelectDateFilter: (DateRangeFilter) -> Unit,
    onStepMonth: (Int) -> Unit,
    onSaveCategoryBudget: (categoryId: String, categoryName: String, limitCents: Long) -> Unit
) {
    val prefs = LocalMonoPreferences.current
    val haptics = rememberMonoHaptics()

    var selectedBucketIndex by remember(dailyChartPoints) {
        mutableIntStateOf((dailyChartPoints.size - 1).coerceAtLeast(0))
    }
    var expandedCategoryId by remember { mutableStateOf<String?>(null) }
    var chartLaunched by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        chartLaunched = true
    }

    val chartDrawFactor by animateFloatAsState(
        targetValue = if (chartLaunched) 1f else 0f,
        animationSpec = MonoMotion.numberCountSpec(prefs.reducedMotion),
        label = "chartDrawReveal"
    )

    val selectedPoint = dailyChartPoints.getOrNull(selectedBucketIndex)
        ?: DailyChartPoint("PERIOD", 1, 0L, 0L, System.currentTimeMillis())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MonoBlack)
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header & Date Range Switching
        item(key = "analytics_header") {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        MonoBadge(
                            text = "ANALYTICS // TELEMETRY",
                            backgroundColor = AcidLimeSurface,
                            borderColor = AcidLime,
                            textColor = AcidLime
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "CASH-FLOW MATRIX",
                            style = MaterialTheme.typography.headlineLarge,
                            color = AlabasterWhite
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MonoObsidian)
                            .border(1.dp, MonoBorderStructural, RoundedCornerShape(6.dp))
                    ) {
                        IconButton(
                            onClick = {
                                haptics.lightTap()
                                onStepMonth(-1)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous month",
                                tint = AlabasterWhite
                            )
                        }
                        Text(
                            text = monthLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = AcidLime
                        )
                        IconButton(
                            onClick = {
                                haptics.lightTap()
                                onStepMonth(1)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next month",
                                tint = AlabasterWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateRangeFilter.entries.forEach { filter ->
                        val active = selectedDateFilter == filter
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (active) AcidLime else MonoObsidian)
                                .border(
                                    1.dp,
                                    if (active) AcidLime else MonoBorderStructural,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    haptics.mediumImpact()
                                    onSelectDateFilter(filter)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (active) MonoBlack else MutedZinc,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. Interactive Touch-Responsive Cash-Flow Canvas Chart
        item(key = "interactive_chart_card") {
            MonoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("interactive_cashflow_chart_card"),
                backgroundColor = MonoObsidian,
                accentHeaderColor = AcidLime,
                headerTitle = "INTERACTIVE CASH-FLOW TELEMETRY",
                headerRightText = "TAP OR SCRUB POINTS"
            ) {
                // Selected Data Point Inspection Readout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MonoSlateCard)
                        .border(1.dp, MonoBorderStructural, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WINDOW // ${selectedPoint.label}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AcidLime
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "OUT: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedZinc
                            )
                            AnimatedFinancialAmount(
                                targetCents = selectedPoint.expenseCents,
                                profile = profile,
                                style = MaterialTheme.typography.titleLarge,
                                color = AlabasterWhite
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "INFLOW",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberMint
                        )
                        AnimatedFinancialAmount(
                            targetCents = selectedPoint.incomeCents,
                            profile = profile,
                            style = MaterialTheme.typography.titleMedium,
                            color = CyberMint,
                            showPlusForPositive = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Canvas Chart
                val maxValue = remember(dailyChartPoints) {
                    dailyChartPoints.maxOfOrNull { maxOf(it.expenseCents, it.incomeCents) }
                        ?.coerceAtLeast(1000L) ?: 10000L
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(195.dp)
                        .pointerInput(dailyChartPoints.size) {
                            detectTapGestures { offset ->
                                if (dailyChartPoints.isNotEmpty() && size.width > 0) {
                                    val idx = ((offset.x / size.width) * dailyChartPoints.size)
                                        .toInt()
                                        .coerceIn(0, dailyChartPoints.size - 1)
                                    if (idx != selectedBucketIndex) {
                                        haptics.scrubTick()
                                        selectedBucketIndex = idx
                                    }
                                }
                            }
                        }
                        .pointerInput(dailyChartPoints.size) {
                            detectHorizontalDragGestures { change, _ ->
                                if (dailyChartPoints.isNotEmpty() && size.width > 0) {
                                    val idx = ((change.position.x / size.width) * dailyChartPoints.size)
                                        .toInt()
                                        .coerceIn(0, dailyChartPoints.size - 1)
                                    if (idx != selectedBucketIndex) {
                                        haptics.scrubTick()
                                        selectedBucketIndex = idx
                                    }
                                }
                            }
                        }
                        .testTag("interactive_chart_canvas")
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val count = dailyChartPoints.size.coerceAtLeast(1)
                        val slotWidth = size.width / count
                        val chartHeight = size.height - 24.dp.toPx()

                        // Horizontal reference grid lines
                        val gridSteps = 4
                        for (g in 0..gridSteps) {
                            val y = chartHeight * (g.toFloat() / gridSteps)
                            drawLine(
                                color = MonoBorderStructural.copy(alpha = 0.55f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                            )
                        }

                        val trendPath = Path()

                        dailyChartPoints.forEachIndexed { idx, pt ->
                            val centerX = slotWidth * idx + slotWidth / 2f
                            val isSelected = idx == selectedBucketIndex

                            val expRatio = (pt.expenseCents.toFloat() / maxValue.toFloat()).coerceIn(0.03f, 1f) * chartDrawFactor
                            val incRatio = (pt.incomeCents.toFloat() / maxValue.toFloat()).coerceIn(0.02f, 1f) * chartDrawFactor

                            val expBarHeight = chartHeight * expRatio
                            val incBarHeight = chartHeight * incRatio
                            val barW = (slotWidth * 0.28f).coerceAtLeast(6.dp.toPx())

                            // Selected column crosshair
                            if (isSelected) {
                                drawLine(
                                    color = AcidLime.copy(alpha = 0.45f),
                                    start = Offset(centerX, 0f),
                                    end = Offset(centerX, chartHeight),
                                    strokeWidth = 1.5.dp.toPx()
                                )
                            }

                            // Expense Bar (Acid Lime)
                            drawRoundRect(
                                color = if (isSelected) AcidLime else AcidLime.copy(alpha = 0.55f),
                                topLeft = Offset(centerX - barW - 2.dp.toPx(), chartHeight - expBarHeight),
                                size = Size(barW, expBarHeight),
                                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                            )

                            // Income Bar (Cyber Mint)
                            drawRoundRect(
                                color = if (isSelected) CyberMint else CyberMint.copy(alpha = 0.45f),
                                topLeft = Offset(centerX + 2.dp.toPx(), chartHeight - incBarHeight),
                                size = Size(barW, incBarHeight),
                                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                            )

                            val nodeY = chartHeight - expBarHeight
                            if (idx == 0) {
                                trendPath.moveTo(centerX, nodeY)
                            } else {
                                trendPath.lineTo(centerX, nodeY)
                            }

                            if (isSelected) {
                                drawCircle(
                                    color = MonoBlack,
                                    radius = 6.dp.toPx(),
                                    center = Offset(centerX, nodeY)
                                )
                                drawCircle(
                                    color = AcidLime,
                                    radius = 4.dp.toPx(),
                                    center = Offset(centerX, nodeY)
                                )
                            }
                        }

                        drawPath(
                            path = trendPath,
                            color = AlabasterWhite.copy(alpha = 0.65f),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // Accessible Bucket Selector Row below chart
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dailyChartPoints.forEachIndexed { idx, pt ->
                        val active = idx == selectedBucketIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (active) AcidLimeSurface else MonoBlack)
                                .clickable {
                                    haptics.lightTap()
                                    selectedBucketIndex = idx
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "D${pt.dayOfMonth}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (active) AcidLime else MutedZinc,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // 3. Category Breakdown Header
        item(key = "cat_breakdown_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CATEGORY ALLOCATION & LIMITS",
                    style = MaterialTheme.typography.labelLarge,
                    color = AlabasterWhite
                )
                Text(
                    text = "TAP TO EDIT CAP",
                    style = MaterialTheme.typography.labelSmall,
                    color = AcidLime
                )
            }
        }

        // 4. Animated Category Breakdown Cards with Inline Budget Cap Editor
        items(
            items = categoryBreakdown,
            key = { it.categoryId }
        ) { item ->
            val isExpanded = expandedCategoryId == item.categoryId
            var limitDraft by remember(item.budgetLimitCents) {
                mutableStateOf((item.budgetLimitCents / 100L).toString())
            }
            val barColor = when {
                item.isOverBudget -> SignalCrimson
                item.budgetProgress >= 0.8f -> VoltageAmber
                else -> AcidLime
            }

            MonoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem()
                    .testTag("category_breakdown_${item.categoryId}"),
                backgroundColor = MonoSlateCard,
                borderColor = if (isExpanded) AcidLime else MonoBorderStructural,
                contentPadding = PaddingValues(14.dp),
                onClick = {
                    expandedCategoryId = if (isExpanded) null else item.categoryId
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MonoBadge(
                            text = item.tagCode,
                            backgroundColor = AcidLimeSurface,
                            borderColor = barColor,
                            textColor = barColor
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = item.categoryName.uppercase(Locale.US),
                                style = MaterialTheme.typography.titleSmall,
                                color = AlabasterWhite
                            )
                            Text(
                                text = "CAP: ${MonoCatalog.formatMoney(item.budgetLimitCents, profile)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedZinc
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        AnimatedFinancialAmount(
                            targetCents = item.spentCents,
                            profile = profile,
                            style = MaterialTheme.typography.titleLarge,
                            color = if (item.isOverBudget) SignalCrimson else AlabasterWhite
                        )
                        AnimatedPercentageText(
                            targetPercent = (item.percentageOfTotalSpend * 100f).roundToInt(),
                            suffix = "% OF SPEND",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedZinc
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                MonoStripedProgressBar(
                    progress = item.budgetProgress.coerceIn(0f, 1f),
                    height = 12.dp,
                    fillColor = barColor
                )

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MonoBorderStructural)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "SET MONTHLY CATEGORY CAP (${MonoCatalog.resolveSymbol(profile)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = AcidLime
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = limitDraft,
                                onValueChange = { next ->
                                    if (next.length <= 9 && next.all { it.isDigit() || it == '.' }) {
                                        limitDraft = next
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AcidLime,
                                    unfocusedBorderColor = MonoBorderStructural,
                                    focusedContainerColor = MonoBlack,
                                    unfocusedContainerColor = MonoBlack
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            MonoButton(
                                text = "SAVE CAP",
                                backgroundColor = AcidLime,
                                contentColor = MonoBlack,
                                height = 48.dp,
                                useMediumHaptic = true,
                                onClick = {
                                    val parsed = limitDraft.toDoubleOrNull() ?: 500.0
                                    val cents = (parsed * 100.0).roundToLong().coerceAtLeast(100L)
                                    onSaveCategoryBudget(item.categoryId, item.categoryName, cents)
                                    expandedCategoryId = null
                                }
                            )
                        }
                    }
                }
            }
        }

        item(key = "analytics_bottom_spacer") {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
