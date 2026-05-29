package com.mokelab.sisyphus.feature.subject

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mokelab.sisyphus.core.database.entity.TextbookEntity
import com.mokelab.sisyphus.core.database.entity.TextbookType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextbookListScreen(
    subjectId: Long,
    subjectName: String,
    viewModel: TextbookViewModel,
    onBack: () -> Unit = {},
    onTextbookClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(subjectId) {
        viewModel.loadTextbooks(subjectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subjectName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "添加教材")
            }
        }
    ) { padding ->
        if (uiState.textbooks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无教材，点击右下角添加", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.textbooks, key = { it.id }) { textbook ->
                    TextbookItem(
                        textbook = textbook,
                        onClick = { onTextbookClick(textbook.id) },
                        onDelete = { viewModel.deleteTextbook(textbook) }
                    )
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddTextbookDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { name, type ->
                viewModel.addTextbook(name, type)
                viewModel.hideAddDialog()
            }
        )
    }
}

@Composable
private fun TextbookItem(
    textbook: TextbookEntity,
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
                Text(text = textbook.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "类型: ${textbook.type.name}",
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
private fun AddTextbookDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: TextbookType) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TextbookType.TUTORIAL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加教材") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("教材名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("类型", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextbookType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, selectedType)
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
