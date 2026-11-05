package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.curriculum.CurriculumCatalog
import com.example.data.curriculum.CurriculumExtras
import com.example.data.curriculum.OfflineJsonCacheStats
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
import com.example.ui.theme.NeoBrutalIllustratedEmblem
import com.example.ui.theme.NeoBrutalProgressBar
import com.example.ui.theme.NeoBrutalTabRow
import com.example.ui.theme.neoBrutalSurface
import java.util.Locale

@Composable
fun ProgressScreen(
    profile: UserProfile,
    flashcards: List<FlashcardItem>,
    downloadProgressMap: Map<String, Int>,
    onDownloadPack: (String) -> Unit,
    onRemovePack: (String) -> Unit,
    onUpdateDailyGoal: (Int) -> Unit,
    onSignOut: () -> Unit,
    offlineJsonCacheStats: OfflineJsonCacheStats? = null
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        NeoBrutalTabRow(
            tabs = listOf("Analytics & Readiness", "Milestones & Badges", "Offline Downloads"),
            selectedIndex = selectedSubTab,
            onSelect = { selectedSubTab = it },
            tagPrefix = "progress_tab"
        )

        when (selectedSubTab) {
            0 -> AnalyticsOverviewTab(
                profile = profile,
                flashcards = flashcards,
                onUpdateDailyGoal = onUpdateDailyGoal,
                onSignOut = onSignOut
            )
            1 -> GamificationBadgesTab(profile = profile)
            2 -> OfflineDownloadManagerTab(
                profile = profile,
                downloadProgressMap = downloadProgressMap,
                offlineJsonCacheStats = offlineJsonCacheStats,
                onDownloadPack = onDownloadPack,
                onRemovePack = onRemovePack
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnalyticsOverviewTab(
    profile: UserProfile,
    flashcards: List<FlashcardItem>,
    onUpdateDailyGoal: (Int) -> Unit,
    onSignOut: () -> Unit
) {
    val newCount = flashcards.count { it.status == "NEW" }
    val learningCount = flashcards.count { it.status == "LEARNING" }
    val familiarCount = flashcards.count { it.status == "PRACTICING" || it.status == "FAMILIAR" }
    val masteredCount = flashcards.count { it.status == "MASTERED" }

    val hskReadiness = ((profile.completedLessonIds.size.toFloat() / maxOf(1, CurriculumCatalog.lessons.size)) * 50f +
        (profile.speakingAccuracy + profile.listeningAccuracy + profile.readingAccuracy) / 6f)
        .toInt().coerceIn(15, 99)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "LEARNING ANALYTICS & HSK READINESS",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Top Cumulative Stats & Neo-Brutalist Bar Chart
        item {
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalYellow,
                headerTitle = "7-DAY STUDY ACTIVITY // MINUTES PER DAY",
                headerRightText = "HSK ${profile.currentHskLevel} READY: $hskReadiness%"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BrutalSummaryTile(
                        modifier = Modifier.weight(1f),
                        label = "TOTAL TIME",
                        value = "${profile.totalStudyMinutes}M",
                        bg = BrutalWhite
                    )
                    BrutalSummaryTile(
                        modifier = Modifier.weight(1f),
                        label = "STREAK",
                        value = "${profile.streakDays}D",
                        bg = BrutalYellow
                    )
                    BrutalSummaryTile(
                        modifier = Modifier.weight(1f),
                        label = "READINESS",
                        value = "$hskReadiness%",
                        bg = BrutalMint
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val todayMin = profile.studyMinutesToday.toFloat().coerceAtLeast(10f)
                val weeklyBars = listOf(18f, 25f, 15f, 30f, 22f, 28f, todayMin)
                val dayLabels = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "TODAY")
                val maxBar = (weeklyBars.maxOrNull() ?: 30f).coerceAtLeast(30f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                        .neoBrutalSurface(
                            backgroundColor = BrutalWhite,
                            borderColor = BrutalBlack,
                            shadowColor = BrutalBlack,
                            borderWidth = 2.5.dp,
                            shadowOffset = 3.5.dp
                        )
                        .padding(14.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val count = weeklyBars.size
                        val slotW = size.width / count
                        val barW = (slotW * 0.54f).coerceAtLeast(12f)
                        weeklyBars.forEachIndexed { idx, value ->
                            val frac = (value / maxBar).coerceIn(0.14f, 0.96f)
                            val barH = size.height * frac
                            val left = idx * slotW + (slotW - barW) / 2f
                            val top = size.height - barH

                            // Hard offset shadow per bar
                            drawRect(
                                color = BrutalBlack,
                                topLeft = Offset(left + 3.dp.toPx(), top + 3.dp.toPx()),
                                size = Size(barW, barH)
                            )
                            // Solid high-contrast bar fill
                            drawRect(
                                color = if (idx == count - 1) BrutalOrange else if (idx % 2 == 0) BrutalYellow else BrutalCobalt,
                                topLeft = Offset(left, top),
                                size = Size(barW, barH)
                            )
                            // Thick black border around bar
                            drawRect(
                                color = BrutalBlack,
                                topLeft = Offset(left, top),
                                size = Size(barW, barH),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    dayLabels.forEach { d ->
                        Text(
                            text = d,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Skill Accuracy & Vocabulary Retention Card
        item {
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalCobalt,
                headerTitle = "SKILL ACCURACY & WEAK-AREA DIAGNOSTICS",
                headerRightText = "LIVE TELEMETRY"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BrutalSkillBar("LISTENING COMPREHENSION", profile.listeningAccuracy.toInt(), BrutalMint)
                    BrutalSkillBar("READING & GRAMMAR", profile.readingAccuracy.toInt(), BrutalYellow)
                    BrutalSkillBar("SPEAKING & TONE CONTOUR", profile.speakingAccuracy.toInt(), BrutalOrange)

                    Spacer(modifier = Modifier.height(4.dp))
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
                                text = "VOCABULARY MASTERY // $newCount NEW · $learningCount LEARNING · $familiarCount PRACTICING · $masteredCount MASTERED",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val weakestArea = when {
                                profile.speakingAccuracy <= profile.listeningAccuracy && profile.speakingAccuracy <= profile.readingAccuracy ->
                                    "FOCUS RECOMMENDATION: Practice 3rd-tone sandhi and retroflex initials in the Speaking Center."
                                profile.listeningAccuracy <= profile.readingAccuracy ->
                                    "FOCUS RECOMMENDATION: Complete minimal-pair tone discrimination drills in the Listening Center."
                                else ->
                                    "FOCUS RECOMMENDATION: Review 把 (bǎ) and 被 (bèi) structures in the Grammar Library."
                            }
                            Text(
                                text = weakestArea,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrutalOrange
                            )
                        }
                    }
                }
            }
        }

        // Personalization & Study Goal Settings
        item {
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalOrange,
                headerTitle = "DAILY GOAL & ACCOUNT SETTINGS",
                headerRightText = "${profile.dailyGoalMinutes} MIN/DAY"
            ) {
                Text(
                    text = "SET DAILY STUDY TARGET",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10, 20, 30, 45, 60).forEach { mins ->
                        NeoBrutalChip(
                            text = "$mins MIN/DAY",
                            selected = profile.dailyGoalMinutes.toInt() == mins,
                            selectedColor = BrutalYellow,
                            onClick = { onUpdateDailyGoal(mins) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                NeoBrutalButton(
                    text = "SIGN OUT OF GOOGLE ACCOUNT",
                    icon = Icons.AutoMirrored.Filled.Logout,
                    backgroundColor = BrutalWhite,
                    contentColor = BrutalBlack,
                    onClick = onSignOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sign_out_button")
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun BrutalSummaryTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    bg: Color
) {
    Box(
        modifier = modifier
            .neoBrutalSurface(
                backgroundColor = bg,
                borderColor = BrutalBlack,
                shadowColor = BrutalBlack,
                borderWidth = 2.dp,
                shadowOffset = 3.dp
            )
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = BrutalBlack
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = BrutalBlack
            )
        }
    }
}

@Composable
private fun BrutalSkillBar(label: String, percent: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            NeoBrutalBadge(
                text = "$percent%",
                backgroundColor = color,
                textColor = if (color == BrutalOrange || color == BrutalCobalt) Color.White else BrutalBlack
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        NeoBrutalProgressBar(
            progress = (percent / 100f).coerceIn(0f, 1f),
            fillColor = color,
            trackColor = BrutalWhite,
            height = 18.dp
        )
    }
}

@Composable
private fun GamificationBadgesTab(profile: UserProfile) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "IMPERIAL SEALS & MILESTONE BADGES",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "TOTAL XP: ${profile.points} XP · UNLOCKED ${profile.unlockedBadgeIds.size} OF ${CurriculumExtras.milestoneBadges.size} SEALS",
                style = MaterialTheme.typography.labelLarge,
                color = BrutalOrange
            )
        }

        items(CurriculumExtras.milestoneBadges, key = { it.id }) { badge ->
            val unlocked = profile.unlockedBadgeIds.contains(badge.id) || profile.points >= badge.requiredPoints
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = if (unlocked) BrutalMint else BrutalYellow,
                headerTitle = badge.title,
                headerRightText = if (unlocked) "✓ UNLOCKED" else "${badge.requiredPoints} XP REQUIRED"
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NeoBrutalIllustratedEmblem(
                        glyph = badge.chineseSeal,
                        backgroundColor = if (unlocked) BrutalOrange else BrutalWhite,
                        size = 58.dp
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = badge.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OfflineDownloadManagerTab(
    profile: UserProfile,
    downloadProgressMap: Map<String, Int>,
    offlineJsonCacheStats: OfflineJsonCacheStats?,
    onDownloadPack: (String) -> Unit,
    onRemovePack: (String) -> Unit
) {
    val downloadedPacks = CurriculumExtras.offlinePacks.filter {
        profile.downloadedPackIds.contains(it.id)
    }
    val usedMb = downloadedPacks.sumOf { it.sizeMb.toDouble() }.toFloat()
    val totalAvailableMb = 512f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "OFFLINE CONTENT DOWNLOAD MANAGER",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Select HSK lessons, vocabulary audio packs, and grammar modules to cache locally as JSON for full offline study.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Storage Space Summary Card
        item {
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalYellow,
                headerTitle = "LOCAL STORAGE ALLOCATION",
                headerRightText = String.format(Locale.US, "%.1f / %.0f MB", usedMb, totalAvailableMb)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Offline storage",
                            tint = BrutalOrange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${downloadedPacks.size} OF ${CurriculumExtras.offlinePacks.size} PACKS CACHED OFFLINE",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                NeoBrutalProgressBar(
                    progress = (usedMb / totalAvailableMb).coerceIn(0.05f, 1f),
                    fillColor = BrutalMint,
                    trackColor = BrutalWhite
                )
                if (offlineJsonCacheStats != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "JSON CACHE ENGINE: ${offlineJsonCacheStats.cachedLevelFiles.size} local .json file(s) (${offlineJsonCacheStats.totalBytesOnDisk / 1024} KB on disk)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (offlineJsonCacheStats.cachedLevelFiles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            offlineJsonCacheStats.cachedLevelFiles.forEach { fileName ->
                                NeoBrutalBadge(
                                    text = fileName,
                                    backgroundColor = BrutalWhite,
                                    textColor = BrutalBlack
                                )
                            }
                        }
                    }
                }
            }
        }

        items(CurriculumExtras.offlinePacks, key = { it.id }) { pack ->
            val isDownloaded = profile.downloadedPackIds.contains(pack.id)
            val activeProgress = downloadProgressMap[pack.id]

            NeoBrutalCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_pack_${pack.id}"),
                accentHeaderColor = if (isDownloaded) BrutalMint else BrutalCobalt,
                headerTitle = "${pack.category} // ${pack.hskTag}",
                headerRightText = "${pack.sizeMb} MB"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pack.title.uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = pack.itemCountDescription,
                            style = MaterialTheme.typography.labelSmall,
                            color = BrutalOrange
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pack.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    if (isDownloaded) {
                        NeoBrutalIconButton(
                            onClick = { onRemovePack(pack.id) },
                            backgroundColor = BrutalOrange,
                            contentColor = Color.White,
                            modifier = Modifier.testTag("delete_pack_${pack.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete downloaded pack",
                                tint = Color.White
                            )
                        }
                    } else if (activeProgress == null) {
                        NeoBrutalButton(
                            text = "GET",
                            icon = Icons.Default.CloudDownload,
                            backgroundColor = BrutalYellow,
                            height = 44.dp,
                            onClick = { onDownloadPack(pack.id) },
                            modifier = Modifier.testTag("download_pack_${pack.id}")
                        )
                    }
                }

                if (activeProgress != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "DOWNLOADING OFFLINE PACKAGE...",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrutalOrange
                        )
                        Text(
                            text = "$activeProgress%",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrutalOrange
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    NeoBrutalProgressBar(
                        progress = (activeProgress / 100f).coerceIn(0f, 1f),
                        fillColor = BrutalYellow,
                        trackColor = BrutalWhite,
                        height = 16.dp
                    )
                } else if (isDownloaded) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.OfflinePin,
                            contentDescription = "Cached offline",
                            tint = BrutalBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "READY FOR OFFLINE STUDY · TAP TRASH TO FREE ${pack.sizeMb} MB",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
