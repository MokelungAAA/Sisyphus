package com.mokelab.sisyphus.feature.stats

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

/**
 * 洞察（数据Tab）子页面枚举
 */
enum class StatsSubTab(val title: String) {
    OVERVIEW("总览"),
    PROGRESS("进度"),
    KNOWLEDGE("知识"),
    READING("阅读")
}

/**
 * 洞察（数据Tab）主页面
 * 顶部4个子页面切换
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsTabScreen(
    onNavigateToExamStats: () -> Unit = {},
    onNavigateToLog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(StatsSubTab.OVERVIEW) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("洞察") }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 子页面切换
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                StatsSubTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.title) }
                    )
                }
            }

            // 子页面内容
            when (selectedTab) {
                StatsSubTab.OVERVIEW -> {
                    OverviewStatsContent(
                        onNavigateToExamStats = onNavigateToExamStats,
                        onNavigateToLog = onNavigateToLog
                    )
                }
                StatsSubTab.PROGRESS -> {
                    ProgressStatsContent()
                }
                StatsSubTab.KNOWLEDGE -> {
                    KnowledgeStatsContent()
                }
                StatsSubTab.READING -> {
                    ReadingStatsContent()
                }
            }
        }
    }
}

/**
 * 总览子页面
 */
@Composable
private fun OverviewStatsContent(
    onNavigateToExamStats: () -> Unit,
    onNavigateToLog: () -> Unit,
    viewModel: StudyStatsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 学习时长趋势
        StudyTimeTrendCard(
            weekTrend = uiState.weekTrend,
            isLoading = uiState.isLoading
        )

        // 各科时间分布
        SubjectTimeDistributionCard(
            subjectDistribution = uiState.subjectDistribution,
            isLoading = uiState.isLoading
        )

        // 学习日志入口
        LogEntryCard(onClick = onNavigateToLog)

        // 考试统计入口
        ExamStatsEntryCard(onClick = onNavigateToExamStats)
    }
}

/**
 * 进度子页面
 */
@Composable
private fun ProgressStatsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "进度统计",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "教辅目录进度、网课目录进度、复习推进情况",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // TODO: Implement progress stats
    }
}

/**
 * 知识子页面
 */
@Composable
private fun KnowledgeStatsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "知识掌握",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "知识点掌握度展示、各学科/章节/小节掌握详情",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // TODO: Implement knowledge stats
    }
}

/**
 * 阅读子页面
 */
@Composable
private fun ReadingStatsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "阅读记录",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "读书记录、听书记录、阅读笔记",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // TODO: Implement reading stats
    }
}

/**
 * 学习时长趋势卡片
 */
@Composable
private fun StudyTimeTrendCard(
    weekTrend: List<StudyTrendPoint>,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "学习时长趋势",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "最近7天的学习时长变化",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                StudyTrendChart(
                    data = weekTrend,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

/**
 * 各科时间分布卡片
 */
@Composable
private fun SubjectTimeDistributionCard(
    subjectDistribution: List<SubjectTimeData>,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "各科时间分布",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "各学科学习时长占比",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                SubjectDistributionChart(
                    data = subjectDistribution,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 考试统计入口卡片
 */
@Composable
private fun ExamStatsEntryCard(
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "考试统计",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "查看考试成绩分析、趋势变化、各科对比",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 学习日志入口卡片
 */
@Composable
private fun LogEntryCard(
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "学习日志",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "查看详细学习记录，支持按日期/学科筛选",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
