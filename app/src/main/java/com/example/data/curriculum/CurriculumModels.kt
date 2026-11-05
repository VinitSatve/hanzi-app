package com.example.data.curriculum

data class PinyinItem(
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

data class LessonExercise(
    val id: String,
    val question: String,
    val promptChinese: String,
    val promptPinyin: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class CurriculumLesson(
    val id: String,
    val stage: Int, // 0 = Pinyin, 1..6 = HSK 1..6
    val hskStandard: String, // "BOTH", "HSK_2", "HSK_3"
    val title: String,
    val subtitle: String,
    val chineseFocus: String,
    val traditionalFocus: String,
    val pinyinFocus: String,
    val summary: String,
    val keyPoints: List<String>,
    val exercises: List<LessonExercise>
)

data class VocabReference(
    val id: String,
    val simplified: String,
    val traditional: String,
    val pinyin: String,
    val english: String,
    val partOfSpeech: String,
    val hskLevel: Int,
    val hskStandard: String, // "HSK_2" or "HSK_3"
    val category: String,
    val exampleSimplified: String,
    val exampleTraditional: String,
    val examplePinyin: String,
    val exampleEnglish: String,
    val relatedWords: List<String>,
    val usageNote: String
)

data class CharacterStrokeData(
    val id: String,
    val simplified: String,
    val traditional: String,
    val pinyin: String,
    val meaning: String,
    val hskLevel: Int,
    val radical: String,
    val radicalMeaning: String,
    val components: String,
    val strokeCount: Int,
    val strokeOrderNames: List<String>,
    val similarCharacters: String,
    // Normalized 0f..1f polyline strokes for interactive stroke order animation & tracing
    val strokePaths: List<List<Pair<Float, Float>>>
)

data class GrammarTopic(
    val id: String,
    val particle: String,
    val title: String,
    val pattern: String,
    val hskLevel: Int,
    val explanation: String,
    val exampleSimplified: String,
    val exampleTraditional: String,
    val examplePinyin: String,
    val exampleEnglish: String,
    val usageRules: List<String>,
    val commonMistake: String,
    val comparisonNote: String,
    val quizQuestion: String,
    val quizOptions: List<String>,
    val quizCorrectIndex: Int
)

data class SpeakingPrompt(
    val id: String,
    val category: String, // "Pinyin & Tones", "Daily Phrases", "Roleplay", "HSK Sentences"
    val level: String,
    val simplified: String,
    val traditional: String,
    val pinyin: String,
    val numericTones: List<Int>, // 1=High Flat, 2=Rising, 3=Dipping, 4=Falling, 5=Neutral
    val syllablePinyins: List<String>,
    val english: String,
    val phonemeTip: String,
    // Normalized pitch curve points (0f..1f height over time) for native speaker reference
    val targetPitchContour: List<Float>
)

data class ListeningExerciseItem(
    val id: String,
    val level: String,
    val category: String, // "Tone Discrimination", "Sentence Comprehension", "Dictation", "Dialogue"
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
    val section: String, // "Listening", "Reading", "Grammar & Writing"
    val promptChinese: String,
    val promptTraditional: String,
    val promptPinyin: String,
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val detailedExplanation: String
)

data class MilestoneBadge(
    val id: String,
    val title: String,
    val chineseSeal: String,
    val description: String,
    val requiredPoints: Int
)

data class OfflineContentPack(
    val id: String,
    val title: String,
    val category: String, // "Curriculum", "Vocabulary & Audio", "Grammar & Characters"
    val hskTag: String,
    val itemCountDescription: String,
    val sizeMb: Float,
    val description: String
)
