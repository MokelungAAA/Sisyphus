package com.mokelab.sisyphus.feature.subject

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
import com.mokelab.sisyphus.core.database.entity.SubjectEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectListScreen(
    viewModel: SubjectViewModel,
    onSubjectClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("学科管理") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "添加学科")
            }
        }
    ) { padding ->
        if (uiState.subjects.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无学科，点击右下角添加", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.subjects, key = { it.id }) { subject ->
                    SubjectItem(
                        subject = subject,
                        onClick = { onSubjectClick(subject.id) },
                        onDelete = { viewModel.deleteSubject(subject) }
                    )
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddSubjectDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { name, weight, isElective ->
                viewModel.addSubject(name, weight.toFloat(), isElective)
                viewModel.hideAddDialog()
            }
        )
    }
}

@Composable
private fun SubjectItem(
    subject: SubjectEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = subject.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "权重: ${subject.weight}${if (subject.isElective) " · 选修" else ""}",
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
private fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, weight: Int, isElective: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("1") }
    var isElective by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加学科") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("学科名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it.filter { c -> c.isDigit() } },
                    label = { Text("权重 (1-10)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isElective, onCheckedChange = { isElective = it })
                    Text("选修科目")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val w = weight.toIntOrNull() ?: 1
                    if (name.isNotBlank()) {
                        onConfirm(name, w.coerceIn(1, 10), isElective)
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
