package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapter_progress")
data class ChapterProgressEntity(
    @PrimaryKey val chapterId: String,
    val title: String,
    val bookCategory: String, // "সাহিত্য সঞ্চয়ন", "আম আঁটির ভেঁপু", "বাংলা ব্যাকরণ", "প্রকল্প ও রচনা"
    val isCompleted: Boolean = false,
    val completedAt: Long = 0L,
    val quizAttemptsCount: Int = 0,
    val bestQuizScorePercent: Float = 0f
)
