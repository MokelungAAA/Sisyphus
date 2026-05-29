package com.mokelab.sisyphus.feature.exam

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mokelab.sisyphus.core.database.entity.ExamRecordEntity
import com.mokelab.sisyphus.core.database.entity.ExamType
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamRecordListScreen(
    viewModel: ExamRecordViewModel,
    onRecordClick: (Long) -> Unit = {},
    onNavigateToStats: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("考试记录") },
                actions = {
                    IconButton(onClick = onNavigateToStats) {
                        Icon(Icons.Default.DateRange, contentDescription = "考试统计")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "添加考试")
            }
        }
    ) { padding ->
        if (uiState.records.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无考试记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.records, key = { it.id }) { record ->
                    ExamRecordItem(
                        record = record,
                        onClick = { onRecordClick(record.id) },
                        onDelete = { viewModel.deleteRecord(record) }
                    )
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddExamRecordDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { subjectId, name, type, score, total, isFull ->
                viewModel.addRecord(subjectId, name, type, score, total, isFull)
                viewModel.hideAddDialog()
            }
        )
    }
}

@Composable
private fun ExamRecordItem(
    record: ExamRecordEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val date = remember(record.examDate) {
        val dt = record.examDate.toLocalDateTime(TimeZone.currentSystemDefault())
        "${dt.date}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = record.examName, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${record.score}/${record.totalScore} (${(record.scoreRate * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${record.examType.name} · $date${if (record.isFullMock) " · 全真模拟" else ""}",
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
private fun AddExamRecordDialog(
    onDismiss: () -> Unit,
    onConfirm: (subjectId: Long, name: String, type: ExamType, score: Float, total: Float, isFull: Boolean) -> Unit
) {
    var subjectId by remember { mutableStateOf("1") }
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ExamType.MONTHLY) }
    var score by remember { mutableStateOf("") }
    var totalScore by remember { mutableStateOf("100") }
    var isFullMock by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加考试记录") },
        text = {
            Column {
                OutlinedTextField(
                    value = subjectId,
                    onValueChange = { subjectId = it.filter { c -> c.isDigit() } },
                    label = { Text("学科ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("考试名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("考试类型", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ExamType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    OutlinedTextField(
                        value = score,
                        onValueChange = { score = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("得分") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = totalScore,
                        onValueChange = { totalScore = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("总分") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFullMock, onCheckedChange = { isFullMock = it })
                    Text("全真模拟")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val sid = subjectId.toLongOrNull() ?: 1L
                    val s = score.toFloatOrNull() ?: 0f
                    val t = totalScore.toFloatOrNull() ?: 100f
                    if (name.isNotBlank()) {
                        onConfirm(sid, name, selectedType, s, t, isFullMock)
                    }
                },
                enabled = name.isNotBlank()
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
