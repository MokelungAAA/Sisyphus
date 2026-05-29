package com.mokelab.sisyphus.feature.nlp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mokelab.sisyphus.core.ui.theme.PomodoroRed
import org.koin.androidx.compose.koinViewModel

/**
 * NLP智能输入界面
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NLPInputScreen(
    onSubmit: (Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NLPViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 输入框
        OutlinedTextField(
            value = uiState.text,
            onValueChange = { viewModel.updateText(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("智能输入") },
            placeholder = { Text("例如：数学必修一第一章 30分钟") },
            trailingIcon = {
                if (uiState.text.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearText() }) {
                        Icon(Icons.Default.Clear, contentDescription = "清空")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (uiState.nlpResult != null) {
                        viewModel.showConfirmation()
                    }
                }
            ),
            singleLine = false,
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 分析状态指示
        AnimatedVisibility(visible = uiState.isAnalyzing) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "正在分析...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 分析结果
        AnimatedVisibility(visible = uiState.nlpResult != null && !uiState.isAnalyzing) {
            uiState.nlpResult?.let { result ->
                Column {
                    // 层级指示
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "分析方式: ${viewModel.getLayerDisplayName()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ConfidenceIndicator(confidence = result.confidence)
                    }

                    // 实体标签
                    EntityChips(entities = result.entities)

                    // 匹配到的模式
                    if (result.matchedPatterns.isNotEmpty()) {
                        PatternLegend(patterns = result.matchedPatterns)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 确认按钮
                    Button(
                        onClick = { viewModel.showConfirmation() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PomodoroRed
                        )
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("确认并编辑")
                    }
                }
            }
        }
    }

    // 确认底部面板
    if (uiState.showConfirmationSheet) {
        ConfirmationBottomSheet(
            entities = uiState.editedEntities,
            onEntityUpdate = { key, value -> viewModel.updateEntity(key, value) },
            onEntityRemove = { key -> viewModel.removeEntity(key) },
            onConfirm = {
                val entities = viewModel.confirmAndSubmit()
                onSubmit(entities)
            },
            onDismiss = { viewModel.hideConfirmation() }
        )
    }
}

/**
 * 置信度指示器
 */
@Composable
private fun ConfidenceIndicator(confidence: Float) {
    val color = when {
        confidence >= 0.7f -> Color(0xFF4CAF50) // 绿色
        confidence >= 0.4f -> Color(0xFFFF9800) // 橙色
        else -> Color(0xFFF44336) // 红色
    }

    val text = when {
        confidence >= 0.7f -> "高"
        confidence >= 0.4f -> "中"
        else -> "低"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "置信度: $text",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 实体标签
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EntityChips(entities: Map<String, String>) {
    val analyzer = NLPAnalyzer()

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        entities.forEach { (key, value) ->
            FilterChip(
                selected = true,
                onClick = { },
                label = {
                    Text(
                        text = "${analyzer.getEntityDisplayName(key)}: ${analyzer.formatEntityValue(key, value)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
        }
    }
}

/**
 * 模式图例
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PatternLegend(patterns: List<MatchedPattern>) {
    val grouped = patterns.groupBy { it.type }

    Column(
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(
            text = "识别模式:",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            grouped.keys.forEach { type ->
                val color = when (type) {
                    PatternType.DURATION -> Color(0xFF2196F3)
                    PatternType.CHAPTER -> Color(0xFF4CAF50)
                    PatternType.PAGE -> Color(0xFFFF9800)
                    PatternType.STUDY_TYPE -> Color(0xFF9C27B0)
                    PatternType.EXAM_SCORE -> Color(0xFFF44336)
                    PatternType.READING -> Color(0xFF795548)
                    PatternType.SUBJECT -> Color(0xFF00BCD4)
                    PatternType.TEXTBOOK -> Color(0xFFE91E63)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(color.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when (type) {
                            PatternType.DURATION -> "时长"
                            PatternType.CHAPTER -> "章节"
                            PatternType.PAGE -> "页码"
                            PatternType.STUDY_TYPE -> "学习类型"
                            PatternType.EXAM_SCORE -> "成绩"
                            PatternType.READING -> "阅读"
                            PatternType.SUBJECT -> "学科"
                            PatternType.TEXTBOOK -> "教材"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = color
                    )
                }
            }
        }
    }
}

/**
 * 确认底部面板
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ConfirmationBottomSheet(
    entities: Map<String, String>,
    onEntityUpdate: (String, String) -> Unit,
    onEntityRemove: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val analyzer = NLPAnalyzer()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 标题
            Text(
                text = "确认学习数据",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 可编辑的实体列表
            entities.forEach { (key, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { onEntityUpdate(key, it) },
                        modifier = Modifier.weight(1f),
                        label = { Text(analyzer.getEntityDisplayName(key)) },
                        singleLine = true
                    )
                    IconButton(onClick = { onEntityRemove(key) }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PomodoroRed
                    )
                ) {
                    Text("确认提交")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
