package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Mandarin Scholar",
    val scriptMode: String = "SIMPLIFIED",
    val hskStandard: String = "HSK_2",
    val currentHskLevel: Long = 1L,
    val dailyGoalMinutes: Long = 20L,
    val studyMinutesToday: Long = 0L,
    val totalStudyMinutes: Long = 0L,
    val streakDays: Long = 1L,
    val points: Long = 0L,
    val completedLessonIds: List<String> = emptyList(),
    val masteredCharacterIds: List<String> = emptyList(),
    val unlockedBadgeIds: List<String> = emptyList(),
    val downloadedPackIds: List<String> = listOf("pack_pinyin", "pack_hsk1"),
    val listeningAccuracy: Long = 88L,
    val readingAccuracy: Long = 90L,
    val speakingAccuracy: Long = 85L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FlashcardItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val simplified: String = "",
    val traditional: String = "",
    val pinyin: String = "",
    val english: String = "",
    val partOfSpeech: String = "Noun",
    val exampleChinese: String = "",
    val examplePinyin: String = "",
    val exampleEnglish: String = "",
    val hskLevel: Long = 1L,
    val category: String = "Core",
    val status: String = "LEARNING",
    val intervalDays: Long = 1L,
    val easeFactor: Long = 250L,
    val repetitions: Long = 0L,
    val nextReviewEpochDay: Long = 0L,
    val isFavorite: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PersonalNote(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val chineseSentence: String = "",
    val pinyin: String = "",
    val englishTranslation: String = "",
    val grammarNotes: String = "",
    val folder: String = "General",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
