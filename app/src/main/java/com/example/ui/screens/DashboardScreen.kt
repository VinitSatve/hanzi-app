package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.curriculum.CurriculumCatalog
import com.example.data.curriculum.CurriculumExtras
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
import com.example.ui.theme.NeoBrutalProgressBar
import com.example.ui.theme.neoBrutalSurface

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    profile: UserProfile,
    flashcards: List<FlashcardItem>,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    onToggleScriptMode: () -> Unit,
    onToggleHskStandard: () -> Unit,
    onSelectHskLevel: (Int) -> Unit,
    onContinueLearning: () -> Unit,
    onOpenSpeakingCenter: () -> Unit,
    onOpenFlashcardReview: () -> Unit,
    onOpenAiTutor: () -> Unit,
    onSpeakSample: (String) -> Unit
) {
    val isTraditional = profile.scriptMode == "TRADITIONAL"
    val todayEpoch = System.currentTimeMillis() / 86_400_000L
    val dueFlashcardsCount = flashcards.count { it.nextReviewEpochDay <= todayEpoch }
    val masteredVocabCount = flashcards.count { it.status == "MASTERED" }
    val totalLessons = CurriculumCatalog.lessons.size
    val completedLessonsCount = profile.completedLessonIds.size

    val recommendedLesson = CurriculumCatalog.lessons.firstOrNull {
        !profile.completedLessonIds.contains(it.id)
    } ?: CurriculumCatalog.lessons.first()

    val studyProgress = (profile.studyMinutesToday.toFloat() / maxOf(1L, profile.dailyGoalMinutes).toFloat())
        .coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Neo-Brutalist Editorial Masthead
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NeoBrutalBadge(
                            text = if (isTraditional) "漢字 · HANZI" else "汉字 · HANZI",
                            backgroundColor = BrutalYellow,
                            textColor = BrutalBlack
                        )
                        NeoBrutalBadge(
                            text = if (profile.hskStandard == "HSK_2") "HSK 2.0" else "HSK 3.0",
                            backgroundColor = BrutalCobalt,
                            textColor = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "NI HAO,\n${profile.displayName.uppercase()}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NeoBrutalIconButton(
                        onClick = onToggleScriptMode,
                        backgroundColor = BrutalYellow,
                        contentColor = BrutalBlack,
                        modifier = Modifier.testTag("toggle_script_button")
                    ) {
                        Text(
                            text = if (isTraditional) "繁" else "简",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BrutalBlack
                        )
                    }
                    NeoBrutalIconButton(
                        onClick = onToggleDarkTheme,
                        backgroundColor = BrutalOrange,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("toggle_theme_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark or Light Theme",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Asymmetric Gamification & Daily Study Target Block
        item {
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surface,
                accentHeaderColor = BrutalYellow,
                headerTitle = "DAILY TELEMETRY // STREAK & XP",
                headerRightText = "ACTIVE",
                shadowOffset = 5.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BrutalStatPill(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.LocalFireDepartment,
                        bgColor = BrutalOrange,
                        fgColor = Color.White,
                        value = "${profile.streakDays}D",
                        label = "STREAK"
                    )
                    BrutalStatPill(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Bolt,
                        bgColor = BrutalYellow,
                        fgColor = BrutalBlack,
                        value = "${profile.points}",
                        label = "TOTAL XP"
                    )
                    BrutalStatPill(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.School,
                        bgColor = BrutalCobalt,
                        fgColor = Color.White,
                        value = "HSK ${profile.currentHskLevel}",
                        label = if (profile.currentHskLevel <= 2L) "STAGE A1–A2" else if (profile.currentHskLevel <= 4L) "STAGE B1–B2" else "STAGE C1–C2"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAILY TARGET · ${profile.studyMinutesToday} / ${profile.dailyGoalMinutes} MIN",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    NeoBrutalBadge(
                        text = "${(studyProgress * 100).toInt()}%",
                        backgroundColor = BrutalMint,
                        textColor = BrutalBlack
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                NeoBrutalProgressBar(
                    progress = studyProgress,
                    fillColor = BrutalMint,
                    trackColor = BrutalWhite
                )
            }
        }

        // Hero Recommended Lesson & Prominent Continue Learning Action
        item {
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = BrutalCobalt,
                accentHeaderColor = BrutalOrange,
                headerTitle = "NEXT MANDARIN MISSION // STAGE ${recommendedLesson.stage}",
                headerRightText = "PRIORITY 01",
                shadowOffset = 6.dp,
                borderWidth = 3.dp,
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_neobrutal_hero_hanzi_1793857326334),
                        contentDescription = "Recommended next lesson Neo-Brutalist banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(BrutalBlack)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrutalCobalt)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = recommendedLesson.title.uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .neoBrutalSurface(
                                        backgroundColor = BrutalWhite,
                                        borderColor = BrutalBlack,
                                        shadowColor = BrutalBlack,
                                        borderWidth = 2.dp,
                                        shadowOffset = 3.dp
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (isTraditional) recommendedLesson.traditionalFocus else recommendedLesson.chineseFocus,
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = BrutalBlack
                                    )
                                    Text(
                                        text = recommendedLesson.pinyinFocus,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = BrutalOrange
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        NeoBrutalIconButton(
                            onClick = {
                                onSpeakSample(
                                    if (isTraditional) recommendedLesson.traditionalFocus
                                    else recommendedLesson.chineseFocus
                                )
                            },
                            backgroundColor = BrutalYellow,
                            contentColor = BrutalBlack
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen to lesson phrase",
                                tint = BrutalBlack
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    NeoBrutalButton(
                        text = "CONTINUE LEARNING (+40 XP)",
                        icon = Icons.Default.PlayArrow,
                        backgroundColor = BrutalYellow,
                        contentColor = BrutalBlack,
                        onClick = onContinueLearning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("continue_learning_button")
                    )
                }
            }
        }

        // HSK Level & Standard Switcher
        item {
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalMint,
                headerTitle = "TARGET PROFICIENCY & HSK STANDARD",
                headerRightText = if (profile.hskStandard == "HSK_2") "HSK 2.0" else "HSK 3.0"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT ACTIVE HSK TRACK",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .neoBrutalSurface(
                                backgroundColor = BrutalOrange,
                                borderColor = BrutalBlack,
                                shadowColor = BrutalBlack,
                                borderWidth = 2.dp,
                                shadowOffset = 3.dp
                            )
                            .clickable(onClick = onToggleHskStandard)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("toggle_hsk_standard_button")
                    ) {
                        Text(
                            text = if (profile.hskStandard == "HSK_2") "SWITCH TO HSK 3.0" else "SWITCH TO HSK 2.0",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..6).forEach { lvl ->
                        NeoBrutalChip(
                            text = "HSK $lvl",
                            selected = profile.currentHskLevel.toInt() == lvl,
                            selectedColor = BrutalYellow,
                            onClick = { onSelectHskLevel(lvl) }
                        )
                    }
                }
            }
        }

        // Asymmetric 2x2 Neo-Brutalist Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricStatCard(
                        modifier = Modifier.weight(1f),
                        headerColor = BrutalYellow,
                        title = "VOCAB DECK",
                        value = "${flashcards.size} WORDS",
                        subtitle = "$masteredVocabCount MASTERED · $dueFlashcardsCount DUE",
                        onClick = onOpenFlashcardReview
                    )
                    MetricStatCard(
                        modifier = Modifier.weight(1f),
                        headerColor = BrutalCobalt,
                        title = "HANZI STUDIO",
                        value = "${profile.masteredCharacterIds.size}/${CurriculumExtras.characters.size}",
                        subtitle = "STROKE CANVAS",
                        onClick = onContinueLearning
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricStatCard(
                        modifier = Modifier.weight(1f),
                        headerColor = BrutalOrange,
                        title = "CURRICULUM",
                        value = "$completedLessonsCount/$totalLessons",
                        subtitle = "A0 TO HSK 6",
                        onClick = onContinueLearning
                    )
                    MetricStatCard(
                        modifier = Modifier.weight(1f),
                        headerColor = BrutalMint,
                        title = "SPEAKING LAB",
                        value = "${profile.speakingAccuracy}%",
                        subtitle = "LISTEN ${profile.listeningAccuracy}% · READ ${profile.readingAccuracy}%",
                        onClick = onOpenSpeakingCenter
                    )
                }
            }
        }

        // Daily Learning Checklist
        item {
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalOrange,
                headerTitle = "DAILY LEARNING CHECKLIST",
                headerRightText = "4 MISSIONS"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ChecklistActionRow(
                        completed = completedLessonsCount > 0,
                        title = "Complete 1 Structured HSK or Pinyin Lesson",
                        subtitle = recommendedLesson.title,
                        onClick = onContinueLearning
                    )
                    ChecklistActionRow(
                        completed = dueFlashcardsCount == 0 && flashcards.isNotEmpty(),
                        title = "Review Spaced Repetition Vocabulary Queue",
                        subtitle = if (dueFlashcardsCount > 0) "$dueFlashcardsCount cards due for SM-2 review today" else "All due flashcards reviewed!",
                        onClick = onOpenFlashcardReview
                    )
                    ChecklistActionRow(
                        completed = profile.unlockedBadgeIds.contains("badge_voice_flow"),
                        title = "Practice Pitch-Contour Speaking & Tones",
                        subtitle = "Analyze waveform & tone curve in Speaking Center",
                        onClick = onOpenSpeakingCenter
                    )
                    ChecklistActionRow(
                        completed = profile.studyMinutesToday >= profile.dailyGoalMinutes,
                        title = "Converse with AI Mandarin Tutor",
                        subtitle = "Roleplay or break down a Chinese sentence",
                        onClick = onOpenAiTutor
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BrutalStatPill(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    bgColor: Color,
    fgColor: Color,
    value: String,
    label: String
) {
    Box(
        modifier = modifier
            .neoBrutalSurface(
                backgroundColor = bgColor,
                borderColor = BrutalBlack,
                shadowColor = BrutalBlack,
                borderWidth = 2.dp,
                shadowOffset = 3.dp
            )
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = fgColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = fgColor
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = fgColor,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun MetricStatCard(
    modifier: Modifier = Modifier,
    headerColor: Color,
    title: String,
    value: String,
    subtitle: String,
    onClick: () -> Unit
) {
    NeoBrutalCard(
        modifier = modifier,
        accentHeaderColor = headerColor,
        headerTitle = title,
        shadowOffset = 4.dp,
        contentPadding = PaddingValues(14.dp),
        onClick = onClick
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChecklistActionRow(
    completed: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .neoBrutalSurface(
                backgroundColor = if (completed) BrutalMint.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface,
                borderColor = BrutalBlack,
                shadowColor = BrutalBlack,
                borderWidth = 2.dp,
                shadowOffset = 3.dp
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .neoBrutalSurface(
                    backgroundColor = if (completed) BrutalMint else BrutalYellow,
                    borderColor = BrutalBlack,
                    shadowColor = BrutalBlack,
                    borderWidth = 2.dp,
                    shadowOffset = 2.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (completed) "✓" else "→",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BrutalBlack
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Go",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp)
        )
    }
}
