package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiTutorService
import com.example.data.local.AkashDatabase
import com.example.data.model.AttachedMedia
import com.example.data.model.ChatMessage
import com.example.data.model.SyllabusItem
import com.example.data.repository.BengaliKnowledgeBase
import com.example.data.repository.ChatRepository
import com.example.util.SpeechToTextManager
import com.example.util.TextToSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class AppTab {
    CHAT,
    SYLLABUS,
    QUIZ,
    PROJECTS
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository
    val speechToText: SpeechToTextManager
    val textToSpeech: TextToSpeechManager

    init {
        val database = AkashDatabase.getInstance(application)
        val tutorService = GeminiTutorService(application)
        repository = ChatRepository(database.chatDao(), tutorService)
        speechToText = SpeechToTextManager(application)
        textToSpeech = TextToSpeechManager(application)

        // Observe persisted messages from Room
        viewModelScope.launch {
            repository.getMessagesFlow().collectLatest { savedMessages ->
                _persistedMessages.value = savedMessages
            }
        }
    }

    private val _persistedMessages = MutableStateFlow<List<ChatMessage>>(emptyList())

    // Active in-progress streaming message
    private val _streamingMessage = MutableStateFlow<ChatMessage?>(null)
    val streamingMessage: StateFlow<ChatMessage?> = _streamingMessage.asStateFlow()

    // Combined messages for UI
    val messages = MutableStateFlow<List<ChatMessage>>(emptyList())

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _attachedMedia = MutableStateFlow<List<AttachedMedia>>(emptyList())
    val attachedMedia: StateFlow<List<AttachedMedia>> = _attachedMedia.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _selectedTab = MutableStateFlow(AppTab.CHAT)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    val quickPrompts = BengaliKnowledgeBase.getQuickPrompts()
    val syllabusItems = BengaliKnowledgeBase.syllabusItems

    init {
        // Sync combined messages list whenever persisted messages or streaming message updates
        viewModelScope.launch {
            _persistedMessages.collectLatest { list ->
                updateCombinedMessages(list, _streamingMessage.value)
            }
        }
        viewModelScope.launch {
            _streamingMessage.collectLatest { streaming ->
                updateCombinedMessages(_persistedMessages.value, streaming)
            }
        }
    }

    private fun updateCombinedMessages(persisted: List<ChatMessage>, streaming: ChatMessage?) {
        messages.value = if (streaming != null) {
            persisted + streaming
        } else {
            persisted
        }
    }

    fun onInputTextChanged(newText: String) {
        _inputText.value = newText
    }

    fun onSelectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun addAttachment(uri: Uri, name: String = "ছবি") {
        val current = _attachedMedia.value.toMutableList()
        if (current.none { it.uri == uri }) {
            current.add(AttachedMedia(uri = uri, name = name))
            _attachedMedia.value = current
        }
    }

    fun removeAttachment(uri: Uri) {
        _attachedMedia.value = _attachedMedia.value.filter { it.uri != uri }
    }

    fun clearAttachments() {
        _attachedMedia.value = emptyList()
    }

    fun sendMessage(customPrompt: String? = null) {
        val promptToSend = customPrompt ?: _inputText.value.trim()
        val imagesToSend = _attachedMedia.value.map { it.uri }

        if (promptToSend.isBlank() && imagesToSend.isEmpty()) return
        if (_isGenerating.value) return

        val displayText = if (promptToSend.isNotBlank()) promptToSend else "সংযুক্ত ছবির প্রশ্নের সমাধান দাও।"

        // Clear input and attachments immediately
        _inputText.value = ""
        clearAttachments()
        _isGenerating.value = true

        viewModelScope.launch {
            // Save user message in DB
            repository.saveMessage(
                isUser = true,
                text = displayText,
                imageUris = imagesToSend
            )

            // Prepare temporary streaming assistant message
            val tempId = System.currentTimeMillis()
            _streamingMessage.value = ChatMessage(
                id = tempId,
                isUser = false,
                text = "...",
                isStreaming = true
            )

            val accumulatedResponse = StringBuilder()
            try {
                repository.streamAiResponse(displayText, imagesToSend).collect { chunk ->
                    accumulatedResponse.append(chunk)
                    _streamingMessage.value = ChatMessage(
                        id = tempId,
                        isUser = false,
                        text = accumulatedResponse.toString(),
                        isStreaming = true
                    )
                }

                // Streaming finished, persist complete message
                val finalText = accumulatedResponse.toString().ifBlank {
                    "উত্তর তৈরি করতে পারিনি, অনুগ্রহ করে পুনরায় চেষ্টা করুন।"
                }
                repository.saveMessage(
                    isUser = false,
                    text = finalText
                )
            } catch (e: Exception) {
                repository.saveMessage(
                    isUser = false,
                    text = "⚠️ উত্তর প্রদানে ত্রুটি: ${e.localizedMessage}"
                )
            } finally {
                _streamingMessage.value = null
                _isGenerating.value = false
            }
        }
    }

    fun onQuickPromptSelected(prompt: String) {
        sendMessage(prompt)
    }

    fun clearConversation() {
        viewModelScope.launch {
            repository.clearHistory()
            textToSpeech.stop()
        }
    }

    fun toggleVoiceRecognition() {
        if (speechToText.isListening.value) {
            speechToText.stopListening()
        } else {
            speechToText.startListening { transcript ->
                if (transcript.isNotBlank()) {
                    _inputText.value = if (_inputText.value.isBlank()) transcript else "${_inputText.value} $transcript"
                }
            }
        }
    }

    fun speak(text: String) {
        textToSpeech.speak(text)
    }

    fun stopSpeaking() {
        textToSpeech.stop()
    }

    override fun onCleared() {
        super.onCleared()
        speechToText.stopListening()
        textToSpeech.shutdown()
    }
}
