package com.mokelab.sisyphus.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToSync: () -> Unit = {},
    onNavigateToAchievement: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("设置") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // 主题设置
            SettingsSection(title = "主题") {
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = "深色模式",
                    subtitle = "跟随系统",
                    onClick = { }
                )
            }

            // 数据同步
            SettingsSection(title = "数据同步") {
                SettingsItem(
                    icon = Icons.Default.Refresh,
                    title = "OneDrive同步",
                    subtitle = "未连接",
                    onClick = onNavigateToSync
                )
                SettingsItem(
                    icon = Icons.Default.Share,
                    title = "导出数据",
                    subtitle = "导出JSON格式",
                    onClick = { }
                )
                SettingsItem(
                    icon = Icons.Default.Add,
                    title = "导入数据",
                    subtitle = "从JSON导入",
                    onClick = { }
                )
            }

            // 番茄钟设置
            SettingsSection(title = "番茄钟") {
                SettingsItem(
                    icon = Icons.Default.PlayArrow,
                    title = "专注时长",
                    subtitle = "25分钟",
                    onClick = { }
                )
                SettingsItem(
                    icon = Icons.Default.Refresh,
                    title = "休息时长",
                    subtitle = "5分钟",
                    onClick = { }
                )
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = "长休息时长",
                    subtitle = "15分钟",
                    onClick = { }
                )
            }

            // 个人信息
            SettingsSection(title = "个人信息") {
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "目标高考分数",
                    subtitle = "650分",
                    onClick = { }
                )
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = "每日学习目标",
                    subtitle = "4小时",
                    onClick = { }
                )
            }

            // 成就
            SettingsSection(title = "游戏化") {
                SettingsItem(
                    icon = Icons.Default.ThumbUp,
                    title = "成就系统",
                    subtitle = "查看已解锁成就",
                    onClick = onNavigateToAchievement
                )
            }

            // 关于
            SettingsSection(title = "关于") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "关于Sisyphus",
                    subtitle = "v0.3.0",
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
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
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
