package com.mokelab.sisyphus.feature.pomodoro

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mokelab.sisyphus.core.ui.theme.PomodoroGreen
import com.mokelab.sisyphus.core.ui.theme.PomodoroRed
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
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
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Subject or break label
            Text(
                text = when {
                    uiState.isOnBreak -> uiState.currentSubjectName.ifEmpty { "休息" }
                    uiState.currentSubjectName.isNotEmpty() -> uiState.currentSubjectName
                    else -> uiState.selectedPreset.name
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (uiState.isOnBreak) PomodoroGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Timer display
            val minutes = uiState.remainingSeconds / 60
            val seconds = uiState.remainingSeconds % 60
            Text(
                text = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Light
                ),
                color = if (uiState.isOnBreak) PomodoroGreen else PomodoroRed
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Progress indicator
            LinearProgressIndicator(
                progress = {
                    if (uiState.totalSeconds > 0) {
                        1f - (uiState.remainingSeconds.toFloat() / uiState.totalSeconds)
                    } else 0f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                color = if (uiState.isOnBreak) PomodoroGreen else PomodoroRed
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!uiState.isRunning && !uiState.isPaused) {
                    Button(onClick = { viewModel.startTimer() }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "开始")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始")
                    }
                } else if (uiState.isPaused) {
                    Button(onClick = { viewModel.resumeTimer() }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "继续")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("继续")
                    }
                    OutlinedButton(onClick = { viewModel.stopTimer() }) {
                        Icon(Icons.Default.Close, contentDescription = "停止")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("停止")
                    }
                } else {
                    Button(onClick = { viewModel.pauseTimer() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "暂停")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("暂停")
                    }
                    OutlinedButton(onClick = { viewModel.stopTimer() }) {
                        Icon(Icons.Default.Close, contentDescription = "停止")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("停止")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Completed sessions
            Text(
                text = "已完成 ${uiState.completedSessions} 个番茄",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Session progress dots
            if (uiState.completedSessions > 0 || uiState.isRunning) {
                Spacer(modifier = Modifier.height(8.dp))
                SessionDots(
                    completed = uiState.completedSessions % 4,
                    total = 4
                )
            }
        }
    }

    // Completion animation dialog
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

    // Preset selector dialog
    if (uiState.showPresetSelector) {
        PresetSelectorDialog(
            currentPreset = uiState.selectedPreset,
            onSelect = { viewModel.selectPreset(it) },
            onDismiss = { viewModel.hidePresetSelector() }
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
    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

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

                // Custom preset option
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
