package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.FlashcardItem
import com.example.data.model.PersonalNote
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class HanziRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        val path = "users/$userId"
        return db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null) {
                    handleFirestoreError(exceptionToLog, OperationType.GET, path)
                }
                throw error
            }
    }

    suspend fun getOrCreateProfile(displayName: String = "Mandarin Scholar"): Result<UserProfile> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                val existing = snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                if (existing != null) {
                    return Result.success(existing)
                }
            }
            val payload = mapOf(
                "userId" to uid,
                "displayName" to displayName.take(80).ifBlank { "Mandarin Scholar" },
                "scriptMode" to "SIMPLIFIED",
                "hskStandard" to "HSK_2",
                "currentHskLevel" to 1L,
                "dailyGoalMinutes" to 20L,
                "studyMinutesToday" to 12L,
                "totalStudyMinutes" to 45L,
                "streakDays" to 3L,
                "points" to 150L,
                "completedLessonIds" to listOf("pinyin_initials", "hsk1_greetings"),
                "masteredCharacterIds" to listOf("char_ni", "char_hao", "char_wo"),
                "unlockedBadgeIds" to listOf("badge_first_step", "badge_pinyin_starter"),
                "downloadedPackIds" to listOf("pack_pinyin", "pack_hsk1"),
                "listeningAccuracy" to 90L,
                "readingAccuracy" to 92L,
                "speakingAccuracy" to 86L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            val createdSnap = docRef.get().await()
            val created = createdSnap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: UserProfile(id = uid, userId = uid, displayName = displayName)
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getProfileByUserId(targetUserId: String): Result<UserProfile?> {
        val docRef = db.collection("users").document(targetUserId)
        return try {
            val snap = docRef.get().await()
            Result.success(
                snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateProfile(profile: UserProfile): Result<Unit> {
        val uid = requireUserId()
        requireNotNull(profile.createdAt) { "UserProfile createdAt must not be null for update" }
        val docRef = db.collection("users").document(uid)
        return try {
            docRef.set(profile.copy(userId = uid, updatedAt = null)).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeFlashcards(userId: String): Flow<List<FlashcardItem>> {
        val path = "users/$userId/flashcards"
        return db.collection("users").document(userId).collection("flashcards")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(FlashcardItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun createFlashcard(
        simplified: String,
        traditional: String,
        pinyin: String,
        english: String,
        partOfSpeech: String = "Noun",
        exampleChinese: String = "",
        examplePinyin: String = "",
        exampleEnglish: String = "",
        hskLevel: Long = 1L,
        category: String = "Core",
        nextReviewEpochDay: Long = System.currentTimeMillis() / 86_400_000L
    ): Result<String> {
        val uid = requireUserId()
        val cardId = "card_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid).collection("flashcards").document(cardId)
        val payload = mapOf(
            "userId" to uid,
            "simplified" to simplified.take(50).ifBlank { "字" },
            "traditional" to traditional.take(50).ifBlank { simplified.take(50).ifBlank { "字" } },
            "pinyin" to pinyin.take(100).ifBlank { "zì" },
            "english" to english.take(300).ifBlank { "Character" },
            "partOfSpeech" to partOfSpeech.take(50).ifBlank { "Noun" },
            "exampleChinese" to exampleChinese.take(300),
            "examplePinyin" to examplePinyin.take(300),
            "exampleEnglish" to exampleEnglish.take(300),
            "hskLevel" to hskLevel.coerceIn(1L, 6L),
            "category" to category.take(60).ifBlank { "Core" },
            "status" to "LEARNING",
            "intervalDays" to 1L,
            "easeFactor" to 250L,
            "repetitions" to 0L,
            "nextReviewEpochDay" to nextReviewEpochDay.coerceIn(0L, 100000L),
            "isFavorite" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(cardId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateFlashcard(card: FlashcardItem): Result<Unit> {
        val uid = requireUserId()
        require(card.id.isNotBlank()) { "Flashcard ID must not be blank" }
        requireNotNull(card.createdAt) { "Flashcard createdAt must not be null for update" }
        val docRef = db.collection("users").document(uid).collection("flashcards").document(card.id)
        return try {
            docRef.set(card.copy(userId = uid, updatedAt = null)).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeNotes(userId: String): Flow<List<PersonalNote>> {
        val path = "users/$userId/notes"
        return db.collection("users").document(userId).collection("notes")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(PersonalNote::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun createNote(
        title: String,
        chineseSentence: String,
        pinyin: String,
        englishTranslation: String,
        grammarNotes: String,
        folder: String = "Grammar"
    ): Result<String> {
        val uid = requireUserId()
        val noteId = "note_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid).collection("notes").document(noteId)
        val payload = mapOf(
            "userId" to uid,
            "title" to title.take(120).ifBlank { "Study Note" },
            "chineseSentence" to chineseSentence.take(500).ifBlank { "学习汉语" },
            "pinyin" to pinyin.take(500),
            "englishTranslation" to englishTranslation.take(500),
            "grammarNotes" to grammarNotes.take(2000),
            "folder" to folder.take(60).ifBlank { "General" },
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(noteId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteNote(noteId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("notes").document(noteId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
