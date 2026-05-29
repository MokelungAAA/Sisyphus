package com.mokelab.sisyphus.feature.sync

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncSettingsScreen(
    viewModel: SyncViewModel,
    onBack: () -> Unit = {},
    onLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.message, uiState.error) {
        if (uiState.message != null || uiState.error != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("云端同步") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 连接状态卡片
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (uiState.isAuthenticated)
                                Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (uiState.isAuthenticated)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (uiState.isAuthenticated) "已连接 OneDrive" else "未连接",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "最后同步: ${uiState.lastSyncTime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 操作按钮
            if (uiState.isAuthenticated) {
                // 同步按钮
                Button(
                    onClick = { viewModel.uploadData("{}") }, // TODO: 实际序列化数据
                    enabled = !uiState.isSyncing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("同步中...")
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("立即同步")
                    }
                }

                // 断开连接
                OutlinedButton(
                    onClick = { viewModel.disconnect() },
                    enabled = !uiState.isSyncing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("断开连接")
                }
            } else {
                // 登录按钮
                Button(
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("连接 OneDrive")
                }
            }

            // 消息提示
            uiState.message?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            uiState.error?.let { err ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = err,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 说明文字
            Text(
                text = "数据将同步到 OneDrive 的 Sisyphus 文件夹。同步包括学科、学习记录、复习卡片等所有数据。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
