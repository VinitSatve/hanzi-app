package com.example.data.repository

import com.example.data.model.MonoCategoryBudget
import com.example.data.model.MonoRecurringBill
import com.example.data.model.MonoSavingsGoal
import com.example.data.model.MonoTransaction
import com.example.data.model.MonoUserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.util.UUID

class MonoRepository(private val db: FirebaseFirestore) {

    private val auth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw FirebaseFirestoreException(
                "User must be signed in with Google before accessing MONO Firestore.",
                FirebaseFirestoreException.Code.PERMISSION_DENIED
            )
    }

    private fun sanitizeId(rawId: String): String {
        val cleaned = rawId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        return cleaned.ifBlank { UUID.randomUUID().toString() }.take(96)
    }

    // ------------------------------------------------------------------------
    // 1. USER PROFILE & PREFERENCES (/users/{userId}/profile/settings)
    // ------------------------------------------------------------------------

    fun observeUserProfile(userId: String): Flow<MonoUserProfile?> = flow {
        val path = "users/$userId/profile"
        val query = db.collection("users")
            .document(userId)
            .collection("profile")
            .whereEqualTo("userId", userId)

        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.documents.firstOrNull()?.toObject(MonoUserProfile::class.java)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception) ?: Exception(error)
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun ensureOrSaveProfile(profile: MonoUserProfile): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("profile").document("settings")
        try {
            val existingSnap = docRef.get().await()
            val validCurrency = if (profile.currencyCode in listOf("USD", "EUR", "GBP", "JPY", "INR", "KRW", "CHF", "CUSTOM")) {
                profile.currencyCode
            } else {
                "USD"
            }
            val validSymbol = profile.customCurrencySymbol.trim().ifBlank { "$" }.take(8)
            val validName = profile.displayName.trim().ifBlank { "MONO Member" }.take(80)
            val validBudget = profile.monthlyBudgetCents.coerceIn(0L, 100000000000L)

            if (!existingSnap.exists()) {
                val createMap = mapOf(
                    "userId" to uid,
                    "displayName" to validName,
                    "currencyCode" to validCurrency,
                    "customCurrencySymbol" to validSymbol,
                    "monthlyBudgetCents" to validBudget,
                    "reducedMotionEnabled" to profile.reducedMotionEnabled,
                    "hapticsEnabled" to profile.hapticsEnabled,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createMap).await()
            } else {
                val updateMap = mapOf(
                    "displayName" to validName,
                    "currencyCode" to validCurrency,
                    "customCurrencySymbol" to validSymbol,
                    "monthlyBudgetCents" to validBudget,
                    "reducedMotionEnabled" to profile.reducedMotionEnabled,
                    "hapticsEnabled" to profile.hapticsEnabled,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updateMap).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // ------------------------------------------------------------------------
    // 2. TRANSACTIONS (/users/{userId}/transactions/{transactionId})
    // ------------------------------------------------------------------------

    fun observeTransactions(userId: String): Flow<List<MonoTransaction>> = flow {
        val path = "users/$userId/transactions"
        val query = db.collection("users")
            .document(userId)
            .collection("transactions")
            .whereEqualTo("userId", userId)

        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(MonoTransaction::class.java)
                        .sortedByDescending { it.timestampMillis }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception) ?: Exception(error)
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun getTransactionById(ownerId: String, transactionId: String): Result<MonoTransaction> = runCatching {
        val docRef = db.collection("users").document(ownerId).collection("transactions").document(transactionId)
        try {
            val snap = docRef.get().await()
            snap.toObject(MonoTransaction::class.java)
                ?: throw IllegalStateException("Transaction not found")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun saveTransaction(
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
        existingId: String? = null
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val txId = if (!existingId.isNullOrBlank()) sanitizeId(existingId) else "tx_${UUID.randomUUID()}"
        val docRef = db.collection("users").document(uid).collection("transactions").document(txId)

        val safeTitle = title.trim().ifBlank { categoryName.ifBlank { "Transaction" } }.take(120)
        val safeAmount = amountCents.coerceIn(1L, 100000000000L)
        val safeType = if (type == "INCOME") "INCOME" else "EXPENSE"
        val safeCatId = categoryId.trim().ifBlank { "dining" }.take(64)
        val safeCatName = categoryName.trim().ifBlank { "Dining & Coffee" }.take(64)
        val safeTs = if (timestampMillis > 0L) timestampMillis else System.currentTimeMillis()
        val safeNote = receiptNote.trim().take(500)
        val safeSplit = splitSummary.trim().take(500)

        try {
            if (!existingId.isNullOrBlank()) {
                val updatePayload = mapOf(
                    "title" to safeTitle,
                    "amountCents" to safeAmount,
                    "type" to safeType,
                    "categoryId" to safeCatId,
                    "categoryName" to safeCatName,
                    "timestampMillis" to safeTs,
                    "receiptNote" to safeNote,
                    "isSplit" to isSplit,
                    "splitSummary" to safeSplit,
                    "isRecurringInstance" to isRecurringInstance,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            } else {
                val createPayload = mapOf(
                    "userId" to uid,
                    "title" to safeTitle,
                    "amountCents" to safeAmount,
                    "type" to safeType,
                    "categoryId" to safeCatId,
                    "categoryName" to safeCatName,
                    "timestampMillis" to safeTs,
                    "receiptNote" to safeNote,
                    "isSplit" to isSplit,
                    "splitSummary" to safeSplit,
                    "isRecurringInstance" to isRecurringInstance,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            }
            txId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteTransaction(transactionId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("transactions").document(sanitizeId(transactionId))
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // ------------------------------------------------------------------------
    // 3. CATEGORY BUDGETS (/users/{userId}/budgets/{budgetId})
    // ------------------------------------------------------------------------

    fun observeBudgets(userId: String): Flow<List<MonoCategoryBudget>> = flow {
        val path = "users/$userId/budgets"
        val query = db.collection("users")
            .document(userId)
            .collection("budgets")
            .whereEqualTo("userId", userId)

        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(MonoCategoryBudget::class.java)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception) ?: Exception(error)
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun upsertCategoryBudget(
        categoryId: String,
        categoryName: String,
        limitCents: Long,
        alertThresholdPercent: Long = 80L
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = sanitizeId(categoryId)
        val docRef = db.collection("users").document(uid).collection("budgets").document(docId)
        val safeCatId = categoryId.trim().ifBlank { "dining" }.take(64)
        val safeCatName = categoryName.trim().ifBlank { "Dining" }.take(64)
        val safeLimit = limitCents.coerceIn(100L, 100000000000L)
        val safeThreshold = alertThresholdPercent.coerceIn(10L, 100L)

        try {
            val snap = docRef.get().await()
            if (!snap.exists()) {
                val createMap = mapOf(
                    "userId" to uid,
                    "categoryId" to safeCatId,
                    "categoryName" to safeCatName,
                    "limitCents" to safeLimit,
                    "alertThresholdPercent" to safeThreshold,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createMap).await()
            } else {
                val updateMap = mapOf(
                    "categoryId" to safeCatId,
                    "categoryName" to safeCatName,
                    "limitCents" to safeLimit,
                    "alertThresholdPercent" to safeThreshold,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updateMap).await()
            }
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // ------------------------------------------------------------------------
    // 4. SAVINGS GOALS (/users/{userId}/goals/{goalId})
    // ------------------------------------------------------------------------

    fun observeSavingsGoals(userId: String): Flow<List<MonoSavingsGoal>> = flow {
        val path = "users/$userId/goals"
        val query = db.collection("users")
            .document(userId)
            .collection("goals")
            .whereEqualTo("userId", userId)

        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(MonoSavingsGoal::class.java)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception) ?: Exception(error)
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveSavingsGoal(
        title: String,
        targetCents: Long,
        savedCents: Long,
        deadlineMillis: Long = System.currentTimeMillis() + 7776000000L,
        accentHex: String = "#D7FF00",
        existingId: String? = null
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val goalId = if (!existingId.isNullOrBlank()) sanitizeId(existingId) else "goal_${UUID.randomUUID()}"
        val docRef = db.collection("users").document(uid).collection("goals").document(goalId)

        val safeTitle = title.trim().ifBlank { "Vault Goal" }.take(100)
        val safeTarget = targetCents.coerceIn(100L, 100000000000L)
        val safeSaved = savedCents.coerceIn(0L, 100000000000L)
        val safeDeadline = deadlineMillis.coerceAtLeast(0L)
        val safeAccent = accentHex.trim().ifBlank { "#D7FF00" }.take(12)
        val isCompleted = safeSaved >= safeTarget

        try {
            if (!existingId.isNullOrBlank()) {
                val updateMap = mapOf(
                    "title" to safeTitle,
                    "targetCents" to safeTarget,
                    "savedCents" to safeSaved,
                    "deadlineMillis" to safeDeadline,
                    "accentHex" to safeAccent,
                    "isCompleted" to isCompleted,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updateMap).await()
            } else {
                val createMap = mapOf(
                    "userId" to uid,
                    "title" to safeTitle,
                    "targetCents" to safeTarget,
                    "savedCents" to safeSaved,
                    "deadlineMillis" to safeDeadline,
                    "accentHex" to safeAccent,
                    "isCompleted" to isCompleted,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createMap).await()
            }
            goalId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteSavingsGoal(goalId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("goals").document(sanitizeId(goalId))
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // ------------------------------------------------------------------------
    // 5. RECURRING BILLS (/users/{userId}/bills/{billId})
    // ------------------------------------------------------------------------

    fun observeRecurringBills(userId: String): Flow<List<MonoRecurringBill>> = flow {
        val path = "users/$userId/bills"
        val query = db.collection("users")
            .document(userId)
            .collection("bills")
            .whereEqualTo("userId", userId)

        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(MonoRecurringBill::class.java)
                        .sortedBy { it.nextDueDayOfMonth }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception) ?: Exception(error)
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveRecurringBill(
        title: String,
        amountCents: Long,
        categoryId: String,
        cadence: String = "MONTHLY",
        nextDueDayOfMonth: Long = 15L,
        isAutoRemind: Boolean = true,
        lastPaidMonthKey: String = "",
        existingId: String? = null
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val billId = if (!existingId.isNullOrBlank()) sanitizeId(existingId) else "bill_${UUID.randomUUID()}"
        val docRef = db.collection("users").document(uid).collection("bills").document(billId)

        val safeTitle = title.trim().ifBlank { "Subscription" }.take(100)
        val safeAmount = amountCents.coerceIn(1L, 100000000000L)
        val safeCatId = categoryId.trim().ifBlank { "subscriptions" }.take(64)
        val safeCadence = if (cadence in listOf("MONTHLY", "WEEKLY", "YEARLY")) cadence else "MONTHLY"
        val safeDueDay = nextDueDayOfMonth.coerceIn(1L, 31L)
        val safePaidKey = lastPaidMonthKey.trim().take(16)

        try {
            if (!existingId.isNullOrBlank()) {
                val updateMap = mapOf(
                    "title" to safeTitle,
                    "amountCents" to safeAmount,
                    "categoryId" to safeCatId,
                    "cadence" to safeCadence,
                    "nextDueDayOfMonth" to safeDueDay,
                    "isAutoRemind" to isAutoRemind,
                    "lastPaidMonthKey" to safePaidKey,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updateMap).await()
            } else {
                val createMap = mapOf(
                    "userId" to uid,
                    "title" to safeTitle,
                    "amountCents" to safeAmount,
                    "categoryId" to safeCatId,
                    "cadence" to safeCadence,
                    "nextDueDayOfMonth" to safeDueDay,
                    "isAutoRemind" to isAutoRemind,
                    "lastPaidMonthKey" to safePaidKey,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createMap).await()
            }
            billId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteRecurringBill(billId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("bills").document(sanitizeId(billId))
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // ------------------------------------------------------------------------
    // 6. RESTORE FROM BACKUP JSON
    // ------------------------------------------------------------------------

    suspend fun restoreFromBackupJson(jsonPayload: String): Result<Int> = runCatching {
        val uid = requireUserId()
        val root = JSONObject(jsonPayload)
        var restoredCount = 0

        root.optJSONObject("profile")?.let { p ->
            val profile = MonoUserProfile(
                userId = uid,
                displayName = p.optString("displayName", "MONO Member"),
                currencyCode = p.optString("currencyCode", "USD"),
                customCurrencySymbol = p.optString("customCurrencySymbol", "$"),
                monthlyBudgetCents = p.optLong("monthlyBudgetCents", 350000L),
                reducedMotionEnabled = p.optBoolean("reducedMotionEnabled", false),
                hapticsEnabled = p.optBoolean("hapticsEnabled", true)
            )
            ensureOrSaveProfile(profile).getOrThrow()
            restoredCount++
        }

        root.optJSONArray("transactions")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                saveTransaction(
                    title = obj.optString("title", "Restored Entry"),
                    amountCents = obj.optLong("amountCents", 1000L),
                    type = obj.optString("type", "EXPENSE"),
                    categoryId = obj.optString("categoryId", "dining"),
                    categoryName = obj.optString("categoryName", "Dining & Coffee"),
                    timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis()),
                    receiptNote = obj.optString("receiptNote", ""),
                    isSplit = obj.optBoolean("isSplit", false),
                    splitSummary = obj.optString("splitSummary", ""),
                    isRecurringInstance = obj.optBoolean("isRecurringInstance", false)
                ).getOrThrow()
                restoredCount++
            }
        }

        root.optJSONArray("budgets")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                upsertCategoryBudget(
                    categoryId = obj.optString("categoryId", "dining"),
                    categoryName = obj.optString("categoryName", "Dining & Coffee"),
                    limitCents = obj.optLong("limitCents", 50000L),
                    alertThresholdPercent = obj.optLong("alertThresholdPercent", 80L)
                ).getOrThrow()
                restoredCount++
            }
        }

        root.optJSONArray("goals")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                saveSavingsGoal(
                    title = obj.optString("title", "Savings Vault"),
                    targetCents = obj.optLong("targetCents", 100000L),
                    savedCents = obj.optLong("savedCents", 0L),
                    deadlineMillis = obj.optLong("deadlineMillis", System.currentTimeMillis() + 7776000000L),
                    accentHex = obj.optString("accentHex", "#D7FF00")
                ).getOrThrow()
                restoredCount++
            }
        }

        root.optJSONArray("bills")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                saveRecurringBill(
                    title = obj.optString("title", "Subscription"),
                    amountCents = obj.optLong("amountCents", 1500L),
                    categoryId = obj.optString("categoryId", "subscriptions"),
                    cadence = obj.optString("cadence", "MONTHLY"),
                    nextDueDayOfMonth = obj.optLong("nextDueDayOfMonth", 15L),
                    isAutoRemind = obj.optBoolean("isAutoRemind", true),
                    lastPaidMonthKey = obj.optString("lastPaidMonthKey", "")
                ).getOrThrow()
                restoredCount++
            }
        }

        restoredCount
    }
}
