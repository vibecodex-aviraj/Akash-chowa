package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.util.ImageUtil
import com.example.util.SpeechToTextManager

@Composable
fun ChatInputField(
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAddAttachment: (Uri, String) -> Unit,
    speechToTextManager: SpeechToTextManager,
    onToggleVoice: () -> Unit,
    hasAttachments: Boolean,
    isGenerating: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showVoiceModal by remember { mutableStateOf(false) }

    // Camera capture temporary URI
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            onAddAttachment(tempCameraUri!!, "ক্যামেরা ছবি")
            Toast.makeText(context, "ছবি সংযুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission Launcher for Camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = ImageUtil.createTempImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "ক্যামেরা অনুমতি প্রয়োজন", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission Launcher for Audio Recording
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showVoiceModal = true
            speechToTextManager.startListening { transcript ->
                if (transcript.isNotBlank()) {
                    onInputTextChanged(if (inputText.isBlank()) transcript else "$inputText $transcript")
                }
            }
        } else {
            Toast.makeText(context, "ভয়েস রেকর্ডিং অনুমতি প্রয়োজন", Toast.LENGTH_SHORT).show()
        }
    }

    // Photo Picker Launcher (multi-image)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        uris.forEach { uri ->
            onAddAttachment(uri, "ছবি")
        }
        if (uris.isNotEmpty()) {
            Toast.makeText(context, "${uris.size}টি ছবি সংযুক্ত হয়েছে", Toast.LENGTH_SHORT).show()
        }
    }

    // PDF / Document Picker Launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            onAddAttachment(uri, "নথি")
        }
    }

    val isListening by speechToTextManager.isListening.collectAsState()

    // Pulse animation for recording microphone
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val micBgColor by animateColorAsState(
        targetValue = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
        label = "micColor"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Feature Quick Action Strip (Camera, Attachments, Voice)
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Feature 1: Camera button pill
                AssistChip(
                    onClick = {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    label = { Text("📸 ক্যামেরা (ছবি তুলুন)") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.primary
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("quick_action_camera")
                )

                // Feature 2: Attachments button pill
                AssistChip(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    label = { Text("📎 ছবি / ফাইল যোগ") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.secondary
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("quick_action_attach")
                )

                // Feature 3: Voice question button pill
                AssistChip(
                    onClick = {
                        audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                    },
                    label = { Text("🎙️ মুখে বলুন (ভয়েস)") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.tertiary
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("quick_action_voice")
                )
            }
        }

        // Main Input Row
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // 1. Direct Camera Button (📸)
                IconButton(
                    onClick = {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("direct_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "ক্যামেরা দিয়ে ছবি তুলুন",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 2. Direct Attachments Button (📎)
                Box {
                    IconButton(
                        onClick = { showAttachmentMenu = true },
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("direct_attachment_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "ছবি বা ফাইল সংযুক্ত করুন",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showAttachmentMenu,
                        onDismissRequest = { showAttachmentMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("🖼️ গ্যালারি থেকে ছবি (একাধিক)") },
                            onClick = {
                                showAttachmentMenu = false
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            modifier = Modifier.testTag("menu_gallery_photos")
                        )

                        DropdownMenuItem(
                            text = { Text("📄 নথি বা PDF ফাইল") },
                            onClick = {
                                showAttachmentMenu = false
                                documentPickerLauncher.launch(arrayOf("application/pdf", "image/*", "text/*"))
                            },
                            leadingIcon = {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            },
                            modifier = Modifier.testTag("menu_pdf_documents")
                        )
                    }
                }

                // 3. Text Field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputTextChanged,
                    placeholder = {
                        Text(
                            text = if (isListening) "বাংলায় বলুন..." else "বাংলায় প্রশ্ন লিখুন...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .testTag("chat_input_text_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Default
                    )
                )

                // 4. Direct Voice Button (🎙️)
                IconButton(
                    onClick = {
                        audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .scale(micScale)
                        .clip(CircleShape)
                        .background(micBgColor)
                        .testTag("direct_voice_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isListening) "ভয়েস বন্ধ করুন" else "বাংলায় কথা বলুন",
                        tint = if (isListening) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // 5. Send Button
                val canSend = (inputText.isNotBlank() || hasAttachments) && !isGenerating
                IconButton(
                    onClick = onSendMessage,
                    enabled = canSend,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .testTag("send_message_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "পাঠান",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Interactive Voice Modal Bottom Sheet
    if (showVoiceModal) {
        VoiceModalBottomSheet(
            speechToTextManager = speechToTextManager,
            onSendTranscript = { transcript ->
                onInputTextChanged(transcript)
                onSendMessage()
            },
            onDismiss = {
                showVoiceModal = false
            }
        )
    }
}
