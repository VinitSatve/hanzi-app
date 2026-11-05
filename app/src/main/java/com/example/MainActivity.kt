package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.MonoTransaction
import com.example.data.model.MonoUserProfile
import com.example.data.repository.MonoRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.screens.AddTransactionSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryLedgerScreen
import com.example.ui.screens.VaultsAndSettingsScreen
import com.example.ui.theme.AcidLime
import com.example.ui.theme.AcidLimeSurface
import com.example.ui.theme.AlabasterWhite
import com.example.ui.theme.LocalMonoPreferences
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorderStructural
import com.example.ui.theme.MonoObsidian
import com.example.ui.theme.MonoSlateCard
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.MutedZinc
import com.example.ui.theme.neoBrutalSurface
import com.example.ui.viewmodel.DataUiState
import com.example.ui.viewmodel.MonoViewModel
import com.example.util.MonoMotion
import com.example.util.configureAppCheck
import com.example.util.rememberMonoHaptics
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            MonoRootApp()
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun MonoRootApp(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        MonoTheme {
            AuthScreen(onAuthSuccess = {})
        }
    } else {
        MonoAuthenticatedWorkspace(currentUserId = user.uid)
    }
}

private data class MonoNavDestination(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

@Composable
fun MonoAuthenticatedWorkspace(currentUserId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: MonoViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                MonoViewModel(MonoRepository(db), currentUserId)
            }
        }
    )

    val profileUiState by viewModel.profileState.collectAsStateWithLifecycle()
    val transactionsUiState by viewModel.transactionsState.collectAsStateWithLifecycle()
    val budgetsUiState by viewModel.budgetsState.collectAsStateWithLifecycle()
    val goalsUiState by viewModel.goalsState.collectAsStateWithLifecycle()
    val billsUiState by viewModel.billsState.collectAsStateWithLifecycle()

    val selectedDateFilter by viewModel.selectedDateFilter.collectAsStateWithLifecycle()
    val monthOffset by viewModel.monthOffset.collectAsStateWithLifecycle()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    val activeProfile = (profileUiState as? DataUiState.Success)?.data
        ?: MonoUserProfile(id = "settings", userId = currentUserId)
    val allTransactions = (transactionsUiState as? DataUiState.Success)?.data ?: emptyList()
    val activeBudgets = (budgetsUiState as? DataUiState.Success)?.data ?: emptyList()
    val activeGoals = (goalsUiState as? DataUiState.Success)?.data ?: emptyList()
    val activeBills = (billsUiState as? DataUiState.Success)?.data ?: emptyList()
    val isLoadingInitial = transactionsUiState is DataUiState.Loading

    val filteredTransactions = remember(
        allTransactions,
        selectedDateFilter,
        monthOffset,
        selectedCategoryFilter,
        searchQuery
    ) {
        viewModel.filterTransactions(
            allTransactions = allTransactions,
            dateFilter = selectedDateFilter,
            monthOffset = monthOffset,
            categoryFilter = selectedCategoryFilter,
            search = searchQuery
        )
    }

    val dailyChartPoints = remember(filteredTransactions, monthOffset) {
        viewModel.buildDailyChartPoints(filteredTransactions, monthOffset)
    }

    val categoryBreakdown = remember(filteredTransactions, activeBudgets) {
        viewModel.buildCategoryBreakdown(filteredTransactions, activeBudgets)
    }

    val currentMonthLabel = remember(monthOffset) {
        viewModel.currentMonthLabel(monthOffset)
    }
    val currentMonthKey = remember {
        viewModel.currentMonthKey(0)
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var isTransactionSheetOpen by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<MonoTransaction?>(null) }

    val stateHolder = rememberSaveableStateHolder()

    if (selectedTab != 0) {
        BackHandler { selectedTab = 0 }
    }

    val destinations = remember {
        listOf(
            MonoNavDestination("HOME", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
            MonoNavDestination("TELEMETRY", Icons.Filled.Analytics, Icons.Outlined.Analytics, "nav_analytics"),
            MonoNavDestination("LEDGER", Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong, "nav_ledger"),
            MonoNavDestination("VAULTS", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "nav_vaults")
        )
    }

    MonoTheme(
        userReducedMotion = activeProfile.reducedMotionEnabled,
        hapticsEnabled = activeProfile.hapticsEnabled
    ) {
        val haptics = rememberMonoHaptics()
        val prefs = LocalMonoPreferences.current

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MonoBlack,
            contentWindowInsets = WindowInsets.safeDrawing,
            floatingActionButton = {
                val fabInteraction = remember { MutableInteractionSource() }
                val isFabPressed by fabInteraction.collectIsPressedAsState()
                val fabScale by animateFloatAsState(
                    targetValue = if (isFabPressed) 0.92f else 1f,
                    animationSpec = MonoMotion.buttonSpring(prefs.reducedMotion),
                    label = "fabScale"
                )
                Box(
                    modifier = Modifier
                        .scale(fabScale)
                        .size(56.dp)
                        .neoBrutalSurface(
                            backgroundColor = AcidLime,
                            borderColor = AcidLime,
                            shadowColor = MonoBlack,
                            borderWidth = 2.dp,
                            shadowOffset = 4.dp,
                            cornerRadius = 12.dp
                        )
                        .clickable(
                            interactionSource = fabInteraction,
                            indication = null,
                            onClick = {
                                haptics.mediumImpact()
                                editingTransaction = null
                                isTransactionSheetOpen = true
                            }
                        )
                        .testTag("fab_add_transaction"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add transaction",
                        tint = MonoBlack,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            bottomBar = {
                MonoBottomNavigationBar(
                    destinations = destinations,
                    selectedTab = selectedTab,
                    onSelectTab = { idx ->
                        if (idx != selectedTab) {
                            haptics.lightTap()
                            selectedTab = idx
                        }
                    }
                )
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MonoBlack
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Animated Status Banner
                    AnimatedVisibility(
                        visible = statusBanner != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                                .neoBrutalSurface(
                                    backgroundColor = AcidLimeSurface,
                                    borderColor = AcidLime,
                                    shadowColor = MonoBlack,
                                    borderWidth = 1.5.dp,
                                    shadowOffset = 3.dp,
                                    cornerRadius = 6.dp
                                )
                                .clickable {
                                    haptics.lightTap()
                                    viewModel.clearStatusBanner()
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("status_banner")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "● ${statusBanner ?: ""}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AcidLime,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "[DISMISS]",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AlabasterWhite
                                )
                            }
                        }
                    }

                    // Directional Spring Screen Transitions Preserving State
                    Box(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                MonoMotion.directionalTabTransition(
                                    initialIndex = initialState,
                                    targetIndex = targetState,
                                    reducedMotion = prefs.reducedMotion
                                )
                            },
                            label = "monoScreenTransition"
                        ) { tabIndex ->
                            stateHolder.SaveableStateProvider(key = "tab_$tabIndex") {
                                when (tabIndex) {
                                    0 -> DashboardScreen(
                                        profile = activeProfile,
                                        transactions = filteredTransactions,
                                        goals = activeGoals,
                                        bills = activeBills,
                                        isLoadingInitialData = isLoadingInitial,
                                        selectedDateFilter = selectedDateFilter,
                                        monthLabel = currentMonthLabel,
                                        currentMonthKey = currentMonthKey,
                                        onSelectDateFilter = { viewModel.setDateFilter(it) },
                                        onStepMonth = { viewModel.stepMonthOffset(it) },
                                        onRefresh = { viewModel.triggerRefreshPulse() },
                                        onOpenAddTransaction = { tx ->
                                            editingTransaction = tx
                                            isTransactionSheetOpen = true
                                        },
                                        onNavigateToAnalytics = { selectedTab = 1 },
                                        onNavigateToHistory = { selectedTab = 2 },
                                        onNavigateToVaults = { selectedTab = 3 },
                                        onUpdateMonthlyBudget = { newBudgetCents ->
                                            viewModel.updateProfileSettings(
                                                currentProfile = activeProfile,
                                                monthlyBudgetCents = newBudgetCents
                                            )
                                        },
                                        onMarkBillPaid = { bill ->
                                            viewModel.markRecurringBillPaid(bill)
                                        },
                                        onDeleteTransaction = { tx ->
                                            viewModel.deleteTransaction(tx.id, tx.title)
                                        }
                                    )
                                    1 -> AnalyticsScreen(
                                        profile = activeProfile,
                                        dailyChartPoints = dailyChartPoints,
                                        categoryBreakdown = categoryBreakdown,
                                        selectedDateFilter = selectedDateFilter,
                                        monthLabel = currentMonthLabel,
                                        onSelectDateFilter = { viewModel.setDateFilter(it) },
                                        onStepMonth = { viewModel.stepMonthOffset(it) },
                                        onSaveCategoryBudget = { catId, catName, limitCents ->
                                            viewModel.saveCategoryBudget(catId, catName, limitCents)
                                        }
                                    )
                                    2 -> HistoryLedgerScreen(
                                        profile = activeProfile,
                                        transactions = filteredTransactions,
                                        searchQuery = searchQuery,
                                        selectedCategoryFilter = selectedCategoryFilter,
                                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                        onSelectCategoryFilter = { viewModel.setCategoryFilter(it) },
                                        onOpenEditTransaction = { tx ->
                                            editingTransaction = tx
                                            isTransactionSheetOpen = true
                                        },
                                        onDuplicateTransaction = { tx ->
                                            viewModel.duplicateTransaction(tx)
                                        },
                                        onDeleteTransaction = { tx ->
                                            viewModel.deleteTransaction(tx.id, tx.title)
                                        }
                                    )
                                    3 -> VaultsAndSettingsScreen(
                                        profile = activeProfile,
                                        goals = activeGoals,
                                        bills = activeBills,
                                        currentMonthKey = currentMonthKey,
                                        onCreateOrUpdateGoal = { title, target, saved, id, onMilestone ->
                                            viewModel.createOrUpdateSavingsGoal(
                                                title = title,
                                                targetCents = target,
                                                savedCents = saved,
                                                existingId = id,
                                                onGoalCompleted = onMilestone
                                            )
                                        },
                                        onDeleteGoal = { goal ->
                                            viewModel.deleteSavingsGoal(goal.id, goal.title)
                                        },
                                        onCreateOrUpdateBill = { title, amount, catId, cadence, dueDay, id ->
                                            viewModel.createOrUpdateRecurringBill(
                                                title = title,
                                                amountCents = amount,
                                                categoryId = catId,
                                                cadence = cadence,
                                                nextDueDayOfMonth = dueDay,
                                                existingId = id
                                            )
                                        },
                                        onMarkBillPaid = { bill ->
                                            viewModel.markRecurringBillPaid(bill)
                                        },
                                        onDeleteBill = { bill ->
                                            viewModel.deleteRecurringBill(bill.id, bill.title)
                                        },
                                        onUpdateProfile = { currencyCode, symbol, reducedMotion, hapticsOn ->
                                            viewModel.updateProfileSettings(
                                                currentProfile = activeProfile,
                                                currencyCode = currencyCode,
                                                customCurrencySymbol = symbol,
                                                reducedMotionEnabled = reducedMotion,
                                                hapticsEnabled = hapticsOn
                                            )
                                        },
                                        onExportBackupJson = {
                                            viewModel.generateBackupJson(
                                                profile = activeProfile,
                                                transactions = allTransactions,
                                                budgets = activeBudgets,
                                                goals = activeGoals,
                                                bills = activeBills
                                            )
                                        },
                                        onRestoreBackupJson = { json, onSuccess, onError ->
                                            viewModel.restoreFromBackupJson(json, onSuccess, onError)
                                        },
                                        onSignOut = {
                                            signOut(
                                                context = context,
                                                credentialManager = credentialManager,
                                                onSignOutComplete = {},
                                                scope = scope
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom-Up Add / Edit Transaction Modal Sheet
                if (isTransactionSheetOpen) {
                    val currentMonthExpenses = remember(filteredTransactions) {
                        filteredTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amountCents }
                    }
                    AddTransactionSheet(
                        profile = activeProfile,
                        editingTransaction = editingTransaction,
                        onDismiss = {
                            isTransactionSheetOpen = false
                            editingTransaction = null
                        },
                        onSaveTransaction = { title, amountCents, type, catId, catName, note, isSplit, splitSummary, existingId, onConfirmed, onError ->
                            viewModel.saveTransactionConfirmed(
                                title = title,
                                amountCents = amountCents,
                                type = type,
                                categoryId = catId,
                                categoryName = catName,
                                receiptNote = note,
                                isSplit = isSplit,
                                splitSummary = splitSummary,
                                existingId = existingId,
                                currentMonthExpenseCents = currentMonthExpenses,
                                monthlyBudgetCents = activeProfile.monthlyBudgetCents,
                                onSuccessConfirmed = onConfirmed,
                                onError = onError
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MonoBottomNavigationBar(
    destinations: List<MonoNavDestination>,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    val prefs = LocalMonoPreferences.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MonoObsidian)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(MonoBorderStructural)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEachIndexed { idx, dest ->
                val isSelected = selectedTab == idx
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.94f else 1f,
                    animationSpec = MonoMotion.buttonSpring(prefs.reducedMotion),
                    label = "navTabScale"
                )
                val pressOffset by animateDpAsState(
                    targetValue = if (isPressed) 2.dp else 0.dp,
                    animationSpec = tween(MonoMotion.BUTTON_PRESS_MS),
                    label = "navTabOffset"
                )
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) AcidLime else MonoSlateCard,
                    animationSpec = tween(MonoMotion.MICRO_INTERACTION_MS),
                    label = "navTabBg"
                )
                val fgColor = if (isSelected) MonoBlack else MutedZinc

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .scale(scale)
                        .offset(x = pressOffset, y = pressOffset)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor)
                        .border(
                            width = 1.5.dp,
                            color = if (isSelected) AcidLime else MonoBorderStructural,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onSelectTab(idx) }
                        )
                        .testTag(dest.tag),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .background(MonoBlack)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Icon(
                                imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                contentDescription = dest.label,
                                tint = fgColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dest.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = fgColor
                        )
                    }
                }
            }
        }
    }
}
