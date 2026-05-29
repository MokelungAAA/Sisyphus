package com.mokelab.sisyphus.feature.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mokelab.sisyphus.core.database.entity.ReviewCardEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewCardListScreen(
    viewModel: ReviewCardViewModel,
    onCardClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("复习卡片") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "添加卡片")
            }
        }
    ) { padding ->
        if (uiState.cards.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无复习卡片", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.dueCards.isNotEmpty()) {
                    item {
                        Text(
                            text = "待复习 (${uiState.dueCards.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(uiState.dueCards, key = { it.id }) { card ->
                        ReviewCardItem(
                            card = card,
                            isDue = true,
                            onClick = { onCardClick(card.id) },
                            onDelete = { viewModel.deleteCard(card) }
                        )
                    }
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = "全部卡片",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                items(uiState.cards, key = { it.id }) { card ->
                    ReviewCardItem(
                        card = card,
                        isDue = false,
                        onClick = { onCardClick(card.id) },
                        onDelete = { viewModel.deleteCard(card) }
                    )
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddReviewCardDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { knowledgePointId ->
                viewModel.addCard(knowledgePointId)
                viewModel.hideAddDialog()
            }
        )
    }
}

@Composable
private fun ReviewCardItem(
    card: ReviewCardEntity,
    isDue: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = if (isDue) CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) else CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "知识点 #${card.knowledgePointId}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "状态: ${card.state} · 复习: ${card.reps}次",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "稳定性: ${String.format("%.2f", card.stability)} · 难度: ${String.format("%.2f", card.difficulty)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
    }
}

@Composable
private fun AddReviewCardDialog(
    onDismiss: () -> Unit,
    onConfirm: (knowledgePointId: Long) -> Unit
) {
    var knowledgePointId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加复习卡片") },
        text = {
            Column {
                OutlinedTextField(
                    value = knowledgePointId,
                    onValueChange = { knowledgePointId = it.filter { c -> c.isDigit() } },
                    label = { Text("知识点ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val id = knowledgePointId.toLongOrNull() ?: 0L
                    if (id > 0) {
                        onConfirm(id)
                    }
                },
                enabled = knowledgePointId.isNotBlank()
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
