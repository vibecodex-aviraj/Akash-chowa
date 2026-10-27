package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ChatInputField
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.MediaAttachmentRow
import com.example.ui.components.QuickPromptChips
import com.example.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onOpenSyllabus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val attachedMedia by viewModel.attachedMedia.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isSpeaking by viewModel.textToSpeech.isSpeaking.collectAsState()

    val listState = rememberLazyListState()
    var showClearDialog by remember { mutableStateOf(false) }

    // Auto-scroll to latest message on new message or streaming text
    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "আকাশ ছোঁয়া",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "CBSE ক্লাস ৯ বাংলা শিক্ষক",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Quick button to open syllabus library
                    IconButton(
                        onClick = onOpenSyllabus,
                        modifier = Modifier.testTag("open_syllabus_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = "পাঠ্যসূচি ও গাইড",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Clear conversation button
                    if (messages.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier.testTag("clear_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "আলোচনা মুছুন",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .navigationBarsPadding()
        ) {
            // Chat Message List or Welcome State
            if (messages.isEmpty()) {
                WelcomeView(
                    onPromptClick = { prompt -> viewModel.sendMessage(prompt) },
                    onOpenSyllabus = onOpenSyllabus,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        ChatMessageItem(
                            message = message,
                            isSpeakingThis = isSpeaking,
                            onSpeakClick = { text -> viewModel.speak(text) },
                            onStopSpeakingClick = { viewModel.stopSpeaking() }
                        )
                    }
                }
            }

            // Quick Prompt Suggestion Chips
            QuickPromptChips(
                prompts = viewModel.quickPrompts,
                onPromptClick = { prompt -> viewModel.sendMessage(prompt) }
            )

            // Attached Media Preview Row
            MediaAttachmentRow(
                attachments = attachedMedia,
                onRemoveAttachment = { media -> viewModel.removeAttachment(media.uri) }
            )

            // Bottom Input Bar with Camera, Voice & Send
            ChatInputField(
                inputText = inputText,
                onInputTextChanged = { viewModel.onInputTextChanged(it) },
                onSendMessage = { viewModel.sendMessage() },
                onAddAttachment = { uri, name -> viewModel.addAttachment(uri, name) },
                speechToTextManager = viewModel.speechToText,
                onToggleVoice = { viewModel.toggleVoiceRecognition() },
                hasAttachments = attachedMedia.isNotEmpty(),
                isGenerating = isGenerating
            )
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("নতুন আলোচনা শুরু করতে চান?") },
            text = { Text("বর্তমান কথোপকথনের সমস্ত বার্তা মুছে যাবে এবং নতুন করে প্রশ্ন শুরু করতে পারবেন।") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearConversation()
                        showClearDialog = false
                    }
                ) {
                    Text("মুছে ফেলুন", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun WelcomeView(
    onPromptClick: (String) -> Unit,
    onOpenSyllabus: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        item {
            Text(
                text = "নমস্কার! আমি আকাশ ছোঁয়া",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "তোমার CBSE ক্লাস ৯ বাংলা গৃহশিক্ষক ও সহায়ক",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }

        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📚 আমি যেভাবে তোমায় সাহায্য করতে পারি:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 'আম আঁটির ভেঁপু' উপন্যাসের সম্পূর্ণ সারসংক্ষেপ, অপু-দুর্গার চরিত্র ও অধ্যায়ভিত্তিক প্রশ্ন\n" +
                                "• 'সাহিত্য সঞ্চয়ন' এর দাম, ইলিয়াস, কলিঙ্গদেশে ঝড়-বৃষ্টি, ধীবর-বৃত্তান্তের ১, ৩ ও ৫ নম্বরের মডেল উত্তর\n" +
                                "• বাংলা ব্যাকরণ: ধ্বনি পরিবর্তন, সন্ধি, শব্দ ও পদ\n" +
                                "• হোমওয়ার্কের খাতা বা বইয়ের ছবি তুলে সরাসরি পাঠালে নির্ভুল সমাধান\n" +
                                "• বাংলায় কথা বলে বা শুনে শেখার সহজ ব্যবস্থা",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "শুরু করতে যেকোনো একটি কার্ডে ট্যাপ করুন:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Quick feature cards
        item {
            StarterCard(
                title = "আম আঁটির ভেঁপু সারসংক্ষেপ ও চরিত্র",
                subtitle = "অপু, দুর্গা ও নিশ্চিন্দিপুর গ্রামের পূর্ণ বিবরণ",
                icon = Icons.Default.Book,
                iconTint = MaterialTheme.colorScheme.tertiary,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                onClick = { onPromptClick("আম আঁটির ভেঁপু উপন্যাসের মূল ভাব ও অপু-দুর্গার শৈশব সম্পর্কে সংক্ষেপে বুঝিয়ে দাও।") }
            )
        }

        item {
            StarterCard(
                title = "দাম গল্পের mastermoshai ও সুকুমার",
                subtitle = "সাহিত্য সঞ্চয়নের ৫ নম্বরের গুরুত্বপূর্ণ প্রশ্ন",
                icon = Icons.Default.School,
                iconTint = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                onClick = { onPromptClick("'দাম' গল্পে সুকুমারের অনুশোচনা ও অঙ্কের মাস্টারমশাইয়ের স্নেহের রূপ বিশ্লেষণ করো।") }
            )
        }

        item {
            StarterCard(
                title = "ধ্বনি পরিবর্তন ও ব্যাকরণ নিয়ম",
                subtitle = "স্বরভক্তি, অপিনিহিতি, অভিশ্রুতির উদাহরণসহ নিয়ম",
                icon = Icons.Default.AutoAwesome,
                iconTint = MaterialTheme.colorScheme.secondary,
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                onClick = { onPromptClick("বাংলা ব্যাকরণে স্বরভক্তি, অপিনিহিতি ও অভিশ্রুতির নিয়ম উদাহরণসহ আলোচনা করো।") }
            )
        }

        item {
            StarterCard(
                title = "সম্পূর্ণ সিলেবাস ও গাইড খুলুন",
                subtitle = "সাহিত্য সঞ্চয়ন ও আম আঁটির ভেঁপুর প্রশ্নভাণ্ডার",
                icon = Icons.Default.Book,
                iconTint = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = onOpenSyllabus
            )
        }
    }
}

@Composable
fun StarterCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    containerColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("starter_card_${title.hashCode()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
