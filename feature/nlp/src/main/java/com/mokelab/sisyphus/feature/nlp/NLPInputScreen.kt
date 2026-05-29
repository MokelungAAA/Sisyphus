package com.mokelab.sisyphus.feature.nlp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mokelab.sisyphus.core.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLayoutApi::class)

// Pattern highlight colors
private val patternColors = mapOf(
    PatternType.DURATION to Color(0xFF4CAF50),      // Green
    PatternType.CHAPTER to Color(0xFF2196F3),       // Blue
    PatternType.PAGE to Color(0xFF9C27B0),          // Purple
    PatternType.STUDY_TYPE to Color(0xFFFF9800),    // Orange
    PatternType.EXAM_SCORE to Color(0xFFE91E63),    // Pink
    PatternType.READING to Color(0xFF00BCD4)        // Cyan
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NLPInputScreen(
    viewModel: NLPViewModel = koinViewModel(),
    onResult: (NLPResult) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("智能录入") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Input field with highlighting
            OutlinedTextField(
                value = uiState.text,
                onValueChange = { viewModel.updateText(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("输入学习内容...") },
                placeholder = { Text("例如：学了2小时数学，做了第3章的题目") },
                trailingIcon = {
                    if (uiState.text.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearText() }) {
                            Icon(Icons.Default.Clear, contentDescription = "清空")
                        }
                    }
                },
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Highlighted preview
            if (uiState.nlpResult != null && uiState.nlpResult!!.matchedPatterns.isNotEmpty()) {
                Text(
                    text = "识别结果",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Highlighted text
                HighlightedText(
                    text = uiState.text,
                    patterns = uiState.nlpResult!!.matchedPatterns
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Extracted entities
                EntityChips(entities = uiState.nlpResult!!.entities)

                Spacer(modifier = Modifier.height(16.dp))

                // Intent and confidence
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Intent chip
                    SuggestionChip(
                        onClick = {},
                        label = { Text(uiState.nlpResult!!.intent.displayName) }
                    )

                    // Confidence
                    Text(
                        text = "置信度: ${(uiState.nlpResult!!.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Result summary
                val summary = viewModel.getResultSummary()
                if (summary.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = summary,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm button
                Button(
                    onClick = { onResult(uiState.nlpResult!!) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("确认录入")
                }
            } else if (uiState.text.isNotEmpty()) {
                // No patterns matched
                Text(
                    text = "正在分析...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pattern legend
            PatternLegend()
        }
    }
}

@Composable
private fun HighlightedText(text: String, patterns: List<MatchedPattern>) {
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0

        // Sort patterns by start index
        val sortedPatterns = patterns.sortedBy { it.startIndex }

        for (pattern in sortedPatterns) {
            // Add text before this pattern
            if (pattern.startIndex > lastIndex) {
                append(text.substring(lastIndex, pattern.startIndex))
            }

            // Add highlighted pattern
            val color = patternColors[pattern.type] ?: Color.Gray
            withStyle(
                SpanStyle(
                    color = color,
                    fontWeight = FontWeight.Bold,
                    background = color.copy(alpha = 0.1f)
                )
            ) {
                append(text.substring(pattern.startIndex, pattern.endIndex))
            }

            lastIndex = pattern.endIndex
        }

        // Add remaining text
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EntityChips(entities: Map<String, String>) {
    val analyzer = NLPAnalyzer()

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        entities.forEach { (key, value) ->
            SuggestionChip(
                onClick = {},
                label = {
                    Text("${analyzer.getEntityDisplayName(key)}: ${analyzer.formatEntityValue(key, value)}")
                }
            )
        }
    }
}

@Composable
private fun PatternLegend() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "颜色说明",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            patternColors.forEach { (type, color) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (type) {
                            PatternType.DURATION -> "时长"
                            PatternType.CHAPTER -> "章节"
                            PatternType.PAGE -> "页码"
                            PatternType.STUDY_TYPE -> "学习类型"
                            PatternType.EXAM_SCORE -> "成绩"
                            PatternType.READING -> "阅读"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
