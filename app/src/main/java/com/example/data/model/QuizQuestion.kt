package com.example.data.model

data class QuizQuestion(
    val id: String,
    val chapterTitle: String,
    val category: String, // "সাহিত্য সঞ্চয়ন", "আম আঁটির ভেঁপু", "ব্যাকরণ"
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class ChapterQuizSet(
    val chapterTitle: String,
    val category: String,
    val questions: List<QuizQuestion>
)
