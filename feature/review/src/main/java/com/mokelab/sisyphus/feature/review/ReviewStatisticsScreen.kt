package com.mokelab.sisyphus.feature.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mokelab.sisyphus.core.database.entity.ReviewHistoryEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewStatisticsScreen(
    viewModel: ReviewCardViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("复习统计") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StatisticsSummaryCard(
                    totalReviewCount = uiState.totalReviewCount,
                    lapseCount = uiState.lapseCount,
                    todayReviewCount = uiState.todayReviewCount,
                    totalCards = uiState.cards.size,
                    dueCards = uiState.dueCards.size
                )
            }

            item {
                Text(
                    text = "复习历史",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (uiState.reviewHistory.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无复习记录",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(uiState.reviewHistory, key = { it.id }) { history ->
                    ReviewHistoryItem(history = history)
                }
            }
        }
    }
}

@Composable
private fun StatisticsSummaryCard(
    totalReviewCount: Int,
    lapseCount: Int,
    todayReviewCount: Int,
    totalCards: Int,
    dueCards: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "统计概览",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "总复习次数",
                    value = totalReviewCount.toString()
                )
                StatItem(
                    label = "遗忘次数",
                    value = lapseCount.toString()
                )
                StatItem(
                    label = "今日复习",
                    value = todayReviewCount.toString()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "总卡片数",
                    value = totalCards.toString()
                )
                StatItem(
                    label = "待复习",
                    value = dueCards.toString()
                )
                StatItem(
                    label = "遗忘率",
                    value = if (totalReviewCount > 0) {
                        "${String.format("%.1f", lapseCount.toFloat() / totalReviewCount * 100)}%"
                    } else {
                        "0%"
                    }
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReviewHistoryItem(history: ReviewHistoryEntity) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "卡片 #${history.cardId}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = ratingToString(history.rating),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ratingToColor(history.rating)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "稳定性: ${String.format("%.2f", history.stabilityBefore)} → ${String.format("%.2f", history.stabilityAfter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "难度: ${String.format("%.2f", history.difficultyBefore)} → ${String.format("%.2f", history.difficultyAfter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "间隔: ${history.intervalBefore}天 → ${history.intervalAfter}天",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun ratingToString(rating: Int): String {
    return when (rating) {
        0 -> "忘记"
        1 -> "困难"
        2 -> "良好"
        3 -> "简单"
        else -> "未知"
    }
}

@Composable
private fun ratingToColor(rating: Int): androidx.compose.ui.graphics.Color {
    return when (rating) {
        0 -> MaterialTheme.colorScheme.error
        1 -> MaterialTheme.colorScheme.tertiary
        2 -> MaterialTheme.colorScheme.primary
        3 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurface
    }
}
