package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MonoRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveAndObserveTransactions_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = MonoRepository(firestore)

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveTransaction(
                title = "Architectural Espresso Bar",
                amountCents = 850L,
                type = "EXPENSE",
                categoryId = "dining",
                categoryName = "Dining & Coffee",
                receiptNote = "Double shot + oat",
                isSplit = true,
                splitSummary = "Alex 50% / Maya 50%"
            )
        }
        assertTrue(saveResult.isSuccess)
        val createdId = saveResult.getOrThrow()

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeTransactions(aliceUid).first { list -> list.any { it.id == createdId } }
        }
        assertTrue(observed.any { it.id == createdId && it.amountCents == 850L })
    }

    @Test
    fun getTransactionById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = MonoRepository(firestore)
        val txId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveTransaction(
                title = "Private Advisory Retainer",
                amountCents = 420000L,
                type = "INCOME",
                categoryId = "freelance",
                categoryName = "Design & Advisory"
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = MonoRepository(firestore)
        val crossUserResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getTransactionById(aliceUid, txId)
        }
        assertTrue(crossUserResult.isFailure)
        val firestoreEx = crossUserResult.exceptionOrNull()?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun observeTransactions_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repo = MonoRepository(firestore)

        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeTransactions(aliceUid).first()
            }
        } catch (t: Throwable) {
            caught = t
        }
        val firestoreEx = caught?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_mono@test.com"
        const val BOB_EMAIL = "bob_mono@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
