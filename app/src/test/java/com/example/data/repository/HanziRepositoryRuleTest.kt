package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HanziRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun getOrCreateProfile_authenticatedOwner_createsAndReturnsValidProfile() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = HanziRepository(firestore)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getOrCreateProfile("Alice Chen")
        }
        assertTrue("Profile creation should succeed: ${result.exceptionOrNull()}", result.isSuccess)
        val profile = result.getOrThrow()
        assertEquals(uid, profile.userId)
        assertEquals("Alice Chen", profile.displayName)
    }

    @Test
    fun createAndObserveFlashcard_authenticatedOwner_emitsRealtimeFlashcard() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = HanziRepository(firestore)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createFlashcard(
                simplified = "学习",
                traditional = "學習",
                pinyin = "xuéxí",
                english = "To study; to learn",
                partOfSpeech = "Verb",
                exampleChinese = "我每天学习汉语。",
                examplePinyin = "Wǒ měitiān xuéxí Hànyǔ.",
                exampleEnglish = "I study Chinese every day.",
                hskLevel = 1L,
                category = "Education"
            )
        }
        assertTrue(createResult.isSuccess)
        val cardId = createResult.getOrThrow()

        val cards = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeFlashcards(uid).first { list -> list.any { it.id == cardId } }
        }
        assertTrue(cards.any { it.id == cardId && it.simplified == "学习" })
    }

    @Test
    fun createAndObserveNote_authenticatedOwner_emitsRealtimeNote() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = HanziRepository(firestore)

        val noteResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createNote(
                title = "Tone Sandhi Rule",
                chineseSentence = "你好",
                pinyin = "nǐ hǎo -> ní hǎo",
                englishTranslation = "Hello",
                grammarNotes = "Two 3rd tones in a row: first becomes 2nd tone.",
                folder = "Pinyin"
            )
        }
        assertTrue(noteResult.isSuccess)
        val noteId = noteResult.getOrThrow()

        val notes = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeNotes(uid).first { list -> list.any { it.id == noteId } }
        }
        assertTrue(notes.any { it.id == noteId && it.title == "Tone Sandhi Rule" })
    }

    @Test
    fun getProfileByUserId_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = HanziRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.getOrCreateProfile("Alice Chen").getOrThrow() }

        signInTestUser(BOB_EMAIL)
        val bobRepo = HanziRepository(firestore)
        val crossResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getProfileByUserId(aliceUid)
        }
        assertTrue(crossResult.isFailure)
        val firestoreEx = crossResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun observeFlashcards_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = HanziRepository(firestore)

        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeFlashcards(aliceUid).first()
            }
        } catch (e: Throwable) {
            caught = e
        }
        assertNotNull("Expected exception when observing flashcards unauthenticated", caught)
        val firestoreEx = caught?.unwrapFirestoreException()
        assertNotNull("Expected nested FirebaseFirestoreException, got $caught", firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice.chen@hanzi.test"
        const val BOB_EMAIL = "bob.chen@hanzi.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
