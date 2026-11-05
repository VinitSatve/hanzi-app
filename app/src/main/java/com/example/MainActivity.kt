package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.UserProfile
import com.example.data.repository.HanziRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LearnScreen
import com.example.ui.screens.NotebookScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.theme.BrutalBlack
import com.example.ui.theme.BrutalCobalt
import com.example.ui.theme.BrutalMint
import com.example.ui.theme.BrutalOrange
import com.example.ui.theme.BrutalYellow
import com.example.ui.theme.HanziTheme
import com.example.ui.theme.neoBrutalSurface
import com.example.ui.viewmodel.DataUiState
import com.example.ui.viewmodel.HanziViewModel
import com.example.util.MandarinSpeechManager
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before any UI or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            HanziRootApp()
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun HanziRootApp(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        HanziTheme(darkTheme = false) {
            AuthScreen(onAuthSuccess = {})
        }
    } else {
        HanziAuthenticatedWorkspace(currentUserId = user.uid)
    }
}

private data class NavDestination(
    val label: String,
    val hanziStamp: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val accentColor: Color,
    val tag: String
)

@Composable
fun HanziAuthenticatedWorkspace(currentUserId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val speechManager = remember { MandarinSpeechManager(context) }

    DisposableEffect(Unit) {
        onDispose { speechManager.shutdown() }
    }

    val viewModel: HanziViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                HanziViewModel(HanziRepository(db), currentUserId, app.applicationContext)
            }
        }
    )

    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val curriculumManifest by viewModel.curriculumManifest.collectAsStateWithLifecycle()
    val offlineJsonCacheStats by viewModel.offlineJsonCacheStats.collectAsStateWithLifecycle()
    val profileUiState by viewModel.profileState.collectAsStateWithLifecycle()
    val flashcardsUiState by viewModel.flashcardsState.collectAsStateWithLifecycle()
    val notesUiState by viewModel.notesState.collectAsStateWithLifecycle()
    val downloadProgressMap by viewModel.downloadProgressMap.collectAsStateWithLifecycle()
    val pronunciationEvaluation by viewModel.pronunciationEvaluation.collectAsStateWithLifecycle()
    val speakingAiState by viewModel.speakingAiState.collectAsStateWithLifecycle()
    val tutorMessages by viewModel.tutorMessages.collectAsStateWithLifecycle()
    val tutorConversations by viewModel.tutorConversations.collectAsStateWithLifecycle()
    val activeConversationId by viewModel.activeConversationId.collectAsStateWithLifecycle()
    val tutorAiState by viewModel.tutorAiState.collectAsStateWithLifecycle()
    val sentenceBreakdown by viewModel.sentenceBreakdown.collectAsStateWithLifecycle()
    val breakdownAiState by viewModel.breakdownAiState.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var practiceInitialSubTab by remember { mutableIntStateOf(0) }
    var notebookInitialSubTab by remember { mutableIntStateOf(0) }

    if (selectedTab != 0) {
        BackHandler { selectedTab = 0 }
    }

    val activeProfile = (profileUiState as? DataUiState.Success)?.data
        ?: UserProfile(id = currentUserId, userId = currentUserId)
    val activeFlashcards = (flashcardsUiState as? DataUiState.Success)?.data ?: emptyList()
    val activeNotes = (notesUiState as? DataUiState.Success)?.data ?: emptyList()

    val destinations = listOf(
        NavDestination("Home", "首", Icons.Filled.Home, Icons.Outlined.Home, HanziTheme.PureBlack, "nav_home"),
        NavDestination("Learn", "学", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook, HanziTheme.PureBlack, "nav_learn"),
        NavDestination("Practice", "练", Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq, HanziTheme.PureBlack, "nav_practice"),
        NavDestination("Tutor", "师", Icons.Filled.Book, Icons.Outlined.Book, HanziTheme.PureBlack, "nav_notebook"),
        NavDestination("Progress", "进", Icons.Filled.Analytics, Icons.Outlined.Analytics, HanziTheme.PureBlack, "nav_progress")
    )

    HanziTheme(darkTheme = isDarkTheme) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                NeoBrutalBottomNavigationBar(
                    destinations = destinations,
                    selectedTab = selectedTab,
                    onSelectTab = { idx ->
                        speechManager.triggerHaptic()
                        selectedTab = idx
                    }
                )
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = HanziTheme.colors.background
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    AnimatedVisibility(visible = statusBanner != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                                .neoBrutalSurface(
                                    backgroundColor = HanziTheme.colors.electricYellow,
                                    borderColor = HanziTheme.colors.borderBlack,
                                    shadowColor = HanziTheme.shadows.shadowColor,
                                    borderWidth = HanziTheme.borders.thinWidth,
                                    shadowOffset = 3.dp,
                                    cornerRadius = HanziTheme.borders.cornerRadius
                                )
                                .clickable { viewModel.clearStatusBanner() }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "● ${statusBanner ?: ""}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = BrutalBlack,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "[DISMISS]",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrutalBlack
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTab) {
                            0 -> DashboardScreen(
                                profile = activeProfile,
                                flashcards = activeFlashcards,
                                isDarkTheme = isDarkTheme,
                                onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                                onToggleScriptMode = { viewModel.toggleScriptMode() },
                                onToggleHskStandard = { viewModel.toggleHskStandard() },
                                onSelectHskLevel = { viewModel.updateTargetHskLevel(it) },
                                onContinueLearning = { selectedTab = 1 },
                                onOpenSpeakingCenter = {
                                    practiceInitialSubTab = 0
                                    selectedTab = 2
                                },
                                onOpenFlashcardReview = {
                                    practiceInitialSubTab = 1
                                    selectedTab = 2
                                },
                                onOpenAiTutor = {
                                    notebookInitialSubTab = 0
                                    selectedTab = 3
                                },
                                onSpeakSample = { text -> speechManager.speak(text, false) }
                            )
                            1 -> LearnScreen(
                                profile = activeProfile,
                                onSpeak = { text, slow -> speechManager.speak(text, slow) },
                                onCompleteLesson = { lessonId, pts ->
                                    viewModel.recordLearningMilestone(
                                        pointsEarned = pts,
                                        minutesAdded = 5,
                                        completedLessonId = lessonId,
                                        badgeToUnlock = "badge_first_step"
                                    )
                                },
                                onAddVocabToSrs = { word ->
                                    viewModel.addCustomVocabularyCard(
                                        simplified = word.simplified,
                                        traditional = word.traditional,
                                        pinyin = word.pinyin,
                                        english = word.english,
                                        partOfSpeech = word.partOfSpeech,
                                        exampleChinese = word.exampleSimplified,
                                        examplePinyin = word.examplePinyin,
                                        exampleEnglish = word.exampleEnglish,
                                        hskLevel = word.hskLevel,
                                        category = word.category
                                    )
                                },
                                onAddCustomVocab = { simp, trad, pin, eng, lvl ->
                                    viewModel.addCustomVocabularyCard(
                                        simplified = simp,
                                        traditional = trad,
                                        pinyin = pin,
                                        english = eng,
                                        partOfSpeech = "Custom",
                                        exampleChinese = "",
                                        examplePinyin = "",
                                        exampleEnglish = "",
                                        hskLevel = lvl,
                                        category = "Personal"
                                    )
                                },
                                curriculumManifest = curriculumManifest,
                                offlineJsonCacheStats = offlineJsonCacheStats
                            )
                            2 -> PracticeScreen(
                                initialSubTab = practiceInitialSubTab,
                                profile = activeProfile,
                                flashcards = activeFlashcards,
                                speechManager = speechManager,
                                pronunciationEvaluation = pronunciationEvaluation,
                                speakingAiState = speakingAiState,
                                onAnalyzePronunciation = { prompt, text, wave, pitch ->
                                    viewModel.analyzePronunciationAttempt(prompt, text, wave, pitch)
                                },
                                onReviewFlashcard = { card, grade ->
                                    viewModel.reviewFlashcard(card, grade)
                                },
                                onToggleFavoriteCard = { card ->
                                    viewModel.toggleFavoriteCard(card)
                                },
                                onMasterCharacter = { charId ->
                                    viewModel.recordLearningMilestone(
                                        pointsEarned = 30,
                                        minutesAdded = 3,
                                        masteredCharId = charId,
                                        badgeToUnlock = "badge_hanzi_scribe"
                                    )
                                },
                                onCompleteExamOrListening = { pts ->
                                    viewModel.recordLearningMilestone(pointsEarned = pts, minutesAdded = 3)
                                }
                            )
                            3 -> NotebookScreen(
                                initialSubTab = notebookInitialSubTab,
                                notes = activeNotes,
                                tutorMessages = tutorMessages,
                                tutorConversations = tutorConversations,
                                activeConversationId = activeConversationId,
                                tutorAiState = tutorAiState,
                                sentenceBreakdown = sentenceBreakdown,
                                breakdownAiState = breakdownAiState,
                                onSendTutorMessage = { text, mode ->
                                    viewModel.sendTutorMessage(text, mode)
                                },
                                onStartNewConversation = { mode ->
                                    viewModel.startNewTutorConversation(mode)
                                },
                                onSelectConversation = { threadId ->
                                    viewModel.selectTutorConversation(threadId)
                                },
                                onRetryTutorMessage = {
                                    viewModel.retryLastTutorMessage()
                                },
                                onExplainSentence = { sentence ->
                                    viewModel.explainChineseSentence(sentence)
                                },
                                onAddNote = { title, chinese, pinyin, english, grammar, folder ->
                                    viewModel.addPersonalNote(title, chinese, pinyin, english, grammar, folder)
                                },
                                onDeleteNote = { noteId ->
                                    viewModel.deletePersonalNote(noteId)
                                },
                                onSpeak = { text -> speechManager.speak(text, false) }
                            )
                            4 -> ProgressScreen(
                                profile = activeProfile,
                                flashcards = activeFlashcards,
                                downloadProgressMap = downloadProgressMap,
                                onDownloadPack = { viewModel.downloadOfflinePack(it) },
                                onRemovePack = { viewModel.removeOfflinePack(it) },
                                onUpdateDailyGoal = { viewModel.updateDailyGoalMinutes(it) },
                                onSignOut = {
                                    signOut(
                                        context = context,
                                        credentialManager = credentialManager,
                                        onSignOutComplete = {},
                                        scope = scope
                                    )
                                },
                                offlineJsonCacheStats = offlineJsonCacheStats
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NeoBrutalBottomNavigationBar(
    destinations: List<NavDestination>,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HanziTheme.colors.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HanziTheme.borders.thinWidth)
                .background(HanziTheme.colors.borderBlack)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEachIndexed { idx, dest ->
                val isSelected = selectedTab == idx
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val pressOffset by animateDpAsState(
                    targetValue = if (isPressed) 2.dp else 0.dp,
                    animationSpec = tween(60),
                    label = "navItemPress"
                )
                val fgColor = if (isSelected) Color.White else HanziTheme.colors.onSurface

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .offset(x = pressOffset, y = pressOffset)
                        .then(
                            if (isSelected) {
                                Modifier.neoBrutalSurface(
                                    backgroundColor = HanziTheme.PureBlack,
                                    borderColor = HanziTheme.colors.borderBlack,
                                    shadowColor = HanziTheme.VividOrange,
                                    borderWidth = HanziTheme.borders.thinWidth,
                                    shadowOffset = (3.dp - pressOffset).coerceAtLeast(1.dp),
                                    cornerRadius = HanziTheme.borders.cornerRadius
                                )
                            } else {
                                Modifier.background(Color.Transparent)
                            }
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onSelectTab(idx) }
                        )
                        .testTag(dest.tag),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(HanziTheme.VividOrange)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Icon(
                                imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                contentDescription = dest.label,
                                tint = fgColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dest.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = fgColor
                        )
                    }
                }
            }
        }
    }
}
