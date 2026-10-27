package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val chapterTitle: String,
    val bookCategory: String,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Float, // e.g. 80.0f
    val timestamp: Long,
    val timeSpentSeconds: Int = 0
)
