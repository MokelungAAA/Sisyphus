package com.mokelab.sisyphus.feature.recommendation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mokelab.sisyphus.core.ui.theme.PomodoroRed
import com.mokelab.sisyphus.core.ui.theme.PomodoroRedLight

/**
 * 今日推荐卡片
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecommendationCard(
    result: RecommendationResult,
    onItemClick: (RecommendationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题和时间预算
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "今日推荐",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${result.totalEstimatedMinutes}分钟",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 输入输出比例指示
            BalanceIndicator(
                inputPercentage = result.inputPercentage,
                outputPercentage = result.outputPercentage
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 推荐项目列表
            result.items.forEach { item ->
                RecommendationItemRow(
                    item = item,
                    onClick = { onItemClick(item) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * 输入输出比例指示器
 */
@Composable
private fun BalanceIndicator(
    inputPercentage: Float,
    outputPercentage: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "输入 ${(inputPercentage * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF2196F3)
            )
            Text(
                text = "输出 ${(outputPercentage * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF4CAF50)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 比例条
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .weight(inputPercentage.coerceAtLeast(0.01f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF2196F3))
            )
            Spacer(modifier = Modifier.width(2.dp))
            Box(
                modifier = Modifier
                    .weight(outputPercentage.coerceAtLeast(0.01f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF4CAF50))
            )
        }
    }
}

/**
 * 推荐项目行
 */
@Composable
private fun RecommendationItemRow(
    item: RecommendationItem,
    onClick: () -> Unit
) {
    val typeColor = when (item.type) {
        RecommendationType.FSRS_REVIEW -> PomodoroRed
        RecommendationType.WEAK_SUBJECT -> Color(0xFFFF9800)
        RecommendationType.INPUT_ACTIVITY -> Color(0xFF2196F3)
        RecommendationType.OUTPUT_ACTIVITY -> Color(0xFF4CAF50)
        RecommendationType.NEW_CONTENT -> Color(0xFF9C27B0)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(typeColor.copy(alpha = 0.05f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 类型图标
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(typeColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.type.icon,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 内容
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 时长
        Text(
            text = "${item.estimatedMinutes}分钟",
            style = MaterialTheme.typography.labelMedium,
            color = typeColor
        )
    }
}

/**
 * 学科权重编辑器
 */
@Composable
fun WeightEditorCard(
    weights: List<SubjectWeight>,
    onWeightChange: (Long, Float) -> Unit,
    onAutoCalculate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            // 标题
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "学科权重",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "收起" else "展开"
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))

                // 自动推算按钮
                Text(
                    text = "自动推算",
                    style = MaterialTheme.typography.labelMedium,
                    color = PomodoroRed,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onAutoCalculate)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 权重滑块
                weights.forEach { weight ->
                    WeightSlider(
                        weight = weight,
                        onValueChange = { onWeightChange(weight.subjectId, it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * 学科权重滑块
 */
@Composable
private fun WeightSlider(
    weight: SubjectWeight,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = weight.subjectName,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${weight.weight.toInt()}/10",
                style = MaterialTheme.typography.labelMedium,
                color = if (weight.isManual) PomodoroRed
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Slider(
            value = weight.weight,
            onValueChange = onValueChange,
            valueRange = 0f..10f,
            steps = 9,
            colors = SliderDefaults.colors(
                thumbColor = PomodoroRed,
                activeTrackColor = PomodoroRed
            )
        )
    }
}

/**
 * 时间预算选择器
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimeBudgetSelector(
    currentMinutes: Int,
    onMinutesChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(30, 60, 90, 120, 180, 240)

    Column(modifier = modifier) {
        Text(
            text = "每日学习时间",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { minutes ->
                val isSelected = minutes == currentMinutes
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) PomodoroRed
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onMinutesChange(minutes) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${minutes / 60}小时${if (minutes % 60 > 0) "${minutes % 60}分" else ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
