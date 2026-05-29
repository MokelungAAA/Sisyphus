package com.mokelab.sisyphus.feature.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSync: () -> Unit = {},
    onNavigateToAchievement: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    viewModel: DataExportImportViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isDarkMode by remember { mutableStateOf(false) }
    var showPomodoroSettings by remember { mutableStateOf(false) }

    // Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportToJson(it) }
    }

    // Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importFromJson(it) }
    }

    // Show messages
    LaunchedEffect(uiState.message, uiState.error) {
        if (uiState.message != null || uiState.error != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Message bar
            uiState.message?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Theme section
            SettingsSection(title = "主题") {
                SettingsSwitchItem(
                    title = "深色模式",
                    subtitle = "手动切换深色/浅色主题",
                    icon = Icons.Default.Star,
                    checked = isDarkMode,
                    onCheckedChange = { isDarkMode = it }
                )
            }

            // Data section
            SettingsSection(title = "数据") {
                SettingsClickableItem(
                    title = "导出数据",
                    subtitle = "导出所有数据为 JSON 文件",
                    icon = Icons.Default.Share,
                    onClick = { exportLauncher.launch("sisyphus_backup.json") },
                    enabled = !uiState.isExporting
                )
                SettingsClickableItem(
                    title = "导入数据",
                    subtitle = "从 JSON 文件恢复数据",
                    icon = Icons.Default.Add,
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                    enabled = !uiState.isImporting
                )
                if (uiState.isExporting || uiState.isImporting) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            // Pomodoro section
            SettingsSection(title = "番茄钟") {
                SettingsClickableItem(
                    title = "番茄钟设置",
                    subtitle = "配置工作时长、休息时长等",
                    icon = Icons.Default.PlayArrow,
                    onClick = { showPomodoroSettings = true }
                )
            }

            // Personal info section
            SettingsSection(title = "个人信息") {
                SettingsClickableItem(
                    title = "编辑个人信息",
                    subtitle = "修改昵称、年级等信息",
                    icon = Icons.Default.Person,
                    onClick = { /* TODO */ }
                )
            }

            // Achievement section
            SettingsSection(title = "游戏化") {
                SettingsClickableItem(
                    title = "成就系统",
                    subtitle = "查看已解锁成就",
                    icon = Icons.Default.ThumbUp,
                    onClick = onNavigateToAchievement
                )
            }

            // About section
            SettingsSection(title = "关于") {
                SettingsClickableItem(
                    title = "关于 Sisyphus",
                    subtitle = "版本、开发者信息、致谢",
                    icon = Icons.Default.Info,
                    onClick = onNavigateToAbout
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Pomodoro settings dialog
    if (showPomodoroSettings) {
        PomodoroSettingsDialog(onDismiss = { showPomodoroSettings = false })
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsClickableItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PomodoroSettingsDialog(onDismiss: () -> Unit) {
    var workDuration by remember { mutableStateOf("25") }
    var shortBreak by remember { mutableStateOf("5") }
    var longBreak by remember { mutableStateOf("15") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("番茄钟设置") },
        text = {
            Column {
                OutlinedTextField(
                    value = workDuration,
                    onValueChange = { workDuration = it },
                    label = { Text("工作时长（分钟）") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = shortBreak,
                    onValueChange = { shortBreak = it },
                    label = { Text("短休息（分钟）") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = longBreak,
                    onValueChange = { longBreak = it },
                    label = { Text("长休息（分钟）") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
