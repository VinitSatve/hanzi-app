package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.curriculum.CurriculumExtras
import com.example.data.curriculum.SpeakingPrompt
import com.example.data.model.FlashcardItem
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
import com.example.ui.theme.neoBrutalSurface
import com.example.ui.viewmodel.AiUiState
import com.example.ui.viewmodel.PronunciationEvaluation
import com.example.util.MandarinSpeechManager
import kotlinx.coroutines.delay

@Composable
fun PracticeScreen(
    initialSubTab: Int = 0,
    profile: UserProfile,
    flashcards: List<FlashcardItem>,
    speechManager: MandarinSpeechManager,
    pronunciationEvaluation: PronunciationEvaluation?,
    speakingAiState: AiUiState,
    onAnalyzePronunciation: (SpeakingPrompt, String, List<Float>, List<Float>) -> Unit,
    onReviewFlashcard: (FlashcardItem, Int) -> Unit,
    onToggleFavoriteCard: (FlashcardItem) -> Unit,
    onMasterCharacter: (String) -> Unit,
    onCompleteExamOrListening: (points: Int) -> Unit
) {
    var selectedSubTab by remember(initialSubTab) { mutableIntStateOf(initialSubTab) }
    val isTraditional = profile.scriptMode == "TRADITIONAL"

    Column(modifier = Modifier.fillMaxSize()) {
        NeoBrutalTabRow(
            tabs = listOf("Speaking Center", "SRS Flashcards", "Hanzi Studio", "Listening & HSK Exam"),
            selectedIndex = selectedSubTab,
            onSelect = { selectedSubTab = it },
            tagPrefix = "practice_tab"
        )

        when (selectedSubTab) {
            0 -> SpeakingCenterView(
                isTraditional = isTraditional,
                speechManager = speechManager,
                evaluation = pronunciationEvaluation,
                aiState = speakingAiState,
                onAnalyzeAttempt = onAnalyzePronunciation
            )
            1 -> SrsFlashcardsView(
                isTraditional = isTraditional,
                flashcards = flashcards,
                onSpeak = { text, slow -> speechManager.speak(text, slow) },
                onReviewCard = onReviewFlashcard,
                onToggleFavorite = onToggleFavoriteCard
            )
            2 -> HanziCharacterStudioView(
                isTraditional = isTraditional,
                masteredCharIds = profile.masteredCharacterIds,
                onSpeak = { text -> speechManager.speak(text, false) },
                onMasterCharacter = onMasterCharacter
            )
            3 -> ListeningAndExamView(
                isTraditional = isTraditional,
                hskStandard = profile.hskStandard,
                onSpeak = { text, slow -> speechManager.speak(text, slow) },
                onCompleteActivity = onCompleteExamOrListening
            )
        }
    }
}

@Composable
private fun SrsFlashcardsView(
    isTraditional: Boolean,
    flashcards: List<FlashcardItem>,
    onSpeak: (String, Boolean) -> Unit,
    onReviewCard: (FlashcardItem, Int) -> Unit,
    onToggleFavorite: (FlashcardItem) -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    if (flashcards.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            NeoBrutalCard(
                accentHeaderColor = BrutalYellow,
                headerTitle = "SM-2 DECK EMPTY"
            ) {
                Text(
                    text = "Your SRS Flashcard deck is initializing. Visit Vocabulary Lab to add words!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val safeIndex = currentIndex.coerceIn(0, flashcards.lastIndex)
    val card = flashcards[safeIndex]

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SPACED REPETITION // SM-2 ENGINE",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "CARD ${safeIndex + 1} OF ${flashcards.size} · ${card.status} · INTERVAL ${card.intervalDays}D",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                NeoBrutalIconButton(
                    onClick = { onToggleFavorite(card) },
                    backgroundColor = if (card.isFavorite) BrutalYellow else BrutalWhite
                ) {
                    Icon(
                        imageVector = if (card.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite card",
                        tint = BrutalBlack
                    )
                }
            }
        }

        // Interactive Neo-Brutalist Flip Card with 6dp Hard Offset Shadow
        item {
            NeoBrutalCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("srs_flashcard_surface"),
                backgroundColor = if (isFlipped) BrutalYellow.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                accentHeaderColor = if (isFlipped) BrutalMint else BrutalCobalt,
                headerTitle = "HSK ${card.hskLevel} // ${card.category}",
                headerRightText = if (isFlipped) "ANSWER REVEALED" else "TAP TO FLIP",
                shadowOffset = 6.dp,
                borderWidth = 3.dp,
                contentPadding = PaddingValues(24.dp),
                onClick = { isFlipped = !isFlipped }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeoBrutalBadge(
                        text = card.partOfSpeech,
                        backgroundColor = BrutalYellow
                    )
                    NeoBrutalIconButton(
                        onClick = { onSpeak(card.simplified, false) },
                        backgroundColor = BrutalOrange,
                        contentColor = Color.White,
                        size = 42.dp
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Pronounce flashcard",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isTraditional) card.traditional else card.simplified,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (isFlipped) {
                        NeoBrutalBadge(
                            text = card.pinyin,
                            backgroundColor = BrutalYellow,
                            textColor = BrutalBlack
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = card.english,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        if (card.exampleChinese.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .neoBrutalSurface(
                                        backgroundColor = BrutalWhite,
                                        borderColor = BrutalBlack,
                                        shadowColor = BrutalBlack,
                                        borderWidth = 2.dp,
                                        shadowOffset = 2.5.dp
                                    )
                                    .padding(10.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "${card.exampleChinese} (${card.examplePinyin})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BrutalBlack,
                                        textAlign = TextAlign.Center
                                    )
                                    if (card.exampleEnglish.isNotBlank()) {
                                        Text(
                                            text = card.exampleEnglish,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BrutalCobalt,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Flip,
                                contentDescription = "Tap to reveal",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TAP CARD TO REVEAL PINYIN & MEANING",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Four SM-2 Grading Buttons (Again, Hard, Good, Easy)
        item {
            Text(
                text = "RATE RECALL QUALITY (SM-2 SCHEDULER)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SrsGradeButton(
                    modifier = Modifier.weight(1f),
                    label = "AGAIN",
                    intervalHint = "1D",
                    bgColor = BrutalOrange,
                    fgColor = Color.White,
                    onClick = {
                        onReviewCard(card, 0)
                        isFlipped = false
                        currentIndex = (safeIndex + 1) % flashcards.size
                    }
                )
                SrsGradeButton(
                    modifier = Modifier.weight(1f),
                    label = "HARD",
                    intervalHint = "${maxOf(1L, (card.intervalDays * 1.2).toLong())}D",
                    bgColor = BrutalYellow,
                    fgColor = BrutalBlack,
                    onClick = {
                        onReviewCard(card, 1)
                        isFlipped = false
                        currentIndex = (safeIndex + 1) % flashcards.size
                    }
                )
                SrsGradeButton(
                    modifier = Modifier.weight(1f),
                    label = "GOOD",
                    intervalHint = "${maxOf(2L, (card.intervalDays * (card.easeFactor / 100.0)).toLong())}D",
                    bgColor = BrutalMint,
                    fgColor = BrutalBlack,
                    onClick = {
                        onReviewCard(card, 2)
                        isFlipped = false
                        currentIndex = (safeIndex + 1) % flashcards.size
                    }
                )
                SrsGradeButton(
                    modifier = Modifier.weight(1f),
                    label = "EASY",
                    intervalHint = "${maxOf(4L, (card.intervalDays * 3))}D",
                    bgColor = BrutalCobalt,
                    fgColor = Color.White,
                    onClick = {
                        onReviewCard(card, 3)
                        isFlipped = false
                        currentIndex = (safeIndex + 1) % flashcards.size
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun SrsGradeButton(
    modifier: Modifier = Modifier,
    label: String,
    intervalHint: String,
    bgColor: Color,
    fgColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(62.dp)
            .neoBrutalSurface(
                backgroundColor = bgColor,
                borderColor = BrutalBlack,
                shadowColor = BrutalBlack,
                borderWidth = 2.5.dp,
                shadowOffset = 3.5.dp
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = fgColor,
                fontSize = 13.sp
            )
            Text(
                text = intervalHint,
                style = MaterialTheme.typography.labelSmall,
                color = fgColor
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HanziCharacterStudioView(
    isTraditional: Boolean,
    masteredCharIds: List<String>,
    onSpeak: (String) -> Unit,
    onMasterCharacter: (String) -> Unit
) {
    var selectedChar by remember { mutableStateOf(CurriculumExtras.characters.first()) }
    var animatedStrokeStep by remember(selectedChar.id) { mutableIntStateOf(selectedChar.strokeCount) }
    var isAnimatingStrokes by remember { mutableStateOf(false) }
    val userDrawnStrokes = remember(selectedChar.id) { mutableStateListOf<List<Offset>>() }
    var currentStrokePoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var writingFeedback by remember(selectedChar.id) { mutableStateOf<String?>(null) }

    LaunchedEffect(isAnimatingStrokes, selectedChar.id) {
        if (isAnimatingStrokes) {
            for (step in 1..selectedChar.strokeCount) {
                animatedStrokeStep = step
                delay(550)
            }
            isAnimatingStrokes = false
        }
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
                text = "HANZI CHARACTER STUDIO // STROKE CANVAS",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurriculumExtras.characters.forEach { ch ->
                    val isMastered = masteredCharIds.contains(ch.id)
                    NeoBrutalChip(
                        text = "${if (isTraditional) ch.traditional else ch.simplified} (${ch.pinyin})" +
                            if (isMastered) " ✓" else "",
                        selected = selectedChar.id == ch.id,
                        selectedColor = if (isMastered) BrutalMint else BrutalYellow,
                        onClick = { selectedChar = ch }
                    )
                }
            }
        }

        // Character Breakdown Card
        item {
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalYellow,
                headerTitle = "RADICAL & ETYMOLOGY DECOMPOSITION",
                headerRightText = "HSK ${selectedChar.hskLevel} · ${selectedChar.strokeCount} STROKES"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .neoBrutalSurface(
                                    backgroundColor = BrutalCobalt,
                                    borderColor = BrutalBlack,
                                    shadowColor = BrutalBlack,
                                    borderWidth = 2.5.dp,
                                    shadowOffset = 3.5.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isTraditional) selectedChar.traditional else selectedChar.simplified,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "${selectedChar.pinyin} — ${selectedChar.meaning}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "RADICAL: ${selectedChar.radical} (${selectedChar.radicalMeaning})",
                                style = MaterialTheme.typography.labelLarge,
                                color = BrutalOrange
                            )
                        }
                    }
                    NeoBrutalIconButton(
                        onClick = { onSpeak(selectedChar.simplified) },
                        backgroundColor = BrutalYellow
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Pronounce character",
                            tint = BrutalBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "COMPONENTS: ${selectedChar.components}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "LOOK-ALIKE CONTRAST: ${selectedChar.similarCharacters}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Interactive Mi-Zi-Ge (米字格) Stroke Order Animation & Handwriting Canvas
        item {
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalCobalt,
                headerTitle = "MI-ZI-GE (米字格) CALLIGRAPHY PAD",
                headerRightText = "DRAWN ${userDrawnStrokes.size}/${selectedChar.strokeCount}"
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .neoBrutalSurface(
                                backgroundColor = BrutalWhite,
                                borderColor = BrutalBlack,
                                shadowColor = BrutalBlack,
                                borderWidth = 3.dp,
                                shadowOffset = 5.dp
                            )
                            .pointerInput(selectedChar.id) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentStrokePoints = listOf(offset)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        currentStrokePoints = currentStrokePoints + change.position
                                    },
                                    onDragEnd = {
                                        if (currentStrokePoints.size > 2) {
                                            userDrawnStrokes.add(currentStrokePoints)
                                            val countNow = userDrawnStrokes.size
                                            writingFeedback = when {
                                                countNow < selectedChar.strokeCount ->
                                                    "STROKE $countNow RECORDED → NEXT: STROKE ${countNow + 1} (${selectedChar.strokeOrderNames.getOrNull(countNow) ?: ""})"
                                                countNow == selectedChar.strokeCount ->
                                                    "✓ ALL ${selectedChar.strokeCount} STROKES COMPLETED! TAP 'MASTER +30 XP' BELOW."
                                                else ->
                                                    "NOTE: ${selectedChar.simplified} HAS ${selectedChar.strokeCount} STANDARD STROKES (YOU DREW $countNow)."
                                            }
                                        }
                                        currentStrokePoints = emptyList()
                                    }
                                )
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
                            val gridCol = BrutalBlack.copy(alpha = 0.25f)

                            // Mi-Zi-Ge rice grid lines
                            drawLine(gridCol, Offset(w / 2f, 0f), Offset(w / 2f, h), 1.5.dp.toPx(), pathEffect = dash)
                            drawLine(gridCol, Offset(0f, h / 2f), Offset(w, h / 2f), 1.5.dp.toPx(), pathEffect = dash)
                            drawLine(gridCol, Offset(0f, 0f), Offset(w, h), 1.5.dp.toPx(), pathEffect = dash)
                            drawLine(gridCol, Offset(w, 0f), Offset(0f, h), 1.5.dp.toPx(), pathEffect = dash)

                            // Reference Guide Strokes
                            selectedChar.strokePaths.take(animatedStrokeStep).forEachIndexed { idx, normPts ->
                                if (normPts.size >= 2) {
                                    val guidePath = Path()
                                    normPts.forEachIndexed { pIdx, (nx, ny) ->
                                        val px = nx * w
                                        val py = ny * h
                                        if (pIdx == 0) guidePath.moveTo(px, py) else guidePath.lineTo(px, py)
                                    }
                                    drawPath(
                                        path = guidePath,
                                        color = if (idx == animatedStrokeStep - 1 && isAnimatingStrokes)
                                            BrutalOrange
                                        else BrutalBlack.copy(alpha = 0.16f),
                                        style = Stroke(
                                            width = 14.dp.toPx(),
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                            }

                            // User Drawn Brush Strokes
                            userDrawnStrokes.forEach { pts ->
                                if (pts.size >= 2) {
                                    val path = Path()
                                    pts.forEachIndexed { i, pt ->
                                        if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                                    }
                                    drawPath(
                                        path = path,
                                        color = BrutalCobalt,
                                        style = Stroke(width = 11.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                }
                            }

                            // Active Stroke in progress
                            if (currentStrokePoints.size >= 2) {
                                val activePath = Path()
                                currentStrokePoints.forEachIndexed { i, pt ->
                                    if (i == 0) activePath.moveTo(pt.x, pt.y) else activePath.lineTo(pt.x, pt.y)
                                }
                                drawPath(
                                    path = activePath,
                                    color = BrutalOrange,
                                    style = Stroke(width = 11.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (writingFeedback != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .neoBrutalSurface(
                                    backgroundColor = BrutalMint,
                                    borderColor = BrutalBlack,
                                    shadowColor = BrutalBlack,
                                    borderWidth = 2.dp,
                                    shadowOffset = 2.5.dp
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                text = writingFeedback ?: "",
                                style = MaterialTheme.typography.labelLarge,
                                color = BrutalBlack,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NeoBrutalButton(
                            text = "ANIMATE",
                            icon = Icons.Default.PlayArrow,
                            backgroundColor = BrutalYellow,
                            contentColor = BrutalBlack,
                            modifier = Modifier.weight(1f),
                            onClick = { isAnimatingStrokes = true }
                        )
                        NeoBrutalButton(
                            text = "CLEAR",
                            icon = Icons.Default.Clear,
                            backgroundColor = BrutalWhite,
                            contentColor = BrutalBlack,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                userDrawnStrokes.clear()
                                writingFeedback = null
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    NeoBrutalButton(
                        text = "VERIFY & MASTER CHARACTER (+30 XP)",
                        backgroundColor = BrutalMint,
                        contentColor = BrutalBlack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("master_character_button"),
                        onClick = {
                            onMasterCharacter(selectedChar.id)
                            writingFeedback = "✓ MARKED '${selectedChar.simplified}' AS MASTERED (+30 XP)!"
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "STANDARD STROKE ORDER SEQUENCE:",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        selectedChar.strokeOrderNames.forEach { sName ->
                            Text(
                                text = "• $sName",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun ListeningAndExamView(
    isTraditional: Boolean,
    hskStandard: String,
    onSpeak: (String, Boolean) -> Unit,
    onCompleteActivity: (Int) -> Unit
) {
    val listeningAnswers = remember { mutableStateMapOf<String, Int>() }
    val examAnswers = remember { mutableStateMapOf<String, Int>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "LISTENING CENTER // AUDIO COMPREHENSION",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(CurriculumExtras.listeningExercises, key = { it.id }) { item ->
            val chosen = listeningAnswers[item.id]
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalYellow,
                headerTitle = "${item.level} // ${item.category}",
                headerRightText = "+20 XP"
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NeoBrutalButton(
                        text = "PLAY 1.0X",
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        backgroundColor = BrutalOrange,
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = { onSpeak(item.audioTextSimplified, false) }
                    )
                    NeoBrutalButton(
                        text = "SLOW 0.6X",
                        icon = Icons.Default.SlowMotionVideo,
                        backgroundColor = BrutalWhite,
                        contentColor = BrutalBlack,
                        modifier = Modifier.weight(1f),
                        onClick = { onSpeak(item.audioTextSimplified, true) }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = item.questionPrompt,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                item.options.forEachIndexed { idx, opt ->
                    val isChosen = chosen == idx
                    val isRight = idx == item.correctIndex
                    val optBg = when {
                        chosen == null -> MaterialTheme.colorScheme.surface
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
                                listeningAnswers[item.id] = idx
                                if (idx == item.correctIndex) onCompleteActivity(20)
                            }
                            .padding(12.dp)
                    ) {
                        Text(
                            text = opt,
                            fontWeight = FontWeight.Bold,
                            color = if (chosen != null && isChosen && !isRight) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                AnimatedVisibility(visible = chosen != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .neoBrutalSurface(
                                backgroundColor = BrutalYellow,
                                borderColor = BrutalBlack,
                                shadowColor = BrutalBlack,
                                borderWidth = 2.dp,
                                shadowOffset = 2.dp
                            )
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "TRANSCRIPT // ${if (isTraditional) item.audioTextTraditional else item.audioTextSimplified} (${item.pinyin}) — ${item.englishTranslation}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrutalBlack
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "HSK 1–6 EXAMINATION CENTER (${hskStandard.replace("_", " ")})",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(CurriculumExtras.examQuestions, key = { it.id }) { q ->
            val chosen = examAnswers[q.id]
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalCobalt,
                headerTitle = "HSK ${q.hskLevel} (${q.hskStandard.replace("_", " ")}) // ${q.section}",
                headerRightText = "+25 XP"
            ) {
                Text(
                    text = if (isTraditional) q.promptTraditional else q.promptChinese,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = q.promptPinyin,
                    style = MaterialTheme.typography.labelLarge,
                    color = BrutalOrange
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = q.questionText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                q.options.forEachIndexed { idx, opt ->
                    val isChosen = chosen == idx
                    val isRight = idx == q.correctIndex
                    val optBg = when {
                        chosen == null -> MaterialTheme.colorScheme.surface
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
                                examAnswers[q.id] = idx
                                if (idx == q.correctIndex) onCompleteActivity(25)
                            }
                            .padding(12.dp)
                    ) {
                        Text(
                            text = opt,
                            fontWeight = FontWeight.Bold,
                            color = if (chosen != null && isChosen && !isRight) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                AnimatedVisibility(visible = chosen != null) {
                    Text(
                        text = "✓ ANALYSIS: ${q.detailedExplanation}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrutalCobalt,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
