package com.mokelab.sisyphus.feature.pomodoro

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("番茄钟") })
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Timer display
            val minutes = uiState.remainingSeconds / 60
            val seconds = uiState.remainingSeconds % 60
            Text(
                text = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Progress indicator
            LinearProgressIndicator(
                progress = {
                    if (uiState.totalSeconds > 0) {
                        1f - (uiState.remainingSeconds.toFloat() / uiState.totalSeconds)
                    } else 0f
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!uiState.isRunning) {
                    Button(onClick = { viewModel.startTimer() }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "开始")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始")
                    }
                } else {
                    if (uiState.isPaused) {
                        Button(onClick = { viewModel.resumeTimer() }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "继续")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("继续")
                        }
                    } else {
                        Button(onClick = { viewModel.pauseTimer() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "暂停")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("暂停")
                        }
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
                text = "已完成: ${uiState.completedSessions} 个番茄",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
