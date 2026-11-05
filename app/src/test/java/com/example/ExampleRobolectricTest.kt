package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.MonoBackupSerializer
import com.example.data.model.MonoCatalog
import com.example.data.model.MonoTransaction
import com.example.data.model.MonoUserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    @Test
    fun `read MONO app_name and format financial amounts`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MONO", appName)

        val usdProfile = MonoUserProfile(currencyCode = "USD", customCurrencySymbol = "$")
        assertEquals("+$1,250.50", MonoCatalog.formatMoney(125050L, usdProfile, showPlusForPositive = true))

        val json = MonoBackupSerializer.exportToJson(
            profile = usdProfile,
            transactions = listOf(MonoTransaction(id = "tx_1", title = "Coffee", amountCents = 650L)),
            budgets = emptyList(),
            goals = emptyList(),
            bills = emptyList()
        )
        assertTrue(json.contains("Coffee"))
    }
}
