package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.MonoBackupSerializer
import com.example.data.model.MonoCatalog
import com.example.data.model.MonoCategoryBudget
import com.example.data.model.MonoRecurringBill
import com.example.data.model.MonoSavingsGoal
import com.example.data.model.MonoTransaction
import com.example.data.model.MonoUserProfile
import com.example.data.repository.MonoRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface DataUiState<out T> {
    data object Loading : DataUiState<Nothing>
    data class Success<T>(val data: T) : DataUiState<T>
    data class Error(val message: String) : DataUiState<Nothing>
}

enum class DateRangeFilter(val label: String) {
    THIS_MONTH("MONTH"),
    LAST_30_DAYS("30 DAYS"),
    ALL_TIME("ALL TIME")
}

data class DailyChartPoint(
    val label: String,
    val dayOfMonth: Int,
    val expenseCents: Long,
    val incomeCents: Long,
    val timestampMillis: Long
)

data class CategoryBreakdownItem(
    val categoryId: String,
    val categoryName: String,
    val tagCode: String,
    val spentCents: Long,
    val budgetLimitCents: Long,
    val percentageOfTotalSpend: Float,
    val budgetProgress: Float,
    val isOverBudget: Boolean
)

class MonoViewModel(
    private val repository: MonoRepository,
    private val userId: String
) : ViewModel() {

    val profileState: StateFlow<DataUiState<MonoUserProfile>> = repository
        .observeUserProfile(userId)
        .map<MonoUserProfile?, DataUiState<MonoUserProfile>> { profile ->
            DataUiState.Success(
                profile ?: MonoUserProfile(id = "settings", userId = userId)
            )
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            emit(DataUiState.Error(rootError.message ?: "Failed to load profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = DataUiState.Loading
        )

    val transactionsState: StateFlow<DataUiState<List<MonoTransaction>>> = repository
        .observeTransactions(userId)
        .map<List<MonoTransaction>, DataUiState<List<MonoTransaction>>> { DataUiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            emit(DataUiState.Error(rootError.message ?: "Failed to load transactions"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = DataUiState.Loading
        )

    val budgetsState: StateFlow<DataUiState<List<MonoCategoryBudget>>> = repository
        .observeBudgets(userId)
        .map<List<MonoCategoryBudget>, DataUiState<List<MonoCategoryBudget>>> { DataUiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            emit(DataUiState.Error(rootError.message ?: "Failed to load budgets"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = DataUiState.Loading
        )

    val goalsState: StateFlow<DataUiState<List<MonoSavingsGoal>>> = repository
        .observeSavingsGoals(userId)
        .map<List<MonoSavingsGoal>, DataUiState<List<MonoSavingsGoal>>> { DataUiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            emit(DataUiState.Error(rootError.message ?: "Failed to load savings goals"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = DataUiState.Loading
        )

    val billsState: StateFlow<DataUiState<List<MonoRecurringBill>>> = repository
        .observeRecurringBills(userId)
        .map<List<MonoRecurringBill>, DataUiState<List<MonoRecurringBill>>> { DataUiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            emit(DataUiState.Error(rootError.message ?: "Failed to load recurring bills"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = DataUiState.Loading
        )

    private val _selectedDateFilter = MutableStateFlow(DateRangeFilter.THIS_MONTH)
    val selectedDateFilter: StateFlow<DateRangeFilter> = _selectedDateFilter.asStateFlow()

    // Month offset relative to current month (0 = current month, -1 = previous month, etc.)
    private val _monthOffset = MutableStateFlow(0)
    val monthOffset: StateFlow<Int> = _monthOffset.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun setDateFilter(filter: DateRangeFilter) {
        _selectedDateFilter.value = filter
    }

    fun stepMonthOffset(delta: Int) {
        _monthOffset.value = (_monthOffset.value + delta).coerceIn(-24, 12)
        _selectedDateFilter.value = DateRangeFilter.THIS_MONTH
    }

    fun resetToCurrentMonth() {
        _monthOffset.value = 0
    }

    fun setCategoryFilter(categoryId: String?) {
        _selectedCategoryFilter.value = if (_selectedCategoryFilter.value == categoryId) null else categoryId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun triggerRefreshPulse() {
        viewModelScope.launch {
            _isRefreshing.value = true
            kotlinx.coroutines.delay(420L)
            _isRefreshing.value = false
            _statusBanner.value = "LEDGER SYNCHRONIZED WITH CLOUD"
        }
    }

    // ------------------------------------------------------------------------
    // DERIVED TELEMETRY & FILTERING HELPERS
    // ------------------------------------------------------------------------

    fun currentMonthLabel(offset: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, offset)
        val fmt = SimpleDateFormat("MMMM yyyy", Locale.US)
        return fmt.format(cal.time).uppercase(Locale.US)
    }

    fun currentMonthKey(offset: Int = 0): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, offset)
        val fmt = SimpleDateFormat("yyyy-MM", Locale.US)
        return fmt.format(cal.time)
    }

    fun filterTransactions(
        allTransactions: List<MonoTransaction>,
        dateFilter: DateRangeFilter,
        monthOffset: Int,
        categoryFilter: String?,
        search: String
    ): List<MonoTransaction> {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            add(Calendar.MONTH, monthOffset)
        }
        val targetYear = cal.get(Calendar.YEAR)
        val targetMonth = cal.get(Calendar.MONTH)

        return allTransactions.filter { tx ->
            val matchesDate = when (dateFilter) {
                DateRangeFilter.THIS_MONTH -> {
                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestampMillis }
                    txCal.get(Calendar.YEAR) == targetYear && txCal.get(Calendar.MONTH) == targetMonth
                }
                DateRangeFilter.LAST_30_DAYS -> {
                    tx.timestampMillis >= (now - 30L * 24L * 3600L * 1000L)
                }
                DateRangeFilter.ALL_TIME -> true
            }
            val matchesCat = categoryFilter == null || tx.categoryId == categoryFilter
            val q = search.trim()
            val matchesSearch = q.isEmpty() ||
                tx.title.contains(q, ignoreCase = true) ||
                tx.categoryName.contains(q, ignoreCase = true) ||
                tx.receiptNote.contains(q, ignoreCase = true) ||
                tx.splitSummary.contains(q, ignoreCase = true)

            matchesDate && matchesCat && matchesSearch
        }
    }

    fun buildDailyChartPoints(
        filteredTransactions: List<MonoTransaction>,
        monthOffset: Int
    ): List<DailyChartPoint> {
        val cal = Calendar.getInstance().apply {
            add(Calendar.MONTH, monthOffset)
        }
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val monthShort = SimpleDateFormat("MMM", Locale.US).format(cal.time).uppercase(Locale.US)

        // Group into 7 clean buckets across the month for crisp interactive touch targets
        val bucketCount = 7
        val daysPerBucket = (maxDay.toFloat() / bucketCount).coerceAtLeast(1f)

        return (0 until bucketCount).map { idx ->
            val startDay = (idx * daysPerBucket).toInt() + 1
            val endDay = if (idx == bucketCount - 1) maxDay else ((idx + 1) * daysPerBucket).toInt()

            var exp = 0L
            var inc = 0L
            filteredTransactions.forEach { tx ->
                val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestampMillis }
                if (txCal.get(Calendar.YEAR) == year && txCal.get(Calendar.MONTH) == month) {
                    val d = txCal.get(Calendar.DAY_OF_MONTH)
                    if (d in startDay..endDay) {
                        if (tx.type == "INCOME") inc += tx.amountCents else exp += tx.amountCents
                    }
                } else {
                    // For LAST_30_DAYS or ALL_TIME fallback bucket mapping
                    val bucketIdx = (idx % bucketCount)
                    if (Math.abs((tx.timestampMillis / 86400000L).toInt()) % bucketCount == bucketIdx) {
                        if (tx.type == "INCOME") inc += tx.amountCents else exp += tx.amountCents
                    }
                }
            }

            DailyChartPoint(
                label = "$monthShort $startDay–$endDay",
                dayOfMonth = startDay,
                expenseCents = exp,
                incomeCents = inc,
                timestampMillis = System.currentTimeMillis()
            )
        }
    }

    fun buildCategoryBreakdown(
        filteredTransactions: List<MonoTransaction>,
        customBudgets: List<MonoCategoryBudget>
    ): List<CategoryBreakdownItem> {
        val expenseTxs = filteredTransactions.filter { it.type == "EXPENSE" }
        val totalExpense = expenseTxs.sumOf { it.amountCents }.coerceAtLeast(1L)
        val budgetByCat = customBudgets.associateBy { it.categoryId }

        return MonoCatalog.categories
            .filter { !it.isIncomeCategory }
            .map { cat ->
                val spent = expenseTxs.filter { it.categoryId == cat.id }.sumOf { it.amountCents }
                val limit = budgetByCat[cat.id]?.limitCents ?: cat.defaultLimitCents
                val pctOfTotal = if (spent == 0L) 0f else (spent.toFloat() / totalExpense.toFloat()).coerceIn(0f, 1f)
                val budgetProgress = if (limit <= 0L) 0f else (spent.toFloat() / limit.toFloat())
                CategoryBreakdownItem(
                    categoryId = cat.id,
                    categoryName = cat.name,
                    tagCode = cat.tagCode,
                    spentCents = spent,
                    budgetLimitCents = limit,
                    percentageOfTotalSpend = pctOfTotal,
                    budgetProgress = budgetProgress,
                    isOverBudget = limit > 0L && spent > limit
                )
            }
            .sortedByDescending { it.spentCents }
    }

    // ------------------------------------------------------------------------
    // CONFIRMED DATABASE WRITE ACTIONS (WITH HAPTIC & UI CALLBACKS)
    // ------------------------------------------------------------------------

    fun saveTransactionConfirmed(
        title: String,
        amountCents: Long,
        type: String,
        categoryId: String,
        categoryName: String,
        timestampMillis: Long = System.currentTimeMillis(),
        receiptNote: String = "",
        isSplit: Boolean = false,
        splitSummary: String = "",
        isRecurringInstance: Boolean = false,
        existingId: String? = null,
        currentMonthExpenseCents: Long = 0L,
        monthlyBudgetCents: Long = 350000L,
        onSuccessConfirmed: (exceededBudget: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        if (amountCents <= 0L) {
            onError("Enter an amount greater than 0")
            return
        }
        viewModelScope.launch {
            val result = repository.saveTransaction(
                title = title,
                amountCents = amountCents,
                type = type,
                categoryId = categoryId,
                categoryName = categoryName,
                timestampMillis = timestampMillis,
                receiptNote = receiptNote,
                isSplit = isSplit,
                splitSummary = splitSummary,
                isRecurringInstance = isRecurringInstance,
                existingId = existingId
            )
            result.fold(
                onSuccess = {
                    val newMonthlyExpense = if (type == "EXPENSE") {
                        currentMonthExpenseCents + amountCents
                    } else {
                        currentMonthExpenseCents
                    }
                    val exceeded = type == "EXPENSE" && monthlyBudgetCents > 0L && newMonthlyExpense > monthlyBudgetCents
                    _statusBanner.value = if (exceeded) {
                        "BUDGET ALERT // MONTHLY SPEND LIMIT EXCEEDED"
                    } else {
                        "LEDGER UPDATED // ${title.uppercase(Locale.US)}"
                    }
                    onSuccessConfirmed(exceeded)
                },
                onFailure = { err ->
                    onError(err.localizedMessage ?: "Failed to record transaction")
                }
            )
        }
    }

    fun duplicateTransaction(tx: MonoTransaction, onCompleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveTransaction(
                title = "${tx.title} (Copy)",
                amountCents = tx.amountCents,
                type = tx.type,
                categoryId = tx.categoryId,
                categoryName = tx.categoryName,
                timestampMillis = System.currentTimeMillis(),
                receiptNote = tx.receiptNote,
                isSplit = tx.isSplit,
                splitSummary = tx.splitSummary,
                isRecurringInstance = tx.isRecurringInstance
            ).onSuccess {
                _statusBanner.value = "DUPLICATED // ${tx.title.uppercase(Locale.US)}"
                onCompleted()
            }
        }
    }

    fun deleteTransaction(txId: String, title: String, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteTransaction(txId).onSuccess {
                _statusBanner.value = "DELETED // ${title.uppercase(Locale.US)}"
                onDeleted()
            }
        }
    }

    fun updateProfileSettings(
        currentProfile: MonoUserProfile,
        displayName: String = currentProfile.displayName,
        currencyCode: String = currentProfile.currencyCode,
        customCurrencySymbol: String = currentProfile.customCurrencySymbol,
        monthlyBudgetCents: Long = currentProfile.monthlyBudgetCents,
        reducedMotionEnabled: Boolean = currentProfile.reducedMotionEnabled,
        hapticsEnabled: Boolean = currentProfile.hapticsEnabled,
        onSaved: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val updated = currentProfile.copy(
                userId = userId,
                displayName = displayName,
                currencyCode = currencyCode,
                customCurrencySymbol = customCurrencySymbol,
                monthlyBudgetCents = monthlyBudgetCents,
                reducedMotionEnabled = reducedMotionEnabled,
                hapticsEnabled = hapticsEnabled
            )
            repository.ensureOrSaveProfile(updated).onSuccess {
                _statusBanner.value = "PREFERENCES SAVED"
                onSaved()
            }
        }
    }

    fun saveCategoryBudget(
        categoryId: String,
        categoryName: String,
        limitCents: Long,
        onSaved: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.upsertCategoryBudget(
                categoryId = categoryId,
                categoryName = categoryName,
                limitCents = limitCents
            ).onSuccess {
                _statusBanner.value = "BUDGET UPDATED // ${categoryName.uppercase(Locale.US)}"
                onSaved()
            }
        }
    }

    fun createOrUpdateSavingsGoal(
        title: String,
        targetCents: Long,
        savedCents: Long,
        existingId: String? = null,
        onGoalCompleted: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.saveSavingsGoal(
                title = title,
                targetCents = targetCents,
                savedCents = savedCents,
                existingId = existingId
            ).onSuccess {
                val reached = savedCents >= targetCents
                _statusBanner.value = if (reached) {
                    "MILESTONE UNLOCKED // ${title.uppercase(Locale.US)} 100% FUNDED"
                } else {
                    "VAULT UPDATED // ${title.uppercase(Locale.US)}"
                }
                onGoalCompleted(reached)
            }
        }
    }

    fun deleteSavingsGoal(goalId: String, title: String) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goalId).onSuccess {
                _statusBanner.value = "VAULT REMOVED // ${title.uppercase(Locale.US)}"
            }
        }
    }

    fun createOrUpdateRecurringBill(
        title: String,
        amountCents: Long,
        categoryId: String,
        cadence: String,
        nextDueDayOfMonth: Long,
        existingId: String? = null,
        onSaved: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.saveRecurringBill(
                title = title,
                amountCents = amountCents,
                categoryId = categoryId,
                cadence = cadence,
                nextDueDayOfMonth = nextDueDayOfMonth,
                existingId = existingId
            ).onSuccess {
                _statusBanner.value = "RECURRING BILL SAVED // ${title.uppercase(Locale.US)}"
                onSaved()
            }
        }
    }

    fun markRecurringBillPaid(
        bill: MonoRecurringBill,
        onConfirmed: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val monthKey = currentMonthKey(0)
            val catName = MonoCatalog.categories.firstOrNull { it.id == bill.categoryId }?.name ?: "Subscriptions"
            repository.saveRecurringBill(
                title = bill.title,
                amountCents = bill.amountCents,
                categoryId = bill.categoryId,
                cadence = bill.cadence,
                nextDueDayOfMonth = bill.nextDueDayOfMonth,
                isAutoRemind = bill.isAutoRemind,
                lastPaidMonthKey = monthKey,
                existingId = bill.id
            )
            repository.saveTransaction(
                title = "${bill.title} (${bill.cadence})",
                amountCents = bill.amountCents,
                type = "EXPENSE",
                categoryId = bill.categoryId,
                categoryName = catName,
                receiptNote = "Recurring bill settled for $monthKey",
                isRecurringInstance = true
            ).onSuccess {
                _statusBanner.value = "BILL PAID & RECORDED // ${bill.title.uppercase(Locale.US)}"
                onConfirmed()
            }
        }
    }

    fun deleteRecurringBill(billId: String, title: String) {
        viewModelScope.launch {
            repository.deleteRecurringBill(billId).onSuccess {
                _statusBanner.value = "BILL REMOVED // ${title.uppercase(Locale.US)}"
            }
        }
    }

    fun generateBackupJson(
        profile: MonoUserProfile,
        transactions: List<MonoTransaction>,
        budgets: List<MonoCategoryBudget>,
        goals: List<MonoSavingsGoal>,
        bills: List<MonoRecurringBill>
    ): String {
        return MonoBackupSerializer.exportToJson(
            profile = profile,
            transactions = transactions,
            budgets = budgets,
            goals = goals,
            bills = bills
        )
    }

    fun restoreFromBackupJson(
        jsonPayload: String,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            repository.restoreFromBackupJson(jsonPayload).fold(
                onSuccess = { count ->
                    _statusBanner.value = "BACKUP RESTORED // $count RECORDS SYNCHRONIZED"
                    onSuccess(count)
                },
                onFailure = { e ->
                    onError(e.localizedMessage ?: "Invalid backup JSON format")
                }
            )
        }
    }
}
