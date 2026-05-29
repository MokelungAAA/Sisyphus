package com.mokelab.sisyphus.feature.reading

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
import com.mokelab.sisyphus.core.database.entity.ReadingRecordEntity
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingRecordListScreen(
    viewModel: ReadingRecordViewModel,
    onRecordClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("阅读记录") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "添加阅读")
            }
        }
    ) { padding ->
        if (uiState.records.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无阅读记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.records, key = { it.id }) { record ->
                    ReadingRecordItem(
                        record = record,
                        onClick = { onRecordClick(record.id) },
                        onDelete = { viewModel.deleteRecord(record) }
                    )
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddReadingRecordDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { bookName, author, duration, note ->
                viewModel.addRecord(bookName, author, duration, note)
                viewModel.hideAddDialog()
            }
        )
    }
}

@Composable
private fun ReadingRecordItem(
    record: ReadingRecordEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val date = remember(record.createdAt) {
        val dt = record.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
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
                Text(text = record.bookName, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                val author = record.author
                if (author != null) {
                    Text(
                        text = author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = "${record.durationMinutes}分钟 · $date",
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
private fun AddReadingRecordDialog(
    onDismiss: () -> Unit,
    onConfirm: (bookName: String, author: String, duration: Int, note: String) -> Unit
) {
    var bookName by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("30") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加阅读记录") },
        text = {
            Column {
                OutlinedTextField(
                    value = bookName,
                    onValueChange = { bookName = it },
                    label = { Text("书名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("作者 (可选)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it.filter { c -> c.isDigit() } },
                    label = { Text("时长(分钟)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
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
                    val dur = duration.toIntOrNull() ?: 30
                    if (bookName.isNotBlank()) {
                        onConfirm(bookName, author, dur, note)
                    }
                },
                enabled = bookName.isNotBlank()
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
