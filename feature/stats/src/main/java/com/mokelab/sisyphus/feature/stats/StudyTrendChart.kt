package com.mokelab.sisyphus.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 学习趋势折线图
 */
@Composable
fun StudyTrendChart(
    data: List<StudyTrendPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF0EA5E9), // Sky500
    showPomodoro: Boolean = true
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

    val textMeasurer = rememberTextMeasurer()
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier) {
        // 图例
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ChartLegend(color = lineColor, label = "学习时长")
            if (showPomodoro) {
                ChartLegend(color = Color(0xFF10B981), label = "番茄钟")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val maxValue = data.maxOf { maxOf(it.studyMinutes, it.pomodoroMinutes) }
            if (maxValue == 0) return@Canvas

            val padding = 40.dp.toPx()
            val chartWidth = size.width - padding * 2
            val chartHeight = size.height - padding * 2

            val stepX = chartWidth / (data.size - 1).coerceAtLeast(1)
            val scaleY = chartHeight / maxValue

            // 绘制网格线
            val gridColor = Color.LightGray.copy(alpha = 0.3f)
            for (i in 0..4) {
                val y = padding + chartHeight * i / 4
                drawLine(
                    color = gridColor,
                    start = Offset(padding, y),
                    end = Offset(size.width - padding, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 绘制学习时长折线
            val studyPath = Path()
            data.forEachIndexed { index, point ->
                val x = padding + stepX * index
                val y = padding + chartHeight - point.studyMinutes * scaleY
                if (index == 0) {
                    studyPath.moveTo(x, y)
                } else {
                    studyPath.lineTo(x, y)
                }
            }
            drawPath(
                path = studyPath,
                color = lineColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // 绘制番茄钟折线
            if (showPomodoro) {
                val pomodoroPath = Path()
                data.forEachIndexed { index, point ->
                    val x = padding + stepX * index
                    val y = padding + chartHeight - point.pomodoroMinutes * scaleY
                    if (index == 0) {
                        pomodoroPath.moveTo(x, y)
                    } else {
                        pomodoroPath.lineTo(x, y)
                    }
                }
                drawPath(
                    path = pomodoroPath,
                    color = Color(0xFF10B981),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 绘制X轴标签（每隔几天显示一个）
            val labelInterval = if (data.size > 7) data.size / 5 else 1
            data.forEachIndexed { index, point ->
                if (index % labelInterval == 0) {
                    val x = padding + stepX * index
                    val label = "${point.date.monthNumber}/${point.date.dayOfMonth}"
                    val textLayoutResult = textMeasurer.measure(
                        text = label,
                        style = TextStyle(fontSize = 10.sp, color = textColor)
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x - textLayoutResult.size.width / 2,
                            size.height - padding + 10.dp.toPx()
                        )
                    )
                }
            }
        }
    }
}

/**
 * 图例组件
 */
@Composable
private fun ChartLegend(
    color: Color,
    label: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = color)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
