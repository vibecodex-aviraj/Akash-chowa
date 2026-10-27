package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class QuizDataPoint(
    val timestamp: Long,
    val scorePercent: Float,
    val chapterTitle: String,
    val dateLabel: String
)

data class ChapterScoreBarData(
    val chapterTitle: String,
    val averageScore: Float,
    val attempts: Int
)

/**
 * Recharts-style Area & Line chart showing Quiz Performance over time.
 */
@Composable
fun RechartsPerformanceLineChart(
    dataPoints: List<QuizDataPoint>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "কোনো কুইজের ডেটা এখনো সংরক্ষিত নেই।",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    var selectedPoint by remember { mutableStateOf<QuizDataPoint?>(null) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(dataPoints) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Interactive Tooltip Box
        if (selectedPoint != null) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${selectedPoint!!.chapterTitle} • ${selectedPoint!!.scorePercent.toInt()}% (${selectedPoint!!.dateLabel})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        } else {
            Text(
                text = "💡 গ্রাফের পয়েন্টে চাপ দিয়ে বিস্তারিত স্কোর দেখুন",
                style = MaterialTheme.typography.labelSmall,
                color = onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 6.dp)
            )
        }

        // Canvas Recharts Graph
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .testTag("recharts_line_chart")
                .pointerInput(dataPoints) {
                    detectTapGestures { tapOffset ->
                        val paddingLeft = 40.dp.toPx()
                        val paddingRight = 20.dp.toPx()
                        val chartWidth = size.width - paddingLeft - paddingRight
                        val stepX = if (dataPoints.size > 1) chartWidth / (dataPoints.size - 1) else 0f

                        // Find closest point
                        var closestIndex = 0
                        var minDistance = Float.MAX_VALUE
                        dataPoints.forEachIndexed { index, _ ->
                            val px = if (dataPoints.size > 1) paddingLeft + index * stepX else size.width / 2f
                            val distance = kotlin.math.abs(tapOffset.x - px)
                            if (distance < minDistance) {
                                minDistance = distance
                                closestIndex = index
                            }
                        }
                        if (minDistance < 60.dp.toPx()) {
                            selectedPoint = dataPoints[closestIndex]
                        }
                    }
                }
        ) {
            val paddingLeft = 42.dp.toPx()
            val paddingRight = 20.dp.toPx()
            val paddingTop = 20.dp.toPx()
            val paddingBottom = 35.dp.toPx()

            val chartWidth = size.width - paddingLeft - paddingRight
            val chartHeight = size.height - paddingTop - paddingBottom

            // Draw Y-Axis grid lines & labels (0%, 25%, 50%, 75%, 100%)
            val ySteps = listOf(0, 25, 50, 75, 100)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

            val paint = android.graphics.Paint().apply {
                color = onSurfaceVariant.copy(alpha = 0.8f).hashCode()
                textSize = 28f
                isAntiAlias = true
            }

            ySteps.forEach { step ->
                val y = paddingTop + chartHeight * (1f - (step / 100f))
                drawLine(
                    color = surfaceVariant.copy(alpha = 0.6f),
                    start = Offset(paddingLeft, y),
                    end = Offset(size.width - paddingRight, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "$step%",
                    8.dp.toPx(),
                    y + 10f,
                    paint
                )
            }

            // Benchmark 80% line (Distinction / CBSE Target)
            val benchmarkY = paddingTop + chartHeight * (1f - 0.80f)
            drawLine(
                color = Color(0xFF4CAF50).copy(alpha = 0.5f),
                start = Offset(paddingLeft, benchmarkY),
                end = Offset(size.width - paddingRight, benchmarkY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
            )

            if (dataPoints.size == 1) {
                // Single point
                val x = paddingLeft + chartWidth / 2f
                val y = paddingTop + chartHeight * (1f - (dataPoints[0].scorePercent / 100f).coerceIn(0f, 1f))
                drawCircle(
                    color = primaryColor,
                    radius = 7.dp.toPx(),
                    center = Offset(x, y)
                )
                return@Canvas
            }

            val stepX = chartWidth / (dataPoints.size - 1)
            val points = dataPoints.mapIndexed { index, dp ->
                val x = paddingLeft + index * stepX
                val progress = animationProgress.value
                val rawRatio = (dp.scorePercent / 100f).coerceIn(0f, 1f)
                val animatedRatio = rawRatio * progress
                val y = paddingTop + chartHeight * (1f - animatedRatio)
                Offset(x, y)
            }

            // Create smooth curve Path
            val curvePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlX = (p0.x + p1.x) / 2f
                    cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                }
            }

            // Area Gradient Under Curve (Recharts AreaChart look)
            val fillPath = Path().apply {
                addPath(curvePath)
                lineTo(points.last().x, paddingTop + chartHeight)
                lineTo(points.first().x, paddingTop + chartHeight)
                close()
            }

            val areaBrush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.35f * animationProgress.value),
                    primaryColor.copy(alpha = 0.05f * animationProgress.value),
                    Color.Transparent
                ),
                startY = paddingTop,
                endY = paddingTop + chartHeight
            )
            drawPath(fillPath, brush = areaBrush)

            // Draw primary curve line
            drawPath(
                path = curvePath,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw data points & X-axis labels
            points.forEachIndexed { index, point ->
                val isSelected = selectedPoint == dataPoints[index]
                val pointRadius = if (isSelected) 8.dp.toPx() else 5.dp.toPx()

                // Glow ring
                drawCircle(
                    color = if (isSelected) secondaryColor else primaryColor.copy(alpha = 0.25f),
                    radius = pointRadius + 4.dp.toPx(),
                    center = point
                )
                // Center dot
                drawCircle(
                    color = if (isSelected) secondaryColor else primaryColor,
                    radius = pointRadius,
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = point
                )

                // X-axis date labels (show first, middle, last or all if small)
                if (dataPoints.size <= 5 || index == 0 || index == dataPoints.size - 1 || index == dataPoints.size / 2) {
                    val label = dataPoints[index].dateLabel
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        point.x - 30f,
                        size.height - 8f,
                        paint
                    )
                }
            }
        }
    }
}

/**
 * Recharts-style Bar Chart comparing performance across chapters.
 */
@Composable
fun RechartsChapterBarChart(
    chaptersData: List<ChapterScoreBarData>,
    modifier: Modifier = Modifier
) {
    if (chaptersData.isEmpty()) return

    val onSurface = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier.fillMaxWidth()) {
        chaptersData.forEach { chapter ->
            val score = chapter.averageScore.toInt()
            val barColor = when {
                score >= 80 -> Color(0xFF2E7D32) // Excellent (Green)
                score >= 60 -> MaterialTheme.colorScheme.primary // Good (Blue)
                else -> Color(0xFFF57C00) // Needs revision (Orange)
            }

            Column(modifier = Modifier.padding(vertical = 5.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chapter.chapterTitle,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$score% (${chapter.attempts} বার চেষ্টা)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (chapter.averageScore / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = barColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Radial Donut Meter for Overall Chapter Completion.
 */
@Composable
fun RechartsChapterCompletionDonut(
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val percent = (progress * 100).toInt()

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Ring Gauge
            Box(
                modifier = Modifier.size(92.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 10.dp.toPx()
                    // Track
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    // Progress
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(primaryColor, Color(0xFF00B0FF), primaryColor)
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$percent%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                    Text(
                        text = "সম্পন্ন",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "পাঠ্যক্রম সমাপ্তির অগ্রগতি",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ক্লাস ৯ বাংলা CBSE সিলেবাসের মোট $totalCount টি অধ্যায়ের মধ্যে $completedCount টি অধ্যায় সম্পূর্ণ শেষ হয়েছে।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
