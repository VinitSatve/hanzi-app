package com.example.data.curriculum

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Local JSON-based loader and offline cache manager for the HSK 1–6 curriculum.
 *
 * Responsibilities:
 * 1. Organizes Stage 0 + HSK 1–6 into structured [HskLevelCurriculum] records, each containing:
 *    - [CurriculumUnit] list
 *    - [CurriculumLesson] list (with interactive [LessonExercise] steps)
 *    - 12-point [VocabReference] list
 *    - 12-point [GrammarTopic] list
 * 2. Serializes & deserializes the entire HSK 1–6 curriculum hierarchy to/from JSON (`org.json`).
 * 3. Loads the bundled JSON asset (`assets/curriculum/hsk_curriculum_v1.json`) and merges it with
 *    locally cached per-level JSON files stored in `context.filesDir/hanzi_offline_curriculum/`.
 * 4. Writes and deletes real per-level `.json` files on disk (`hsk_level_1.json` .. `hsk_level_6.json`)
 *    when the user downloads or removes offline packs.
 */
object HskJsonCurriculumLoader {

    private const val ASSET_CURRICULUM_PATH = "curriculum/hsk_curriculum_v1.json"
    private const val OFFLINE_CACHE_DIR_NAME = "hanzi_offline_curriculum"
    private const val MANIFEST_FILE_NAME = "hsk_curriculum_manifest.json"

    private data class LevelMeta(
        val level: Int,
        val title: String,
        val cefrBand: String,
        val hsk2Words: Int,
        val hsk3Words: Int,
        val description: String
    )

    private val defaultLevelMetadata: List<LevelMeta> = listOf(
        LevelMeta(
            level = 0,
            title = "Stage 0 · Pinyin & Tone Architecture",
            cefrBand = "PRE-A1",
            hsk2Words = 57,
            hsk3Words = 57,
            description = "21 initials, 36 finals, 4 tones + neutral tone, and mandatory 3rd-tone/一/不 sandhi rules."
        ),
        LevelMeta(
            level = 1,
            title = "HSK 1 · Novice Survival Foundations",
            cefrBand = "CEFR A1",
            hsk2Words = 150,
            hsk3Words = 500,
            description = "Essential greetings, numbers, family, time, food, daily SVO sentences, and core particles (是, 有, 在, 吗)."
        ),
        LevelMeta(
            level = 2,
            title = "HSK 2 · Elementary Daily Communication",
            cefrBand = "CEFR A2",
            hsk2Words = 300,
            hsk3Words = 1272,
            description = "Comparisons (比), completed vs experiential aspect (了/过), structural particles (的/地/得), and travel dialogues."
        ),
        LevelMeta(
            level = 3,
            title = "HSK 3 · Intermediate Fluency & Complements",
            cefrBand = "CEFR B1",
            hsk2Words = 600,
            hsk3Words = 2245,
            description = "Object manipulation with 把, passive voice with 被, directional/potential complements, and workplace communication."
        ),
        LevelMeta(
            level = 4,
            title = "HSK 4 · Upper-Intermediate Discourse & Society",
            cefrBand = "CEFR B2",
            hsk2Words = 1200,
            hsk3Words = 3245,
            description = "Formal discourse connectors (无论…都…, 不仅…而且…), professional registers, and multi-clause reading."
        ),
        LevelMeta(
            level = 5,
            title = "HSK 5 · Advanced Media, Literature & Idioms",
            cefrBand = "CEFR C1",
            hsk2Words = 2500,
            hsk3Words = 4316,
            description = "Four-character idioms (成语), effect-first causal frames (之所以…是因为…), and newspaper/essay analysis."
        ),
        LevelMeta(
            level = 6,
            title = "HSK 6 · Mastery Rhetoric & Classical Registers",
            cefrBand = "CEFR C2",
            hsk2Words = 5000,
            hsk3Words = 5456,
            description = "Literary Chinese particles, rhetorical preference frames (与其…不如…), and academic synthesis."
        )
    )

    /**
     * Builds the complete in-memory HSK 0–6 structured manifest from [CurriculumCatalog] and [CurriculumExtras].
     * Guarantees that every HSK level 1–6 has non-empty units, lessons, vocabulary, and grammar.
     */
    fun buildDefaultManifest(): HskCurriculumManifest {
        val levels = defaultLevelMetadata.map { meta ->
            val units = CurriculumCatalog.units.filter { it.stage == meta.level }
            val lessons = CurriculumCatalog.lessons.filter { it.stage == meta.level }
            val vocab = if (meta.level == 0) {
                CurriculumCatalog.vocabDatabase.filter { it.hskLevel == 1 }.take(4)
            } else {
                CurriculumCatalog.vocabDatabase.filter { it.hskLevel == meta.level }
            }
            val grammar = if (meta.level == 0) {
                CurriculumExtras.grammarTopics.filter { it.hskLevel == 1 }.take(1)
            } else {
                CurriculumExtras.grammarTopics.filter { it.hskLevel == meta.level }
            }

            HskLevelCurriculum(
                level = meta.level,
                title = meta.title,
                cefrBand = meta.cefrBand,
                hsk2WordCount = meta.hsk2Words,
                hsk3WordCount = meta.hsk3Words,
                description = meta.description,
                units = units,
                lessons = lessons,
                vocabulary = vocab,
                grammar = grammar
            )
        }
        return HskCurriculumManifest(
            schemaVersion = 1,
            lastUpdatedIso = "2026-11-05T00:00:00Z",
            hskStandardsSupported = listOf("HSK_2", "HSK_3"),
            levels = levels
        )
    }

    /**
     * Loads the HSK 1–6 curriculum offline-first:
     * 1. Reads bundled JSON asset (`assets/curriculum/hsk_curriculum_v1.json`) if available.
     * 2. Overlays any locally cached per-level JSON files in `context.filesDir/hanzi_offline_curriculum/hsk_level_<n>.json`.
     * 3. Hydrates full lessons, vocabulary, and grammar so every level 1–6 is complete offline.
     * 4. Persists the root manifest JSON to `filesDir/hanzi_offline_curriculum/hsk_curriculum_manifest.json`.
     */
    fun loadCurriculumOfflineFirst(context: Context): HskCurriculumManifest {
        val fallbackManifest = buildDefaultManifest()
        val cacheDir = getCacheDir(context)

        // 1. Parse asset JSON if present
        val assetJsonLevels = mutableMapOf<Int, HskLevelCurriculum>()
        var assetLoaded = false
        try {
            context.assets.open(ASSET_CURRICULUM_PATH).bufferedReader().use { reader ->
                val raw = reader.readText()
                if (raw.isNotBlank()) {
                    val parsedAsset = fromJson(raw, fallbackManifest)
                    parsedAsset.levels.forEach { assetJsonLevels[it.level] = it }
                    assetLoaded = true
                }
            }
        } catch (_: Exception) {
            assetLoaded = false
        }

        // 2. Overlay any per-level cached JSON files on disk
        val diskLevels = mutableMapOf<Int, HskLevelCurriculum>()
        for (level in 0..6) {
            val file = File(cacheDir, levelFileName(level))
            if (file.exists() && file.length() > 0L) {
                try {
                    val jsonObj = JSONObject(file.readText())
                    val fallbackLevel = fallbackManifest.levels.firstOrNull { it.level == level }
                    diskLevels[level] = levelFromJson(jsonObj, fallbackLevel)
                } catch (_: Exception) {
                    // Ignore corrupted file and fall back cleanly
                }
            }
        }

        // 3. Merge in priority order: Disk JSON -> Asset JSON + Hydrated Catalog -> Default Manifest
        val mergedLevels = fallbackManifest.levels.map { defaultLevel ->
            val diskLevel = diskLevels[defaultLevel.level]
            val assetLevel = assetJsonLevels[defaultLevel.level]
            when {
                diskLevel != null -> diskLevel.copy(
                    units = diskLevel.units.ifEmpty { defaultLevel.units },
                    lessons = diskLevel.lessons.ifEmpty { defaultLevel.lessons },
                    vocabulary = diskLevel.vocabulary.ifEmpty { defaultLevel.vocabulary },
                    grammar = diskLevel.grammar.ifEmpty { defaultLevel.grammar }
                )
                assetLevel != null -> assetLevel.copy(
                    units = assetLevel.units.ifEmpty { defaultLevel.units },
                    lessons = assetLevel.lessons.ifEmpty { defaultLevel.lessons },
                    vocabulary = assetLevel.vocabulary.ifEmpty { defaultLevel.vocabulary },
                    grammar = assetLevel.grammar.ifEmpty { defaultLevel.grammar }
                )
                else -> defaultLevel
            }
        }

        val finalManifest = fallbackManifest.copy(levels = mergedLevels)

        // 4. Write the root manifest JSON to local disk so offline inspection is always ready
        try {
            val manifestFile = File(cacheDir, MANIFEST_FILE_NAME)
            if (!manifestFile.exists() || assetLoaded) {
                manifestFile.writeText(toJson(finalManifest))
            }
        } catch (_: Exception) {
            // Non-fatal if storage is restricted
        }

        return finalManifest
    }

    /**
     * Caches a specific HSK level (0..6) as a standalone local JSON file (`hsk_level_<level>.json`).
     */
    fun cacheLevelToLocalJson(
        context: Context,
        level: Int,
        manifest: HskCurriculumManifest = buildDefaultManifest()
    ): File {
        val cacheDir = getCacheDir(context)
        val targetLevel = manifest.levels.firstOrNull { it.level == level }
            ?: buildDefaultManifest().levels.first { it.level == level.coerceIn(0, 6) }
        val file = File(cacheDir, levelFileName(targetLevel.level))
        file.writeText(levelToJson(targetLevel))
        return file
    }

    /**
     * Deletes a locally cached HSK level JSON file (`hsk_level_<level>.json`).
     */
    fun deleteCachedLevelJson(context: Context, level: Int): Boolean {
        val file = File(getCacheDir(context), levelFileName(level))
        return if (file.exists()) file.delete() else false
    }

    /**
     * Maps an [OfflineContentPack] ID to its corresponding HSK level(s) and writes their
     * complete JSON files to `filesDir/hanzi_offline_curriculum/`.
     */
    fun cachePackToLocalJson(
        context: Context,
        packId: String,
        manifest: HskCurriculumManifest = buildDefaultManifest()
    ): List<File> {
        val levelsToCache = levelsForPackId(packId)
        return levelsToCache.map { lvl -> cacheLevelToLocalJson(context, lvl, manifest) }
    }

    /**
     * Deletes the local JSON files associated with an [OfflineContentPack] ID.
     */
    fun deletePackLocalJson(context: Context, packId: String): Int {
        val levels = levelsForPackId(packId)
        var deletedCount = 0
        levels.forEach { lvl ->
            if (deleteCachedLevelJson(context, lvl)) deletedCount++
        }
        return deletedCount
    }

    /**
     * Synchronizes the local JSON cache folder with the user's set of downloaded pack IDs.
     */
    fun syncDownloadedPacksToDisk(
        context: Context,
        downloadedPackIds: Set<String>,
        manifest: HskCurriculumManifest = buildDefaultManifest()
    ): OfflineJsonCacheStats {
        downloadedPackIds.forEach { packId ->
            cachePackToLocalJson(context, packId, manifest)
        }
        return getOfflineCacheStats(context)
    }

    /**
     * Returns real on-disk statistics for the local JSON curriculum cache.
     */
    fun getOfflineCacheStats(context: Context): OfflineJsonCacheStats {
        val cacheDir = getCacheDir(context)
        val jsonFiles = cacheDir.listFiles { file -> file.isFile && file.name.endsWith(".json") }
            ?.sortedBy { it.name }
            ?: emptyList()

        val cachedLevelFiles = jsonFiles.map { it.name }
        val cachedLevels = jsonFiles.mapNotNull { file ->
            val match = Regex("hsk_level_(\\d+)\\.json").find(file.name)
            match?.groupValues?.getOrNull(1)?.toIntOrNull()
        }.sorted()

        val totalBytes = jsonFiles.sumOf { it.length() }
        val assetExists = try {
            context.assets.open(ASSET_CURRICULUM_PATH).use { true }
        } catch (_: Exception) {
            false
        }

        return OfflineJsonCacheStats(
            cacheDirectoryPath = cacheDir.absolutePath,
            cachedLevelFiles = cachedLevelFiles,
            cachedLevels = cachedLevels,
            totalBytesOnDisk = totalBytes,
            sourceAssetLoaded = assetExists
        )
    }

    fun levelsForPackId(packId: String): List<Int> = when (packId) {
        "pack_pinyin" -> listOf(0)
        "pack_hsk1" -> listOf(1)
        "pack_hsk2" -> listOf(2)
        "pack_hsk3" -> listOf(3)
        "pack_hsk4_6" -> listOf(4, 5, 6)
        else -> listOf(1)
    }

    private fun getCacheDir(context: Context): File {
        val dir = File(context.filesDir, OFFLINE_CACHE_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun levelFileName(level: Int): String = "hsk_level_$level.json"

    // =========================================================================
    // JSON Serialization & Deserialization (org.json)
    // =========================================================================

    fun toJson(manifest: HskCurriculumManifest): String {
        val root = JSONObject()
        root.put("schemaVersion", manifest.schemaVersion)
        root.put("lastUpdatedIso", manifest.lastUpdatedIso)
        root.put("hskStandardsSupported", JSONArray(manifest.hskStandardsSupported))

        val levelsArray = JSONArray()
        manifest.levels.forEach { levelCurriculum ->
            levelsArray.put(levelToJsonObject(levelCurriculum))
        }
        root.put("levels", levelsArray)
        return root.toString(2)
    }

    fun levelToJson(levelCurriculum: HskLevelCurriculum): String {
        return levelToJsonObject(levelCurriculum).toString(2)
    }

    private fun levelToJsonObject(levelCurriculum: HskLevelCurriculum): JSONObject {
        val obj = JSONObject()
        obj.put("level", levelCurriculum.level)
        obj.put("title", levelCurriculum.title)
        obj.put("cefrBand", levelCurriculum.cefrBand)
        obj.put("hsk2WordCount", levelCurriculum.hsk2WordCount)
        obj.put("hsk3WordCount", levelCurriculum.hsk3WordCount)
        obj.put("description", levelCurriculum.description)

        val unitsArr = JSONArray()
        levelCurriculum.units.forEach { unit ->
            val uObj = JSONObject()
            uObj.put("id", unit.id)
            uObj.put("stage", unit.stage)
            uObj.put("unitNumber", unit.unitNumber)
            uObj.put("title", unit.title)
            uObj.put("description", unit.description)
            uObj.put("hsk2VocabTarget", unit.hsk2VocabTarget)
            uObj.put("hsk3VocabTarget", unit.hsk3VocabTarget)
            uObj.put("grammarTargets", JSONArray(unit.grammarTargets))
            uObj.put("vocabularyTargets", JSONArray(unit.vocabularyTargets))
            uObj.put("lessonIds", JSONArray(unit.lessonIds))
            if (unit.prerequisiteUnitId != null) {
                uObj.put("prerequisiteUnitId", unit.prerequisiteUnitId)
            }
            unitsArr.put(uObj)
        }
        obj.put("units", unitsArr)

        val lessonsArr = JSONArray()
        levelCurriculum.lessons.forEach { lesson ->
            val lObj = JSONObject()
            lObj.put("id", lesson.id)
            lObj.put("stage", lesson.stage)
            lObj.put("unitId", lesson.unitId)
            lObj.put("lessonType", lesson.lessonType)
            lObj.put("title", lesson.title)
            lObj.put("subtitle", lesson.subtitle)
            lObj.put("chineseFocus", lesson.chineseFocus)
            lObj.put("traditionalFocus", lesson.traditionalFocus)
            lObj.put("pinyinFocus", lesson.pinyinFocus)
            lObj.put("summary", lesson.summary)
            lObj.put("keyPoints", JSONArray(lesson.keyPoints))
            lObj.put("vocabularyTargets", JSONArray(lesson.vocabularyTargets))
            lObj.put("grammarTargets", JSONArray(lesson.grammarTargets))
            if (lesson.prerequisiteLessonId != null) {
                lObj.put("prerequisiteLessonId", lesson.prerequisiteLessonId)
            }
            lObj.put("xpReward", lesson.xpReward)

            val exArr = JSONArray()
            lesson.exercises.forEach { ex ->
                val exObj = JSONObject()
                exObj.put("id", ex.id)
                exObj.put("type", ex.type.name)
                exObj.put("question", ex.question)
                exObj.put("promptChinese", ex.promptChinese)
                exObj.put("promptPinyin", ex.promptPinyin)
                exObj.put("options", JSONArray(ex.options))
                exObj.put("correctIndex", ex.correctIndex)
                exObj.put("explanation", ex.explanation)
                exObj.put("wordBankTiles", JSONArray(ex.wordBankTiles))
                exObj.put("correctWordOrder", JSONArray(ex.correctWordOrder))

                val pairsArr = JSONArray()
                ex.matchPairs.forEach { (left, right) ->
                    val pairObj = JSONObject()
                    pairObj.put("left", left)
                    pairObj.put("right", right)
                    pairsArr.put(pairObj)
                }
                exObj.put("matchPairs", pairsArr)
                exObj.put("expectedTypedAnswers", JSONArray(ex.expectedTypedAnswers))
                exObj.put("readingPassageChinese", ex.readingPassageChinese)
                exObj.put("readingPassagePinyin", ex.readingPassagePinyin)
                exObj.put("readingPassageEnglish", ex.readingPassageEnglish)
                exObj.put("incorrectSentence", ex.incorrectSentence)
                exArr.put(exObj)
            }
            lObj.put("exercises", exArr)
            lessonsArr.put(lObj)
        }
        obj.put("lessons", lessonsArr)

        val vocabArr = JSONArray()
        levelCurriculum.vocabulary.forEach { v ->
            val vObj = JSONObject()
            vObj.put("id", v.id)
            vObj.put("simplified", v.simplified)
            vObj.put("traditional", v.traditional)
            vObj.put("pinyin", v.pinyin)
            vObj.put("english", v.english)
            vObj.put("partOfSpeech", v.partOfSpeech)
            vObj.put("hskLevel", v.hskLevel)
            vObj.put("category", v.category)
            vObj.put("exampleSimplified", v.exampleSimplified)
            vObj.put("exampleTraditional", v.exampleTraditional)
            vObj.put("examplePinyin", v.examplePinyin)
            vObj.put("exampleEnglish", v.exampleEnglish)
            vObj.put("pronunciationGuide", v.pronunciationGuide)
            vObj.put("strokeOrderGuide", v.strokeOrderGuide)
            vObj.put("secondExampleSimplified", v.secondExampleSimplified)
            vObj.put("secondExamplePinyin", v.secondExamplePinyin)
            vObj.put("secondExampleEnglish", v.secondExampleEnglish)
            vObj.put("commonUsage", v.commonUsage)
            vObj.put("relatedVocabulary", JSONArray(v.relatedVocabulary))
            vObj.put("commonMistake", v.commonMistake)
            vObj.put("practiceQuestion", v.practiceQuestion)
            vObj.put("practiceOptions", JSONArray(v.practiceOptions))
            vObj.put("practiceCorrectIndex", v.practiceCorrectIndex)
            vocabArr.put(vObj)
        }
        obj.put("vocabulary", vocabArr)

        val grammarArr = JSONArray()
        levelCurriculum.grammar.forEach { g ->
            val gObj = JSONObject()
            gObj.put("id", g.id)
            gObj.put("particle", g.particle)
            gObj.put("title", g.title)
            gObj.put("hskLevel", g.hskLevel)
            gObj.put("pattern", g.pattern)
            gObj.put("explanation", g.explanation)
            gObj.put("exampleSimplified", g.exampleSimplified)
            gObj.put("exampleTraditional", g.exampleTraditional)
            gObj.put("examplePinyin", g.examplePinyin)
            gObj.put("exampleEnglish", g.exampleEnglish)
            gObj.put("commonMistake", g.commonMistake)
            gObj.put("quizQuestion", g.quizQuestion)
            gObj.put("quizOptions", JSONArray(g.quizOptions))
            gObj.put("quizCorrectIndex", g.quizCorrectIndex)
            gObj.put("wordOrderBreakdown", g.wordOrderBreakdown)
            gObj.put("whenToUse", g.whenToUse)
            gObj.put("whenNotToUse", g.whenNotToUse)
            gObj.put("secondExampleSimplified", g.secondExampleSimplified)
            gObj.put("secondExamplePinyin", g.secondExamplePinyin)
            gObj.put("secondExampleEnglish", g.secondExampleEnglish)
            gObj.put("thirdExampleSimplified", g.thirdExampleSimplified)
            gObj.put("thirdExamplePinyin", g.thirdExamplePinyin)
            gObj.put("thirdExampleEnglish", g.thirdExampleEnglish)
            gObj.put("comparisonWithSimilar", g.comparisonWithSimilar)
            gObj.put("quizExplanation", g.quizExplanation)
            grammarArr.put(gObj)
        }
        obj.put("grammar", grammarArr)

        return obj
    }

    fun fromJson(
        jsonString: String,
        fallbackManifest: HskCurriculumManifest = buildDefaultManifest()
    ): HskCurriculumManifest {
        val root = JSONObject(jsonString)
        val schemaVersion = root.optInt("schemaVersion", 1)
        val lastUpdatedIso = root.optString("lastUpdatedIso", "2026-11-05T00:00:00Z")
        val standards = root.optJSONArray("hskStandardsSupported").toStringList()
            .ifEmpty { listOf("HSK_2", "HSK_3") }

        val levelsArr = root.optJSONArray("levels") ?: JSONArray()
        val parsedLevels = mutableListOf<HskLevelCurriculum>()
        for (i in 0 until levelsArr.length()) {
            val lObj = levelsArr.optJSONObject(i) ?: continue
            val levelNum = lObj.optInt("level", i + 1)
            val fallbackLevel = fallbackManifest.levels.firstOrNull { it.level == levelNum }
            parsedLevels.add(levelFromJson(lObj, fallbackLevel))
        }

        return HskCurriculumManifest(
            schemaVersion = schemaVersion,
            lastUpdatedIso = lastUpdatedIso,
            hskStandardsSupported = standards,
            levels = parsedLevels
        )
    }

    fun levelFromJson(
        obj: JSONObject,
        fallbackLevel: HskLevelCurriculum? = null
    ): HskLevelCurriculum {
        val level = obj.optInt("level", fallbackLevel?.level ?: 1)
        val title = obj.optString("title", fallbackLevel?.title ?: "HSK $level")
        val cefrBand = obj.optString("cefrBand", fallbackLevel?.cefrBand ?: "CEFR")
        val hsk2WordCount = obj.optInt("hsk2WordCount", fallbackLevel?.hsk2WordCount ?: 150)
        val hsk3WordCount = obj.optInt("hsk3WordCount", fallbackLevel?.hsk3WordCount ?: 500)
        val description = obj.optString("description", fallbackLevel?.description ?: "")

        val units = parseUnits(obj.optJSONArray("units")).ifEmpty {
            fallbackLevel?.units ?: emptyList()
        }
        val lessons = parseLessons(obj.optJSONArray("lessons")).ifEmpty {
            fallbackLevel?.lessons ?: emptyList()
        }
        val vocabulary = parseVocabulary(obj.optJSONArray("vocabulary")).ifEmpty {
            fallbackLevel?.vocabulary ?: emptyList()
        }
        val grammar = parseGrammar(obj.optJSONArray("grammar")).ifEmpty {
            fallbackLevel?.grammar ?: emptyList()
        }

        return HskLevelCurriculum(
            level = level,
            title = title,
            cefrBand = cefrBand,
            hsk2WordCount = hsk2WordCount,
            hsk3WordCount = hsk3WordCount,
            description = description,
            units = units,
            lessons = lessons,
            vocabulary = vocabulary,
            grammar = grammar
        )
    }

    private fun parseUnits(arr: JSONArray?): List<CurriculumUnit> {
        if (arr == null) return emptyList()
        val list = mutableListOf<CurriculumUnit>()
        for (i in 0 until arr.length()) {
            val u = arr.optJSONObject(i) ?: continue
            val prereq = u.optString("prerequisiteUnitId", "").takeIf { it.isNotBlank() }
            list.add(
                CurriculumUnit(
                    id = u.optString("id"),
                    stage = u.optInt("stage", 1),
                    unitNumber = u.optInt("unitNumber", i + 1),
                    title = u.optString("title"),
                    description = u.optString("description"),
                    hsk2VocabTarget = u.optInt("hsk2VocabTarget", 150),
                    hsk3VocabTarget = u.optInt("hsk3VocabTarget", 500),
                    grammarTargets = u.optJSONArray("grammarTargets").toStringList(),
                    vocabularyTargets = u.optJSONArray("vocabularyTargets").toStringList(),
                    lessonIds = u.optJSONArray("lessonIds").toStringList(),
                    prerequisiteUnitId = prereq
                )
            )
        }
        return list
    }

    private fun parseLessons(arr: JSONArray?): List<CurriculumLesson> {
        if (arr == null) return emptyList()
        val list = mutableListOf<CurriculumLesson>()
        for (i in 0 until arr.length()) {
            val l = arr.optJSONObject(i) ?: continue
            val stage = l.optInt("stage", 1)
            val prereq = l.optString("prerequisiteLessonId", "").takeIf { it.isNotBlank() }
            val exArr = l.optJSONArray("exercises")
            val exercises = mutableListOf<LessonExercise>()
            if (exArr != null) {
                for (j in 0 until exArr.length()) {
                    val ex = exArr.optJSONObject(j) ?: continue
                    val typeName = ex.optString("type", ExerciseType.MULTIPLE_CHOICE.name)
                    val exType = runCatching { ExerciseType.valueOf(typeName) }
                        .getOrDefault(ExerciseType.MULTIPLE_CHOICE)
                    val pairsArr = ex.optJSONArray("matchPairs")
                    val pairs = mutableListOf<Pair<String, String>>()
                    if (pairsArr != null) {
                        for (k in 0 until pairsArr.length()) {
                            val pObj = pairsArr.optJSONObject(k) ?: continue
                            pairs.add(pObj.optString("left") to pObj.optString("right"))
                        }
                    }
                    exercises.add(
                        LessonExercise(
                            id = ex.optString("id"),
                            type = exType,
                            question = ex.optString("question"),
                            promptChinese = ex.optString("promptChinese"),
                            promptPinyin = ex.optString("promptPinyin"),
                            options = ex.optJSONArray("options").toStringList(),
                            correctIndex = ex.optInt("correctIndex", 0),
                            explanation = ex.optString("explanation"),
                            wordBankTiles = ex.optJSONArray("wordBankTiles").toStringList(),
                            correctWordOrder = ex.optJSONArray("correctWordOrder").toStringList(),
                            matchPairs = pairs,
                            expectedTypedAnswers = ex.optJSONArray("expectedTypedAnswers").toStringList(),
                            readingPassageChinese = ex.optString("readingPassageChinese"),
                            readingPassagePinyin = ex.optString("readingPassagePinyin"),
                            readingPassageEnglish = ex.optString("readingPassageEnglish"),
                            incorrectSentence = ex.optString("incorrectSentence")
                        )
                    )
                }
            }
            list.add(
                CurriculumLesson(
                    id = l.optString("id"),
                    stage = stage,
                    unitId = l.optString("unitId", "unit_stage_$stage"),
                    lessonType = l.optString("lessonType", "CORE_LESSON"),
                    title = l.optString("title"),
                    subtitle = l.optString("subtitle"),
                    chineseFocus = l.optString("chineseFocus"),
                    traditionalFocus = l.optString("traditionalFocus"),
                    pinyinFocus = l.optString("pinyinFocus"),
                    summary = l.optString("summary"),
                    keyPoints = l.optJSONArray("keyPoints").toStringList(),
                    vocabularyTargets = l.optJSONArray("vocabularyTargets").toStringList(),
                    grammarTargets = l.optJSONArray("grammarTargets").toStringList(),
                    prerequisiteLessonId = prereq,
                    xpReward = l.optInt("xpReward", 40),
                    exercises = exercises
                )
            )
        }
        return list
    }

    private fun parseVocabulary(arr: JSONArray?): List<VocabReference> {
        if (arr == null) return emptyList()
        val list = mutableListOf<VocabReference>()
        for (i in 0 until arr.length()) {
            val v = arr.optJSONObject(i) ?: continue
            val simplified = v.optString("simplified")
            val pinyin = v.optString("pinyin")
            val english = v.optString("english")
            val hskLevel = v.optInt("hskLevel", 1)
            val category = v.optString("category", "General")
            list.add(
                VocabReference(
                    id = v.optString("id"),
                    simplified = simplified,
                    traditional = v.optString("traditional", simplified),
                    pinyin = pinyin,
                    english = english,
                    partOfSpeech = v.optString("partOfSpeech", "Noun"),
                    hskLevel = hskLevel,
                    category = category,
                    exampleSimplified = v.optString("exampleSimplified"),
                    exampleTraditional = v.optString("exampleTraditional"),
                    examplePinyin = v.optString("examplePinyin"),
                    exampleEnglish = v.optString("exampleEnglish"),
                    pronunciationGuide = v.optString(
                        "pronunciationGuide",
                        "Pay close attention to the tone contour ($pinyin) and crisp vowel ending."
                    ),
                    strokeOrderGuide = v.optString(
                        "strokeOrderGuide",
                        "Write left-to-right, top-to-bottom; complete outer framing strokes before closing."
                    ),
                    secondExampleSimplified = v.optString("secondExampleSimplified", "${simplified}在日常对话中非常常用。"),
                    secondExamplePinyin = v.optString("secondExamplePinyin", "$pinyin zài rìcháng duìhuà zhōng fēicháng chángyòng."),
                    secondExampleEnglish = v.optString("secondExampleEnglish", "\"$simplified\" ($english) is very frequently used in daily conversation."),
                    commonUsage = v.optString("commonUsage", "Used in both spoken and written Mandarin ($category context, HSK $hskLevel)."),
                    relatedVocabulary = v.optJSONArray("relatedVocabulary").toStringList(),
                    commonMistake = v.optString("commonMistake", "Avoid flattening the tone contour on $pinyin."),
                    practiceQuestion = v.optString("practiceQuestion", "Which English meaning best matches '$simplified' ($pinyin)?"),
                    practiceOptions = v.optJSONArray("practiceOptions").toStringList()
                        .ifEmpty { listOf(english, "To depart immediately", "Yesterday afternoon", "A pair of shoes") },
                    practiceCorrectIndex = v.optInt("practiceCorrectIndex", 0)
                )
            )
        }
        return list
    }

    private fun parseGrammar(arr: JSONArray?): List<GrammarTopic> {
        if (arr == null) return emptyList()
        val list = mutableListOf<GrammarTopic>()
        for (i in 0 until arr.length()) {
            val g = arr.optJSONObject(i) ?: continue
            val particle = g.optString("particle")
            val title = g.optString("title")
            val hskLevel = g.optInt("hskLevel", 1)
            val pattern = g.optString("pattern")
            val commonMistake = g.optString("commonMistake")
            list.add(
                GrammarTopic(
                    id = g.optString("id"),
                    particle = particle,
                    title = title,
                    hskLevel = hskLevel,
                    pattern = pattern,
                    explanation = g.optString("explanation"),
                    exampleSimplified = g.optString("exampleSimplified"),
                    exampleTraditional = g.optString("exampleTraditional"),
                    examplePinyin = g.optString("examplePinyin"),
                    exampleEnglish = g.optString("exampleEnglish"),
                    commonMistake = commonMistake,
                    quizQuestion = g.optString("quizQuestion"),
                    quizOptions = g.optJSONArray("quizOptions").toStringList(),
                    quizCorrectIndex = g.optInt("quizCorrectIndex", 0),
                    wordOrderBreakdown = g.optString("wordOrderBreakdown", "Subject → Particle ($particle) → Predicate"),
                    whenToUse = g.optString("whenToUse", "Use when expressing ${title.lowercase()} in HSK $hskLevel."),
                    whenNotToUse = g.optString("whenNotToUse", "Do not use when the clause violates $particle."),
                    secondExampleSimplified = g.optString("secondExampleSimplified"),
                    secondExamplePinyin = g.optString("secondExamplePinyin"),
                    secondExampleEnglish = g.optString("secondExampleEnglish"),
                    thirdExampleSimplified = g.optString("thirdExampleSimplified"),
                    thirdExamplePinyin = g.optString("thirdExamplePinyin"),
                    thirdExampleEnglish = g.optString("thirdExampleEnglish"),
                    comparisonWithSimilar = g.optString("comparisonWithSimilar"),
                    quizExplanation = g.optString("quizExplanation", "Follow '$pattern' and avoid: $commonMistake")
                )
            )
        }
        return list
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until length()) {
            list.add(optString(i))
        }
        return list
    }
}
