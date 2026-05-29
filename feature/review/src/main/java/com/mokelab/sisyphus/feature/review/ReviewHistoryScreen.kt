package com.mokelab.sisyphus.feature.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mokelab.sisyphus.core.database.entity.ReviewHistoryEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewHistoryScreen(
    viewModel: ReviewCardViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("复习历史") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.reviewHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无复习记录",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.reviewHistory, key = { it.id }) { history ->
                    ReviewHistoryItem(
                        history = history,
                        onDelete = { viewModel.deleteHistory(history) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewHistoryItem(
    history: ReviewHistoryEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
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
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
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
