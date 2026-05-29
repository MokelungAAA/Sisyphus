package com.mokelab.sisyphus.feature.achievement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import com.mokelab.sisyphus.core.database.entity.AchievementEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(
    navController: NavController,
    viewModel: AchievementViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("成就") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
        ) {
            // 总进度卡片
            AchievementProgressCard(
                unlockedCount = uiState.unlockedCount,
                totalCount = uiState.totalCount
            )

            // 分类选择
            CategoryTabs(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            // 成就列表
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 已解锁
                    val unlocked = uiState.achievements.filter { it.unlockedAt != null }
                    if (unlocked.isNotEmpty()) {
                        item {
                            Text(
                                text = "已解锁 (${unlocked.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(unlocked) { achievement ->
                            AchievementCard(achievement = achievement, isUnlocked = true)
                        }
                    }

                    // 未解锁
                    val locked = uiState.achievements.filter { it.unlockedAt == null }
                    if (locked.isNotEmpty()) {
                        item {
                            Text(
                                text = "待解锁 (${locked.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(locked) { achievement ->
                            AchievementCard(achievement = achievement, isUnlocked = false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementProgressCard(
    unlockedCount: Int,
    totalCount: Int
) {
    val progress = if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "成就进度",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$unlockedCount / $totalCount",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategoryTabs(
    selectedCategory: AchievementDefinitions.Category,
    onCategorySelected: (AchievementDefinitions.Category) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = AchievementDefinitions.Category.entries.indexOf(selectedCategory),
        modifier = Modifier.fillMaxWidth(),
        edgePadding = 16.dp
    ) {
        AchievementDefinitions.Category.entries.forEach { category ->
            Tab(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                text = { Text(category.label) }
            )
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: AchievementEntity,
    isUnlocked: Boolean
) {
    val rarityColor = when (achievement.rarity) {
        "COMMON" -> Color(0xFF9E9E9E)
        "RARE" -> Color(0xFF2196F3)
        "EPIC" -> Color(0xFF9C27B0)
        "LEGENDARY" -> Color(0xFFFF9800)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) rarityColor.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.outlineVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (achievement.category) {
                        "PROGRESS" -> Icons.Default.Star
                        "EXPLORE" -> Icons.Default.Search
                        "SCORE" -> Icons.Default.ThumbUp
                        "EASTER_EGG" -> Icons.Default.Favorite
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = if (isUnlocked) rarityColor else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 文字
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isUnlocked) achievement.name else "???",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isUnlocked)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isUnlocked) {
                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    achievement.unlockedAt?.let { timestamp ->
                        Text(
                            text = "解锁于 ${formatTimestamp(timestamp)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // 稀有度标签
            if (isUnlocked) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = rarityColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = when (achievement.rarity) {
                            "COMMON" -> "普通"
                            "RARE" -> "稀有"
                            "EPIC" -> "史诗"
                            "LEGENDARY" -> "传说"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = rarityColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
