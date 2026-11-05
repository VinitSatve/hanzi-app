package com.example.data.curriculum

enum class ExerciseType(val displayLabel: String) {
    CHARACTER_RECOGNITION("CHARACTER RECOGNITION"),
    MATCH_PAIRS("MATCH PAIRS"),
    MULTIPLE_CHOICE("MULTIPLE CHOICE"),
    FILL_BLANK("FILL IN THE BLANK"),
    SENTENCE_BUILDER("SENTENCE BUILDER"),
    TRANSLATE_EN_TO_ZH("TRANSLATE EN → ZH"),
    TRANSLATE_ZH_TO_EN("TRANSLATE ZH → EN"),
    LISTEN_IDENTIFY("LISTEN & IDENTIFY"),
    TONE_IDENTIFY("TONE IDENTIFICATION"),
    TYPE_CHARACTER("TYPE HANZI / PINYIN"),
    READING_COMPREHENSION("SHORT PASSAGE READING"),
    GRAMMAR_CORRECTION("GRAMMAR CORRECTION")
}

data class PinyinDrillItem(
    val id: String,
    val symbol: String,
    val group: String, // "INITIAL", "FINAL", "TONE_PAIR"
    val ipaGuide: String,
    val englishHint: String,
    val exampleHanzi: String,
    val exampleTrad: String,
    val examplePinyin: String,
    val exampleMeaning: String,
    val commonMistake: String
)

data class CurriculumUnit(
    val id: String,
    val stage: Int, // 0 = Pinyin Foundation, 1..6 = HSK 1..6
    val unitNumber: Int,
    val title: String,
    val description: String,
    val hsk2VocabTarget: Int,
    val hsk3VocabTarget: Int,
    val grammarTargets: List<String>,
    val vocabularyTargets: List<String>,
    val lessonIds: List<String>,
    val prerequisiteUnitId: String? = null
)

data class CurriculumLesson(
    val id: String,
    val stage: Int, // 0 = Pinyin Foundation, 1..6 = HSK 1..6
    val unitId: String = "unit_stage_$stage",
    val lessonType: String = "CORE_LESSON", // "CORE_LESSON", "REVISION", "UNIT_TEST", "LEVEL_ASSESSMENT"
    val title: String,
    val subtitle: String,
    val chineseFocus: String,
    val traditionalFocus: String,
    val pinyinFocus: String,
    val summary: String,
    val keyPoints: List<String>,
    val vocabularyTargets: List<String> = emptyList(),
    val grammarTargets: List<String> = emptyList(),
    val prerequisiteLessonId: String? = null,
    val xpReward: Int = 40,
    val exercises: List<LessonExercise>
)

data class LessonExercise(
    val id: String,
    val type: ExerciseType = ExerciseType.MULTIPLE_CHOICE,
    val question: String,
    val promptChinese: String,
    val promptPinyin: String,
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val explanation: String,
    val wordBankTiles: List<String> = emptyList(),
    val correctWordOrder: List<String> = emptyList(),
    val matchPairs: List<Pair<String, String>> = emptyList(),
    val expectedTypedAnswers: List<String> = emptyList(),
    val readingPassageChinese: String = "",
    val readingPassagePinyin: String = "",
    val readingPassageEnglish: String = "",
    val incorrectSentence: String = ""
)

/**
 * Complete 12-Point Vocabulary Teaching Model:
 * 1. Chinese character (simplified & traditional)
 * 2. Pinyin with tone marks
 * 3. English meaning
 * 4. Word category / part of speech
 * 5. Pronunciation guidance
 * 6. Stroke order guidance
 * 7. Example sentences (multiple)
 * 8. English sentence translations
 * 9. Common usage notes
 * 10. Related vocabulary
 * 11. Common mistakes
 * 12. Practice question
 */
data class VocabReference(
    val id: String,
    val simplified: String,
    val traditional: String,
    val pinyin: String,
    val english: String,
    val partOfSpeech: String,
    val hskLevel: Int,
    val category: String,
    val exampleSimplified: String,
    val exampleTraditional: String,
    val examplePinyin: String,
    val exampleEnglish: String,
    val pronunciationGuide: String = "Pay close attention to the tone contour ($pinyin) and crisp vowel ending.",
    val strokeOrderGuide: String = "Write left-to-right, top-to-bottom; complete outer framing strokes before closing.",
    val secondExampleSimplified: String = "${simplified}在日常对话中非常常用。",
    val secondExamplePinyin: String = "$pinyin zài rìcháng duìhuà zhōng fēicháng chángyòng.",
    val secondExampleEnglish: String = "\"$simplified\" ($english) is very frequently used in daily conversation.",
    val commonUsage: String = "Used in both spoken and written Mandarin (${category} context, HSK $hskLevel).",
    val relatedVocabulary: List<String> = emptyList(),
    val commonMistake: String = "Avoid flattening the tone contour on $pinyin or confusing its word order in a clause.",
    val practiceQuestion: String = "Which English meaning best matches '$simplified' ($pinyin)?",
    val practiceOptions: List<String> = listOf(english, "To depart immediately", "Yesterday afternoon", "A pair of shoes"),
    val practiceCorrectIndex: Int = 0
)

/**
 * Complete 12-Point Grammar Teaching Model:
 * 1. Grammar concept (particle + title)
 * 2. Detailed explanation in simple English
 * 3. Sentence structure formula (pattern)
 * 4. Word order breakdown
 * 5. When to use it
 * 6. When NOT to use it
 * 7. Multiple Chinese examples
 * 8. Pinyin for every example
 * 9. English translations
 * 10. Comparison with similar grammar
 * 11. Common mistakes
 * 12. Practice exercises
 */
data class GrammarTopic(
    val id: String,
    val particle: String,
    val title: String,
    val hskLevel: Int,
    val pattern: String,
    val explanation: String,
    val exampleSimplified: String,
    val exampleTraditional: String,
    val examplePinyin: String,
    val exampleEnglish: String,
    val commonMistake: String,
    val quizQuestion: String,
    val quizOptions: List<String>,
    val quizCorrectIndex: Int,
    val wordOrderBreakdown: String = "Subject → Time/Adverbial → Verb/Particle ($particle) → Object/Complement",
    val whenToUse: String = "Use this pattern when expressing ${title.lowercase()} in HSK $hskLevel communication.",
    val whenNotToUse: String = "Do not use when the clause violates the core structural condition of $particle.",
    val secondExampleSimplified: String = "",
    val secondExamplePinyin: String = "",
    val secondExampleEnglish: String = "",
    val thirdExampleSimplified: String = "",
    val thirdExamplePinyin: String = "",
    val thirdExampleEnglish: String = "",
    val comparisonWithSimilar: String = "Unlike English word order, Mandarin places modifiers and time/place phrases before the main predicate.",
    val quizExplanation: String = "Follow the formula '$pattern' and avoid: $commonMistake"
)

data class CharacterWritingGuide(
    val id: String,
    val simplified: String,
    val traditional: String,
    val pinyin: String,
    val meaning: String,
    val hskLevel: Int,
    val strokeCount: Int,
    val radical: String,
    val radicalMeaning: String,
    val components: String,
    val similarCharacters: String,
    val strokeOrderNames: List<String>,
    val strokePaths: List<List<Pair<Float, Float>>>
)

data class ListeningExerciseItem(
    val id: String,
    val level: String,
    val category: String,
    val audioTextSimplified: String,
    val audioTextTraditional: String,
    val pinyin: String,
    val englishTranslation: String,
    val questionPrompt: String,
    val options: List<String>,
    val correctIndex: Int
)

data class HskExamQuestion(
    val id: String,
    val hskLevel: Int,
    val hskStandard: String, // "HSK_2" or "HSK_3"
    val section: String, // "LISTENING", "READING", "WRITING"
    val promptChinese: String,
    val promptTraditional: String,
    val promptPinyin: String,
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val detailedExplanation: String
)

data class SpeakingPrompt(
    val id: String,
    val level: String,
    val category: String,
    val simplified: String,
    val traditional: String,
    val pinyin: String,
    val english: String,
    val phonemeTip: String,
    val syllablePinyins: List<String> = emptyList(),
    val numericTones: List<Int> = emptyList(), // 1..4, 5 = neutral
    val targetPitchContour: List<Float> = emptyList() // Normalized 0.0..1.0 pitch heights
)

data class OfflineContentPack(
    val id: String,
    val title: String,
    val category: String, // "LESSONS", "VOCABULARY", "GRAMMAR", "AUDIO_VOICE"
    val hskTag: String,
    val description: String,
    val itemCountDescription: String,
    val sizeMb: Float
)

data class MilestoneBadge(
    val id: String,
    val title: String,
    val chineseSeal: String,
    val description: String,
    val requiredPoints: Long
)

/**
 * Structured HSK Level container grouping Units, Lessons, 12-Point Vocabulary,
 * and 12-Point Grammar for a single HSK level (HSK 1 through HSK 6, plus Stage 0 Pinyin).
 */
data class HskLevelCurriculum(
    val level: Int, // 0 = Pinyin Foundation, 1..6 = HSK 1..6
    val title: String,
    val cefrBand: String,
    val hsk2WordCount: Int,
    val hsk3WordCount: Int,
    val description: String,
    val units: List<CurriculumUnit>,
    val lessons: List<CurriculumLesson>,
    val vocabulary: List<VocabReference>,
    val grammar: List<GrammarTopic>
)

/**
 * Root offline JSON manifest for the entire HSK 1–6 Curriculum.
 */
data class HskCurriculumManifest(
    val schemaVersion: Int = 1,
    val lastUpdatedIso: String = "2026-11-05T00:00:00Z",
    val hskStandardsSupported: List<String> = listOf("HSK_2", "HSK_3"),
    val levels: List<HskLevelCurriculum>
)

/**
 * Real on-disk status of the local JSON curriculum cache in filesDir/hanzi_offline_curriculum.
 */
data class OfflineJsonCacheStats(
    val cacheDirectoryPath: String,
    val cachedLevelFiles: List<String>,
    val cachedLevels: List<Int>,
    val totalBytesOnDisk: Long,
    val sourceAssetLoaded: Boolean
)

