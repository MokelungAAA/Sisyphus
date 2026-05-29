package com.mokelab.sisyphus.feature.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mokelab.sisyphus.core.database.entity.StudyRecordEntity
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/**
 * 日志视图模式
 */
enum class LogViewMode(val title: String) {
    DATE("按日期"),
    SUBJECT("按学科")
}

/**
 * 日志页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    records: List<StudyRecordEntity>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(LogViewMode.DATE) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var selectedSubjectId by remember { mutableStateOf<Long?>(null) }

    // 获取所有学科
    val subjects = records.map { it.subjectId }.distinct()

    // 根据视图模式分组
    val timeZone = TimeZone.currentSystemDefault()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("学习日志") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    // 视图模式切换
                    TextButton(onClick = {
                        viewMode = when (viewMode) {
                            LogViewMode.DATE -> LogViewMode.SUBJECT
                            LogViewMode.SUBJECT -> LogViewMode.DATE
                        }
                    }) {
                        Text(viewMode.title)
                    }

                    // 筛选按钮
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "筛选"
                        )
                    }

                    // 筛选菜单
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("全部学科") },
                            onClick = {
                                selectedSubjectId = null
                                showFilterMenu = false
                            }
                        )
                        subjects.forEach { subjectId ->
                            DropdownMenuItem(
                                text = { Text("学科 $subjectId") },
                                onClick = {
                                    selectedSubjectId = subjectId
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (records.isEmpty()) {
            // 空状态
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无学习记录",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (viewMode) {
                    LogViewMode.DATE -> {
                        val dateGrouped = records
                            .filter { selectedSubjectId == null || it.subjectId == selectedSubjectId }
                            .groupBy { it.startTime.toLocalDateTime(timeZone).date }
                            .entries
                            .sortedByDescending { it.key }
                            .associate { it.toPair() }
                        dateGrouped.forEach { (date, dayRecords) ->
                            item {
                                Text(
                                    text = formatDate(date),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(dayRecords) { record ->
                                LogRecordItem(record = record)
                            }
                        }
                    }
                    LogViewMode.SUBJECT -> {
                        val subjectGrouped = records
                            .filter { selectedSubjectId == null || it.subjectId == selectedSubjectId }
                            .groupBy { it.subjectId }
                        subjectGrouped.forEach { (subjectId, subjectRecords) ->
                            item {
                                Text(
                                    text = "学科 $subjectId",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(subjectRecords) { record ->
                                LogRecordItem(record = record)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 单条记录项
 */
@Composable
private fun LogRecordItem(
    record: StudyRecordEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "学科 ${record.subjectId}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${record.durationMinutes}分钟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            record.note?.let { note ->
                if (note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 格式化日期
 */
private fun formatDate(date: LocalDate): String {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    return if (date == today) {
        "今天"
    } else if (date == yesterday) {
        "昨天"
    } else {
        "${date.monthNumber}月${date.dayOfMonth}日"
    }
}
