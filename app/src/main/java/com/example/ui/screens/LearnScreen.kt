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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.unit.sp
import com.example.data.curriculum.CurriculumCatalog
import com.example.data.curriculum.CurriculumExtras
import com.example.data.curriculum.CurriculumLesson
import com.example.data.curriculum.VocabReference
import com.example.data.model.UserProfile
import com.example.ui.theme.BrutalBlack
import com.example.ui.theme.BrutalCobalt
import com.example.ui.theme.BrutalMint
import com.example.ui.theme.BrutalOrange
import com.example.ui.theme.BrutalWhite
import com.example.ui.theme.BrutalYellow
import com.example.ui.theme.NeoBrutalBadge
import com.example.ui.theme.NeoBrutalButton
import com.example.ui.theme.NeoBrutalCard
import com.example.ui.theme.NeoBrutalChip
import com.example.ui.theme.NeoBrutalIconButton
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
    onAddCustomVocab: (simplified: String, traditional: String, pinyin: String, english: String, hskLevel: Int) -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    var activeLesson by remember { mutableStateOf<CurriculumLesson?>(null) }
    val isTraditional = profile.scriptMode == "TRADITIONAL"

    if (activeLesson != null) {
        val lesson = activeLesson!!
        BackHandler { activeLesson = null }
        LessonDetailSheet(
            lesson = lesson,
            isTraditional = isTraditional,
            isCompleted = profile.completedLessonIds.contains(lesson.id),
            onBack = { activeLesson = null },
            onSpeak = onSpeak,
            onFinishLesson = {
                onCompleteLesson(lesson.id, 40)
                activeLesson = null
            }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        NeoBrutalTabRow(
            tabs = listOf("HSK Path (0–6)", "Pinyin Academy", "Vocabulary Lab", "Grammar Library"),
            selectedIndex = selectedSubTab,
            onSelect = { selectedSubTab = it },
            tagPrefix = "learn_tab"
        )

        when (selectedSubTab) {
            0 -> CurriculumPathView(
                profile = profile,
                isTraditional = isTraditional,
                onSelectLesson = { activeLesson = it },
                onSpeak = onSpeak
            )
            1 -> PinyinAcademyView(
                isTraditional = isTraditional,
                onSpeak = onSpeak,
                onCompleteDrill = { onCompleteLesson("pinyin_drill", 20) }
            )
            2 -> VocabularyLabView(
                isTraditional = isTraditional,
                onSpeak = onSpeak,
                onAddVocabToSrs = onAddVocabToSrs,
                onAddCustomVocab = onAddCustomVocab
            )
            3 -> GrammarLibraryView(
                isTraditional = isTraditional,
                onSpeak = onSpeak,
                onCompletedQuiz = { onCompleteLesson("grammar_quiz", 15) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CurriculumPathView(
    profile: UserProfile,
    isTraditional: Boolean,
    onSelectLesson: (CurriculumLesson) -> Unit,
    onSpeak: (String, Boolean) -> Unit
) {
    var selectedStage by remember { mutableIntStateOf(-1) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "COMPLETE MANDARIN CURRICULUM (${if (profile.hskStandard == "HSK_2") "HSK 2.0" else "HSK 3.0"})",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NeoBrutalChip(
                    text = "ALL STAGES",
                    selected = selectedStage == -1,
                    selectedColor = BrutalYellow,
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
                        selectedColor = BrutalCobalt,
                        onClick = { selectedStage = stage }
                    )
                }
            }
        }

        val filtered = CurriculumCatalog.lessons.filter {
            selectedStage == -1 || it.stage == selectedStage
        }

        items(filtered, key = { it.id }) { lesson ->
            val completed = profile.completedLessonIds.contains(lesson.id)
            val headerBg = when (lesson.stage) {
                0 -> BrutalYellow
                1, 2 -> BrutalOrange
                3, 4 -> BrutalCobalt
                else -> BrutalMint
            }
            NeoBrutalCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lesson_card_${lesson.id}"),
                accentHeaderColor = headerBg,
                headerTitle = if (lesson.stage == 0) "STAGE 0 // PINYIN FOUNDATION" else "STAGE ${lesson.stage} // HSK ${lesson.stage}",
                headerRightText = if (completed) "✓ COMPLETED" else "+40 XP",
                onClick = { onSelectLesson(lesson) }
            ) {
                Text(
                    text = lesson.title.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lesson.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalSurface(
                            backgroundColor = BrutalYellow.copy(alpha = 0.25f),
                            borderColor = BrutalBlack,
                            shadowColor = BrutalBlack,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTraditional) lesson.traditionalFocus else lesson.chineseFocus,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = lesson.pinyinFocus,
                            style = MaterialTheme.typography.labelLarge,
                            color = BrutalOrange
                        )
                    }
                    NeoBrutalIconButton(
                        onClick = {
                            onSpeak(
                                if (isTraditional) lesson.traditionalFocus else lesson.chineseFocus,
                                false
                            )
                        },
                        backgroundColor = BrutalYellow,
                        size = 44.dp
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
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun LessonDetailSheet(
    lesson: CurriculumLesson,
    isTraditional: Boolean,
    isCompleted: Boolean,
    onBack: () -> Unit,
    onSpeak: (String, Boolean) -> Unit,
    onFinishLesson: () -> Unit
) {
    val selectedAnswers = remember { mutableStateMapOf<String, Int>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                NeoBrutalIconButton(
                    onClick = onBack,
                    backgroundColor = BrutalYellow,
                    modifier = Modifier.testTag("lesson_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to curriculum",
                        tint = BrutalBlack
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = lesson.title.uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        item {
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalCobalt,
                headerTitle = "CORE EXPRESSION & THEORY",
                headerRightText = "HSK ${lesson.stage}"
            ) {
                Text(
                    text = if (isTraditional) lesson.traditionalFocus else lesson.chineseFocus,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lesson.pinyinFocus,
                    style = MaterialTheme.typography.titleLarge,
                    color = BrutalOrange
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NeoBrutalButton(
                        text = "NORMAL 1.0X",
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        backgroundColor = BrutalYellow,
                        contentColor = BrutalBlack,
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
                        NeoBrutalBadge(text = "KEY", backgroundColor = BrutalYellow)
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

        items(lesson.exercises, key = { it.id }) { ex ->
            val chosen = selectedAnswers[ex.id]
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalOrange,
                headerTitle = "INTERACTIVE CHECKPOINT",
                headerRightText = "DRILL"
            ) {
                Text(
                    text = ex.question,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${ex.promptChinese} (${ex.promptPinyin})",
                    style = MaterialTheme.typography.labelLarge,
                    color = BrutalCobalt
                )
                Spacer(modifier = Modifier.height(12.dp))
                ex.options.forEachIndexed { idx, opt ->
                    val isSelected = chosen == idx
                    val isCorrect = idx == ex.correctIndex
                    val optBg = when {
                        chosen == null -> MaterialTheme.colorScheme.surface
                        isCorrect -> BrutalMint
                        isSelected -> BrutalOrange
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val optFg = if (chosen != null && isSelected && !isCorrect) Color.White else BrutalBlack
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
                            .clickable { selectedAnswers[ex.id] = idx }
                            .padding(14.dp)
                    ) {
                        Text(
                            text = opt,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (chosen == null) MaterialTheme.colorScheme.onSurface else optFg
                        )
                    }
                }
                AnimatedVisibility(visible = chosen != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .neoBrutalSurface(
                                backgroundColor = if (chosen == ex.correctIndex) BrutalMint else BrutalYellow,
                                borderColor = BrutalBlack,
                                shadowColor = BrutalBlack,
                                borderWidth = 2.dp,
                                shadowOffset = 2.dp
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = if (chosen == ex.correctIndex) "✓ CORRECT // ${ex.explanation}"
                            else "✗ EXPLANATION // ${ex.explanation}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrutalBlack
                        )
                    }
                }
            }
        }

        item {
            NeoBrutalButton(
                text = if (isCompleted) "REVIEW COMPLETE (+40 XP)" else "MARK LESSON COMPLETE (+40 XP)",
                icon = Icons.Default.CheckCircle,
                backgroundColor = BrutalMint,
                contentColor = BrutalBlack,
                onClick = onFinishLesson,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("complete_lesson_button")
            )
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PinyinAcademyView(
    isTraditional: Boolean,
    onSpeak: (String, Boolean) -> Unit,
    onCompleteDrill: () -> Unit
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
                text = "PINYIN ACADEMY // PHONETICS & TONES",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("INITIAL" to "21 INITIALS", "FINAL" to "FINALS & Ü", "TONE_PAIR" to "TONE SANDHI").forEach { (key, label) ->
                    NeoBrutalChip(
                        text = label,
                        selected = groupFilter == key,
                        selectedColor = BrutalYellow,
                        onClick = {
                            groupFilter = key
                            selectedPinyin = CurriculumCatalog.pinyinItems.first { it.group == key }
                        }
                    )
                }
            }
        }

        // Interactive Neo-Brutalist Grid of Pinyin Symbols
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
                                borderWidth = 2.5.dp,
                                shadowOffset = if (isSelected) 4.dp else 2.5.dp
                            )
                            .clickable {
                                selectedPinyin = item
                                onSpeak(item.exampleHanzi, false)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = item.symbol,
                                style = MaterialTheme.typography.titleLarge,
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

        // Detailed Phonetic & Articulation Inspector Card
        item {
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalCobalt,
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
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NeoBrutalButton(
                        text = "NORMAL 1.0X",
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        backgroundColor = BrutalYellow,
                        contentColor = BrutalBlack,
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
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalSurface(
                            backgroundColor = BrutalYellow.copy(alpha = 0.25f),
                            borderColor = BrutalBlack,
                            shadowColor = BrutalBlack,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "TONGUE & MOUTH POSITION:",
                            style = MaterialTheme.typography.labelLarge,
                            color = BrutalBlack
                        )
                        Text(
                            text = selectedPinyin.englishHint,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "AVOID THIS TRAP:",
                            style = MaterialTheme.typography.labelLarge,
                            color = BrutalOrange
                        )
                        Text(
                            text = selectedPinyin.commonMistake,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

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
                    NeoBrutalTextField(
                        value = simp,
                        onValueChange = { simp = it },
                        label = "Simplified Chinese",
                        placeholder = "e.g. 坚持"
                    )
                    NeoBrutalTextField(
                        value = trad,
                        onValueChange = { trad = it },
                        label = "Traditional Chinese (Optional)",
                        placeholder = "e.g. 堅持"
                    )
                    NeoBrutalTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = "Pinyin",
                        placeholder = "e.g. jiānchí"
                    )
                    NeoBrutalTextField(
                        value = eng,
                        onValueChange = { eng = it },
                        label = "English Meaning",
                        placeholder = "To persist; persevere"
                    )
                }
            },
            confirmButton = {
                NeoBrutalButton(
                    text = "SAVE TO SRS",
                    backgroundColor = BrutalMint,
                    onClick = {
                        if (simp.isNotBlank() && eng.isNotBlank()) {
                            onAddCustomVocab(simp, trad.ifBlank { simp }, pin.ifBlank { "zì" }, eng, 1)
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
                    text = "VOCABULARY LAB",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                NeoBrutalButton(
                    text = "+ CUSTOM WORD",
                    icon = Icons.Default.Add,
                    backgroundColor = BrutalYellow,
                    height = 44.dp,
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_custom_vocab_button")
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            NeoBrutalTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search Lexicon",
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
                    selectedColor = BrutalYellow,
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
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalYellow,
                headerTitle = "HSK ${word.hskLevel} // ${word.category}",
                headerRightText = word.partOfSpeech.uppercase()
            ) {
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    NeoBrutalIconButton(
                        onClick = { onSpeak(word.simplified, false) },
                        backgroundColor = BrutalCobalt,
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Pronounce ${word.simplified}",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalSurface(
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            borderColor = BrutalBlack,
                            shadowColor = BrutalBlack,
                            borderWidth = 2.dp,
                            shadowOffset = 2.5.dp
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = if (isTraditional) word.exampleTraditional else word.exampleSimplified,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = word.examplePinyin,
                            style = MaterialTheme.typography.labelLarge,
                            color = BrutalCobalt
                        )
                        Text(
                            text = word.exampleEnglish,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                NeoBrutalButton(
                    text = "+ SEND TO SM-2 FLASHCARD DECK",
                    backgroundColor = BrutalMint,
                    contentColor = BrutalBlack,
                    height = 44.dp,
                    onClick = { onAddVocabToSrs(word) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

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
                text = "MANDARIN GRAMMAR COMPENDIUM (17 PATTERNS)",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))
            NeoBrutalTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Filter Grammar Pattern",
                placeholder = "Search 是, 有, 在, 的, 了, 把, 被, 比...",
                leadingIcon = Icons.Default.Search
            )
        }

        items(filteredGrammar, key = { it.id }) { topic ->
            val selectedOpt = selectedQuizAnswers[topic.id]
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalCobalt,
                headerTitle = "${topic.particle} // ${topic.title}",
                headerRightText = "HSK ${topic.hskLevel}"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeoBrutalBadge(
                        text = "FORMULA: ${topic.pattern}",
                        backgroundColor = BrutalYellow,
                        textColor = BrutalBlack
                    )
                    NeoBrutalIconButton(
                        onClick = { onSpeak(topic.exampleSimplified, false) },
                        backgroundColor = BrutalYellow,
                        size = 42.dp
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak grammar example",
                            tint = BrutalBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = topic.explanation,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalSurface(
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            borderColor = BrutalBlack,
                            shadowColor = BrutalBlack,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = if (isTraditional) topic.exampleTraditional else topic.exampleSimplified,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = topic.examplePinyin,
                            style = MaterialTheme.typography.labelLarge,
                            color = BrutalOrange
                        )
                        Text(
                            text = topic.exampleEnglish,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✗ COMMON MISTAKE: ${topic.commonMistake}",
                    style = MaterialTheme.typography.labelLarge,
                    color = BrutalOrange
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "CHECKPOINT: ${topic.quizQuestion}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
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
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
