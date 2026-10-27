package com.example.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun QuickPromptChips(
    prompts: List<String>,
    onPromptClick: (String) -> Unit,
    onAppendWordLimit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val wordLimits = listOf(
        "২০ শব্দে (১ নম্বর)",
        "৫০ শব্দে (৩ নম্বর)",
        "১০০ শব্দে",
        "১৫০ শব্দে (৫ নম্বর)",
        "২৫০ শব্দে (বিশদ)"
    )

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        // Word Limit Badges
        items(wordLimits) { limitTag ->
            AssistChip(
                onClick = {
                    onAppendWordLimit(limitTag)
                },
                label = {
                    Text(
                        text = "📏 $limitTag",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                modifier = Modifier
                    .padding(end = 6.dp)
                    .testTag("word_limit_chip_${limitTag.hashCode()}"),
                shape = RoundedCornerShape(16.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    labelColor = MaterialTheme.colorScheme.primary
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            )
        }

        // Syllabus Topic Chips
        items(prompts) { prompt ->
            AssistChip(
                onClick = { onPromptClick(prompt) },
                label = {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                modifier = Modifier
                    .padding(end = 6.dp)
                    .testTag("quick_prompt_${prompt.hashCode()}"),
                shape = RoundedCornerShape(16.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }
}
