package com.mokelab.sisyphus.feature.pomodoro.floating

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mokelab.sisyphus.core.ui.theme.PomodoroRed

/**
 * 悬浮窗番茄钟内容
 * 环状图标，随时间填充红色，中间显示剩余分钟数
 */
@Composable
fun FloatingPomodoroContent(
    progress: Float,
    remainingMinutes: Int,
    isPaused: Boolean,
    isRunning: Boolean,
    subjectName: String
) {
    val size = 64.dp

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        // Ring progress
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 6.dp.toPx()
            val arcSize = Size(
                this.size.width - strokeWidth * 2,
                this.size.height - strokeWidth * 2
            )
            val topLeft = Offset(strokeWidth, strokeWidth)

            // Background ring (gray)
            drawArc(
                color = Color.Gray.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress ring (red, fills from top)
            drawArc(
                color = PomodoroRed,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isPaused) {
                Text(
                    text = "⏸",
                    fontSize = 16.sp,
                    color = Color.White
                )
            } else if (isRunning) {
                Text(
                    text = "${remainingMinutes}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "min",
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "🍅",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
