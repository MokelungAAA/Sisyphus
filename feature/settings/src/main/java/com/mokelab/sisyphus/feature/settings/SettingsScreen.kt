package com.mokelab.sisyphus.feature.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import com.mokelab.sisyphus.core.preferences.PomodoroPreferences
import com.mokelab.sisyphus.core.preferences.ThemePreferences
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSync: () -> Unit,
    onNavigateToAchievement: () -> Unit,
    onNavigateToAbout: () -> Unit,
    viewModel: DataExportImportViewModel = koinViewModel(),
    themePreferences: ThemePreferences = koinInject(),
    pomodoroPreferences: PomodoroPreferences = koinInject()
) {
    val darkMode by themePreferences.isDarkModeFlow.collectAsState()
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showPomodoroSettings by remember { mutableStateOf(false) }
    var workDuration by remember { mutableIntStateOf(pomodoroPreferences.getWorkDuration()) }
    var breakDuration by remember { mutableIntStateOf(pomodoroPreferences.getBreakDuration()) }

    val isExporting by viewModel.isExporting.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val message by viewModel.message.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // 消息提示
    LaunchedEffect(message) {
        message?.let {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    // JSON 导出 launcher
    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportToJson(it) }
    }

    // CSV 导出 launcher
    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportToCsv(it) }
    }

    // 导入 launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importFromJson(it) }
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
            // 主题设置
            SettingsSection(title = "主题") {
                SettingsSwitchItem(
                    title = "深色模式",
                    description = "启用深色主题",
                    icon = Icons.Filled.Star,
                    checked = darkMode,
                    onCheckedChange = {
                        themePreferences.setDarkMode(it)
                    }
                )
            }

            // 数据设置
            SettingsSection(title = "数据") {
                SettingsClickableItem(
                    title = "导出JSON",
                    description = "导出所有数据为JSON文件（完整备份）",
                    icon = Icons.Filled.Share,
                    onClick = {
                        exportJsonLauncher.launch("sisyphus_backup.json")
                    }
                )
                SettingsClickableItem(
                    title = "导出CSV",
                    description = "导出数据为CSV文件（可用Excel查看）",
                    icon = Icons.Filled.Share,
                    onClick = {
                        exportCsvLauncher.launch("sisyphus_data.csv")
                    }
                )
                if (isExporting) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                SettingsClickableItem(
                    title = "导入数据",
                    description = "从JSON文件导入数据",
                    icon = Icons.Filled.Add,
                    onClick = {
                        importLauncher.launch(arrayOf("application/json"))
                    }
                )
                if (isImporting) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            // 番茄钟设置
            SettingsSection(title = "番茄钟") {
                SettingsClickableItem(
                    title = "番茄钟设置",
                    description = "工作时长: ${workDuration}分钟, 休息: ${breakDuration}分钟",
                    icon = Icons.Filled.PlayArrow,
                    onClick = { showPomodoroSettings = true }
                )
            }

            // 个人信息
            SettingsSection(title = "个人信息") {
                SettingsClickableItem(
                    title = "学习目标",
                    description = "设置学习目标和计划",
                    icon = Icons.Filled.ThumbUp,
                    onClick = { }
                )
            }

            // 游戏化
            SettingsSection(title = "游戏化") {
                SettingsClickableItem(
                    title = "成就",
                    description = "查看已获得的成就",
                    icon = Icons.Filled.Star,
                    onClick = onNavigateToAchievement
                )
            }

            // 关于
            SettingsSection(title = "关于") {
                SettingsClickableItem(
                    title = "关于 Sisyphus",
                    description = "版本信息、开发者信息",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = onNavigateToAbout
                )
            }

            // 消息提示
            message?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(msg)
                }
            }

            errorMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(msg)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // 番茄钟设置对话框
    if (showPomodoroSettings) {
        PomodoroSettingsDialog(
            workDuration = workDuration,
            breakDuration = breakDuration,
            onWorkDurationChange = {
                workDuration = it
                pomodoroPreferences.setWorkDuration(it)
            },
            onBreakDurationChange = {
                breakDuration = it
                pomodoroPreferences.setBreakDuration(it)
            },
            onDismiss = { showPomodoroSettings = false }
        )
    }
}
