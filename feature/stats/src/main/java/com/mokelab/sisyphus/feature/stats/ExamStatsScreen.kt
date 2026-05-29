package com.mokelab.sisyphus.feature.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mokelab.sisyphus.core.database.entity.ExamType
import com.mokelab.sisyphus.core.database.entity.SubjectEntity
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamStatsScreen(
    viewModel: ExamStatsViewModel,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("考试统计") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.Search, contentDescription = "筛选")
                    }
                    FilterDropdownMenu(
                        expanded = showFilterMenu,
                        onDismiss = { showFilterMenu = false },
                        selectedSubjectId = uiState.selectedSubjectId,
                        selectedExamType = uiState.selectedExamType,
                        subjects = uiState.subjects.values.toList(),
                        onSubjectSelected = { viewModel.selectSubject(it) },
                        onExamTypeSelected = { viewModel.selectExamType(it) }
                    )
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.allRecords.isEmpty() && uiState.totalExams == 0) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无考试记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 概览卡片
                StatsOverviewCard(
                    totalExams = uiState.totalExams,
                    avgScoreRate = uiState.avgScoreRate,
                    bestScoreRate = uiState.bestScoreRate,
                    improvementRate = uiState.improvementRate
                )

                // 成绩变化曲线
                if (uiState.trendData.isNotEmpty()) {
                    ScoreTrendChart(trendData = uiState.trendData)
                }

                // 各科成绩对比
                if (uiState.subjectComparison.isNotEmpty()) {
                    SubjectComparisonCard(comparisonData = uiState.subjectComparison)
                }
            }
        }
    }
}

@Composable
private fun StatsOverviewCard(
    totalExams: Int,
    avgScoreRate: Float,
    bestScoreRate: Float,
    improvementRate: Float
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("考试概览", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatsItem(label = "总考试数", value = "$totalExams")
                StatsItem(label = "平均得分率", value = "${(avgScoreRate * 100).toInt()}%")
                StatsItem(label = "最高得分率", value = "${(bestScoreRate * 100).toInt()}%")
                StatsItem(
                    label = "提升幅度",
                    value = "${if (improvementRate >= 0) "+" else ""}${(improvementRate * 100).toInt()}%"
                )
            }
        }
    }
}

@Composable
private fun StatsItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ScoreTrendChart(trendData: List<ScoreTrendPoint>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("成绩变化趋势", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            val modelProducer = remember { CartesianChartModelProducer() }

            LaunchedEffect(trendData) {
                modelProducer.runTransaction {
                    lineSeries {
                        series(
                            y = trendData.map { (it.scoreRate * 100).toInt() }
                        )
                    }
                }
            }

            val bottomAxisValueFormatter = CartesianValueFormatter { _, x, _ ->
                val index = x.toInt().coerceIn(0, trendData.size - 1)
                trendData[index].label
            }

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = bottomAxisValueFormatter
                    )
                ),
                modelProducer = modelProducer,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "得分率 (%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SubjectComparisonCard(comparisonData: List<SubjectScoreData>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("各科成绩对比", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            comparisonData.forEach { data ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = data.subjectName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(72.dp)
                    )
                    LinearProgressIndicator(
                        progress = { data.avgScoreRate },
                        modifier = Modifier
                            .weight(1f)
                            .height(16.dp),
                        color = getScoreColor(data.avgScoreRate),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${(data.avgScoreRate * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(48.dp),
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = "(${data.examCount}次)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun getScoreColor(scoreRate: Float) = when {
    scoreRate >= 0.9f -> MaterialTheme.colorScheme.primary
    scoreRate >= 0.7f -> MaterialTheme.colorScheme.tertiary
    scoreRate >= 0.6f -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.error
}

@Composable
private fun FilterDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    selectedSubjectId: Long?,
    selectedExamType: ExamType?,
    subjects: List<SubjectEntity>,
    onSubjectSelected: (Long?) -> Unit,
    onExamTypeSelected: (ExamType?) -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        Text(
            "学科筛选",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        DropdownMenuItem(
            text = { Text("全部学科") },
            onClick = { onSubjectSelected(null); onDismiss() }
        )
        subjects.forEach { subject ->
            DropdownMenuItem(
                text = { Text(subject.name) },
                onClick = { onSubjectSelected(subject.id); onDismiss() },
                trailingIcon = {
                    if (selectedSubjectId == subject.id) {
                        Text("✓", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }

        HorizontalDivider()

        Text(
            "考试类型",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        DropdownMenuItem(
            text = { Text("全部类型") },
            onClick = { onExamTypeSelected(null); onDismiss() }
        )
        ExamType.entries.forEach { type ->
            val typeName = when (type) {
                ExamType.MONTHLY -> "月考"
                ExamType.MIDTERM -> "期中"
                ExamType.FINAL -> "期末"
                ExamType.MOCK -> "模拟考"
                ExamType.SIMULATION -> "小测"
            }
            DropdownMenuItem(
                text = { Text(typeName) },
                onClick = { onExamTypeSelected(type); onDismiss() },
                trailingIcon = {
                    if (selectedExamType == type) {
                        Text("✓", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    }
}
