package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val isUser: Boolean,
    val text: String,
    val imageUrisJoined: String = "", // comma-separated URIs
    val timestamp: Long = System.currentTimeMillis()
)
