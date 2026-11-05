package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.MonoCatalog
import com.example.data.model.MonoRecurringBill
import com.example.data.model.MonoSavingsGoal
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
import com.example.ui.theme.MonoSwitch
import com.example.ui.theme.MutedZinc
import com.example.ui.theme.SignalCrimson
import com.example.ui.theme.VoltageAmber
import com.example.util.AnimatedFinancialAmount
import com.example.util.AnimatedPercentageText
import com.example.util.rememberMonoHaptics
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VaultsAndSettingsScreen(
    profile: MonoUserProfile,
    goals: List<MonoSavingsGoal>,
    bills: List<MonoRecurringBill>,
    currentMonthKey: String,
    onCreateOrUpdateGoal: (title: String, targetCents: Long, savedCents: Long, existingId: String?, onMilestone: (Boolean) -> Unit) -> Unit,
    onDeleteGoal: (MonoSavingsGoal) -> Unit,
    onCreateOrUpdateBill: (title: String, amountCents: Long, categoryId: String, cadence: String, dueDay: Long, existingId: String?) -> Unit,
    onMarkBillPaid: (MonoRecurringBill) -> Unit,
    onDeleteBill: (MonoRecurringBill) -> Unit,
    onUpdateProfile: (
        currencyCode: String,
        customSymbol: String,
        reducedMotion: Boolean,
        hapticsEnabled: Boolean
    ) -> Unit,
    onExportBackupJson: () -> String,
    onRestoreBackupJson: (jsonPayload: String, onSuccess: (Int) -> Unit, onError: (String) -> Unit) -> Unit,
    onSignOut: () -> Unit
) {
    val haptics = rememberMonoHaptics()
    val clipboard = LocalClipboardManager.current

    // Savings Goal Creator State
    var showNewGoalForm by remember { mutableStateOf(false) }
    var newGoalTitle by remember { mutableStateOf("") }
    var newGoalTargetInput by remember { mutableStateOf("2500") }

    // Recurring Bill Creator State
    var showNewBillForm by remember { mutableStateOf(false) }
    var newBillTitle by remember { mutableStateOf("") }
    var newBillAmountInput by remember { mutableStateOf("29.00") }
    var newBillDueDayInput by remember { mutableStateOf("15") }
    var newBillCadence by remember { mutableStateOf("MONTHLY") }

    // Custom Currency Symbol Input
    var customSymbolDraft by remember(profile.customCurrencySymbol) {
        mutableStateOf(profile.customCurrencySymbol)
    }

    // Backup & Restore JSON Box
    var showBackupPanel by remember { mutableStateOf(false) }
    var backupJsonText by remember { mutableStateOf("") }
    var backupStatusText by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MonoBlack)
            .testTag("vaults_and_settings_screen"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header
        item(key = "vaults_header") {
            Column {
                MonoBadge(
                    text = "VAULTS // SUBSCRIPTIONS // SYSTEM",
                    backgroundColor = AcidLimeSurface,
                    borderColor = AcidLime,
                    textColor = AcidLime
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "CAPITAL & CONTROLS",
                    style = MaterialTheme.typography.headlineLarge,
                    color = AlabasterWhite
                )
            }
        }

        // 2. Savings Goals Section
        item(key = "savings_goals_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SAVINGS VAULTS & MILESTONES",
                    style = MaterialTheme.typography.labelLarge,
                    color = CyberMint
                )
                MonoButton(
                    text = if (showNewGoalForm) "CLOSE" else "+ NEW VAULT",
                    backgroundColor = AcidLime,
                    contentColor = MonoBlack,
                    height = 38.dp,
                    modifier = Modifier.testTag("toggle_new_goal_button"),
                    onClick = { showNewGoalForm = !showNewGoalForm }
                )
            }
        }

        if (showNewGoalForm) {
            item(key = "new_goal_form") {
                MonoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MonoObsidian,
                    borderColor = AcidLime
                ) {
                    Text(
                        text = "CREATE SAVINGS GOAL VAULT",
                        style = MaterialTheme.typography.labelLarge,
                        color = AcidLime
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newGoalTitle,
                        onValueChange = { newGoalTitle = it },
                        label = { Text("VAULT NAME (e.g. Emergency Reserve)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AcidLime,
                            unfocusedBorderColor = MonoBorderStructural,
                            focusedContainerColor = MonoBlack,
                            unfocusedContainerColor = MonoBlack
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_goal_title_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newGoalTargetInput,
                        onValueChange = { next ->
                            if (next.all { it.isDigit() || it == '.' }) newGoalTargetInput = next
                        },
                        label = { Text("TARGET AMOUNT (${MonoCatalog.resolveSymbol(profile)})") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AcidLime,
                            unfocusedBorderColor = MonoBorderStructural,
                            focusedContainerColor = MonoBlack,
                            unfocusedContainerColor = MonoBlack
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_goal_target_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    MonoButton(
                        text = "INITIALIZE VAULT →",
                        backgroundColor = AcidLime,
                        contentColor = MonoBlack,
                        useMediumHaptic = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_new_goal_button"),
                        onClick = {
                            val targetCents = ((newGoalTargetInput.toDoubleOrNull() ?: 1000.0) * 100.0)
                                .roundToLong()
                                .coerceAtLeast(1000L)
                            onCreateOrUpdateGoal(
                                newGoalTitle.ifBlank { "Treasury Vault" },
                                targetCents,
                                0L,
                                null
                            ) {
                                haptics.success()
                            }
                            newGoalTitle = ""
                            showNewGoalForm = false
                        }
                    )
                }
            }
        }

        if (goals.isEmpty()) {
            item(key = "empty_goals") {
                MonoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MonoSlateCard
                ) {
                    Text(
                        text = "NO ACTIVE SAVINGS VAULTS",
                        style = MaterialTheme.typography.titleSmall,
                        color = AlabasterWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap '+ NEW VAULT' above to track savings milestones with animated progress bars.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedZinc
                    )
                }
            }
        } else {
            items(items = goals, key = { it.id }) { goal ->
                val ratio = (goal.savedCents.toFloat() / goal.targetCents.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
                val pct = (ratio * 100f).roundToInt()

                MonoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    backgroundColor = MonoSlateCard,
                    borderColor = if (goal.isCompleted) CyberMint else MonoBorderStructural
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = goal.title.uppercase(Locale.US),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AlabasterWhite
                                )
                                if (goal.isCompleted) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    MonoBadge(
                                        text = "100% FUNDED",
                                        backgroundColor = CyberMintSurface,
                                        borderColor = CyberMint,
                                        textColor = CyberMint
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${MonoCatalog.formatMoney(goal.savedCents, profile)} of ${MonoCatalog.formatMoney(goal.targetCents, profile)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedZinc
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AnimatedPercentageText(
                                targetPercent = pct,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (goal.isCompleted) CyberMint else AcidLime
                            )
                            IconButton(
                                onClick = {
                                    haptics.warning()
                                    onDeleteGoal(goal)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete goal",
                                    tint = MutedZinc
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    MonoStripedProgressBar(
                        progress = ratio,
                        height = 14.dp,
                        fillColor = if (goal.isCompleted) CyberMint else AcidLime
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // One-Tap Contribution Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val symbol = MonoCatalog.resolveSymbol(profile)
                        listOf(2500L to "+${symbol}25", 10000L to "+${symbol}100", 50000L to "+${symbol}500").forEach { (addCents, label) ->
                            MonoButton(
                                text = label,
                                backgroundColor = MonoObsidian,
                                contentColor = AcidLime,
                                borderColor = AcidLime,
                                height = 38.dp,
                                useMediumHaptic = true,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val nextSaved = goal.savedCents + addCents
                                    onCreateOrUpdateGoal(
                                        goal.title,
                                        goal.targetCents,
                                        nextSaved,
                                        goal.id
                                    ) { completed ->
                                        if (completed) haptics.success() else haptics.mediumImpact()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // 3. Recurring Bills & Subscriptions Section
        item(key = "recurring_bills_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECURRING BILLS & SUBSCRIPTIONS",
                    style = MaterialTheme.typography.labelLarge,
                    color = VoltageAmber
                )
                MonoButton(
                    text = if (showNewBillForm) "CLOSE" else "+ ADD BILL",
                    backgroundColor = VoltageAmber,
                    contentColor = MonoBlack,
                    borderColor = VoltageAmber,
                    height = 38.dp,
                    modifier = Modifier.testTag("toggle_new_bill_button"),
                    onClick = { showNewBillForm = !showNewBillForm }
                )
            }
        }

        if (showNewBillForm) {
            item(key = "new_bill_form") {
                MonoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MonoObsidian,
                    borderColor = VoltageAmber
                ) {
                    Text(
                        text = "ADD RECURRING SUBSCRIPTION / BILL",
                        style = MaterialTheme.typography.labelLarge,
                        color = VoltageAmber
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newBillTitle,
                        onValueChange = { newBillTitle = it },
                        label = { Text("MERCHANT / SERVICE NAME") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AcidLime,
                            unfocusedBorderColor = MonoBorderStructural,
                            focusedContainerColor = MonoBlack,
                            unfocusedContainerColor = MonoBlack
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newBillAmountInput,
                            onValueChange = { next ->
                                if (next.all { it.isDigit() || it == '.' }) newBillAmountInput = next
                            },
                            label = { Text("AMOUNT") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AcidLime,
                                unfocusedBorderColor = MonoBorderStructural,
                                focusedContainerColor = MonoBlack,
                                unfocusedContainerColor = MonoBlack
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newBillDueDayInput,
                            onValueChange = { next ->
                                if (next.length <= 2 && next.all { it.isDigit() }) newBillDueDayInput = next
                            },
                            label = { Text("DUE DAY (1-31)") },
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
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("MONTHLY", "WEEKLY", "YEARLY").forEach { cad ->
                            val active = newBillCadence == cad
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (active) AcidLime else MonoBlack)
                                    .border(1.dp, if (active) AcidLime else MonoBorderStructural, RoundedCornerShape(6.dp))
                                    .clickable {
                                        haptics.lightTap()
                                        newBillCadence = cad
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cad,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) MonoBlack else MutedZinc
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    MonoButton(
                        text = "SAVE RECURRING BILL →",
                        backgroundColor = AcidLime,
                        contentColor = MonoBlack,
                        useMediumHaptic = true,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val cents = ((newBillAmountInput.toDoubleOrNull() ?: 15.0) * 100.0)
                                .roundToLong()
                                .coerceAtLeast(100L)
                            val dueDay = (newBillDueDayInput.toLongOrNull() ?: 15L).coerceIn(1L, 31L)
                            onCreateOrUpdateBill(
                                newBillTitle.ifBlank { "Cloud Subscription" },
                                cents,
                                "subscriptions",
                                newBillCadence,
                                dueDay,
                                null
                            )
                            newBillTitle = ""
                            showNewBillForm = false
                        }
                    )
                }
            }
        }

        items(items = bills, key = { it.id }) { bill ->
            val isPaidThisMonth = bill.lastPaidMonthKey == currentMonthKey
            MonoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(),
                backgroundColor = MonoSlateCard,
                borderColor = if (isPaidThisMonth) CyberMint else MonoBorderStructural,
                contentPadding = PaddingValues(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DAY ${bill.nextDueDayOfMonth} · ${bill.cadence}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isPaidThisMonth) CyberMint else VoltageAmber
                        )
                        Text(
                            text = bill.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = AlabasterWhite
                        )
                        AnimatedFinancialAmount(
                            targetCents = bill.amountCents,
                            profile = profile,
                            style = MaterialTheme.typography.titleLarge,
                            color = AcidLime
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isPaidThisMonth) {
                            MonoButton(
                                text = "PAY NOW",
                                icon = Icons.Default.CheckCircle,
                                backgroundColor = AcidLime,
                                contentColor = MonoBlack,
                                height = 40.dp,
                                useMediumHaptic = true,
                                onClick = {
                                    haptics.success()
                                    onMarkBillPaid(bill)
                                }
                            )
                        } else {
                            MonoBadge(
                                text = "PAID ✓",
                                backgroundColor = CyberMintSurface,
                                borderColor = CyberMint,
                                textColor = CyberMint
                            )
                        }
                        IconButton(
                            onClick = {
                                haptics.warning()
                                onDeleteBill(bill)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete bill",
                                tint = MutedZinc
                            )
                        }
                    }
                }
            }
        }

        // 4. Multi-Currency Selector Card
        item(key = "currency_selector_card") {
            MonoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MonoSlateCard,
                accentHeaderColor = AcidLime,
                headerTitle = "MULTI-CURRENCY ENGINE",
                headerRightText = profile.currencyCode
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MonoCatalog.currencies.forEach { preset ->
                        val active = profile.currencyCode == preset.code
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (active) AcidLime else MonoObsidian)
                                .border(
                                    1.dp,
                                    if (active) AcidLime else MonoBorderStructural,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    haptics.lightTap()
                                    onUpdateProfile(
                                        preset.code,
                                        if (preset.code == "CUSTOM") customSymbolDraft else preset.symbol,
                                        profile.reducedMotionEnabled,
                                        profile.hapticsEnabled
                                    )
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("currency_option_${preset.code.lowercase()}")
                        ) {
                            Text(
                                text = "${preset.code} (${preset.symbol})",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) MonoBlack else AlabasterWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = profile.currencyCode == "CUSTOM",
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customSymbolDraft,
                            onValueChange = { if (it.length <= 6) customSymbolDraft = it },
                            label = { Text("CUSTOM CURRENCY SYMBOL") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AcidLime,
                                unfocusedBorderColor = MonoBorderStructural,
                                focusedContainerColor = MonoBlack,
                                unfocusedContainerColor = MonoBlack
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        MonoButton(
                            text = "SET SYMBOL",
                            backgroundColor = AcidLime,
                            contentColor = MonoBlack,
                            height = 48.dp,
                            onClick = {
                                onUpdateProfile(
                                    "CUSTOM",
                                    customSymbolDraft.ifBlank { "$" },
                                    profile.reducedMotionEnabled,
                                    profile.hapticsEnabled
                                )
                            }
                        )
                    }
                }
            }
        }

        // 5. Motion, Haptics & Accessibility Card
        item(key = "accessibility_card") {
            MonoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MonoSlateCard,
                headerTitle = "MOTION & HAPTIC CALIBRATION",
                headerRightText = "ACCESSIBILITY"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TACTILE HAPTIC ENGINE",
                            style = MaterialTheme.typography.titleSmall,
                            color = AlabasterWhite
                        )
                        Text(
                            text = "Native light, medium, success, warning, and error vibrations",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedZinc
                        )
                    }
                    MonoSwitch(
                        checked = profile.hapticsEnabled,
                        onCheckedChange = { enabled ->
                            onUpdateProfile(
                                profile.currencyCode,
                                profile.customCurrencySymbol,
                                profile.reducedMotionEnabled,
                                enabled
                            )
                        },
                        modifier = Modifier.testTag("toggle_haptics_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MonoBorderStructural)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "REDUCED MOTION MODE",
                            style = MaterialTheme.typography.titleSmall,
                            color = AlabasterWhite
                        )
                        Text(
                            text = "Instant number updates and simplified transitions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedZinc
                        )
                    }
                    MonoSwitch(
                        checked = profile.reducedMotionEnabled,
                        onCheckedChange = { reduced ->
                            onUpdateProfile(
                                profile.currencyCode,
                                profile.customCurrencySymbol,
                                reduced,
                                profile.hapticsEnabled
                            )
                        },
                        modifier = Modifier.testTag("toggle_reduced_motion_switch")
                    )
                }
            }
        }

        // 6. Portable JSON Backup & Restore Card
        item(key = "backup_restore_card") {
            MonoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MonoSlateCard,
                headerTitle = "PORTABLE LEDGER BACKUP & RESTORE",
                headerRightText = "JSON"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MonoButton(
                        text = "EXPORT JSON",
                        icon = Icons.Default.FileUpload,
                        backgroundColor = AcidLime,
                        contentColor = MonoBlack,
                        height = 44.dp,
                        useMediumHaptic = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_backup_button"),
                        onClick = {
                            val exported = onExportBackupJson()
                            backupJsonText = exported
                            clipboard.setText(AnnotatedString(exported))
                            showBackupPanel = true
                            backupStatusText = "EXPORTED TO CLIPBOARD & EDITOR BELOW"
                            haptics.success()
                        }
                    )
                    MonoButton(
                        text = "RESTORE JSON",
                        icon = Icons.Default.FileDownload,
                        backgroundColor = MonoObsidian,
                        contentColor = AlabasterWhite,
                        borderColor = MonoBorderStructural,
                        height = 44.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("toggle_restore_panel_button"),
                        onClick = {
                            showBackupPanel = !showBackupPanel
                        }
                    )
                }

                AnimatedVisibility(
                    visible = showBackupPanel,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        if (backupStatusText != null) {
                            Text(
                                text = backupStatusText ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberMint
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        OutlinedTextField(
                            value = backupJsonText,
                            onValueChange = { backupJsonText = it },
                            label = { Text("LEDGER JSON PAYLOAD") },
                            maxLines = 6,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AcidLime,
                                unfocusedBorderColor = MonoBorderStructural,
                                focusedContainerColor = MonoBlack,
                                unfocusedContainerColor = MonoBlack
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        MonoButton(
                            text = "CONFIRM & RESTORE LEDGER →",
                            backgroundColor = CyberMint,
                            contentColor = MonoBlack,
                            borderColor = CyberMint,
                            useMediumHaptic = true,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onRestoreBackupJson(
                                    backupJsonText,
                                    { count ->
                                        haptics.success()
                                        backupStatusText = "RESTORED $count RECORDS SUCCESSFULLY"
                                    },
                                    { err ->
                                        haptics.error()
                                        backupStatusText = "RESTORE ERROR: $err"
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }

        // 7. Sign Out Action
        item(key = "sign_out_card") {
            MonoButton(
                text = "SIGN OUT OF MONO CLOUD",
                icon = Icons.AutoMirrored.Filled.Logout,
                backgroundColor = MonoObsidian,
                contentColor = SignalCrimson,
                borderColor = SignalCrimson,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sign_out_button"),
                onClick = onSignOut
            )
            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}
