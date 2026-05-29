package com.mokelab.sisyphus.feature.entry

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
import com.mokelab.sisyphus.core.database.entity.InputOutputType
import com.mokelab.sisyphus.core.database.entity.StudyRecordEntity
import com.mokelab.sisyphus.core.database.entity.StudyType
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyRecordListScreen(
    viewModel: StudyRecordViewModel,
    onRecordClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("学习记录") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "添加记录")
            }
        }
    ) { padding ->
        if (uiState.records.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无学习记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.records, key = { it.id }) { record ->
                    StudyRecordItem(
                        record = record,
                        onClick = { onRecordClick(record.id) },
                        onDelete = { viewModel.deleteRecord(record) }
                    )
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddStudyRecordDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { subjectId, studyType, duration, inputType, note ->
                viewModel.addRecord(subjectId, studyType, duration, inputType, note)
                viewModel.hideAddDialog()
            }
        )
    }
}

@Composable
private fun StudyRecordItem(
    record: StudyRecordEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateTime = remember(record.createdAt) {
        val dt = record.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
        "${dt.date} ${dt.hour}:${dt.minute.toString().padStart(2, '0')}"
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
                Text(text = record.studyType.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${record.durationMinutes}分钟 · ${record.inputType.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val note = record.note
                if (!note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
    }
}

@Composable
private fun AddStudyRecordDialog(
    onDismiss: () -> Unit,
    onConfirm: (subjectId: Long, studyType: StudyType, duration: Int, inputType: InputOutputType, note: String) -> Unit
) {
    var subjectId by remember { mutableStateOf("1") }
    var selectedStudyType by remember { mutableStateOf(StudyType.COURSE) }
    var duration by remember { mutableStateOf("30") }
    var selectedInputType by remember { mutableStateOf(InputOutputType.INPUT) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加学习记录") },
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
                // Study Type selector
                Text("学习类型", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    StudyType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedStudyType == type,
                            onClick = { selectedStudyType = type },
                            label = { Text(type.name) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it.filter { c -> c.isDigit() } },
                    label = { Text("时长(分钟)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Input/Output Type selector
                Text("输入/输出类型", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InputOutputType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedInputType == type,
                            onClick = { selectedInputType = type },
                            label = { Text(type.name) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注 (可选)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val sid = subjectId.toLongOrNull() ?: 1L
                    val dur = duration.toIntOrNull() ?: 30
                    onConfirm(sid, selectedStudyType, dur, selectedInputType, note)
                },
                enabled = subjectId.isNotBlank()
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
