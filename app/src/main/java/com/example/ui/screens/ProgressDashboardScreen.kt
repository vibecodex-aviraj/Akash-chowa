package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.local.ChapterProgressEntity
import com.example.data.local.QuizResultEntity
import com.example.ui.components.ChapterScoreBarData
import com.example.ui.components.QuizDataPoint
import com.example.ui.components.RechartsChapterBarChart
import com.example.ui.components.RechartsChapterCompletionDonut
import com.example.ui.components.RechartsPerformanceLineChart
import com.example.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressDashboardScreen(
    viewModel: ChatViewModel,
    onNavigateToQuiz: () -> Unit,
    onNavigateToSyllabus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chapterProgressList by viewModel.chapterProgress.collectAsState()
    val quizResults by viewModel.quizResults.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()

    var selectedTimeFilter by remember { mutableStateOf("সব সময়") }
    var selectedCategoryFilter by remember { mutableStateOf("সকল") }

    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    val fullDateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    // Filter quiz results by time
    val currentTime = System.currentTimeMillis()
    val filteredQuizResults = remember(quizResults, selectedTimeFilter) {
        when (selectedTimeFilter) {
            "গত ৭ দিন" -> {
                val sevenDaysAgo = currentTime - 7L * 24 * 60 * 60 * 1000
                quizResults.filter { it.timestamp >= sevenDaysAgo }
            }
            "গত ৩০ দিন" -> {
                val thirtyDaysAgo = currentTime - 30L * 24 * 60 * 60 * 1000
                quizResults.filter { it.timestamp >= thirtyDaysAgo }
            }
            else -> quizResults
        }
    }

    // Convert to Chart Data Points for Recharts Line Chart
    val lineChartPoints = remember(filteredQuizResults) {
        filteredQuizResults.map { result ->
            QuizDataPoint(
                timestamp = result.timestamp,
                scorePercent = result.percentage,
                chapterTitle = result.chapterTitle,
                dateLabel = dateFormat.format(Date(result.timestamp))
            )
        }
    }

    // Group for Chapter Bar Chart
    val chapterBarData = remember(quizResults) {
        quizResults.groupBy { it.chapterTitle }.map { (title, list) ->
            val avg = list.map { it.percentage }.average().toFloat()
            ChapterScoreBarData(
                chapterTitle = title,
                averageScore = avg,
                attempts = list.size
            )
        }.sortedByDescending { it.averageScore }
    }

    val completedChaptersCount = chapterProgressList.count { it.isCompleted }
    val totalChaptersCount = chapterProgressList.size.coerceAtLeast(1)
    val averageScore = if (quizResults.isNotEmpty()) {
        quizResults.map { it.percentage }.average().toInt()
    } else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("progress_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ভিজ্যুয়াল পারফরম্যান্স ড্যাশবোর্ড",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "অধ্যায় সমাপ্তির অগ্রগতি ও কুইজ বিশ্লেষণ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Offline Mode Switch & Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOfflineMode)
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                    else
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = if (isOfflineMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isOfflineMode) "অফলাইন মোড সক্রিয়" else "অনলাইন মোড (Gemini AI)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isOfflineMode) "ইন্টারনেট ও টোকেন খরচ ছাড়া স্থানীয় ডেটায় পড়াশোনা" else "ক্লাউড এআই বিশ্লেষণ ও উচ্চ চিন্তন ক্ষমতা",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isOfflineMode,
                        onCheckedChange = { viewModel.toggleOfflineMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.tertiary,
                            checkedTrackColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        modifier = Modifier.testTag("offline_mode_switch")
                    )
                }
            }
        }

        // Recharts-style Chapter Completion Donut
        item {
            RechartsChapterCompletionDonut(
                completedCount = completedChaptersCount,
                totalCount = totalChaptersCount
            )
        }

        // 3 Key Performance Indicator (KPI) Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // KPI 1: Completed Chapters
                KpiMetricCard(
                    title = "সম্পন্ন অধ্যায়",
                    value = "$completedChaptersCount / $totalChaptersCount",
                    icon = Icons.Default.MenuBook,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                // KPI 2: Average Score
                KpiMetricCard(
                    title = "গড় কুইজ স্কোর",
                    value = "$averageScore%",
                    icon = Icons.Default.EmojiEvents,
                    iconTint = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )

                // KPI 3: Total Tests
                KpiMetricCard(
                    title = "মোট মক টেস্ট",
                    value = "${quizResults.size} টি",
                    icon = Icons.Default.BarChart,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section 1: Recharts Line Chart - Quiz Performance Over Time
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📈 কুইজ পারফরম্যান্স ট্রেন্ড (Over Time)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Time Filters
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("সব সময়", "গত ৭ দিন", "গত ৩০ দিন").forEach { filter ->
                            FilterChip(
                                selected = selectedTimeFilter == filter,
                                onClick = { selectedTimeFilter = filter },
                                label = { Text(filter, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("filter_chip_${filter.hashCode()}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Line & Area Chart
                    RechartsPerformanceLineChart(
                        dataPoints = lineChartPoints
                    )
                }
            }
        }

        // Section 2: Recharts Bar Chart - Chapter Performance Comparison
        if (chapterBarData.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📊 অধ্যায়ভিত্তিক গড় স্কোর তুলনা",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "সবুজ: ৮০%+ (অনবদ্য) • নীল: ৬০-৮০% (ভালো) • কমলা: ৬০% এর নিচে (পুনরাবৃত্তি আবশ্যক)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        RechartsChapterBarChart(
                            chaptersData = chapterBarData
                        )
                    }
                }
            }
        }

        // Section 3: Interactive Chapter Completion Checklist
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "✅ পাঠ্যক্রম অধ্যায় সমাপ্তির তালিকা",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "পড়া শেষ হলে টিক চিহ্ন দিয়ে সমাপ্ত হিসেবে চিহ্নিত করুন:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Tabs
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("সকল", "সাহিত্য সঞ্চয়ন", "আম আঁটির ভেঁপু", "বাংলা ব্যাকরণ").forEach { cat ->
                            item {
                                FilterChip(
                                    selected = selectedCategoryFilter == cat,
                                    onClick = { selectedCategoryFilter = cat },
                                    label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val filteredChapters = chapterProgressList.filter {
                        selectedCategoryFilter == "সকল" || it.bookCategory == selectedCategoryFilter
                    }

                    filteredChapters.forEach { chapter ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.toggleChapterCompletion(chapter.chapterId, !chapter.isCompleted)
                                }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (chapter.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (chapter.isCompleted) "সম্পন্ন" else "অসম্পূর্ণ",
                                tint = if (chapter.isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chapter.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (chapter.isCompleted) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = chapter.bookCategory,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Recent Quiz History Records
        if (quizResults.isNotEmpty()) {
            item {
                Text(
                    text = "🕒 সাম্প্রতিক কুইজ প্রচেষ্টার ইতিহাস",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(quizResults.take(6), key = { it.id }) { result ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = result.chapterTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = fullDateFormat.format(Date(result.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                result.percentage >= 80 -> Color(0xFFE8F5E9)
                                result.percentage >= 60 -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            }
                        ) {
                            Text(
                                text = "${result.score}/${result.totalQuestions} (${result.percentage.toInt()}%)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    result.percentage >= 80 -> Color(0xFF2E7D32)
                                    result.percentage >= 60 -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.error
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
