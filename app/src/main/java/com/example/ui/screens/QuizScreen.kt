package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChapterQuizSet
import com.example.data.repository.BengaliQuizData

@Composable
fun QuizScreen(
    onAskAiQuestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val quizSets = BengaliQuizData.allQuizzes
    var selectedSetIndex by remember { mutableIntStateOf(0) }
    val currentSet = quizSets.getOrElse(selectedSetIndex) { quizSets.first() }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    // Map of questionId to selected option index
    val userAnswers = remember { mutableStateMapOf<String, Int>() }

    val currentQuestion = currentSet.questions.getOrNull(currentQuestionIndex)
    val selectedOption = currentQuestion?.let { userAnswers[it.id] }
    val isAnswered = selectedOption != null

    // Score calculation
    val totalAnswered = currentSet.questions.count { userAnswers[it.id] != null }
    val score = currentSet.questions.count { q -> userAnswers[q.id] == q.correctIndex }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "অধ্যায়ভিত্তিক মক টেস্ট ও কুইজ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "সাহিত্য সঞ্চয়ন ও আম আঁটির ভেঁপু প্রশ্নমালা",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Chapter Selector Chips
        item {
            Text(
                text = "অধ্যায় বেছে নিন:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quizSets.indices.toList()) { index ->
                    val set = quizSets[index]
                    FilterChip(
                        selected = selectedSetIndex == index,
                        onClick = {
                            selectedSetIndex = index
                            currentQuestionIndex = 0
                            userAnswers.clear()
                        },
                        label = {
                            Text(
                                text = set.chapterTitle.replace("সাহিত্য সঞ্চয়ন — ", "").replace("আম আঁটির ভেঁপু — ", ""),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier.testTag("quiz_chapter_chip_$index")
                    )
                }
            }
        }

        // Question Progress & Score Card
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "প্রশ্ন: ${currentQuestionIndex + 1} / ${currentSet.questions.size}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "স্কোর: $score / $totalAnswered সঠিক",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Active Question Card
        if (currentQuestion != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = currentQuestion.question,
                            style = MaterialTheme.typography.titleMedium.copy(lineHeight = 24.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Options
                        currentQuestion.options.forEachIndexed { index, optionText ->
                            val isThisSelected = selectedOption == index
                            val isCorrectOption = index == currentQuestion.correctIndex

                            val borderCol = when {
                                !isAnswered -> MaterialTheme.colorScheme.outlineVariant
                                isCorrectOption -> Color(0xFF2E7D32) // Green
                                isThisSelected -> MaterialTheme.colorScheme.error // Red
                                else -> MaterialTheme.colorScheme.outlineVariant
                            }

                            val bgCol = when {
                                !isAnswered -> MaterialTheme.colorScheme.surface
                                isCorrectOption -> Color(0xFFE8F5E9)
                                isThisSelected -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.surface
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.5.dp, borderCol, RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isAnswered) {
                                        userAnswers[currentQuestion.id] = index
                                    }
                                    .testTag("quiz_option_${currentQuestion.id}_$index"),
                                color = bgCol
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val prefix = when (index) {
                                        0 -> "(ক)"
                                        1 -> "(খ)"
                                        2 -> "(গ)"
                                        else -> "(ঘ)"
                                    }
                                    Text(
                                        text = prefix,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCorrectOption && isAnswered) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (isAnswered && isCorrectOption) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "সঠিক",
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Detailed Explanation
                        AnimatedVisibility(visible = isAnswered) {
                            Column(modifier = Modifier.padding(top = 14.dp)) {
                                Surface(
                                    color = if (selectedOption == currentQuestion.correctIndex)
                                        Color(0xFFE8F5E9)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = if (selectedOption == currentQuestion.correctIndex)
                                                "🎉 চমৎকার! সঠিক উত্তর।"
                                            else
                                                "⚠️ উত্তরটি সঠিক হয়নি। সঠিক উত্তর: ( ${when(currentQuestion.correctIndex) { 0 -> "ক"; 1 -> "খ"; 2 -> "গ"; else -> "ঘ" }} )",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedOption == currentQuestion.correctIndex) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "💡 ব্যাখ্যা: ${currentQuestion.explanation}",
                                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Button to ask Gemini bot for more details on this question
                                OutlinedButton(
                                    onClick = {
                                        val prompt = "কুইজের এই প্রশ্নটি বিশদে বুঝিয়ে দাও:\nপ্রশ্ন: ${currentQuestion.question}\nসঠিক উত্তর: ${currentQuestion.options[currentQuestion.correctIndex]}\nব্যাখ্যা: ${currentQuestion.explanation}"
                                        onAskAiQuestion(prompt)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ask_gemini_quiz_question"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Gemini শিক্ষকের কাছে এই প্রশ্নটি আরও বিস্তারিত বুঝুন")
                                }
                            }
                        }

                        // Navigation Buttons
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (currentQuestionIndex > 0) {
                                OutlinedButton(
                                    onClick = { currentQuestionIndex-- },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("আগের প্রশ্ন")
                                }
                            } else {
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            if (currentQuestionIndex < currentSet.questions.size - 1) {
                                Button(
                                    onClick = { currentQuestionIndex++ },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("next_question_button")
                                ) {
                                    Text("পরের প্রশ্ন")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        // Reset to first question
                                        currentQuestionIndex = 0
                                        userAnswers.clear()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.testTag("restart_quiz_button")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("আবার শুরু করুন")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
