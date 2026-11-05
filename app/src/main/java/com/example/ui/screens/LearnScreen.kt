package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.curriculum.CurriculumCatalog
import com.example.data.curriculum.CurriculumExtras
import com.example.data.curriculum.CurriculumLesson
import com.example.data.curriculum.ExerciseType
import com.example.data.curriculum.HskCurriculumManifest
import com.example.data.curriculum.HskJsonCurriculumLoader
import com.example.data.curriculum.OfflineJsonCacheStats
import com.example.data.curriculum.VocabReference
import com.example.data.model.UserProfile
import com.example.ui.theme.BrutalBlack
import com.example.ui.theme.BrutalCobalt
import com.example.ui.theme.BrutalCream
import com.example.ui.theme.BrutalMint
import com.example.ui.theme.BrutalOrange
import com.example.ui.theme.BrutalWhite
import com.example.ui.theme.BrutalYellow
import com.example.ui.theme.NeoBrutalBadge
import com.example.ui.theme.NeoBrutalButton
import com.example.ui.theme.NeoBrutalCard
import com.example.ui.theme.NeoBrutalChip
import com.example.ui.theme.NeoBrutalIconButton
import com.example.ui.theme.NeoBrutalProgressBar
import com.example.ui.theme.NeoBrutalTabRow
import com.example.ui.theme.NeoBrutalTextField
import com.example.ui.theme.neoBrutalSurface

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LearnScreen(
    profile: UserProfile,
    onSpeak: (text: String, slow: Boolean) -> Unit,
    onCompleteLesson: (lessonId: String, points: Int) -> Unit,
    onAddVocabToSrs: (VocabReference) -> Unit,
    onAddCustomVocab: (simplified: String, traditional: String, pinyin: String, english: String, hskLevel: Int) -> Unit,
    curriculumManifest: HskCurriculumManifest = HskJsonCurriculumLoader.buildDefaultManifest(),
    offlineJsonCacheStats: OfflineJsonCacheStats? = null
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    var activeLesson by remember { mutableStateOf<CurriculumLesson?>(null) }
    val isTraditional = profile.scriptMode == "TRADITIONAL"

    if (activeLesson != null) {
        val lesson = activeLesson!!
        BackHandler { activeLesson = null }
        InteractiveLessonPlayer(
            lesson = lesson,
            isTraditional = isTraditional,
            isCompleted = profile.completedLessonIds.contains(lesson.id),
            onBack = { activeLesson = null },
            onSpeak = onSpeak,
            onFinishLesson = { earnedXp ->
                onCompleteLesson(lesson.id, earnedXp)
                activeLesson = null
            }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        NeoBrutalTabRow(
            tabs = listOf("HSK Units (0–6)", "12-Pt Vocab", "12-Pt Grammar", "Pinyin Lab"),
            selectedIndex = selectedSubTab,
            onSelect = { selectedSubTab = it },
            tagPrefix = "learn_tab"
        )

        when (selectedSubTab) {
            0 -> CurriculumUnitsView(
                profile = profile,
                isTraditional = isTraditional,
                curriculumManifest = curriculumManifest,
                offlineJsonCacheStats = offlineJsonCacheStats,
                onSelectLesson = { activeLesson = it },
                onSpeak = onSpeak
            )
            1 -> VocabularyLabView(
                isTraditional = isTraditional,
                onSpeak = onSpeak,
                onAddVocabToSrs = onAddVocabToSrs,
                onAddCustomVocab = onAddCustomVocab
            )
            2 -> GrammarLibraryView(
                isTraditional = isTraditional,
                onSpeak = onSpeak,
                onCompletedQuiz = { onCompleteLesson("grammar_quiz", 15) }
            )
            3 -> PinyinAcademyView(
                isTraditional = isTraditional,
                onSpeak = onSpeak
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CurriculumUnitsView(
    profile: UserProfile,
    isTraditional: Boolean,
    curriculumManifest: HskCurriculumManifest,
    offlineJsonCacheStats: OfflineJsonCacheStats?,
    onSelectLesson: (CurriculumLesson) -> Unit,
    onSpeak: (String, Boolean) -> Unit
) {
    var selectedStage by remember { mutableIntStateOf(-1) }
    val isHsk2 = profile.hskStandard == "HSK_2"
    val allUnitsFromManifest = curriculumManifest.levels.flatMap { it.units }.ifEmpty { CurriculumCatalog.units }
    val allLessonsFromManifest = curriculumManifest.levels.flatMap { it.lessons }.ifEmpty { CurriculumCatalog.lessons }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "STRUCTURED HSK CURRICULUM · ${if (isHsk2) "HSK 2.0" else "HSK 3.0"}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Loaded offline-first from local JSON manifest (Schema v${curriculumManifest.schemaVersion} · HSK 1–6).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NeoBrutalChip(
                    text = "ALL UNITS",
                    selected = selectedStage == -1,
                    selectedColor = BrutalOrange,
                    onClick = { selectedStage = -1 }
                )
                NeoBrutalChip(
                    text = "STAGE 0 · PINYIN",
                    selected = selectedStage == 0,
                    selectedColor = BrutalOrange,
                    onClick = { selectedStage = 0 }
                )
                (1..6).forEach { stage ->
                    NeoBrutalChip(
                        text = "HSK $stage",
                        selected = selectedStage == stage,
                        selectedColor = BrutalOrange,
                        onClick = { selectedStage = stage }
                    )
                }
            }
        }

        if (selectedStage in 0..6) {
            val selectedLevelCurriculum = curriculumManifest.levels.firstOrNull { it.level == selectedStage }
            if (selectedLevelCurriculum != null) {
                item {
                    val isCachedOnDisk = offlineJsonCacheStats?.cachedLevels?.contains(selectedStage) == true
                    NeoBrutalCard(
                        modifier = Modifier.fillMaxWidth(),
                        accentHeaderColor = BrutalYellow,
                        headerTitle = "${selectedLevelCurriculum.cefrBand} // JSON LEVEL ARCHITECTURE",
                        headerRightText = if (isCachedOnDisk) "OFFLINE JSON CACHED" else "BUNDLED JSON ASSET"
                    ) {
                        Text(
                            text = selectedLevelCurriculum.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedLevelCurriculum.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            NeoBrutalBadge(
                                text = "${selectedLevelCurriculum.units.size} UNITS",
                                backgroundColor = BrutalWhite
                            )
                            NeoBrutalBadge(
                                text = "${selectedLevelCurriculum.lessons.size} LESSONS",
                                backgroundColor = BrutalWhite
                            )
                            NeoBrutalBadge(
                                text = "${selectedLevelCurriculum.vocabulary.size} CORE VOCAB",
                                backgroundColor = BrutalMint
                            )
                            NeoBrutalBadge(
                                text = "${selectedLevelCurriculum.grammar.size} GRAMMAR PATTERNS",
                                backgroundColor = BrutalYellow
                            )
                        }
                    }
                }
            }
        }

        val filteredUnits = allUnitsFromManifest.filter {
            selectedStage == -1 || it.stage == selectedStage
        }

        items(filteredUnits, key = { it.id }) { unit ->
            val unitLessons = allLessonsFromManifest.filter { it.unitId == unit.id }
            val completedInUnit = unitLessons.count { profile.completedLessonIds.contains(it.id) }
            val vocabReq = if (isHsk2) unit.hsk2VocabTarget else unit.hsk3VocabTarget

            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalBlack,
                headerTitle = if (unit.stage == 0) "STAGE 0 // FOUNDATION" else "HSK ${unit.stage} // UNIT ${unit.unitNumber}",
                headerRightText = "$completedInUnit/${unitLessons.size} COMPLETED · $vocabReq WORDS"
            ) {
                Text(
                    text = unit.title.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = unit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    unit.grammarTargets.forEach { gTarget ->
                        NeoBrutalBadge(
                            text = "GRAMMAR: $gTarget",
                            backgroundColor = BrutalCream,
                            textColor = BrutalBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    unitLessons.forEach { lesson ->
                        val isCompleted = profile.completedLessonIds.contains(lesson.id)
                        val isUnlocked = lesson.prerequisiteLessonId == null ||
                            profile.completedLessonIds.contains(lesson.prerequisiteLessonId) ||
                            profile.currentHskLevel >= lesson.stage ||
                            isCompleted

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .neoBrutalSurface(
                                    backgroundColor = when {
                                        isCompleted -> BrutalMint.copy(alpha = 0.2f)
                                        isUnlocked -> BrutalWhite
                                        else -> BrutalCream
                                    },
                                    borderColor = BrutalBlack,
                                    shadowColor = BrutalBlack,
                                    borderWidth = 2.dp,
                                    shadowOffset = 3.dp
                                )
                                .clickable { onSelectLesson(lesson) }
                                .padding(12.dp)
                                .testTag("lesson_card_${lesson.id}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = when {
                                            isCompleted -> Icons.Default.CheckCircle
                                            isUnlocked -> Icons.Default.LockOpen
                                            else -> Icons.Default.Lock
                                        },
                                        contentDescription = null,
                                        tint = if (isCompleted) BrutalBlack else BrutalOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    NeoBrutalBadge(
                                        text = when {
                                            isCompleted -> "✓ COMPLETED"
                                            lesson.lessonType == "UNIT_TEST" -> "UNIT TEST · +${lesson.xpReward} XP"
                                            lesson.lessonType == "LEVEL_ASSESSMENT" -> "ASSESSMENT · +${lesson.xpReward} XP"
                                            isUnlocked -> "UNLOCKED · +${lesson.xpReward} XP"
                                            else -> "PREVIEW / PREREQ"
                                        },
                                        backgroundColor = if (isCompleted) BrutalMint else if (isUnlocked) BrutalYellow else BrutalCream
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = lesson.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${if (isTraditional) lesson.traditionalFocus else lesson.chineseFocus} (${lesson.pinyinFocus})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = BrutalOrange
                                )
                                Text(
                                    text = "${lesson.exercises.size} Interactive Exercises",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            NeoBrutalIconButton(
                                onClick = {
                                    onSpeak(
                                        if (isTraditional) lesson.traditionalFocus else lesson.chineseFocus,
                                        false
                                    )
                                },
                                backgroundColor = BrutalWhite,
                                size = 42.dp
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Play lesson audio",
                                    tint = BrutalBlack
                                )
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

/**
 * Phase 3 & Phase 5: Distraction-Free Step-by-Step Interactive Lesson & Exercise Player
 * Supports all 12 exercise types with immediate WHY feedback.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InteractiveLessonPlayer(
    lesson: CurriculumLesson,
    isTraditional: Boolean,
    isCompleted: Boolean,
    onBack: () -> Unit,
    onSpeak: (String, Boolean) -> Unit,
    onFinishLesson: (Int) -> Unit
) {
    // Step 0 = Theory & Core Expression; Steps 1..N = Interactive Exercises; Step N+1 = Summary
    val totalSteps = lesson.exercises.size + 1
    var currentStep by remember(lesson.id) { mutableIntStateOf(0) }
    var correctCount by remember(lesson.id) { mutableIntStateOf(0) }

    val progress = ((currentStep + 1).toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        // Distraction-Free Top Progress Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NeoBrutalIconButton(
                onClick = onBack,
                backgroundColor = BrutalWhite,
                size = 42.dp,
                modifier = Modifier.testTag("lesson_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Exit lesson",
                    tint = BrutalBlack
                )
            }
            NeoBrutalProgressBar(
                progress = progress,
                fillColor = BrutalOrange,
                trackColor = BrutalWhite,
                height = 16.dp,
                modifier = Modifier.weight(1f)
            )
            NeoBrutalBadge(
                text = "${currentStep + 1}/$totalSteps",
                backgroundColor = BrutalBlack,
                textColor = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (currentStep == 0) {
            // STEP 0: Core Expression & Instructional Briefing
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    NeoBrutalCard(
                        modifier = Modifier.fillMaxWidth(),
                        accentHeaderColor = BrutalBlack,
                        headerTitle = "STEP 1 // CORE EXPRESSION & THEORY",
                        headerRightText = "HSK ${lesson.stage}"
                    ) {
                        Text(
                            text = lesson.title.uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isTraditional) lesson.traditionalFocus else lesson.chineseFocus,
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = lesson.pinyinFocus,
                            style = MaterialTheme.typography.titleLarge,
                            color = BrutalOrange
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            NeoBrutalButton(
                                text = "LISTEN 1.0X",
                                icon = Icons.AutoMirrored.Filled.VolumeUp,
                                backgroundColor = BrutalOrange,
                                contentColor = Color.White,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    onSpeak(
                                        if (isTraditional) lesson.traditionalFocus else lesson.chineseFocus,
                                        false
                                    )
                                }
                            )
                            NeoBrutalButton(
                                text = "SLOW 0.6X",
                                icon = Icons.Default.SlowMotionVideo,
                                backgroundColor = BrutalWhite,
                                contentColor = BrutalBlack,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    onSpeak(
                                        if (isTraditional) lesson.traditionalFocus else lesson.chineseFocus,
                                        true
                                    )
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = lesson.summary,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        lesson.keyPoints.forEach { pt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                NeoBrutalBadge(text = "RULE", backgroundColor = BrutalYellow)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = pt,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            NeoBrutalButton(
                text = "START INTERACTIVE EXERCISES (${lesson.exercises.size})",
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                backgroundColor = BrutalOrange,
                contentColor = Color.White,
                onClick = { currentStep = 1 },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            )
        } else {
            val exIndex = (currentStep - 1).coerceIn(0, lesson.exercises.lastIndex)
            val ex = lesson.exercises[exIndex]

            // State for current exercise
            var selectedOption by remember(ex.id) { mutableStateOf<Int?>(null) }
            val assembledWords = remember(ex.id) { mutableStateListOf<String>() }
            var typedAnswer by remember(ex.id) { mutableStateOf("") }
            var selectedLeftMatch by remember(ex.id) { mutableStateOf<String?>(null) }
            val matchedLeftKeys = remember(ex.id) { mutableStateListOf<String>() }
            var isEvaluated by remember(ex.id) { mutableStateOf(false) }
            var isAnswerCorrect by remember(ex.id) { mutableStateOf(false) }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    NeoBrutalCard(
                        modifier = Modifier.fillMaxWidth(),
                        accentHeaderColor = BrutalBlack,
                        headerTitle = "EXERCISE $currentStep OF ${lesson.exercises.size}",
                        headerRightText = ex.type.displayLabel
                    ) {
                        Text(
                            text = ex.question,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Reading Passage Box if READING_COMPREHENSION
                        if (ex.type == ExerciseType.READING_COMPREHENSION && ex.readingPassageChinese.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .neoBrutalSurface(
                                        backgroundColor = BrutalCream,
                                        borderColor = BrutalBlack,
                                        shadowColor = BrutalBlack,
                                        borderWidth = 2.dp,
                                        shadowOffset = 3.dp
                                    )
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = ex.readingPassageChinese,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = BrutalBlack
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ex.readingPassagePinyin,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = BrutalOrange
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Incorrect Sentence Box if GRAMMAR_CORRECTION
                        if (ex.type == ExerciseType.GRAMMAR_CORRECTION && ex.incorrectSentence.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .neoBrutalSurface(
                                        backgroundColor = BrutalYellow.copy(alpha = 0.35f),
                                        borderColor = BrutalBlack,
                                        shadowColor = BrutalBlack,
                                        borderWidth = 2.dp,
                                        shadowOffset = 2.5.dp
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "✗ ERROR TO FIX: ${ex.incorrectSentence}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrutalBlack
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Prompt + Audio Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .neoBrutalSurface(
                                    backgroundColor = BrutalCream,
                                    borderColor = BrutalBlack,
                                    shadowColor = BrutalBlack,
                                    borderWidth = 2.dp,
                                    shadowOffset = 2.5.dp
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ex.promptChinese,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = BrutalBlack
                                )
                                Text(
                                    text = ex.promptPinyin,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = BrutalOrange
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                NeoBrutalIconButton(
                                    onClick = { onSpeak(ex.promptChinese, false) },
                                    backgroundColor = BrutalWhite,
                                    size = 40.dp
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Listen 1.0x",
                                        tint = BrutalBlack
                                    )
                                }
                                NeoBrutalIconButton(
                                    onClick = { onSpeak(ex.promptChinese, true) },
                                    backgroundColor = BrutalYellow,
                                    size = 40.dp
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SlowMotionVideo,
                                        contentDescription = "Listen slow 0.6x",
                                        tint = BrutalBlack
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        when (ex.type) {
                            ExerciseType.SENTENCE_BUILDER -> {
                                Text(
                                    text = "TAP WORD TILES IN ORDER TO BUILD THE SENTENCE:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .neoBrutalSurface(
                                            backgroundColor = BrutalWhite,
                                            borderColor = BrutalBlack,
                                            shadowColor = BrutalBlack,
                                            borderWidth = 2.dp,
                                            shadowOffset = 3.dp
                                        )
                                        .padding(12.dp)
                                ) {
                                    if (assembledWords.isEmpty()) {
                                        Text(
                                            text = "Tap word tiles below to assemble...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            assembledWords.forEachIndexed { idx, tile ->
                                                NeoBrutalChip(
                                                    text = tile,
                                                    selected = true,
                                                    selectedColor = BrutalOrange,
                                                    onClick = {
                                                        if (!isEvaluated) assembledWords.removeAt(idx)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ex.wordBankTiles.forEach { tile ->
                                        val alreadyUsed = assembledWords.contains(tile)
                                        NeoBrutalChip(
                                            text = tile,
                                            selected = false,
                                            unselectedColor = if (alreadyUsed) BrutalCream else BrutalWhite,
                                            onClick = {
                                                if (!isEvaluated && !alreadyUsed) {
                                                    assembledWords.add(tile)
                                                }
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                if (!isEvaluated) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        NeoBrutalButton(
                                            text = "CHECK WORD ORDER",
                                            backgroundColor = BrutalYellow,
                                            enabled = assembledWords.isNotEmpty(),
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                val ok = assembledWords.toList() == ex.correctWordOrder
                                                isAnswerCorrect = ok
                                                isEvaluated = true
                                                if (ok) correctCount++
                                            }
                                        )
                                        NeoBrutalIconButton(
                                            onClick = { assembledWords.clear() },
                                            backgroundColor = BrutalWhite,
                                            size = 52.dp
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Reset tiles",
                                                tint = BrutalBlack
                                            )
                                        }
                                    }
                                }
                            }

                            ExerciseType.MATCH_PAIRS -> {
                                Text(
                                    text = "TAP A CHINESE TERM ON THE LEFT, THEN ITS MATCH ON THE RIGHT:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val rightItems = remember(ex.id) { ex.matchPairs.map { it.second }.reversed() }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ex.matchPairs.forEach { (leftKey, _) ->
                                            val isMatched = matchedLeftKeys.contains(leftKey)
                                            val isPicked = selectedLeftMatch == leftKey
                                            NeoBrutalChip(
                                                text = if (isMatched) "✓ $leftKey" else leftKey,
                                                selected = isPicked || isMatched,
                                                selectedColor = if (isMatched) BrutalMint else BrutalOrange,
                                                onClick = {
                                                    if (!isMatched) selectedLeftMatch = leftKey
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rightItems.forEach { rightVal ->
                                            val matchingPair = ex.matchPairs.find { it.second == rightVal }
                                            val isMatched = matchingPair != null && matchedLeftKeys.contains(matchingPair.first)
                                            NeoBrutalChip(
                                                text = if (isMatched) "✓ $rightVal" else rightVal,
                                                selected = isMatched,
                                                selectedColor = BrutalMint,
                                                onClick = {
                                                    val currentLeft = selectedLeftMatch
                                                    if (currentLeft != null && matchingPair?.first == currentLeft) {
                                                        matchedLeftKeys.add(currentLeft)
                                                        selectedLeftMatch = null
                                                        if (matchedLeftKeys.size == ex.matchPairs.size && !isEvaluated) {
                                                            isAnswerCorrect = true
                                                            isEvaluated = true
                                                            correctCount++
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }

                            ExerciseType.TYPE_CHARACTER -> {
                                NeoBrutalTextField(
                                    value = typedAnswer,
                                    onValueChange = { typedAnswer = it },
                                    label = "Type Chinese Characters or Pinyin",
                                    placeholder = "e.g. ${ex.promptChinese} or ${ex.promptPinyin}"
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                if (!isEvaluated) {
                                    NeoBrutalButton(
                                        text = "VERIFY ANSWER",
                                        backgroundColor = BrutalYellow,
                                        enabled = typedAnswer.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            val normalized = typedAnswer.trim().lowercase()
                                            val ok = ex.expectedTypedAnswers.any { it.lowercase() == normalized } ||
                                                normalized == ex.promptChinese.lowercase()
                                            isAnswerCorrect = ok
                                            isEvaluated = true
                                            if (ok) correctCount++
                                        }
                                    )
                                }
                            }

                            else -> {
                                // Multiple Choice / Character Recognition / Fill Blank / Translate / Listen / Tone / Grammar Correction
                                ex.options.forEachIndexed { idx, opt ->
                                    val isSelected = selectedOption == idx
                                    val isCorrect = idx == ex.correctIndex
                                    val optBg = when {
                                        !isEvaluated -> MaterialTheme.colorScheme.surface
                                        isCorrect -> BrutalMint
                                        isSelected -> BrutalOrange
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                    val optFg = if (isEvaluated && isSelected && !isCorrect) Color.White else BrutalBlack
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .neoBrutalSurface(
                                                backgroundColor = optBg,
                                                borderColor = BrutalBlack,
                                                shadowColor = BrutalBlack,
                                                borderWidth = 2.dp,
                                                shadowOffset = 3.dp
                                            )
                                            .clickable(enabled = !isEvaluated) {
                                                selectedOption = idx
                                                isAnswerCorrect = (idx == ex.correctIndex)
                                                isEvaluated = true
                                                if (idx == ex.correctIndex) correctCount++
                                            }
                                            .padding(14.dp)
                                    ) {
                                        Text(
                                            text = opt,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!isEvaluated) MaterialTheme.colorScheme.onSurface else optFg
                                        )
                                    }
                                }
                            }
                        }

                        // Immediate WHY Feedback Banner
                        AnimatedVisibility(visible = isEvaluated) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .neoBrutalSurface(
                                        backgroundColor = if (isAnswerCorrect) BrutalMint else BrutalYellow,
                                        borderColor = BrutalBlack,
                                        shadowColor = BrutalBlack,
                                        borderWidth = 2.dp,
                                        shadowOffset = 3.dp
                                    )
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (isAnswerCorrect) "✓ CORRECT ANSWER" else "✗ WHY THIS MATTERS",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = BrutalBlack
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ex.explanation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = BrutalBlack
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            if (currentStep < lesson.exercises.size) {
                NeoBrutalButton(
                    text = "NEXT EXERCISE",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    backgroundColor = if (isEvaluated) BrutalOrange else BrutalYellow,
                    contentColor = if (isEvaluated) Color.White else BrutalBlack,
                    onClick = { currentStep++ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                )
            } else {
                NeoBrutalButton(
                    text = if (isCompleted) "FINISH REVIEW (+${lesson.xpReward} XP)" else "COMPLETE LESSON (+${lesson.xpReward} XP)",
                    icon = Icons.Default.CheckCircle,
                    backgroundColor = BrutalMint,
                    contentColor = BrutalBlack,
                    onClick = { onFinishLesson(lesson.xpReward) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .testTag("complete_lesson_button")
                )
            }
        }
    }
}

/**
 * Phase 3: Complete 12-Point Vocabulary Teaching Lab
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VocabularyLabView(
    isTraditional: Boolean,
    onSpeak: (String, Boolean) -> Unit,
    onAddVocabToSrs: (VocabReference) -> Unit,
    onAddCustomVocab: (String, String, String, String, Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var hskFilter by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    val selectedQuizAnswers = remember { mutableStateMapOf<String, Int>() }

    if (showAddDialog) {
        var simp by remember { mutableStateOf("") }
        var trad by remember { mutableStateOf("") }
        var pin by remember { mutableStateOf("") }
        var eng by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "ADD CUSTOM VOCABULARY",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NeoBrutalTextField(value = simp, onValueChange = { simp = it }, label = "Simplified Chinese", placeholder = "e.g. 坚持")
                    NeoBrutalTextField(value = trad, onValueChange = { trad = it }, label = "Traditional Chinese", placeholder = "e.g. 堅持")
                    NeoBrutalTextField(value = pin, onValueChange = { pin = it }, label = "Pinyin with Tones", placeholder = "e.g. jiānchí")
                    NeoBrutalTextField(value = eng, onValueChange = { eng = it }, label = "English Meaning", placeholder = "To persist; persevere")
                }
            },
            confirmButton = {
                NeoBrutalButton(
                    text = "SAVE TO SRS",
                    backgroundColor = BrutalMint,
                    onClick = {
                        if (simp.isNotBlank() && eng.isNotBlank()) {
                            onAddCustomVocab(simp, trad.ifBlank { simp }, pin.ifBlank { "hànzì" }, eng, 1)
                            showAddDialog = false
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("CANCEL", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    val filteredVocab = CurriculumCatalog.vocabDatabase.filter { v ->
        val matchesLevel = hskFilter == 0 || v.hskLevel == hskFilter
        val q = searchQuery.trim().lowercase()
        val matchesQuery = q.isEmpty() ||
            v.simplified.contains(q) ||
            v.traditional.contains(q) ||
            v.pinyin.lowercase().contains(q) ||
            v.english.lowercase().contains(q)
        matchesLevel && matchesQuery
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "12-POINT VOCABULARY LAB",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                NeoBrutalButton(
                    text = "+ CUSTOM WORD",
                    icon = Icons.Default.Add,
                    backgroundColor = BrutalYellow,
                    height = 42.dp,
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_custom_vocab_button")
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            NeoBrutalTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search HSK 1–6 Lexicon",
                placeholder = "Search Hanzi, Pinyin, or English...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.testTag("vocab_search_input")
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NeoBrutalChip(
                    text = "ALL LEVELS",
                    selected = hskFilter == 0,
                    selectedColor = BrutalOrange,
                    onClick = { hskFilter = 0 }
                )
                (1..6).forEach { lvl ->
                    NeoBrutalChip(
                        text = "HSK $lvl",
                        selected = hskFilter == lvl,
                        selectedColor = BrutalOrange,
                        onClick = { hskFilter = lvl }
                    )
                }
            }
        }

        items(filteredVocab, key = { it.id }) { word ->
            val pickedQuiz = selectedQuizAnswers[word.id]
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalBlack,
                headerTitle = "HSK ${word.hskLevel} // 4. ${word.category} · ${word.partOfSpeech}",
                headerRightText = "12-PT CARD"
            ) {
                // 1. Character, 2. Pinyin, 3. Meaning
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (isTraditional) word.traditional else word.simplified,
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = word.pinyin,
                                style = MaterialTheme.typography.titleLarge,
                                color = BrutalOrange
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = word.english,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        NeoBrutalIconButton(
                            onClick = { onSpeak(word.simplified, false) },
                            backgroundColor = BrutalOrange,
                            contentColor = Color.White,
                            size = 42.dp
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Pronounce ${word.simplified}",
                                tint = Color.White
                            )
                        }
                        NeoBrutalIconButton(
                            onClick = { onSpeak(word.simplified, true) },
                            backgroundColor = BrutalWhite,
                            size = 42.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.SlowMotionVideo,
                                contentDescription = "Slow audio",
                                tint = BrutalBlack
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                // 5. Pronunciation Guidance & 6. Stroke Order
                Text(
                    text = "5. PRONUNCIATION // ${word.pronunciationGuide}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "6. STROKE ORDER // ${word.strokeOrderGuide}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))
                // 7 & 8. Multiple Example Sentences + Pinyin + English Translations
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalSurface(
                            backgroundColor = BrutalCream,
                            borderColor = BrutalBlack,
                            shadowColor = BrutalBlack,
                            borderWidth = 2.dp,
                            shadowOffset = 2.5.dp
                        )
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "7–8. EXAMPLES IN CONTEXT:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BrutalBlack
                        )
                        Text(
                            text = "① ${if (isTraditional) word.exampleTraditional else word.exampleSimplified}",
                            style = MaterialTheme.typography.titleMedium,
                            color = BrutalBlack
                        )
                        Text(
                            text = "${word.examplePinyin} — ${word.exampleEnglish}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BrutalOrange
                        )
                        if (word.secondExampleSimplified.isNotBlank()) {
                            Text(
                                text = "② ${word.secondExampleSimplified}",
                                style = MaterialTheme.typography.titleMedium,
                                color = BrutalBlack
                            )
                            Text(
                                text = "${word.secondExamplePinyin} — ${word.secondExampleEnglish}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                // 9. Common Usage, 10. Related Vocab, 11. Common Mistakes
                Text(
                    text = "9. COMMON USAGE // ${word.commonUsage}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (word.relatedVocabulary.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "10. RELATED // ${word.relatedVocabulary.joinToString(" · ")}",
                        style = MaterialTheme.typography.labelLarge,
                        color = BrutalCobalt
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "11. COMMON MISTAKE // ${word.commonMistake}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrutalOrange
                )

                // 12. Interactive Mini Practice Question
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "12. QUICK CHECK // ${word.practiceQuestion}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                word.practiceOptions.forEachIndexed { idx, opt ->
                    val isChosen = pickedQuiz == idx
                    val isRight = idx == word.practiceCorrectIndex
                    val bg = when {
                        pickedQuiz == null -> BrutalWhite
                        isRight -> BrutalMint
                        isChosen -> BrutalOrange
                        else -> BrutalWhite
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .neoBrutalSurface(
                                backgroundColor = bg,
                                borderColor = BrutalBlack,
                                shadowColor = BrutalBlack,
                                borderWidth = 1.5.dp,
                                shadowOffset = 2.dp
                            )
                            .clickable { selectedQuizAnswers[word.id] = idx }
                            .padding(10.dp)
                    ) {
                        Text(
                            text = opt,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (pickedQuiz != null && isChosen && !isRight) Color.White else BrutalBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                NeoBrutalButton(
                    text = "+ SAVE WORD TO SRS FLASHCARD DECK",
                    backgroundColor = BrutalYellow,
                    contentColor = BrutalBlack,
                    height = 42.dp,
                    onClick = { onAddVocabToSrs(word) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

/**
 * Phase 3: Complete 12-Point Grammar Teaching Library
 */
@Composable
private fun GrammarLibraryView(
    isTraditional: Boolean,
    onSpeak: (String, Boolean) -> Unit,
    onCompletedQuiz: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val selectedQuizAnswers = remember { mutableStateMapOf<String, Int>() }

    val filteredGrammar = CurriculumExtras.grammarTopics.filter { g ->
        val q = searchQuery.trim().lowercase()
        q.isEmpty() ||
            g.particle.lowercase().contains(q) ||
            g.title.lowercase().contains(q) ||
            g.explanation.lowercase().contains(q)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "12-POINT MANDARIN GRAMMAR LIBRARY",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))
            NeoBrutalTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search Grammar Concept",
                placeholder = "Search 是, 有, 在, 的, 了, 过, 比, 把, 被, 虽然, 无论...",
                leadingIcon = Icons.Default.Search
            )
        }

        items(filteredGrammar, key = { it.id }) { topic ->
            val selectedOpt = selectedQuizAnswers[topic.id]
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalBlack,
                headerTitle = "1. ${topic.particle} // ${topic.title}",
                headerRightText = "HSK ${topic.hskLevel}"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeoBrutalBadge(
                        text = "3. FORMULA: ${topic.pattern}",
                        backgroundColor = BrutalYellow,
                        textColor = BrutalBlack
                    )
                    NeoBrutalIconButton(
                        onClick = { onSpeak(topic.exampleSimplified, false) },
                        backgroundColor = BrutalOrange,
                        contentColor = Color.White,
                        size = 40.dp
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak grammar example",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "2. EXPLANATION // ${topic.explanation}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "4. WORD ORDER // ${topic.wordOrderBreakdown}",
                    style = MaterialTheme.typography.labelLarge,
                    color = BrutalCobalt
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "5. WHEN TO USE // ${topic.whenToUse}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "6. WHEN NOT TO USE // ${topic.whenNotToUse}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrutalOrange
                )

                Spacer(modifier = Modifier.height(10.dp))
                // 7, 8, 9. Multiple Chinese Examples + Pinyin + English
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalSurface(
                            backgroundColor = BrutalCream,
                            borderColor = BrutalBlack,
                            shadowColor = BrutalBlack,
                            borderWidth = 2.dp,
                            shadowOffset = 2.5.dp
                        )
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "7–9. EXAMPLES WITH PINYIN & TRANSLATION:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BrutalBlack
                        )
                        Text(
                            text = "① ${if (isTraditional) topic.exampleTraditional else topic.exampleSimplified}",
                            style = MaterialTheme.typography.titleMedium,
                            color = BrutalBlack
                        )
                        Text(
                            text = "${topic.examplePinyin} — ${topic.exampleEnglish}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BrutalOrange
                        )
                        if (topic.secondExampleSimplified.isNotBlank()) {
                            Text(
                                text = "② ${topic.secondExampleSimplified}",
                                style = MaterialTheme.typography.titleMedium,
                                color = BrutalBlack
                            )
                            Text(
                                text = "${topic.secondExamplePinyin} — ${topic.secondExampleEnglish}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BrutalBlack
                            )
                        }
                        if (topic.thirdExampleSimplified.isNotBlank()) {
                            Text(
                                text = "③ ${topic.thirdExampleSimplified}",
                                style = MaterialTheme.typography.titleMedium,
                                color = BrutalBlack
                            )
                            Text(
                                text = "${topic.thirdExamplePinyin} — ${topic.thirdExampleEnglish}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BrutalBlack
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "10. COMPARISON // ${topic.comparisonWithSimilar}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "11. COMMON MISTAKE // ${topic.commonMistake}",
                    style = MaterialTheme.typography.labelLarge,
                    color = BrutalOrange
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "12. PRACTICE EXERCISE // ${topic.quizQuestion}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                topic.quizOptions.forEachIndexed { idx, opt ->
                    val isChosen = selectedOpt == idx
                    val isRight = idx == topic.quizCorrectIndex
                    val optBg = when {
                        selectedOpt == null -> MaterialTheme.colorScheme.surface
                        isRight -> BrutalMint
                        isChosen -> BrutalOrange
                        else -> MaterialTheme.colorScheme.surface
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .neoBrutalSurface(
                                backgroundColor = optBg,
                                borderColor = BrutalBlack,
                                shadowColor = BrutalBlack,
                                borderWidth = 2.dp,
                                shadowOffset = 2.5.dp
                            )
                            .clickable {
                                selectedQuizAnswers[topic.id] = idx
                                if (idx == topic.quizCorrectIndex) {
                                    onCompletedQuiz()
                                }
                            }
                            .padding(12.dp)
                    ) {
                        Text(
                            text = opt,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedOpt != null && isChosen && !isRight) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                AnimatedVisibility(visible = selectedOpt != null) {
                    Text(
                        text = "WHY: ${topic.quizExplanation}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrutalCobalt,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PinyinAcademyView(
    isTraditional: Boolean,
    onSpeak: (String, Boolean) -> Unit
) {
    var groupFilter by remember { mutableStateOf("INITIAL") }
    var selectedPinyin by remember { mutableStateOf(CurriculumCatalog.pinyinItems.first()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "PINYIN & TONE SANDHI LAB",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("INITIAL" to "INITIALS", "FINAL" to "FINALS & Ü", "TONE_PAIR" to "TONE SANDHI").forEach { (key, label) ->
                    NeoBrutalChip(
                        text = label,
                        selected = groupFilter == key,
                        selectedColor = BrutalOrange,
                        onClick = {
                            groupFilter = key
                            selectedPinyin = CurriculumCatalog.pinyinItems.first { it.group == key }
                        }
                    )
                }
            }
        }

        item {
            val itemsInGroup = CurriculumCatalog.pinyinItems.filter { it.group == groupFilter }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsInGroup.forEach { item ->
                    val isSelected = selectedPinyin.id == item.id
                    Box(
                        modifier = Modifier
                            .neoBrutalSurface(
                                backgroundColor = if (isSelected) BrutalOrange else MaterialTheme.colorScheme.surface,
                                borderColor = BrutalBlack,
                                shadowColor = BrutalBlack,
                                borderWidth = 2.dp,
                                shadowOffset = if (isSelected) 3.5.dp else 2.dp
                            )
                            .clickable {
                                selectedPinyin = item
                                onSpeak(item.exampleHanzi, false)
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = item.symbol,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.ipaGuide,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) BrutalYellow else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalBlack,
                headerTitle = "ARTICULATION INSPECTOR // ${selectedPinyin.symbol}",
                headerRightText = selectedPinyin.ipaGuide
            ) {
                Text(
                    text = "${if (isTraditional) selectedPinyin.exampleTrad else selectedPinyin.exampleHanzi} (${selectedPinyin.examplePinyin})",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "MEANING: ${selectedPinyin.exampleMeaning.uppercase()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = BrutalOrange
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NeoBrutalButton(
                        text = "NORMAL 1.0X",
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        backgroundColor = BrutalOrange,
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = { onSpeak(selectedPinyin.exampleHanzi, false) }
                    )
                    NeoBrutalButton(
                        text = "SLOW 0.6X",
                        icon = Icons.Default.SlowMotionVideo,
                        backgroundColor = BrutalWhite,
                        contentColor = BrutalBlack,
                        modifier = Modifier.weight(1f),
                        onClick = { onSpeak(selectedPinyin.exampleHanzi, true) }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "MOUTH & TONGUE POSITION: ${selectedPinyin.englishHint}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "COMMON MISTAKE: ${selectedPinyin.commonMistake}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrutalOrange
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
