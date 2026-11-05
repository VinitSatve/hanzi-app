package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.MonoCatalog
import com.example.data.model.MonoRecurringBill
import com.example.data.model.MonoSavingsGoal
import com.example.data.model.MonoTransaction
import com.example.data.model.MonoUserProfile
import com.example.ui.theme.AcidLime
import com.example.ui.theme.AcidLimeSurface
import com.example.ui.theme.AlabasterWhite
import com.example.ui.theme.CyberMint
import com.example.ui.theme.CyberMintSurface
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
import com.example.ui.theme.SignalCrimsonSurface
import com.example.ui.theme.VoltageAmber
import com.example.ui.theme.VoltageAmberSurface
import com.example.ui.viewmodel.DateRangeFilter
import com.example.util.AnimatedFinancialAmount
import com.example.util.AnimatedPercentageText
import com.example.util.MonoShimmerSkeleton
import com.example.util.rememberMonoHaptics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

@Composable
fun DashboardScreen(
    profile: MonoUserProfile,
    transactions: List<MonoTransaction>,
    goals: List<MonoSavingsGoal>,
    bills: List<MonoRecurringBill>,
    isLoadingInitialData: Boolean,
    selectedDateFilter: DateRangeFilter,
    monthLabel: String,
    currentMonthKey: String,
    onSelectDateFilter: (DateRangeFilter) -> Unit,
    onStepMonth: (Int) -> Unit,
    onRefresh: () -> Unit,
    onOpenAddTransaction: (MonoTransaction?) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToVaults: () -> Unit,
    onUpdateMonthlyBudget: (Long) -> Unit,
    onMarkBillPaid: (MonoRecurringBill) -> Unit,
    onDeleteTransaction: (MonoTransaction) -> Unit
) {
    val haptics = rememberMonoHaptics()
    var isBudgetCardExpanded by remember { mutableStateOf(false) }
    var budgetDraftText by remember(profile.monthlyBudgetCents) {
        mutableStateOf((profile.monthlyBudgetCents / 100L).toString())
    }
    var expandedTransactionId by remember { mutableStateOf<String?>(null) }
    var horizontalSwipeAccum by remember { mutableFloatStateOf(0f) }

    val totalIncomeCents = remember(transactions) {
        transactions.filter { it.type == "INCOME" }.sumOf { it.amountCents }
    }
    val totalExpenseCents = remember(transactions) {
        transactions.filter { it.type == "EXPENSE" }.sumOf { it.amountCents }
    }
    val netBalanceCents = totalIncomeCents - totalExpenseCents

    val budgetLimit = profile.monthlyBudgetCents.coerceAtLeast(100L)
    val budgetRatio = (totalExpenseCents.toFloat() / budgetLimit.toFloat()).coerceAtLeast(0f)
    val budgetPercentInt = (budgetRatio * 100f).toInt()
    val isOverBudget = totalExpenseCents > budgetLimit
    val isWarningBudget = !isOverBudget && budgetRatio >= 0.80f

    val unpaidBillsThisMonth = remember(bills, currentMonthKey) {
        bills.filter { it.lastPaidMonthKey != currentMonthKey }
    }

    val totalSavedCents = remember(goals) { goals.sumOf { it.savedCents } }
    val totalGoalTargetCents = remember(goals) { goals.sumOf { it.targetCents }.coerceAtLeast(1L) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MonoBlack)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Terminal Bar + Month Swipe Header + Date Range Filter
        item(key = "header_bar") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { horizontalSwipeAccum = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                horizontalSwipeAccum += dragAmount
                            },
                            onDragEnd = {
                                if (horizontalSwipeAccum > 95f) {
                                    haptics.lightTap()
                                    onStepMonth(-1)
                                } else if (horizontalSwipeAccum < -95f) {
                                    haptics.lightTap()
                                    onStepMonth(1)
                                }
                                horizontalSwipeAccum = 0f
                            }
                        )
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MonoBadge(
                            text = "MONO // LEDGER",
                            backgroundColor = AcidLime,
                            borderColor = AcidLime,
                            textColor = MonoBlack
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        MonoBadge(
                            text = profile.currencyCode,
                            backgroundColor = MonoSlateCard,
                            borderColor = MonoBorderStructural,
                            textColor = AlabasterWhite
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                haptics.lightTap()
                                onRefresh()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("refresh_ledger_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync ledger",
                                tint = MutedZinc
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Month Navigator + Date Range Filter Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("prev_month_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous month",
                                tint = AlabasterWhite
                            )
                        }
                        Text(
                            text = monthLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = AcidLime,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        IconButton(
                            onClick = {
                                haptics.lightTap()
                                onStepMonth(1)
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("next_month_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next month",
                                tint = AlabasterWhite
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DateRangeFilter.entries.forEach { filter ->
                            val selected = selectedDateFilter == filter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selected) AcidLime else MonoObsidian)
                                    .border(
                                        1.dp,
                                        if (selected) AcidLime else MonoBorderStructural,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        haptics.mediumImpact()
                                        onSelectDateFilter(filter)
                                    }
                                    .padding(horizontal = 9.dp, vertical = 7.dp)
                                    .testTag("date_filter_${filter.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = filter.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) MonoBlack else MutedZinc,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Oversized Net Balance Hero Card with Animated Financial Numbers
        item(key = "hero_balance_card") {
            MonoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_balance_card"),
                backgroundColor = MonoObsidian,
                borderColor = if (netBalanceCents >= 0L) AcidLime.copy(alpha = 0.7f) else SignalCrimson,
                accentHeaderColor = AcidLime,
                headerTitle = "NET PERIOD CASH FLOW",
                headerRightText = "${transactions.size} ENTRIES"
            ) {
                Text(
                    text = "AVAILABLE NET BALANCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedZinc
                )
                Spacer(modifier = Modifier.height(4.dp))

                AnimatedFinancialAmount(
                    targetCents = netBalanceCents,
                    profile = profile,
                    style = MaterialTheme.typography.displayLarge,
                    color = if (netBalanceCents < 0L) SignalCrimson else AlabasterWhite,
                    showPlusForPositive = true,
                    modifier = Modifier.testTag("hero_net_balance_text")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Income vs Expense Split Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MonoSlateCard)
                            .border(1.dp, MonoBorderStructural, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "INFLOW (+)",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberMint
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            AnimatedFinancialAmount(
                                targetCents = totalIncomeCents,
                                profile = profile,
                                style = MaterialTheme.typography.headlineMedium,
                                color = CyberMint
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MonoSlateCard)
                            .border(1.dp, MonoBorderStructural, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "OUTFLOW (-)",
                                style = MaterialTheme.typography.labelSmall,
                                color = AcidLime
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            AnimatedFinancialAmount(
                                targetCents = totalExpenseCents,
                                profile = profile,
                                style = MaterialTheme.typography.headlineMedium,
                                color = AlabasterWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MonoButton(
                        text = "+ RECORD ENTRY",
                        icon = Icons.Default.Add,
                        backgroundColor = AcidLime,
                        contentColor = MonoBlack,
                        borderColor = AcidLime,
                        height = 48.dp,
                        useMediumHaptic = true,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("dashboard_add_transaction_button"),
                        onClick = { onOpenAddTransaction(null) }
                    )
                    MonoButton(
                        text = "TELEMETRY",
                        icon = Icons.Default.Analytics,
                        backgroundColor = MonoSlateCard,
                        contentColor = AlabasterWhite,
                        borderColor = MonoBorderStructural,
                        height = 48.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_open_analytics_button"),
                        onClick = onNavigateToAnalytics
                    )
                }
            }
        }

        // 3. Interactive Monthly Budget Burn Card (Tap to Expand & Edit Limit)
        item(key = "monthly_budget_card") {
            val barColor = when {
                isOverBudget -> SignalCrimson
                isWarningBudget -> VoltageAmber
                else -> AcidLime
            }
            MonoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monthly_budget_card"),
                backgroundColor = MonoSlateCard,
                borderColor = if (isOverBudget) SignalCrimson else MonoBorderStructural,
                onClick = { isBudgetCardExpanded = !isBudgetCardExpanded }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MONTHLY BURN GUARDRAIL",
                                style = MaterialTheme.typography.labelLarge,
                                color = AlabasterWhite
                            )
                            if (isOverBudget) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = "Budget exceeded",
                                    tint = SignalCrimson,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        val remainingCents = (budgetLimit - totalExpenseCents)
                        Text(
                            text = if (remainingCents >= 0L) {
                                "${MonoCatalog.formatMoney(remainingCents, profile)} remaining of ${MonoCatalog.formatMoney(budgetLimit, profile)}"
                            } else {
                                "EXCEEDED BY ${MonoCatalog.formatMoney(-remainingCents, profile)}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isOverBudget) SignalCrimson else MutedZinc
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    isOverBudget -> SignalCrimsonSurface
                                    isWarningBudget -> VoltageAmberSurface
                                    else -> AcidLimeSurface
                                }
                            )
                            .border(1.dp, barColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        AnimatedPercentageText(
                            targetPercent = budgetPercentInt,
                            style = MaterialTheme.typography.labelLarge,
                            color = barColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                MonoStripedProgressBar(
                    progress = budgetRatio.coerceIn(0f, 1f),
                    height = 16.dp,
                    fillColor = barColor
                )

                AnimatedVisibility(
                    visible = isBudgetCardExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 14.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MonoBorderStructural)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "ADJUST MONTHLY BUDGET TARGET (${MonoCatalog.resolveSymbol(profile)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = AcidLime
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = budgetDraftText,
                                onValueChange = { next ->
                                    if (next.length <= 9 && next.all { it.isDigit() || it == '.' }) {
                                        budgetDraftText = next
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
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("budget_limit_input")
                            )
                            MonoButton(
                                text = "APPLY",
                                backgroundColor = AcidLime,
                                contentColor = MonoBlack,
                                height = 50.dp,
                                useMediumHaptic = true,
                                modifier = Modifier.testTag("apply_budget_button"),
                                onClick = {
                                    val parsed = budgetDraftText.toDoubleOrNull() ?: 0.0
                                    val cents = (parsed * 100.0).roundToLong().coerceAtLeast(1000L)
                                    onUpdateMonthlyBudget(cents)
                                    isBudgetCardExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // 4. Savings Vaults & Upcoming Recurring Bills Dual Strip
        item(key = "vaults_and_bills_strip") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MonoCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = MonoSlateCard,
                    onClick = onNavigateToVaults,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = CyberMint,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SAVINGS VAULTS",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberMint
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    AnimatedFinancialAmount(
                        targetCents = totalSavedCents,
                        profile = profile,
                        style = MaterialTheme.typography.titleLarge,
                        color = AlabasterWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val vaultRatio = (totalSavedCents.toFloat() / totalGoalTargetCents.toFloat()).coerceIn(0f, 1f)
                    MonoStripedProgressBar(
                        progress = vaultRatio,
                        height = 8.dp,
                        fillColor = CyberMint
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${goals.size} active goals · ${(vaultRatio * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedZinc
                    )
                }

                MonoCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = MonoSlateCard,
                    onClick = onNavigateToVaults,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Text(
                        text = "RECURRING BILLS",
                        style = MaterialTheme.typography.labelSmall,
                        color = VoltageAmber
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val dueTotal = unpaidBillsThisMonth.sumOf { it.amountCents }
                    AnimatedFinancialAmount(
                        targetCents = dueTotal,
                        profile = profile,
                        style = MaterialTheme.typography.titleLarge,
                        color = AlabasterWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (unpaidBillsThisMonth.isEmpty()) {
                            "All ${bills.size} bills settled"
                        } else {
                            "${unpaidBillsThisMonth.size} upcoming due"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (unpaidBillsThisMonth.isEmpty()) CyberMint else VoltageAmber
                    )
                }
            }
        }

        // Unpaid Recurring Bills Quick-Pay Strip (if any)
        if (unpaidBillsThisMonth.isNotEmpty()) {
            item(key = "upcoming_bills_quick_pay") {
                val nextBill = unpaidBillsThisMonth.first()
                MonoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MonoObsidian,
                    borderColor = VoltageAmber.copy(alpha = 0.7f),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DUE DAY ${nextBill.nextDueDayOfMonth} // ${nextBill.cadence}",
                                style = MaterialTheme.typography.labelSmall,
                                color = VoltageAmber
                            )
                            Text(
                                text = nextBill.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = AlabasterWhite
                            )
                            Text(
                                text = MonoCatalog.formatMoney(nextBill.amountCents, profile),
                                style = MaterialTheme.typography.labelLarge,
                                color = AcidLime
                            )
                        }
                        MonoButton(
                            text = "MARK PAID",
                            icon = Icons.Default.CheckCircle,
                            backgroundColor = AcidLime,
                            contentColor = MonoBlack,
                            height = 42.dp,
                            useMediumHaptic = true,
                            onClick = {
                                haptics.success()
                                onMarkBillPaid(nextBill)
                            }
                        )
                    }
                }
            }
        }

        // 5. Recent Activity Section Header
        item(key = "recent_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT LEDGER STREAM",
                    style = MaterialTheme.typography.labelLarge,
                    color = AlabasterWhite
                )
                Text(
                    text = "VIEW FULL LEDGER →",
                    style = MaterialTheme.typography.labelSmall,
                    color = AcidLime,
                    modifier = Modifier
                        .clickable {
                            haptics.lightTap()
                            onNavigateToHistory()
                        }
                        .padding(vertical = 4.dp)
                        .testTag("view_full_ledger_link")
                )
            }
        }

        // 6. Skeleton Loading OR Empty State OR Animated Transaction List
        if (isLoadingInitialData && transactions.isEmpty()) {
            items(3, key = { "shimmer_$it" }) {
                MonoShimmerSkeleton(height = 74.dp)
            }
        } else if (transactions.isEmpty()) {
            item(key = "empty_ledger_card") {
                MonoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MonoObsidian,
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Text(
                        text = "ZERO ENTRIES IN SELECTED WINDOW",
                        style = MaterialTheme.typography.titleMedium,
                        color = AlabasterWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Record your first expense, income, or split bill to activate real-time cash-flow telemetry and animated budget tracking.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedZinc
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    MonoButton(
                        text = "+ ADD FIRST TRANSACTION",
                        backgroundColor = AcidLime,
                        contentColor = MonoBlack,
                        useMediumHaptic = true,
                        onClick = { onOpenAddTransaction(null) }
                    )
                }
            }
        } else {
            items(
                items = transactions.take(8),
                key = { it.id.ifBlank { "${it.title}_${it.timestampMillis}" } }
            ) { tx ->
                val isExpanded = expandedTransactionId == tx.id
                DashboardTransactionCard(
                    tx = tx,
                    profile = profile,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        expandedTransactionId = if (isExpanded) null else tx.id
                    },
                    onEdit = { onOpenAddTransaction(tx) },
                    onDelete = {
                        haptics.warning()
                        onDeleteTransaction(tx)
                    },
                    modifier = Modifier.animateItem()
                )
            }
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun DashboardTransactionCard(
    tx: MonoTransaction,
    profile: MonoUserProfile,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = tx.type == "INCOME"
    val tagCode = MonoCatalog.categories.firstOrNull { it.id == tx.categoryId }?.tagCode ?: "TXN"
    val dateLabel = remember(tx.timestampMillis) {
        SimpleDateFormat("MMM dd · HH:mm", Locale.US).format(Date(tx.timestampMillis)).uppercase(Locale.US)
    }

    MonoCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tx_card_${tx.id}"),
        backgroundColor = MonoSlateCard,
        borderColor = if (isExpanded) AcidLime else MonoBorderStructural,
        contentPadding = PaddingValues(14.dp),
        onClick = onToggleExpand
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isIncome) CyberMintSurface else AcidLimeSurface)
                        .border(
                            1.dp,
                            if (isIncome) CyberMint else AcidLime,
                            RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tagCode,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isIncome) CyberMint else AcidLime,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tx.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = AlabasterWhite,
                            maxLines = 1
                        )
                        if (tx.isSplit) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CallSplit,
                                contentDescription = "Split transaction",
                                tint = AcidLime,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "${tx.categoryName.uppercase(Locale.US)} · $dateLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedZinc
                    )
                }
            }

            val signedCents = if (isIncome) tx.amountCents else -tx.amountCents
            Text(
                text = MonoCatalog.formatMoney(signedCents, profile, showPlusForPositive = true),
                style = MaterialTheme.typography.titleLarge,
                color = if (isIncome) CyberMint else AlabasterWhite
            )
        }

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

                if (tx.isSplit && tx.splitSummary.isNotBlank()) {
                    Text(
                        text = "SPLIT // ${tx.splitSummary}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AcidLime
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                if (tx.receiptNote.isNotBlank()) {
                    Text(
                        text = "NOTE // ${tx.receiptNote}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AlabasterWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MonoButton(
                        text = "EDIT / SPLIT",
                        icon = Icons.Default.Edit,
                        backgroundColor = MonoObsidian,
                        contentColor = AcidLime,
                        borderColor = AcidLime,
                        height = 40.dp,
                        modifier = Modifier.weight(1f),
                        onClick = onEdit
                    )
                    MonoButton(
                        text = "DELETE",
                        icon = Icons.Default.DeleteOutline,
                        backgroundColor = SignalCrimsonSurface,
                        contentColor = SignalCrimson,
                        borderColor = SignalCrimson,
                        height = 40.dp,
                        modifier = Modifier.weight(1f),
                        onClick = onDelete
                    )
                }
            }
        }
    }
}
