package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.curriculum.CurriculumExtras
import com.example.data.curriculum.SpeakingPrompt
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
import com.example.ui.theme.NeoBrutalTextField
import com.example.ui.theme.neoBrutalSurface
import com.example.ui.viewmodel.AiUiState
import com.example.ui.viewmodel.PronunciationEvaluation
import com.example.util.MandarinSpeechManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeakingCenterView(
    isTraditional: Boolean,
    speechManager: MandarinSpeechManager,
    evaluation: PronunciationEvaluation?,
    aiState: AiUiState,
    onAnalyzeAttempt: (
        prompt: SpeakingPrompt,
        spokenText: String,
        waveform: List<Float>,
        pitchContour: List<Float>
    ) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedPrompt by remember { mutableStateOf(CurriculumExtras.speakingPrompts.first()) }
    var spokenTranscriptInput by remember { mutableStateOf(selectedPrompt.simplified) }
    var isRecordingMic by remember { mutableStateOf(false) }
    var liveWaveform by remember { mutableStateOf<List<Float>>(emptyList()) }
    var livePitch by remember { mutableStateOf<List<Float>>(emptyList()) }
    var recordingJob by remember { mutableStateOf<Job?>(null) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isRecordingMic = true
            recordingJob = scope.launch {
                val (wave, pitch) = speechManager.recordAcousticProfile { w, p ->
                    liveWaveform = w
                    livePitch = p
                }
                isRecordingMic = false
                onAnalyzeAttempt(selectedPrompt, spokenTranscriptInput, wave, pitch)
            }
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
                text = "SPEAKING CENTER // PITCH & PHONEME LAB",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Compare your acoustic F0 pitch curve and waveform against native Mandarin tone contours with syllable-level scoring.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurriculumExtras.speakingPrompts.forEach { prompt ->
                    NeoBrutalChip(
                        text = prompt.level,
                        selected = selectedPrompt.id == prompt.id,
                        selectedColor = BrutalYellow,
                        onClick = {
                            selectedPrompt = prompt
                            spokenTranscriptInput = if (isTraditional) prompt.traditional else prompt.simplified
                        }
                    )
                }
            }
        }

        // Target Sentence & Native Audio Card
        item {
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalCobalt,
                headerTitle = "TARGET UTTERANCE // ${selectedPrompt.category}",
                headerRightText = selectedPrompt.level
            ) {
                Text(
                    text = if (isTraditional) selectedPrompt.traditional else selectedPrompt.simplified,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedPrompt.pinyin,
                    style = MaterialTheme.typography.titleLarge,
                    color = BrutalOrange
                )
                Text(
                    text = selectedPrompt.english,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NeoBrutalButton(
                        text = "NATIVE 1.0X",
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        backgroundColor = BrutalYellow,
                        contentColor = BrutalBlack,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("speaking_play_normal_button"),
                        onClick = { speechManager.speak(selectedPrompt.simplified, slow = false) }
                    )
                    NeoBrutalButton(
                        text = "SLOW 0.6X",
                        icon = Icons.Default.SlowMotionVideo,
                        backgroundColor = BrutalWhite,
                        contentColor = BrutalBlack,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("speaking_play_slow_button"),
                        onClick = { speechManager.speak(selectedPrompt.simplified, slow = true) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neoBrutalSurface(
                            backgroundColor = BrutalYellow.copy(alpha = 0.25f),
                            borderColor = BrutalBlack,
                            shadowColor = BrutalBlack,
                            borderWidth = 2.dp,
                            shadowOffset = 2.5.dp
                        )
                        .padding(10.dp)
                ) {
                    Text(
                        text = "ARTICULATION TIP // ${selectedPrompt.phonemeTip}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Pitch Contour & Waveform Comparison Visualizer Card
        item {
            val activeEval = evaluation?.takeIf { it.promptId == selectedPrompt.id }
            val targetContour = selectedPrompt.targetPitchContour
            val userContour = activeEval?.userPitchContour ?: livePitch
            val userWave = activeEval?.userWaveform ?: liveWaveform

            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalYellow,
                headerTitle = "ACOUSTIC PITCH CONTOUR & WAVEFORM",
                headerRightText = "F0 OSCILLOSCOPE"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendBlock(color = BrutalCobalt, label = "NATIVE PITCH (T1–T4)")
                    LegendBlock(color = BrutalOrange, label = "YOUR RECORDED PITCH")
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Brutalist Pitch Contour Oscilloscope (Chao Tone Levels 5 -> 1)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp)
                        .neoBrutalSurface(
                            backgroundColor = BrutalWhite,
                            borderColor = BrutalBlack,
                            shadowColor = BrutalBlack,
                            borderWidth = 2.5.dp,
                            shadowOffset = 3.5.dp
                        )
                        .padding(12.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Architectural pitch register grid lines (High 5, Mid 3, Low 1)
                        listOf(0.15f, 0.50f, 0.85f).forEach { yFrac ->
                            drawLine(
                                color = BrutalBlack.copy(alpha = 0.25f),
                                start = Offset(0f, h * yFrac),
                                end = Offset(w, h * yFrac),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                            )
                        }

                        // Native Target Pitch Curve (BrutalCobalt)
                        if (targetContour.size >= 2) {
                            val targetPath = Path()
                            targetContour.forEachIndexed { idx, pitchVal ->
                                val x = (idx.toFloat() / (targetContour.size - 1)) * w
                                val y = h * (1f - pitchVal.coerceIn(0.08f, 0.94f))
                                if (idx == 0) targetPath.moveTo(x, y) else targetPath.lineTo(x, y)
                            }
                            drawPath(
                                path = targetPath,
                                color = BrutalCobalt,
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Square)
                            )
                        }

                        // Learner Recorded Pitch Curve (BrutalOrange)
                        if (userContour.size >= 2) {
                            val userPath = Path()
                            userContour.forEachIndexed { idx, pitchVal ->
                                val x = (idx.toFloat() / (userContour.size - 1)) * w
                                val y = h * (1f - pitchVal.coerceIn(0.08f, 0.94f))
                                if (idx == 0) userPath.moveTo(x, y) else userPath.lineTo(x, y)
                            }
                            drawPath(
                                path = userPath,
                                color = BrutalOrange,
                                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Square)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Syllable-by-syllable tone blocks underneath the pitch curve
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    selectedPrompt.syllablePinyins.take(6).forEachIndexed { idx, syl ->
                        val toneNum = selectedPrompt.numericTones.getOrElse(idx) { 5 }
                        val sylScore = activeEval?.syllableScores?.getOrNull(idx)?.second
                        val blockBg = when {
                            sylScore == null -> BrutalWhite
                            sylScore >= 85 -> BrutalMint
                            sylScore >= 75 -> BrutalYellow
                            else -> BrutalOrange
                        }
                        val blockFg = if (blockBg == BrutalOrange) Color.White else BrutalBlack
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .neoBrutalSurface(
                                    backgroundColor = blockBg,
                                    borderColor = BrutalBlack,
                                    shadowColor = BrutalBlack,
                                    borderWidth = 2.dp,
                                    shadowOffset = 2.dp
                                )
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = syl,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = blockFg
                                )
                                Text(
                                    text = if (sylScore != null) "${sylScore}%" else "T$toneNum",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = blockFg
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chunky Neo-Brutalist Waveform Bars
                Text(
                    text = "VOICE AMPLITUDE ENVELOPE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .neoBrutalSurface(
                            backgroundColor = BrutalWhite,
                            borderColor = BrutalBlack,
                            shadowColor = BrutalBlack,
                            borderWidth = 2.dp,
                            shadowOffset = 3.dp
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    val barsToRender = if (userWave.isNotEmpty()) userWave else List(20) { 0.22f }
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val count = barsToRender.size
                        val slot = size.width / count
                        val barWidth = (slot * 0.62f).coerceAtLeast(5f)
                        barsToRender.forEachIndexed { i, amp ->
                            val barHeight = (size.height * amp.coerceIn(0.15f, 0.96f))
                            val top = (size.height - barHeight) / 2f
                            val left = i * slot + (slot - barWidth) / 2f
                            drawRect(
                                color = if (userWave.isNotEmpty()) BrutalOrange else BrutalCobalt.copy(alpha = 0.4f),
                                topLeft = Offset(left, top),
                                size = Size(barWidth, barHeight)
                            )
                            drawRect(
                                color = BrutalBlack,
                                topLeft = Offset(left, top),
                                size = Size(barWidth, barHeight),
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        // Recording & Speech Analysis Controls
        item {
            NeoBrutalCard(
                modifier = Modifier.fillMaxWidth(),
                accentHeaderColor = BrutalOrange,
                headerTitle = "RECORD & ANALYZE PRONUNCIATION",
                headerRightText = "AI + DSP"
            ) {
                NeoBrutalTextField(
                    value = spokenTranscriptInput,
                    onValueChange = { spokenTranscriptInput = it },
                    label = "Spoken Mandarin / Pinyin Attempt",
                    placeholder = "Speak or type Mandarin phrase...",
                    modifier = Modifier.testTag("speaking_transcript_input")
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeoBrutalButton(
                        text = if (isRecordingMic) "STOP & ANALYZE" else "RECORD VOICE",
                        icon = if (isRecordingMic) Icons.Default.Stop else Icons.Default.Mic,
                        backgroundColor = if (isRecordingMic) BrutalYellow else BrutalOrange,
                        contentColor = if (isRecordingMic) BrutalBlack else Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("record_pronunciation_button"),
                        onClick = {
                            if (isRecordingMic) {
                                recordingJob?.cancel()
                                isRecordingMic = false
                                onAnalyzeAttempt(selectedPrompt, spokenTranscriptInput, liveWaveform, livePitch)
                            } else {
                                val hasPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPerm) {
                                    isRecordingMic = true
                                    recordingJob = scope.launch {
                                        val (wave, pitch) = speechManager.recordAcousticProfile { w, p ->
                                            liveWaveform = w
                                            livePitch = p
                                        }
                                        isRecordingMic = false
                                        onAnalyzeAttempt(selectedPrompt, spokenTranscriptInput, wave, pitch)
                                    }
                                } else {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                    )

                    NeoBrutalButton(
                        text = "EVALUATE TONES",
                        icon = Icons.Default.AutoAwesome,
                        backgroundColor = BrutalYellow,
                        contentColor = BrutalBlack,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("analyze_pronunciation_button"),
                        onClick = {
                            onAnalyzeAttempt(selectedPrompt, spokenTranscriptInput, liveWaveform, livePitch)
                        }
                    )
                }
            }
        }

        // Detailed Multi-Dimensional Pronunciation Feedback Report
        if (evaluation != null) {
            item {
                NeoBrutalCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pronunciation_feedback_card"),
                    accentHeaderColor = BrutalMint,
                    headerTitle = if (evaluation.isAiEvaluated) "AI + ACOUSTIC PRONUNCIATION REPORT" else "ACOUSTIC PITCH CONTOUR REPORT",
                    headerRightText = "OVERALL ${evaluation.overallScore}%"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScoreDimensionPill(
                            modifier = Modifier.weight(1f),
                            label = "TONES",
                            score = evaluation.toneAccuracy,
                            bgColor = BrutalMint
                        )
                        ScoreDimensionPill(
                            modifier = Modifier.weight(1f),
                            label = "PHONEMES",
                            score = evaluation.phonemeAccuracy,
                            bgColor = BrutalYellow
                        )
                        ScoreDimensionPill(
                            modifier = Modifier.weight(1f),
                            label = "FLUENCY",
                            score = evaluation.fluencyScore,
                            bgColor = BrutalCobalt
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    if (aiState is AiUiState.Loading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.5.dp,
                                color = BrutalOrange
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "REFINING PHONEME & TONE DIAGNOSTICS WITH GEMINI...",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrutalOrange
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    FeedbackDetailBlock(
                        badgeText = "TONE & SANDHI ACCURACY",
                        badgeColor = BrutalMint,
                        detail = evaluation.toneFeedback
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FeedbackDetailBlock(
                        badgeText = "INDIVIDUAL PHONEME ARTICULATION",
                        badgeColor = BrutalYellow,
                        detail = evaluation.phonemeFeedback
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FeedbackDetailBlock(
                        badgeText = "CONNECTED SPEECH FLUENCY",
                        badgeColor = BrutalOrange,
                        detail = evaluation.fluencyTip
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun LegendBlock(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .neoBrutalSurface(
                    backgroundColor = color,
                    borderColor = BrutalBlack,
                    shadowColor = BrutalBlack,
                    borderWidth = 1.5.dp,
                    shadowOffset = 1.5.dp,
                    cornerRadius = 1.dp
                )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ScoreDimensionPill(
    modifier: Modifier = Modifier,
    label: String,
    score: Int,
    bgColor: Color
) {
    val fg = if (bgColor == BrutalCobalt || bgColor == BrutalOrange) Color.White else BrutalBlack
    Box(
        modifier = modifier
            .neoBrutalSurface(
                backgroundColor = bgColor,
                borderColor = BrutalBlack,
                shadowColor = BrutalBlack,
                borderWidth = 2.dp,
                shadowOffset = 3.dp
            )
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = fg
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = fg
            )
        }
    }
}

@Composable
private fun FeedbackDetailBlock(
    badgeText: String,
    badgeColor: Color,
    detail: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .neoBrutalSurface(
                backgroundColor = MaterialTheme.colorScheme.surface,
                borderColor = BrutalBlack,
                shadowColor = BrutalBlack,
                borderWidth = 2.dp,
                shadowOffset = 3.dp
            )
            .padding(12.dp)
    ) {
        NeoBrutalBadge(
            text = badgeText,
            backgroundColor = badgeColor,
            textColor = if (badgeColor == BrutalOrange || badgeColor == BrutalCobalt) Color.White else BrutalBlack
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
