package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
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
import com.example.ui.theme.MonoBadge
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorderStructural
import com.example.ui.theme.MonoButton
import com.example.ui.theme.MonoCard
import com.example.ui.theme.MonoObsidian
import com.example.ui.theme.MonoSlateCard
import com.example.ui.theme.MutedZinc
import com.example.ui.theme.SignalCrimson
import com.example.ui.theme.SignalCrimsonSurface
import com.example.ui.theme.neoBrutalSurface
import com.example.util.MonoMotion
import com.example.util.rememberMonoHaptics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HistoryLedgerScreen(
    profile: MonoUserProfile,
    transactions: List<MonoTransaction>,
    searchQuery: String,
    selectedCategoryFilter: String?,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategoryFilter: (String?) -> Unit,
    onOpenEditTransaction: (MonoTransaction?) -> Unit,
    onDuplicateTransaction: (MonoTransaction) -> Unit,
    onDeleteTransaction: (MonoTransaction) -> Unit
) {
    val haptics = rememberMonoHaptics()
    var expandedId by remember { mutableStateOf<String?>(null) }
    var swipedOpenId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MonoBlack)
            .testTag("history_ledger_screen"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header
        item(key = "ledger_header") {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        MonoBadge(
                            text = "AUDIT // FULL LEDGER",
                            backgroundColor = AcidLimeSurface,
                            borderColor = AcidLime,
                            textColor = AcidLime
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "TRANSACTION STREAM",
                            style = MaterialTheme.typography.headlineLarge,
                            color = AlabasterWhite
                        )
                    }
                    MonoBadge(
                        text = "${transactions.size} RECORDS",
                        backgroundColor = MonoSlateCard,
                        borderColor = MonoBorderStructural,
                        textColor = AlabasterWhite
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = AcidLime
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    haptics.lightTap()
                                    onSearchQueryChange("")
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MutedZinc
                                )
                            }
                        }
                    },
                    placeholder = {
                        Text("Search merchant, category, split, or receipt memo...", color = MutedZinc)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AcidLime,
                        unfocusedBorderColor = MonoBorderStructural,
                        focusedContainerColor = MonoObsidian,
                        unfocusedContainerColor = MonoObsidian,
                        cursorColor = AcidLime
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ledger_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val allSelected = selectedCategoryFilter == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (allSelected) AcidLime else MonoObsidian)
                            .border(
                                1.dp,
                                if (allSelected) AcidLime else MonoBorderStructural,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                haptics.mediumImpact()
                                onSelectCategoryFilter(null)
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "ALL CATEGORIES",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (allSelected) MonoBlack else MutedZinc,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    MonoCatalog.categories.forEach { cat ->
                        val active = selectedCategoryFilter == cat.id
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
                                    haptics.mediumImpact()
                                    onSelectCategoryFilter(cat.id)
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "[${cat.tagCode}] ${cat.name.uppercase(Locale.US)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (active) MonoBlack else AlabasterWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "TIP: SWIPE LEFT OR LONG-PRESS ANY ENTRY FOR QUICK ACTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedZinc
                )
            }
        }

        // 2. Empty State OR Swipeable/Expandable Transaction Rows
        if (transactions.isEmpty()) {
            item(key = "empty_history") {
                MonoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MonoObsidian,
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Text(
                        text = "NO MATCHING TRANSACTIONS",
                        style = MaterialTheme.typography.titleMedium,
                        color = AlabasterWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Adjust your search filter or record a new transaction entry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedZinc
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MonoButton(
                        text = "+ NEW TRANSACTION",
                        icon = Icons.Default.Add,
                        backgroundColor = AcidLime,
                        contentColor = MonoBlack,
                        onClick = { onOpenEditTransaction(null) }
                    )
                }
            }
        } else {
            items(
                items = transactions,
                key = { it.id.ifBlank { "${it.title}_${it.timestampMillis}" } }
            ) { tx ->
                SwipeableHistoryItem(
                    tx = tx,
                    profile = profile,
                    isExpanded = expandedId == tx.id,
                    isSwipedOpen = swipedOpenId == tx.id,
                    onToggleExpand = {
                        expandedId = if (expandedId == tx.id) null else tx.id
                    },
                    onSwipeStateChange = { open ->
                        swipedOpenId = if (open) tx.id else null
                    },
                    onEdit = {
                        swipedOpenId = null
                        onOpenEditTransaction(tx)
                    },
                    onDuplicate = {
                        swipedOpenId = null
                        haptics.success()
                        onDuplicateTransaction(tx)
                    },
                    onDelete = {
                        swipedOpenId = null
                        haptics.warning()
                        onDeleteTransaction(tx)
                    },
                    modifier = Modifier.animateItem()
                )
            }
        }

        item(key = "history_bottom_spacer") {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SwipeableHistoryItem(
    tx: MonoTransaction,
    profile: MonoUserProfile,
    isExpanded: Boolean,
    isSwipedOpen: Boolean,
    onToggleExpand: () -> Unit,
    onSwipeStateChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs = LocalMonoPreferences.current
    val haptics = rememberMonoHaptics()
    var dragDelta by remember { mutableFloatStateOf(0f) }

    val targetOffsetPx = if (isSwipedOpen) -420f else 0f
    val animatedOffsetPx by animateFloatAsState(
        targetValue = targetOffsetPx,
        animationSpec = MonoMotion.cardSpring(prefs.reducedMotion),
        label = "swipeRowOffset"
    )

    val isIncome = tx.type == "INCOME"
    val tagCode = MonoCatalog.categories.firstOrNull { it.id == tx.categoryId }?.tagCode ?: "TXN"
    val formattedDate = remember(tx.timestampMillis) {
        SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.US).format(Date(tx.timestampMillis)).uppercase(Locale.US)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("history_row_${tx.id}")
    ) {
        // Revealed Action Tray Behind Card
        Row(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(10.dp))
                .background(MonoObsidian)
                .border(1.dp, MonoBorderStructural, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    haptics.lightTap()
                    onEdit()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit or Split",
                    tint = AcidLime
                )
            }
            IconButton(
                onClick = onDuplicate
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Duplicate",
                    tint = CyberMint
                )
            }
            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = SignalCrimson
                )
            }
        }

        // Foreground Transaction Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetPx.roundToInt(), 0) }
                .pointerInput(tx.id, isSwipedOpen) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragDelta = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            dragDelta += dragAmount
                        },
                        onDragEnd = {
                            if (dragDelta < -70f && !isSwipedOpen) {
                                haptics.lightTap()
                                onSwipeStateChange(true)
                            } else if (dragDelta > 60f && isSwipedOpen) {
                                haptics.lightTap()
                                onSwipeStateChange(false)
                            }
                            dragDelta = 0f
                        }
                    )
                }
                .neoBrutalSurface(
                    backgroundColor = MonoSlateCard,
                    borderColor = if (isExpanded || isSwipedOpen) AcidLime else MonoBorderStructural,
                    shadowColor = MonoBlack,
                    borderWidth = 1.5.dp,
                    shadowOffset = 3.dp,
                    cornerRadius = 10.dp
                )
                .combinedClickable(
                    onClick = {
                        haptics.lightTap()
                        if (isSwipedOpen) {
                            onSwipeStateChange(false)
                        } else {
                            onToggleExpand()
                        }
                    },
                    onLongClick = {
                        haptics.mediumImpact()
                        onSwipeStateChange(!isSwipedOpen)
                    }
                )
                .padding(14.dp)
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
                                    contentDescription = "Split",
                                    tint = AcidLime,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = "${tx.categoryName.uppercase(Locale.US)} · $formattedDate",
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

            // Expanded Contextual Details & Accessible Actions
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
                            text = "SPLIT BREAKDOWN // ${tx.splitSummary}",
                            style = MaterialTheme.typography.labelMedium,
                            color = AcidLime
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    if (tx.receiptNote.isNotBlank()) {
                        Text(
                            text = "RECEIPT MEMO // ${tx.receiptNote}",
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
                            height = 42.dp,
                            modifier = Modifier.weight(1f),
                            onClick = onEdit
                        )
                        MonoButton(
                            text = "CLONE",
                            icon = Icons.Default.ContentCopy,
                            backgroundColor = MonoObsidian,
                            contentColor = CyberMint,
                            borderColor = CyberMint,
                            height = 42.dp,
                            modifier = Modifier.weight(1f),
                            onClick = onDuplicate
                        )
                        MonoButton(
                            text = "DELETE",
                            icon = Icons.Default.DeleteOutline,
                            backgroundColor = SignalCrimsonSurface,
                            contentColor = SignalCrimson,
                            borderColor = SignalCrimson,
                            height = 42.dp,
                            modifier = Modifier.weight(1f),
                            onClick = onDelete
                        )
                    }
                }
            }
        }
    }
}
