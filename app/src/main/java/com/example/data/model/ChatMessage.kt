package com.example.data.model

import android.net.Uri

data class ChatMessage(
    val id: Long = 0,
    val isUser: Boolean,
    val text: String,
    val imageUris: List<Uri> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val isError: Boolean = false
)

data class AttachedMedia(
    val uri: Uri,
    val name: String = "",
    val mimeType: String = "image/*"
)

data class SyllabusItem(
    val category: String, // "সাহিত্য সঞ্চয়ন", "আম আঁটির ভেঁপু", "ব্যাকরণ", "নির্মিতি", "প্রকল্প"
    val title: String,
    val author: String = "",
    val summary: String,
    val keyQuestions: List<String>,
    val promptSuggestion: String
)
