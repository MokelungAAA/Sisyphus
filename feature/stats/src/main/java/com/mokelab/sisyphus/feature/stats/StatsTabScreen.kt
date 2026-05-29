package com.mokelab.sisyphus.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mokelab.sisyphus.core.ui.theme.*
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
    onNavigateToSkillTree: () -> Unit = {},
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
                    KnowledgeStatsContent(onNavigateToSkillTree = onNavigateToSkillTree)
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
 * 进度子页面 - 学科进度条/知识点完成率
 */
@Composable
private fun ProgressStatsContent(
    viewModel: StudyStatsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("学科进度", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "基于知识点复习状态的完成率",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (uiState.subjectDistribution.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无学科数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(uiState.subjectDistribution) { subject ->
                SubjectProgressCard(
                    subjectName = subject.subjectName,
                    percentage = subject.percentage,
                    minutes = subject.totalMinutes
                )
            }
        }

        // 学习时间总览
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("学习时间分布", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (uiState.subjectDistribution.isNotEmpty()) {
                        SubjectDistributionChart(
                            data = uiState.subjectDistribution,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectProgressCard(subjectName: String, percentage: Float, minutes: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(subjectName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text("${minutes}分钟", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { percentage },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Sky500,
                trackColor = Sky100
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${(percentage * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 知识子页面 - FSRS数据/知识点掌握度/薄弱知识点
 */
@Composable
private fun KnowledgeStatsContent(
    onNavigateToSkillTree: () -> Unit = {},
    viewModel: StudyStatsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val summary = uiState.fsrsSummary

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("知识点掌握", style = MaterialTheme.typography.titleMedium)
        }

        // FSRS卡片总览
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("记忆卡片", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("总计", "${summary.totalCards}")
                        StatItem("新卡片", "${summary.newCards}")
                        StatItem("学习中", "${summary.learningCards}")
                        StatItem("已掌握", "${summary.masteredCards}")
                    }
                }
            }
        }

        // 卡片状态分布
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("卡片状态分布", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (summary.totalCards > 0) {
                        CardStateBar(
                            newCount = summary.newCards,
                            learningCount = summary.learningCards,
                            reviewCount = summary.reviewCards,
                            masteredCount = summary.masteredCards,
                            total = summary.totalCards
                        )
                    } else {
                        Text("暂无卡片数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 记忆质量指标
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("记忆质量", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("平均稳定性", String.format("%.1f天", summary.averageStability))
                        StatItem("平均难度", String.format("%.2f", summary.averageDifficulty))
                    }
                }
            }
        }

        // 掌握率
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("掌握率", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    val masterRate = if (summary.totalCards > 0) summary.masteredCards.toFloat() / summary.totalCards else 0f
                    LinearProgressIndicator(
                        progress = { masterRate },
                        modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                        color = Sky500,
                        trackColor = Sky100
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${(masterRate * 100).toInt()}% 已掌握 (${summary.masteredCards}/${summary.totalCards})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 查看技能树按钮
        item {
            Button(
                onClick = onNavigateToSkillTree,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("查看完整技能树")
            }
        }
    }
}

@Composable
private fun CardStateBar(
    newCount: Int,
    learningCount: Int,
    reviewCount: Int,
    masteredCount: Int,
    total: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(12.dp))
    ) {
        if (newCount > 0) {
            Box(modifier = Modifier.weight(newCount.toFloat()).fillMaxHeight().background(Color(0xFF9E9E9E)))
        }
        if (learningCount > 0) {
            Box(modifier = Modifier.weight(learningCount.toFloat()).fillMaxHeight().background(Color(0xFFFF9800)))
        }
        if (reviewCount > 0) {
            Box(modifier = Modifier.weight(reviewCount.toFloat()).fillMaxHeight().background(Sky400))
        }
        if (masteredCount > 0) {
            Box(modifier = Modifier.weight(masteredCount.toFloat()).fillMaxHeight().background(Sky500))
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        LegendItem("新", Color(0xFF9E9E9E))
        LegendItem("学习中", Color(0xFFFF9800))
        LegendItem("复习", Sky400)
        LegendItem("已掌握", Sky500)
    }
}

/**
 * 阅读子页面 - 阅读时长/书目/进度
 */
@Composable
private fun ReadingStatsContent(
    viewModel: StudyStatsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val summary = uiState.readingSummary

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("阅读统计", style = MaterialTheme.typography.titleMedium)
        }

        // 阅读总览
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("总时长", "${summary.totalMinutes}分钟")
                        StatItem("书目数量", "${summary.totalBooks}本")
                    }
                }
            }
        }

        // 书目列表
        if (summary.bookNames.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("阅读书目", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        summary.bookNames.forEach { name ->
                            Text(
                                "· $name",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // 最近阅读记录
        if (summary.recentRecords.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("最近阅读", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        summary.recentRecords.forEach { record ->
                            ReadingRecordItem(record)
                            if (record != summary.recentRecords.last()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }

        // 空状态
        if (summary.totalMinutes == 0) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无阅读记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadingRecordItem(record: com.mokelab.sisyphus.core.database.entity.ReadingRecordEntity) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(record.bookName, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${record.durationMinutes}分钟",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==================== 共用组件 ====================

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Sky500
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, style = MaterialTheme.typography.bodySmall)
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
