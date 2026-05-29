package com.mokelab.sisyphus.feature.pomodoro

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mokelab.sisyphus.core.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    onNavigateToHistory: () -> Unit = {},
    viewModel: PomodoroViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isOnBreak) "休息时间" else "番茄钟")
                },
                actions = {
                    // 历史按钮
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.List, contentDescription = "历史记录")
                    }
                    // 预设设置
                    if (!uiState.isRunning && !uiState.isPaused) {
                        IconButton(onClick = { viewModel.showPresetSelector() }) {
                            Icon(Icons.Default.Settings, contentDescription = "预设设置")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 今日数据顶部展示
            TodayStatsCard(
                completedToday = uiState.completedSessions,
                totalMinutesToday = uiState.completedSessions * uiState.selectedPreset.focusMinutes
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 环形进度 + 计时器
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                CircularPomodoroProgress(
                    progress = if (uiState.totalSeconds > 0) {
                        1f - (uiState.remainingSeconds.toFloat() / uiState.totalSeconds)
                    } else 0f,
                    isOnBreak = uiState.isOnBreak,
                    modifier = Modifier.fillMaxSize()
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 时间显示
                    val minutes = uiState.remainingSeconds / 60
                    val seconds = uiState.remainingSeconds % 60
                    Text(
                        text = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Light
                        ),
                        color = if (uiState.isOnBreak) PomodoroGreen else PomodoroRed
                    )

                    // 状态标签
                    Text(
                        text = when {
                            uiState.isOnBreak -> "休息中"
                            uiState.isRunning -> "专注中"
                            uiState.isPaused -> "已暂停"
                            else -> "准备开始"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 当前关联学科
            if (uiState.currentSubjectName.isNotEmpty()) {
                Text(
                    text = uiState.currentSubjectName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 预设信息
            Text(
                text = "${uiState.selectedPreset.name} · ${uiState.selectedPreset.focusMinutes}/${uiState.selectedPreset.breakMinutes}分钟",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 操作按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!uiState.isRunning && !uiState.isPaused) {
                    Button(
                        onClick = { viewModel.startTimer() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "开始")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始专注")
                    }
                } else if (uiState.isPaused) {
                    Button(
                        onClick = { viewModel.resumeTimer() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "继续")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("继续")
                    }
                    OutlinedButton(
                        onClick = { viewModel.stopTimer() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "停止")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("停止")
                    }
                } else {
                    Button(
                        onClick = { viewModel.pauseTimer() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "暂停")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("暂停")
                    }
                    OutlinedButton(
                        onClick = { viewModel.stopTimer() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "停止")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("停止")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 番茄组进度点
            if (uiState.completedSessions > 0 || uiState.isRunning) {
                Text(
                    text = "已完成 ${uiState.completedSessions} 个番茄",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                SessionDots(
                    completed = uiState.completedSessions % 4,
                    total = 4
                )
            }
        }
    }

    // 完成动画弹窗
    if (uiState.showCompletionAnimation) {
        CompletionDialog(
            completedSessions = uiState.completedSessions,
            isGroupComplete = uiState.completedSessions > 0 && uiState.completedSessions % 4 == 0,
            onStartShortBreak = {
                viewModel.dismissCompletionAnimation()
                viewModel.startBreak(isLongBreak = false)
            },
            onStartLongBreak = {
                viewModel.dismissCompletionAnimation()
                viewModel.startBreak(isLongBreak = true)
            },
            onSkipBreak = {
                viewModel.dismissCompletionAnimation()
            }
        )
    }

    // 预设选择弹窗
    if (uiState.showPresetSelector) {
        PresetSelectorDialog(
            currentPreset = uiState.selectedPreset,
            onSelect = { viewModel.selectPreset(it) },
            onDismiss = { viewModel.hidePresetSelector() }
        )
    }
}

/**
 * 今日数据卡片
 */
@Composable
private fun TodayStatsCard(completedToday: Int, totalMinutesToday: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$completedToday",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PomodoroRed
                )
                Text(
                    text = "今日番茄",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${totalMinutesToday}min",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PomodoroRed
                )
                Text(
                    text = "专注时长",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 环形进度指示器
 */
@Composable
private fun CircularPomodoroProgress(
    progress: Float,
    isOnBreak: Boolean,
    modifier: Modifier = Modifier
) {
    val trackColor = MaterialTheme.colorScheme.outlineVariant
    val progressColor = if (isOnBreak) PomodoroGreen else PomodoroRed

    Canvas(modifier = modifier) {
        val strokeWidth = 12.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
        val arcSize = Size(diameter, diameter)

        // 背景轨道
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 进度弧
        drawArc(
            color = progressColor,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun SessionDots(completed: Int, total: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 0 until total) {
            Surface(
                modifier = Modifier.size(12.dp),
                shape = MaterialTheme.shapes.small,
                color = if (i < completed) PomodoroRed else MaterialTheme.colorScheme.outlineVariant
            ) {}
        }
    }
}

@Composable
private fun CompletionDialog(
    completedSessions: Int,
    isGroupComplete: Boolean,
    onStartShortBreak: () -> Unit,
    onStartLongBreak: () -> Unit,
    onSkipBreak: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onSkipBreak,
        title = {
            Text(
                text = "🍅 完成！",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "第 $completedSessions 个番茄钟完成",
                    style = MaterialTheme.typography.bodyLarge
                )

                if (isGroupComplete) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🎉 已完成一组！建议长休息",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PomodoroGreen
                    )
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isGroupComplete) {
                    Button(
                        onClick = onStartLongBreak,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("长休息 (15分钟)")
                    }
                }
                Button(
                    onClick = onStartShortBreak,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("短休息 (5分钟)")
                }
                TextButton(
                    onClick = onSkipBreak,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("跳过休息")
                }
            }
        }
    )
}

@Composable
private fun PresetSelectorDialog(
    currentPreset: PomodoroPreset,
    onSelect: (PomodoroPreset) -> Unit,
    onDismiss: () -> Unit
) {
    var showCustom by remember { mutableStateOf(false) }
    var customFocus by remember { mutableStateOf("25") }
    var customBreak by remember { mutableStateOf("5") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择预设方案") },
        text = {
            Column {
                PomodoroPresets.all.forEach { preset ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentPreset == preset,
                            onClick = { onSelect(preset) }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(preset.name)
                            Text(
                                "专注${preset.focusMinutes}分钟，休息${preset.breakMinutes}分钟",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = showCustom,
                        onClick = { showCustom = true }
                    )
                    Text("自定义")
                }

                if (showCustom) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customFocus,
                            onValueChange = { customFocus = it },
                            label = { Text("专注(分钟)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = customBreak,
                            onValueChange = { customBreak = it },
                            label = { Text("休息(分钟)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val focus = customFocus.toIntOrNull() ?: 25
                            val brk = customBreak.toIntOrNull() ?: 5
                            onSelect(PomodoroPresets.custom(focus, brk))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("应用自定义")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}
