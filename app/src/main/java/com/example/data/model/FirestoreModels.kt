package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@IgnoreExtraProperties
data class MonoUserProfile(
    @DocumentId val id: String = "settings",
    val userId: String = "",
    val displayName: String = "MONO Member",
    val currencyCode: String = "USD",
    val customCurrencySymbol: String = "$",
    val monthlyBudgetCents: Long = 350000L, // $3,500.00 default
    val reducedMotionEnabled: Boolean = false,
    val hapticsEnabled: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MonoTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amountCents: Long = 0L,
    val type: String = "EXPENSE", // "EXPENSE" | "INCOME"
    val categoryId: String = "dining",
    val categoryName: String = "Dining & Coffee",
    val timestampMillis: Long = System.currentTimeMillis(),
    val receiptNote: String = "",
    val isSplit: Boolean = false,
    val splitSummary: String = "",
    val isRecurringInstance: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MonoCategoryBudget(
    @DocumentId val id: String = "",
    val userId: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val limitCents: Long = 50000L,
    val alertThresholdPercent: Long = 80L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MonoSavingsGoal(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val targetCents: Long = 100000L,
    val savedCents: Long = 0L,
    val deadlineMillis: Long = 0L,
    val accentHex: String = "#D7FF00",
    val isCompleted: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MonoRecurringBill(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amountCents: Long = 1500L,
    val categoryId: String = "subscriptions",
    val cadence: String = "MONTHLY", // "MONTHLY" | "WEEKLY" | "YEARLY"
    val nextDueDayOfMonth: Long = 15L,
    val isAutoRemind: Boolean = true,
    val lastPaidMonthKey: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class MonoCategoryOption(
    val id: String,
    val name: String,
    val tagCode: String,
    val isIncomeCategory: Boolean = false,
    val defaultLimitCents: Long = 60000L
)

data class CurrencyPreset(
    val code: String,
    val symbol: String,
    val label: String,
    val zeroDecimal: Boolean = false
)

object MonoCatalog {
    val currencies = listOf(
        CurrencyPreset("USD", "$", "US Dollar ($)"),
        CurrencyPreset("EUR", "€", "Euro (€)"),
        CurrencyPreset("GBP", "£", "British Pound (£)"),
        CurrencyPreset("JPY", "¥", "Japanese Yen (¥)", zeroDecimal = true),
        CurrencyPreset("INR", "₹", "Indian Rupee (₹)"),
        CurrencyPreset("KRW", "₩", "Korean Won (₩)", zeroDecimal = true),
        CurrencyPreset("CHF", "CHF", "Swiss Franc (CHF)"),
        CurrencyPreset("CUSTOM", "¤", "Custom Symbol")
    )

    val categories = listOf(
        MonoCategoryOption("dining", "Dining & Coffee", "DIN", false, 60000L),
        MonoCategoryOption("groceries", "Groceries & Market", "GRO", false, 80000L),
        MonoCategoryOption("housing", "Housing & Studio", "HOU", false, 180000L),
        MonoCategoryOption("transport", "Transit & Mobility", "TRN", false, 40000L),
        MonoCategoryOption("subscriptions", "Digital & SaaS", "SUB", false, 25000L),
        MonoCategoryOption("shopping", "Gear & Apparel", "SHP", false, 50000L),
        MonoCategoryOption("wellness", "Health & Training", "FIT", false, 35000L),
        MonoCategoryOption("entertainment", "Culture & Nightlife", "ENT", false, 30000L),
        MonoCategoryOption("salary", "Primary Salary", "SAL", true, 0L),
        MonoCategoryOption("freelance", "Design & Advisory", "FRL", true, 0L),
        MonoCategoryOption("dividends", "Yield & Returns", "YLD", true, 0L)
    )

    fun resolveSymbol(profile: MonoUserProfile): String {
        if (profile.currencyCode == "CUSTOM") {
            return profile.customCurrencySymbol.ifBlank { "$" }
        }
        return currencies.firstOrNull { it.code == profile.currencyCode }?.symbol ?: "$"
    }

    fun isZeroDecimal(profile: MonoUserProfile): Boolean {
        return currencies.firstOrNull { it.code == profile.currencyCode }?.zeroDecimal == true
    }

    fun formatMoney(
        cents: Long,
        profile: MonoUserProfile,
        showPlusForPositive: Boolean = false
    ): String {
        val symbol = resolveSymbol(profile)
        val sign = when {
            cents < 0 -> "-"
            cents > 0 && showPlusForPositive -> "+"
            else -> ""
        }
        val absCents = abs(cents)
        return if (isZeroDecimal(profile)) {
            val whole = absCents / 100L
            val formatted = NumberFormat.getIntegerInstance(Locale.US).format(whole)
            "$sign$symbol$formatted"
        } else {
            val whole = absCents / 100L
            val rem = absCents % 100L
            val formattedWhole = NumberFormat.getIntegerInstance(Locale.US).format(whole)
            String.format(Locale.US, "%s%s%s.%02d", sign, symbol, formattedWhole, rem)
        }
    }
}

object MonoBackupSerializer {
    fun exportToJson(
        profile: MonoUserProfile,
        transactions: List<MonoTransaction>,
        budgets: List<MonoCategoryBudget>,
        goals: List<MonoSavingsGoal>,
        bills: List<MonoRecurringBill>
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAtMillis", System.currentTimeMillis())

        val profileObj = JSONObject().apply {
            put("displayName", profile.displayName)
            put("currencyCode", profile.currencyCode)
            put("customCurrencySymbol", profile.customCurrencySymbol)
            put("monthlyBudgetCents", profile.monthlyBudgetCents)
            put("reducedMotionEnabled", profile.reducedMotionEnabled)
            put("hapticsEnabled", profile.hapticsEnabled)
        }
        root.put("profile", profileObj)

        val txArray = JSONArray()
        transactions.forEach { tx ->
            txArray.put(
                JSONObject().apply {
                    put("id", tx.id)
                    put("title", tx.title)
                    put("amountCents", tx.amountCents)
                    put("type", tx.type)
                    put("categoryId", tx.categoryId)
                    put("categoryName", tx.categoryName)
                    put("timestampMillis", tx.timestampMillis)
                    put("receiptNote", tx.receiptNote)
                    put("isSplit", tx.isSplit)
                    put("splitSummary", tx.splitSummary)
                    put("isRecurringInstance", tx.isRecurringInstance)
                }
            )
        }
        root.put("transactions", txArray)

        val budgetArray = JSONArray()
        budgets.forEach { b ->
            budgetArray.put(
                JSONObject().apply {
                    put("id", b.id)
                    put("categoryId", b.categoryId)
                    put("categoryName", b.categoryName)
                    put("limitCents", b.limitCents)
                    put("alertThresholdPercent", b.alertThresholdPercent)
                }
            )
        }
        root.put("budgets", budgetArray)

        val goalArray = JSONArray()
        goals.forEach { g ->
            goalArray.put(
                JSONObject().apply {
                    put("id", g.id)
                    put("title", g.title)
                    put("targetCents", g.targetCents)
                    put("savedCents", g.savedCents)
                    put("deadlineMillis", g.deadlineMillis)
                    put("accentHex", g.accentHex)
                    put("isCompleted", g.isCompleted)
                }
            )
        }
        root.put("goals", goalArray)

        val billArray = JSONArray()
        bills.forEach { bill ->
            billArray.put(
                JSONObject().apply {
                    put("id", bill.id)
                    put("title", bill.title)
                    put("amountCents", bill.amountCents)
                    put("categoryId", bill.categoryId)
                    put("cadence", bill.cadence)
                    put("nextDueDayOfMonth", bill.nextDueDayOfMonth)
                    put("isAutoRemind", bill.isAutoRemind)
                    put("lastPaidMonthKey", bill.lastPaidMonthKey)
                }
            )
        }
        root.put("bills", billArray)

        return root.toString(2)
    }
}
