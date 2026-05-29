package com.mokelab.sisyphus.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mokelab.sisyphus.core.database.entity.SubjectEntity
import com.mokelab.sisyphus.core.ui.theme.Sky500
import com.mokelab.sisyphus.core.ui.theme.Sky400
import com.mokelab.sisyphus.core.ui.theme.Sky100
import com.mokelab.sisyphus.core.ui.theme.LightError
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToSubject: (Long) -> Unit,
    onNavigateToPomodoro: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                LoadingState(modifier = Modifier.align(Alignment.Center))
            }
            uiState.error != null -> {
                ErrorState(
                    message = uiState.error ?: "未知错误",
                    onRetry = { /* TODO: Implement retry */ },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                HomeContent(
                    uiState = uiState,
                    onNavigateToSubject = onNavigateToSubject,
                    onNavigateToPomodoro = onNavigateToPomodoro
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onNavigateToSubject: (Long) -> Unit,
    onNavigateToPomodoro: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Sync status indicator
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            SyncStatusIndicator(isSyncing = uiState.isSyncing)
        }

        // Hero Stats - full width
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            HeroStatsCard(
                totalXP = uiState.totalXP,
                todayXP = uiState.todayXP,
                level = uiState.level,
                title = uiState.title,
                streakDays = uiState.streakDays
            )
        }

        // Pomodoro quick card - full width
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            PomodoroQuickCard(onClick = onNavigateToPomodoro)
        }

        // Recommendations section - full width
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            RecommendationsSection()
        }

        // Subjects header
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Text(
                text = "学科",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Subject cards in grid
        if (uiState.subjects.isEmpty()) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                EmptySubjectsState()
            }
        } else {
            items(uiState.subjects) { subject ->
                SubjectGridCard(
                    subject = subject,
                    onClick = { onNavigateToSubject(subject.id) }
                )
            }
        }
    }
}

@Composable
private fun SyncStatusIndicator(
    isSyncing: Boolean,
    modifier: Modifier = Modifier
) {
    if (isSyncing) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    Sky100.copy(alpha = 0.2f),
                    RoundedCornerShape(8.dp)
                )
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = Sky500
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "同步中...",
                style = MaterialTheme.typography.bodySmall,
                color = Sky500
            )
        }
    }
}

@Composable
private fun HeroStatsCard(
    totalXP: Int,
    todayXP: Int,
    level: Int,
    title: String,
    streakDays: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Sky500
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Level and title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lv.$level",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Sky100
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "总学习时长",
                    value = "${totalXP}分钟",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "今日学习",
                    value = "${todayXP}分钟",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "连续天数",
                    value = "${streakDays}天",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Sky100
        )
    }
}

@Composable
private fun PomodoroQuickCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Sky100
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "开始番茄钟",
                modifier = Modifier.size(40.dp),
                tint = Sky500
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "开始番茄钟",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Sky500
                )
                Text(
                    text = "专注学习，高效提升",
                    style = MaterialTheme.typography.bodySmall,
                    color = Sky400
                )
            }
        }
    }
}

@Composable
private fun RecommendationsSection(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "智能推荐",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "基于你的学习记录，为你推荐最佳学习内容",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // TODO: Implement actual recommendations based on FSRS algorithm
        }
    }
}

@Composable
private fun SubjectGridCard(
    subject: SubjectEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = subject.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (subject.isElective) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "选修",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptySubjectsState(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📚",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "还没有学科",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "去设置页面添加学科开始学习吧",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = Sky500
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "加载中...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = "错误",
            modifier = Modifier.size(48.dp),
            tint = LightError
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "出错了",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("重试")
        }
    }
}
