package com.mokelab.sisyphus.feature.review

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mokelab.sisyphus.core.database.entity.ReviewCardEntity
import com.mokelab.sisyphus.feature.review.algorithm.FSRSAlgorithm
import com.mokelab.sisyphus.feature.review.algorithm.Rating

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewSessionScreen(
    viewModel: ReviewCardViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isReviewSessionActive) {
        if (!uiState.isReviewSessionActive && uiState.currentReviewCard == null) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("复习会话") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.endReviewSession()
                        onBack()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.currentReviewCard != null) {
            ReviewCard(
                card = uiState.currentReviewCard!!,
                onRate = { viewModel.rateCard(it) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "没有待复习的卡片",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReviewCard(
    card: ReviewCardEntity,
    onRate: (Rating) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAnswer by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "知识点 #${card.knowledgePointId}",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "状态: ${card.state}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "稳定性: ${String.format("%.2f", card.stability)} · 难度: ${String.format("%.2f", card.difficulty)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "已复习: ${card.reps}次 · 遗忘: ${card.lapses}次",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (showAnswer) {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "回忆概率: ${String.format("%.1f", FSRSAlgorithm.calculateRetrievability(card.stability, card.elapsedDays) * 100)}%",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!showAnswer) {
            Button(
                onClick = { showAnswer = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("显示答案")
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RatingButton(
                    rating = Rating.AGAIN,
                    label = "忘记",
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    onClick = {
                        onRate(Rating.AGAIN)
                        showAnswer = false
                    },
                    modifier = Modifier.weight(1f)
                )
                RatingButton(
                    rating = Rating.HARD,
                    label = "困难",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = {
                        onRate(Rating.HARD)
                        showAnswer = false
                    },
                    modifier = Modifier.weight(1f)
                )
                RatingButton(
                    rating = Rating.GOOD,
                    label = "良好",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = {
                        onRate(Rating.GOOD)
                        showAnswer = false
                    },
                    modifier = Modifier.weight(1f)
                )
                RatingButton(
                    rating = Rating.EASY,
                    label = "简单",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = {
                        onRate(Rating.EASY)
                        showAnswer = false
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RatingButton(
    rating: Rating,
    label: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text = label)
    }
}

private fun calculateRetrievability(stability: Float, elapsedDays: Int): Float {
    if (stability <= 0f) return 0f
    return (1 + elapsedDays.toFloat() / (9 * stability)).pow(-1f)
}

private fun Float.pow(n: Float): Float {
    return Math.pow(this.toDouble(), n.toDouble()).toFloat()
}
