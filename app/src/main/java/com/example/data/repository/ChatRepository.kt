package com.example.data.repository

import android.net.Uri
import com.example.data.ai.GeminiTutorService
import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import com.example.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepository(
    private val chatDao: ChatDao,
    private val tutorService: GeminiTutorService
) {

    fun getMessagesFlow(): Flow<List<ChatMessage>> {
        return chatDao.getAllMessages().map { entities ->
            entities.map { entity ->
                ChatMessage(
                    id = entity.id,
                    isUser = entity.isUser,
                    text = entity.text,
                    imageUris = if (entity.imageUrisJoined.isBlank()) emptyList() else {
                        entity.imageUrisJoined.split(",").filter { it.isNotBlank() }.map { Uri.parse(it) }
                    },
                    timestamp = entity.timestamp
                )
            }
        }
    }

    suspend fun saveMessage(isUser: Boolean, text: String, imageUris: List<Uri> = emptyList()): Long {
        val joinedUris = imageUris.joinToString(",") { it.toString() }
        val entity = ChatMessageEntity(
            isUser = isUser,
            text = text,
            imageUrisJoined = joinedUris,
            timestamp = System.currentTimeMillis()
        )
        return chatDao.insertMessage(entity)
    }

    suspend fun clearHistory() {
        chatDao.clearAll()
    }

    fun streamAiResponse(prompt: String, imageUris: List<Uri>): Flow<String> {
        return tutorService.generateTutorResponseStream(prompt, imageUris)
    }
}
