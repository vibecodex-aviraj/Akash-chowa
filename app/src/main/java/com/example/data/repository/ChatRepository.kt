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
            entities.map { it.toChatMessage() }
        }
    }

    fun searchMessages(query: String): Flow<List<ChatMessage>> {
        return chatDao.searchMessages(query).map { entities ->
            entities.map { it.toChatMessage() }
        }
    }

    fun getMessageCount(): Flow<Int> = chatDao.getMessageCount()

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

    suspend fun deleteMessage(id: Long) {
        chatDao.deleteMessage(id)
    }

    suspend fun clearHistory() {
        chatDao.clearAll()
    }

    fun streamAiResponse(prompt: String, imageUris: List<Uri>): Flow<String> {
        return tutorService.generateTutorResponseStream(prompt, imageUris)
    }

    private fun ChatMessageEntity.toChatMessage(): ChatMessage {
        return ChatMessage(
            id = this.id,
            isUser = this.isUser,
            text = this.text,
            imageUris = if (this.imageUrisJoined.isBlank()) emptyList() else {
                this.imageUrisJoined.split(",").filter { it.isNotBlank() }.map { Uri.parse(it) }
            },
            timestamp = this.timestamp
        )
    }
}
