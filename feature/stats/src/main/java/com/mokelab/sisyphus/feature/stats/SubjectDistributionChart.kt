package com.mokelab.sisyphus.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * 预定义颜色列表
 */
private val chartColors = listOf(
    Color(0xFF0EA5E9), // Sky500
    Color(0xFF10B981), // Emerald
    Color(0xFFF59E0B), // Amber
    Color(0xFFEF4444), // Red
    Color(0xFF8B5CF6), // Violet
    Color(0xFFEC4899), // Pink
    Color(0xFF06B6D4), // Cyan
    Color(0xFFF97316), // Orange
)

/**
 * 学科时间分布饼图
 */
@Composable
fun SubjectDistributionChart(
    data: List<SubjectTimeData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(modifier = modifier) {
            Text(
                text = "暂无数据",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(modifier = modifier) {
        // 饼图
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(180.dp)) {
                val strokeWidth = 30.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)

                var startAngle = -90f

                data.forEachIndexed { index, item ->
                    val sweepAngle = item.percentage * 360f
                    val color = chartColors[index % chartColors.size]

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth)
                    )

                    startAngle += sweepAngle
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 图例
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            data.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Canvas(modifier = Modifier.size(12.dp)) {
                            drawCircle(color = chartColors[index % chartColors.size])
                        }
                        Text(
                            text = item.subjectName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = "${item.totalMinutes}分钟 (${(item.percentage * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 学科时间分布柱状图
 */
@Composable
fun SubjectDistributionBarChart(
    data: List<SubjectTimeData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(modifier = modifier) {
            Text(
                text = "暂无数据",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxValue = data.maxOf { it.totalMinutes }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        data.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 学科名称
                Text(
                    text = item.subjectName,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(60.dp)
                )

                // 柱状条
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // 背景
                        drawRect(
                            color = Color.LightGray.copy(alpha = 0.2f),
                            size = size
                        )
                        // 前景
                        val barWidth = if (maxValue > 0) {
                            size.width * item.totalMinutes / maxValue
                        } else {
                            0f
                        }
                        drawRect(
                            color = chartColors[index % chartColors.size],
                            size = Size(barWidth, size.height)
                        )
                    }
                }

                // 时长
                Text(
                    text = "${item.totalMinutes}分钟",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(60.dp)
                )
            }
        }
    }
}
