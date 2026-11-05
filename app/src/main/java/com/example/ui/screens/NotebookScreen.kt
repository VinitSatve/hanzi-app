package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.PersonalNote
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
import com.example.ui.viewmodel.AiUiState
import com.example.ui.viewmodel.SentenceBreakdownResult
import com.example.ui.viewmodel.TutorChatMessage

@Composable
fun NotebookScreen(
    initialSubTab: Int = 0,
    notes: List<PersonalNote>,
    tutorMessages: List<TutorChatMessage>,
    tutorAiState: AiUiState,
    sentenceBreakdown: SentenceBreakdownResult?,
    breakdownAiState: AiUiState,
    onSendTutorMessage: (text: String, mode: String) -> Unit,
    onExplainSentence: (sentence: String) -> Unit,
    onAddNote: (title: String, chinese: String, pinyin: String, english: String, grammar: String, folder: String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onSpeak: (String) -> Unit
) {
    var selectedSubTab by remember(initialSubTab) { mutableIntStateOf(initialSubTab) }

    Column(modifier = Modifier.fillMaxSize()) {
        NeoBrutalTabRow(
            tabs = listOf("AI Mandarin Tutor", "Explain Sentence", "Personal Notebook"),
            selectedIndex = selectedSubTab,
            onSelect = { selectedSubTab = it },
            tagPrefix = "notebook_tab"
        )

        when (selectedSubTab) {
            0 -> AiMandarinTutorView(
                messages = tutorMessages,
                aiState = tutorAiState,
                onSendMessage = onSendTutorMessage,
                onSpeak = onSpeak
            )
            1 -> ExplainSentenceView(
                breakdown = sentenceBreakdown,
                aiState = breakdownAiState,
                onExplain = onExplainSentence,
                onSaveToNotebook = { b ->
                    onAddNote(
                        "Sentence Breakdown: ${b.simplified.take(16)}",
                        b.simplified,
                        b.pinyin,
                        b.englishTranslation,
                        b.grammarExplanation,
                        "Grammar"
                    )
                },
                onSpeak = onSpeak
            )
            2 -> PersonalNotesView(
                notes = notes,
                onAddNote = onAddNote,
                onDeleteNote = onDeleteNote,
                onSpeak = onSpeak
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiMandarinTutorView(
    messages: List<TutorChatMessage>,
    aiState: AiUiState,
    onSendMessage: (String, String) -> Unit,
    onSpeak: (String) -> Unit
) {
    val modes = listOf(
        "Beginner", "Daily Life", "Restaurant", "Shopping",
        "Travel", "University", "Workplace", "Technology", "Free Conversation"
    )
    var selectedMode by remember { mutableStateOf("Daily Life") }
    var inputMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .neoBrutalSurface(
                        backgroundColor = BrutalYellow,
                        borderColor = BrutalBlack,
                        shadowColor = BrutalBlack,
                        borderWidth = 2.5.dp,
                        shadowOffset = 3.5.dp
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_tutor_avatar_1793854930329),
                    contentDescription = "AI Mandarin Tutor avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "AI MANDARIN TUTOR · 智学老师",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                NeoBrutalBadge(
                    text = "GEMINI 3.8 FLASH // MODE: $selectedMode",
                    backgroundColor = BrutalYellow
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            modes.forEach { mode ->
                NeoBrutalChip(
                    text = mode,
                    selected = selectedMode == mode,
                    selectedColor = BrutalOrange,
                    onClick = { selectedMode = mode }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    NeoBrutalCard(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        backgroundColor = if (msg.isUser) BrutalYellow else MaterialTheme.colorScheme.surface,
                        accentHeaderColor = if (msg.isUser) BrutalOrange else BrutalCobalt,
                        headerTitle = if (msg.isUser) "YOU // ${msg.modeTag}" else "AI TUTOR // ${msg.modeTag}",
                        shadowOffset = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (msg.isUser) BrutalBlack else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (!msg.isUser) {
                                Spacer(modifier = Modifier.width(8.dp))
                                NeoBrutalIconButton(
                                    onClick = { onSpeak(msg.text.take(60)) },
                                    backgroundColor = BrutalYellow,
                                    size = 38.dp
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Speak tutor reply",
                                        tint = BrutalBlack,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (aiState is AiUiState.Loading || aiState is AiUiState.Streaming) {
                item {
                    NeoBrutalCard(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        accentHeaderColor = BrutalMint,
                        headerTitle = "STREAMING MANDARIN RESPONSE..."
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.5.dp,
                                color = BrutalOrange
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TUTOR IS COMPOSING...",
                                style = MaterialTheme.typography.labelLarge,
                                color = BrutalOrange
                            )
                        }
                        if (aiState is AiUiState.Streaming && aiState.partialText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = aiState.partialText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (aiState is AiUiState.Error) {
                item {
                    NeoBrutalCard(
                        modifier = Modifier.fillMaxWidth(),
                        accentHeaderColor = BrutalOrange,
                        headerTitle = "TUTOR NOTICE"
                    ) {
                        Text(
                            text = aiState.message,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            NeoBrutalTextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                label = "Message AI Tutor",
                placeholder = "Ask in Chinese or English...",
                modifier = Modifier
                    .weight(1f)
                    .testTag("tutor_chat_input")
            )
            Spacer(modifier = Modifier.width(8.dp))
            NeoBrutalIconButton(
                onClick = {
                    if (inputMessage.isNotBlank()) {
                        onSendMessage(inputMessage, selectedMode)
                        inputMessage = ""
                    }
                },
                backgroundColor = BrutalOrange,
                contentColor = Color.White,
                size = 54.dp,
                modifier = Modifier.testTag("tutor_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send to AI Tutor",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ExplainSentenceView(
    breakdown: SentenceBreakdownResult?,
    aiState: AiUiState,
    onExplain: (String) -> Unit,
    onSaveToNotebook: (SentenceBreakdownResult) -> Unit,
    onSpeak: (String) -> Unit
) {
    var sentenceInput by remember {
        mutableStateOf("虽然汉字看起来很复杂，但是掌握偏旁部首后就容易多了。")
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
                text = "EXPLAIN THIS SENTENCE // AI PARSER",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Paste any Mandarin sentence to extract Simplified characters, Pinyin, word-by-word tokens, grammar structure, and related examples.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            NeoBrutalTextField(
                value = sentenceInput,
                onValueChange = { sentenceInput = it },
                label = "Chinese Sentence to Analyze",
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("explain_sentence_input")
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NeoBrutalButton(
                    text = "ANALYZE SENTENCE",
                    icon = Icons.Default.AutoAwesome,
                    backgroundColor = BrutalOrange,
                    contentColor = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("explain_sentence_button"),
                    onClick = { onExplain(sentenceInput) }
                )
                NeoBrutalIconButton(
                    onClick = { onSpeak(sentenceInput) },
                    backgroundColor = BrutalYellow,
                    size = 52.dp
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speak sentence",
                        tint = BrutalBlack
                    )
                }
            }
        }

        if (aiState is AiUiState.Loading) {
            item {
                NeoBrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentHeaderColor = BrutalYellow,
                    headerTitle = "PARSING SYNTAX TREE..."
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = BrutalBlack
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "GENERATING STRUCTURED GRAMMATICAL BREAKDOWN...",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        if (aiState is AiUiState.Error) {
            item {
                NeoBrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentHeaderColor = BrutalOrange,
                    headerTitle = "PARSER NOTICE"
                ) {
                    Text(
                        text = aiState.message,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (breakdown != null) {
            item {
                NeoBrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentHeaderColor = BrutalCobalt,
                    headerTitle = "GRAMMATICAL DECONSTRUCTION REPORT",
                    headerRightText = "VERIFIED"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeoBrutalBadge(text = "01 // SIMPLIFIED & PINYIN", backgroundColor = BrutalYellow)
                        NeoBrutalButton(
                            text = "SAVE TO NOTEBOOK",
                            icon = Icons.Default.BookmarkAdded,
                            backgroundColor = BrutalMint,
                            height = 40.dp,
                            onClick = { onSaveToNotebook(breakdown) }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = breakdown.simplified,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = breakdown.pinyin,
                        style = MaterialTheme.typography.titleLarge,
                        color = BrutalOrange
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    NeoBrutalBadge(text = "02 // NATURAL TRANSLATION", backgroundColor = BrutalYellow)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = breakdown.englishTranslation,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    NeoBrutalBadge(text = "03 // WORD-BY-WORD TOKENS", backgroundColor = BrutalMint)
                    Spacer(modifier = Modifier.height(6.dp))
                    breakdown.wordByWord.forEach { w ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .neoBrutalSurface(
                                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                    borderColor = BrutalBlack,
                                    shadowColor = BrutalBlack,
                                    borderWidth = 2.dp,
                                    shadowOffset = 2.dp
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "▪ $w",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    NeoBrutalBadge(text = "04 // GRAMMAR STRUCTURE", backgroundColor = BrutalOrange, textColor = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = breakdown.grammarExplanation,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    NeoBrutalBadge(text = "05 // RELATED EXAMPLES", backgroundColor = BrutalCobalt, textColor = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))
                    breakdown.exampleSentences.forEach { ex ->
                        Text(
                            text = "• $ex",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 2.dp)
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
private fun PersonalNotesView(
    notes: List<PersonalNote>,
    onAddNote: (String, String, String, String, String, String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onSpeak: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFolder by remember { mutableStateOf("All") }
    var showCreateNoteDialog by remember { mutableStateOf(false) }

    if (showCreateNoteDialog) {
        var title by remember { mutableStateOf("") }
        var chinese by remember { mutableStateOf("") }
        var pinyin by remember { mutableStateOf("") }
        var english by remember { mutableStateOf("") }
        var grammar by remember { mutableStateOf("") }
        var folder by remember { mutableStateOf("Grammar") }

        AlertDialog(
            onDismissRequest = { showCreateNoteDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "NEW PERSONAL CHINESE NOTE",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NeoBrutalTextField(value = title, onValueChange = { title = it }, label = "Note Title")
                    NeoBrutalTextField(value = chinese, onValueChange = { chinese = it }, label = "Chinese Sentence / Phrase")
                    NeoBrutalTextField(value = pinyin, onValueChange = { pinyin = it }, label = "Pinyin")
                    NeoBrutalTextField(value = english, onValueChange = { english = it }, label = "English Translation")
                    NeoBrutalTextField(value = grammar, onValueChange = { grammar = it }, label = "Grammar / Usage Notes")
                    NeoBrutalTextField(value = folder, onValueChange = { folder = it }, label = "Folder (Grammar, Vocabulary, Pinyin)")
                }
            },
            confirmButton = {
                NeoBrutalButton(
                    text = "SAVE NOTE",
                    backgroundColor = BrutalMint,
                    onClick = {
                        if (title.isNotBlank() && chinese.isNotBlank()) {
                            onAddNote(title, chinese, pinyin, english, grammar, folder)
                            showCreateNoteDialog = false
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showCreateNoteDialog = false }) {
                    Text("CANCEL", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    val filteredNotes = notes.filter { n ->
        val folderMatch = selectedFolder == "All" || n.folder.equals(selectedFolder, ignoreCase = true)
        val q = searchQuery.trim().lowercase()
        val queryMatch = q.isEmpty() ||
            n.title.lowercase().contains(q) ||
            n.chineseSentence.lowercase().contains(q) ||
            n.pinyin.lowercase().contains(q) ||
            n.englishTranslation.lowercase().contains(q)
        folderMatch && queryMatch
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
                    text = "PERSONAL NOTEBOOK",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                NeoBrutalButton(
                    text = "+ NEW NOTE",
                    icon = Icons.Default.Add,
                    backgroundColor = BrutalYellow,
                    height = 44.dp,
                    onClick = { showCreateNoteDialog = true },
                    modifier = Modifier.testTag("add_note_button")
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            NeoBrutalTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search Notebook",
                placeholder = "Search saved sentences, Pinyin, or grammar notes...",
                leadingIcon = Icons.Default.Search
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Grammar", "Vocabulary", "Pinyin", "General").forEach { folder ->
                    NeoBrutalChip(
                        text = folder,
                        selected = selectedFolder == folder,
                        selectedColor = BrutalYellow,
                        onClick = { selectedFolder = folder }
                    )
                }
            }
        }

        if (filteredNotes.isEmpty()) {
            item {
                NeoBrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentHeaderColor = BrutalYellow,
                    headerTitle = "NOTEBOOK ARCHIVE EMPTY"
                ) {
                    Text(
                        text = "Tap '+ NEW NOTE' or save a breakdown from 'Explain Sentence' to build your personal Mandarin reference.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            items(filteredNotes, key = { it.id }) { note ->
                NeoBrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentHeaderColor = BrutalMint,
                    headerTitle = "FOLDER // ${note.folder.uppercase()}",
                    headerRightText = "CLOUD SYNCED"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = note.title.uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = note.chineseSentence,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            NeoBrutalIconButton(
                                onClick = { onSpeak(note.chineseSentence) },
                                backgroundColor = BrutalYellow,
                                size = 40.dp
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Speak note sentence",
                                    tint = BrutalBlack
                                )
                            }
                            NeoBrutalIconButton(
                                onClick = { onDeleteNote(note.id) },
                                backgroundColor = BrutalOrange,
                                contentColor = Color.White,
                                size = 40.dp
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete note",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    if (note.pinyin.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = note.pinyin,
                            style = MaterialTheme.typography.titleMedium,
                            color = BrutalOrange
                        )
                    }
                    if (note.englishTranslation.isNotBlank()) {
                        Text(
                            text = note.englishTranslation,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (note.grammarNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .neoBrutalSurface(
                                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                    borderColor = BrutalBlack,
                                    shadowColor = BrutalBlack,
                                    borderWidth = 2.dp,
                                    shadowOffset = 2.dp
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                text = note.grammarNotes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
