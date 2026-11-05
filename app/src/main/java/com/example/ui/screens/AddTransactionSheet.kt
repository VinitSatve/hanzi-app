package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.model.MonoCatalog
import com.example.data.model.MonoTransaction
import com.example.data.model.MonoUserProfile
import com.example.ui.theme.AcidLime
import com.example.ui.theme.AcidLimeSurface
import com.example.ui.theme.AlabasterWhite
import com.example.ui.theme.CyberMint
import com.example.ui.theme.CyberMintSurface
import com.example.ui.theme.LocalMonoPreferences
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorderStrong
import com.example.ui.theme.MonoBorderStructural
import com.example.ui.theme.MonoButton
import com.example.ui.theme.MonoObsidian
import com.example.ui.theme.MonoSlateCard
import com.example.ui.theme.MonoSwitch
import com.example.ui.theme.MutedZinc
import com.example.ui.theme.SignalCrimson
import com.example.ui.theme.SignalCrimsonSurface
import com.example.util.MonoMotion
import com.example.util.rememberMonoHaptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionSheet(
    profile: MonoUserProfile,
    editingTransaction: MonoTransaction? = null,
    onDismiss: () -> Unit,
    onSaveTransaction: (
        title: String,
        amountCents: Long,
        type: String,
        categoryId: String,
        categoryName: String,
        receiptNote: String,
        isSplit: Boolean,
        splitSummary: String,
        existingId: String?,
        onDatabaseConfirmed: (exceededBudget: Boolean) -> Unit,
        onValidationError: (String) -> Unit
    ) -> Unit
) {
    val prefs = LocalMonoPreferences.current
    val haptics = rememberMonoHaptics()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val amountFocusRequester = remember { FocusRequester() }

    var transactionType by remember(editingTransaction) {
        mutableStateOf(editingTransaction?.type ?: "EXPENSE")
    }
    var amountInput by remember(editingTransaction) {
        val initAmount = editingTransaction?.amountCents?.let { cents ->
            if (MonoCatalog.isZeroDecimal(profile)) {
                (cents / 100L).toString()
            } else {
                String.format(Locale.US, "%.2f", cents / 100.0)
            }
        } ?: ""
        mutableStateOf(initAmount)
    }
    var titleInput by remember(editingTransaction) {
        mutableStateOf(editingTransaction?.title ?: "")
    }

    val availableCategories = remember(transactionType) {
        MonoCatalog.categories.filter {
            if (transactionType == "INCOME") it.isIncomeCategory else !it.isIncomeCategory
        }
    }

    var selectedCategoryId by remember(editingTransaction, transactionType) {
        val defaultCat = editingTransaction?.categoryId
            ?.takeIf { id -> availableCategories.any { it.id == id } }
            ?: availableCategories.first().id
        mutableStateOf(defaultCat)
    }

    var receiptNoteInput by remember(editingTransaction) {
        mutableStateOf(editingTransaction?.receiptNote ?: "")
    }
    var isSplitEnabled by remember(editingTransaction) {
        mutableStateOf(editingTransaction?.isSplit ?: false)
    }
    var splitCount by remember(editingTransaction) {
        mutableIntStateOf(2)
    }
    var splitParticipantsInput by remember(editingTransaction) {
        mutableStateOf(editingTransaction?.splitSummary ?: "You + 1 Partner (50/50)")
    }

    var isSaving by remember { mutableStateOf(false) }
    var isConfirmedSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Horizontal shake animation on validation error
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(180L)
        try {
            amountFocusRequester.requestFocus()
        } catch (_: Exception) {
        }
    }

    val parsedCents = remember(amountInput) {
        val cleaned = amountInput.replace(",", ".").trim()
        val doubleVal = cleaned.toDoubleOrNull() ?: 0.0
        (doubleVal * 100.0).roundToLong().coerceAtLeast(0L)
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
        sheetState = sheetState,
        containerColor = MonoObsidian,
        contentColor = AlabasterWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("add_transaction_sheet")
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (editingTransaction != null) "EDIT LEDGER ENTRY" else "NEW LEDGER ENTRY",
                        style = MaterialTheme.typography.titleLarge,
                        color = AlabasterWhite
                    )
                    Text(
                        text = "SPRING-CALIBRATED TRANSACTION TERMINAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedZinc
                    )
                }
                IconButton(
                    onClick = {
                        haptics.lightTap()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("close_transaction_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close transaction sheet",
                        tint = AlabasterWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expense vs Income Mode Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MonoBlack)
                    .border(1.5.dp, MonoBorderStructural, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("EXPENSE" to "EXPENSE (-)", "INCOME" to "INCOME (+)").forEach { (mode, label) ->
                    val active = transactionType == mode
                    val tabBg by animateColorAsState(
                        targetValue = when {
                            active && mode == "EXPENSE" -> AcidLime
                            active && mode == "INCOME" -> CyberMint
                            else -> MonoBlack
                        },
                        animationSpec = tween(MonoMotion.MICRO_INTERACTION_MS),
                        label = "typeTabBg"
                    )
                    val tabFg = if (active) MonoBlack else MutedZinc
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(tabBg)
                            .clickable {
                                haptics.lightTap()
                                transactionType = mode
                            }
                            .testTag("mode_tab_${mode.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = tabFg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Oversized Amount Field
            val currencySymbol = MonoCatalog.resolveSymbol(profile)
            Text(
                text = "AMOUNT ($currencySymbol)",
                style = MaterialTheme.typography.labelSmall,
                color = AcidLime
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = amountInput,
                onValueChange = { next ->
                    if (next.length <= 12 && next.all { it.isDigit() || it == '.' || it == ',' }) {
                        amountInput = next
                        errorMessage = null
                    }
                },
                placeholder = {
                    Text(
                        text = "0.00",
                        style = MaterialTheme.typography.displaySmall,
                        color = MutedZinc.copy(alpha = 0.45f)
                    )
                },
                prefix = {
                    Text(
                        text = "$currencySymbol ",
                        style = MaterialTheme.typography.displaySmall,
                        color = if (transactionType == "INCOME") CyberMint else AcidLime
                    )
                },
                textStyle = MaterialTheme.typography.displaySmall.copy(color = AlabasterWhite),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AcidLime,
                    unfocusedBorderColor = MonoBorderStrong,
                    focusedContainerColor = MonoBlack,
                    unfocusedContainerColor = MonoBlack,
                    cursorColor = AcidLime
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(amountFocusRequester)
                    .testTag("transaction_amount_input")
            )

            // Quick Amount Stepper Chips for fast tactile entry
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(500L to "+5", 1500L to "+15", 5000L to "+50", 10000L to "+100").forEach { (addCents, chipText) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MonoSlateCard)
                            .border(1.dp, MonoBorderStructural, RoundedCornerShape(6.dp))
                            .clickable {
                                haptics.lightTap()
                                val updated = parsedCents + addCents
                                amountInput = if (MonoCatalog.isZeroDecimal(profile)) {
                                    (updated / 100L).toString()
                                } else {
                                    String.format(Locale.US, "%.2f", updated / 100.0)
                                }
                                errorMessage = null
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$currencySymbol${chipText.removePrefix("+")}",
                            style = MaterialTheme.typography.labelMedium,
                            color = AlabasterWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Merchant / Title Input
            Text(
                text = "MERCHANT / DESCRIPTION",
                style = MaterialTheme.typography.labelSmall,
                color = MutedZinc
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = titleInput,
                onValueChange = {
                    titleInput = it
                    errorMessage = null
                },
                placeholder = {
                    Text(
                        text = if (transactionType == "INCOME") "e.g. Monthly Retainer / Payroll" else "e.g. Blue Bottle Coffee / Studio Gear",
                        color = MutedZinc
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AcidLime,
                    unfocusedBorderColor = MonoBorderStructural,
                    focusedContainerColor = MonoBlack,
                    unfocusedContainerColor = MonoBlack,
                    cursorColor = AcidLime
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_title_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Category Pills
            Text(
                text = "SELECT CATEGORY",
                style = MaterialTheme.typography.labelSmall,
                color = MutedZinc
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableCategories.forEach { cat ->
                    val isSelected = selectedCategoryId == cat.id
                    val pillScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.03f else 1f,
                        animationSpec = MonoMotion.microSpring(prefs.reducedMotion),
                        label = "catPillScale"
                    )
                    val pillBg by animateColorAsState(
                        targetValue = if (isSelected) AcidLime else MonoSlateCard,
                        animationSpec = tween(MonoMotion.MICRO_INTERACTION_MS),
                        label = "catPillBg"
                    )
                    val pillFg = if (isSelected) MonoBlack else AlabasterWhite

                    Box(
                        modifier = Modifier
                            .scale(pillScale)
                            .clip(RoundedCornerShape(6.dp))
                            .background(pillBg)
                            .border(
                                width = 1.5.dp,
                                color = if (isSelected) AcidLime else MonoBorderStructural,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                haptics.lightTap()
                                selectedCategoryId = cat.id
                            }
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                            .testTag("category_chip_${cat.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "[${cat.tagCode}]",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MonoBlack else AcidLime,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.name.uppercase(Locale.US),
                                style = MaterialTheme.typography.labelMedium,
                                color = pillFg,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Split Transaction & Receipt Note Attachment Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MonoSlateCard)
                    .border(1.dp, MonoBorderStructural, RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CallSplit,
                                contentDescription = null,
                                tint = AcidLime,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "SPLIT TRANSACTION",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AlabasterWhite
                                )
                                Text(
                                    text = "Divide cost across participants or accounts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedZinc
                                )
                            }
                        }
                        MonoSwitch(
                            checked = isSplitEnabled,
                            onCheckedChange = { isSplitEnabled = it },
                            modifier = Modifier.testTag("split_transaction_switch")
                        )
                    }

                    AnimatedVisibility(
                        visible = isSplitEnabled,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(2, 3, 4).forEach { ways ->
                                    val active = splitCount == ways
                                    val perPerson = if (parsedCents > 0) {
                                        MonoCatalog.formatMoney(parsedCents / ways, profile)
                                    } else {
                                        "$currencySymbol 0.00"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (active) AcidLimeSurface else MonoBlack)
                                            .border(
                                                1.dp,
                                                if (active) AcidLime else MonoBorderStructural,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable {
                                                haptics.lightTap()
                                                splitCount = ways
                                                splitParticipantsInput = "$ways-Way Split ($perPerson / ea)"
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "${ways}X SPLIT",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (active) AcidLime else AlabasterWhite
                                            )
                                            Text(
                                                text = perPerson,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MutedZinc
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = splitParticipantsInput,
                                onValueChange = { splitParticipantsInput = it },
                                label = { Text("SPLIT BREAKDOWN NOTE", style = MaterialTheme.typography.labelSmall) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AcidLime,
                                    unfocusedBorderColor = MonoBorderStructural,
                                    focusedContainerColor = MonoBlack,
                                    unfocusedContainerColor = MonoBlack
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Receipt Note Input
            OutlinedTextField(
                value = receiptNoteInput,
                onValueChange = { receiptNoteInput = it },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MutedZinc
                    )
                },
                placeholder = {
                    Text("Receipt memo, tag, or invoice # (optional)", color = MutedZinc)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AcidLime,
                    unfocusedBorderColor = MonoBorderStructural,
                    focusedContainerColor = MonoBlack,
                    unfocusedContainerColor = MonoBlack,
                    cursorColor = AcidLime
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_receipt_note_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Validation Error Banner
            AnimatedVisibility(visible = errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SignalCrimsonSurface)
                        .border(1.5.dp, SignalCrimson, RoundedCornerShape(6.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "VALIDATION ERROR // ${errorMessage ?: ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SignalCrimson
                    )
                }
            }

            // Confirmed Success Animation Banner (ONLY shown after DB write succeeds!)
            AnimatedVisibility(visible = isConfirmedSuccess) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberMintSurface)
                        .border(1.5.dp, CyberMint, RoundedCornerShape(6.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = CyberMint,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CONFIRMED // TRANSACTION RECORDED TO LEDGER",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberMint,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            val selectedCatObj = availableCategories.firstOrNull { it.id == selectedCategoryId }
                ?: availableCategories.first()

            MonoButton(
                text = when {
                    isConfirmedSuccess -> "RECORDED ✓"
                    isSaving -> "COMMITTING TO LEDGER..."
                    editingTransaction != null -> "UPDATE TRANSACTION →"
                    else -> "CONFIRM & RECORD →"
                },
                icon = if (isConfirmedSuccess) Icons.Default.Check else null,
                backgroundColor = if (isConfirmedSuccess) CyberMint else AcidLime,
                contentColor = MonoBlack,
                borderColor = if (isConfirmedSuccess) CyberMint else AcidLime,
                enabled = !isSaving && !isConfirmedSuccess,
                useMediumHaptic = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_transaction_button"),
                onClick = {
                    if (parsedCents <= 0L) {
                        haptics.error()
                        errorMessage = "Please enter an amount greater than 0.00"
                        scope.launch {
                            listOf(-18f, 18f, -12f, 12f, -6f, 6f, 0f).forEach { target ->
                                shakeOffset.animateTo(target, tween(42))
                            }
                        }
                        return@MonoButton
                    }

                    isSaving = true
                    errorMessage = null
                    val resolvedTitle = titleInput.trim().ifBlank { selectedCatObj.name }

                    onSaveTransaction(
                        resolvedTitle,
                        parsedCents,
                        transactionType,
                        selectedCatObj.id,
                        selectedCatObj.name,
                        receiptNoteInput.trim(),
                        isSplitEnabled,
                        if (isSplitEnabled) splitParticipantsInput.trim() else "",
                        editingTransaction?.id,
                        { exceededBudget ->
                            isSaving = false
                            isConfirmedSuccess = true
                            if (exceededBudget) {
                                haptics.warning()
                            } else {
                                haptics.success()
                            }
                            scope.launch {
                                delay(320L)
                                onDismiss()
                            }
                        },
                        { errMsg ->
                            isSaving = false
                            haptics.error()
                            errorMessage = errMsg
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}
