package com.example.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.example.data.repository.BengaliKnowledgeBase
import com.example.util.ImageUtil
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerationConfig
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.Part
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.ThinkingConfig
import com.google.firebase.ai.type.ThinkingLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "GeminiTutorService"

class GeminiTutorService(private val context: Context) {

    private val model by lazy {
        val thinkingConfig = ThinkingConfig.Builder().apply {
            setThinkingLevel(ThinkingLevel.HIGH)
            setIncludeThoughts(false)
        }.build()

        val generationConfig = GenerationConfig.Builder().apply {
            setThinkingConfig(thinkingConfig)
            setMaxOutputTokens(8192) // Unlimited answers without premature truncation
            setTemperature(0.6f)
        }.build()

        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = Content("system", listOf(TextPart(TutorPrompts.SYSTEM_INSTRUCTION))),
            generationConfig = generationConfig
        )
    }

    /**
     * Generates a streaming response for the prompt and optional attached images.
     */
    fun generateTutorResponseStream(
        prompt: String,
        imageUris: List<Uri> = emptyList()
    ): Flow<String> = flow {
        val parts = mutableListOf<Part>()
        parts.add(TextPart(prompt))

        // Load bitmaps safely from URIs (capped at 1024 to stay under 20MB payload)
        for (uri in imageUris) {
            val bmp = ImageUtil.uriToBitmap(context, uri, maxDimension = 1024)
            if (bmp != null) {
                parts.add(ImagePart(bmp))
            }
        }

        try {
            val contentPayload = Content("user", parts)

            withTimeout(60_000L) {
                model.generateContentStream(contentPayload).collect { chunk ->
                    val textChunk = chunk.text.orEmpty()
                    if (textChunk.isNotEmpty()) {
                        emit(textChunk)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI generation error, checking fallback", e)
            val fallback = findFallbackAnswer(prompt)
            if (fallback != null) {
                emit(fallback)
            } else {
                emit(
                    "⚠️ দুঃখিত, এই মুহূর্তে উত্তর তৈরিতে সাময়িক বিঘ্ন ঘটেছে (${e.localizedMessage ?: "নেটওয়ার্ক সমস্যা"})।\n\nঅনুগ্রহ করে আপনার ইন্টারনেট সংযোগ পরীক্ষা করে পুনরায় প্রশ্ন করুন, অথবা নিচের পাঠ্যতালিকা থেকে যেকোনো অধ্যায় বেছে নিন।"
                )
            }
        }
    }

    /**
     * Smart fallback for common queries when offline or network interrupted.
     */
    private fun findFallbackAnswer(query: String): String? {
        val lower = query.lowercase()
        for (item in BengaliKnowledgeBase.syllabusItems) {
            if (lower.contains(item.title.lowercase()) || lower.contains(item.author.lowercase())) {
                return buildString {
                    append("📚 **${item.title}** (${item.author})\n")
                    append("শ্রেণি: ${item.category}\n\n")
                    append("📖 **মূল ভাব ও সারসংক্ষেপ:**\n")
                    append("${item.summary}\n\n")
                    append("🎯 **পরীক্ষার জন্য গুরুত্বপূর্ণ প্রশ্নোত্তর:**\n")
                    item.keyQuestions.forEach { q ->
                        append("• $q\n")
                    }
                    append("\n*(অফলাইন সহায়িকা থেকে প্রস্তুতকৃত)*")
                }
            }
        }
        return null
    }
}
