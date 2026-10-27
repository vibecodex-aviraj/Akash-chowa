package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ChatInputField
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.HistoryBottomSheet
import com.example.ui.components.MediaAttachmentRow
import com.example.ui.components.QuickPromptChips
import com.example.ui.components.VoiceModalBottomSheet
import com.example.ui.viewmodel.ChatViewModel
import com.example.util.ImageUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onOpenSyllabus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val attachedMedia by viewModel.attachedMedia.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isSpeaking by viewModel.textToSpeech.isSpeaking.collectAsState()
    val remainingTokens by viewModel.remainingTokens.collectAsState()
    val nextRenewalDate by viewModel.nextRenewalDate.collectAsState()
    val persistedMessages by viewModel.persistedMessages.collectAsState()

    val listState = rememberLazyListState()
    var showClearDialog by remember { mutableStateOf(false) }
    var showVoiceModal by remember { mutableStateOf(false) }
    var showHistoryModal by remember { mutableStateOf(false) }
    var showTokenInfoDialog by remember { mutableStateOf(false) }

    // Camera URI state
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Direct Camera Launcher for Welcome Cards and Header
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            viewModel.addAttachment(tempCameraUri!!, "ক্যামেরা ছবি")
            Toast.makeText(context, "ছবি সংযুক্ত করা হয়েছে", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = ImageUtil.createTempImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "ক্যামেরা ব্যবহারের অনুমতি আবশ্যক", Toast.LENGTH_SHORT).show()
        }
    }

    // Direct Attachment Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        uris.forEach { uri ->
            viewModel.addAttachment(uri, "ছবি")
        }
        if (uris.isNotEmpty()) {
            Toast.makeText(context, "${uris.size}টি ছবি সংযুক্ত করা হয়েছে", Toast.LENGTH_SHORT).show()
        }
    }

    // Direct Audio Permission Launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showVoiceModal = true
            viewModel.speechToText.startListening { transcript ->
                if (transcript.isNotBlank()) {
                    viewModel.onInputTextChanged(transcript)
                }
            }
        } else {
            Toast.makeText(context, "ভয়েস রেকর্ডিং অনুমতি আবশ্যক", Toast.LENGTH_SHORT).show()
        }
    }

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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "আকাশ ছোঁয়া",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Clickable 8,000 Free Monthly Token Badge
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier
                                        .clickable { showTokenInfoDialog = true }
                                        .testTag("top_bar_token_badge")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "$remainingTokens",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "CBSE ক্লাস ৯ বাংলা শিক্ষক",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // History (Room Database)
                    IconButton(
                        onClick = { showHistoryModal = true },
                        modifier = Modifier.testTag("top_bar_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "পূর্ববর্তী আলোচনার ইতিহাস",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Quick Action: Camera
                    IconButton(
                        onClick = {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.testTag("top_bar_camera_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "ক্যামেরা ছবি",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Quick Action: Attachment
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.testTag("top_bar_attachment_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "ছবি / ফাইল সংযুক্ত করুন",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Quick Action: Voice
                    IconButton(
                        onClick = {
                            audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        },
                        modifier = Modifier.testTag("top_bar_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "ভয়েস রেকর্ড",
                            tint = MaterialTheme.colorScheme.tertiary
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
                    onLaunchCamera = { cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) },
                    onLaunchAttachments = {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onLaunchVoice = { audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO) },
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

            // Quick Prompt & Word Limit Suggestion Chips
            QuickPromptChips(
                prompts = viewModel.quickPrompts,
                onPromptClick = { prompt -> viewModel.sendMessage(prompt) },
                onAppendWordLimit = { limit ->
                    val cleanLimit = limit.replace(" (১ নম্বর)", "").replace(" (৩ নম্বর)", "").replace(" (৫ নম্বর)", "").replace(" (বিশদ)", "")
                    val current = inputText.trim()
                    if (current.isBlank()) {
                        viewModel.onInputTextChanged("$cleanLimit উত্তর দাও: ")
                    } else {
                        viewModel.onInputTextChanged("$current ($cleanLimit মধ্যে উত্তর দাও)")
                    }
                }
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
                onToggleVoice = {
                    audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                },
                hasAttachments = attachedMedia.isNotEmpty(),
                isGenerating = isGenerating
            )
        }
    }

    if (showVoiceModal) {
        VoiceModalBottomSheet(
            speechToTextManager = viewModel.speechToText,
            onSendTranscript = { transcript ->
                viewModel.sendMessage(transcript)
            },
            onDismiss = { showVoiceModal = false }
        )
    }

    if (showHistoryModal) {
        HistoryBottomSheet(
            messages = persistedMessages,
            onSelectMessage = { text ->
                viewModel.onInputTextChanged(text)
            },
            onDeleteMessage = { id ->
                viewModel.deleteMessage(id)
            },
            onSpeakMessage = { text ->
                viewModel.speak(text)
            },
            onDismiss = { showHistoryModal = false }
        )
    }

    if (showTokenInfoDialog) {
        AlertDialog(
            onDismissRequest = { showTokenInfoDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "মাসিক ৮,০০০ ফ্রি টোকেন কোটা",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "আকাশ ছোঁয়া আপনাকে প্রতি মাসের ১লা তারিখে সম্পূর্ণ বিনামূল্যে ৮,০০০ জেমিনি এআই টোকেন প্রদান করে।",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("অবশিষ্ট টোকেন:", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "$remainingTokens / 8000",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("স্বয়ংক্রিয় নবায়ন:", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    nextRenewalDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "💡 আপনার প্রশ্নের উত্তরের জন্য কোনো কৃত্রিম সীমা নেই। ক্লাস ৯ বাংলা সহায়িকা হিসেবে প্রতি মাসের শুরুতে টোকেন স্বয়ংক্রিয়ভাবে ৮,০০০-এ নবায়ন হয়।",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTokenInfoDialog = false }) {
                    Text("ঠিক আছে")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.refreshFreeTokens()
                    Toast.makeText(context, "৮,০০০ ফ্রি টোকেন সফলভাবে নবায়ন হয়েছে!", Toast.LENGTH_SHORT).show()
                }) {
                    Text("ফ্রি রিফ্রেশ", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
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
    onLaunchCamera: () -> Unit,
    onLaunchAttachments: () -> Unit,
    onLaunchVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
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
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "CBSE ক্লাস ৯ বাংলা সহায়ক ও গৃহশিক্ষক",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }

        // 3 Big Feature Cards: Camera, Attachments, Voice
        item {
            Text(
                text = "⚡ এই ৩টি উপায়ে সরাসরি প্রশ্ন করুন:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Feature 1: Camera
                FeatureActionCard(
                    title = "📸 ক্যামেরা",
                    subtitle = "ছবি তুলে প্রশ্ন",
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    onClick = onLaunchCamera,
                    modifier = Modifier.weight(1f)
                )

                // Feature 2: Attachments
                FeatureActionCard(
                    title = "📎 সংযুক্তি",
                    subtitle = "ছবি / PDF ফাইল",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    onClick = onLaunchAttachments,
                    modifier = Modifier.weight(1f)
                )

                // Feature 3: Voice
                FeatureActionCard(
                    title = "🎙️ ভয়েস",
                    subtitle = "মুখে কথা বলুন",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    onClick = onLaunchVoice,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📚 অন্তর্ভুক্ত পাঠ্যসমূহ:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• 'আম আঁটির ভেঁপু' (বিভূতিভূষণ বন্দ্যোপাধ্যায়) — অপুর শৈশব, দুর্গা, বনভোজন, কাশীযাত্রা\n" +
                                "• 'সাহিত্য সঞ্চয়ন' — দাম, কলিঙ্গদেশে ঝড়-বৃষ্টি, ধীবর-বৃত্তান্ত, ইলিয়াস, আবহমান, চিঠি, ভাঙার গান\n" +
                                "• বাংলা ব্যাকরণ — ধ্বনি পরিবর্তন, সন্ধি বিচ্ছেদ, শব্দ ও পদ\n" +
                                "• নির্মিতি ও প্রজেক্ট — ভাবসম্প্রসারণ, রচনা, প্রতিবেদন",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Quick Starter Topic Cards
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
    }
}

@Composable
fun FeatureActionCard(
    title: String,
    subtitle: String,
    containerColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("feature_action_${title.hashCode()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
