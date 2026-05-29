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
import com.mokelab.sisyphus.core.database.entity.SectionEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionListScreen(
    chapterId: Long,
    chapterName: String,
    viewModel: SectionViewModel,
    onBack: () -> Unit = {},
    onSectionClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(chapterId) {
        viewModel.loadSections(chapterId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chapterName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "添加小节")
            }
        }
    ) { padding ->
        if (uiState.sections.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无小节，点击右下角添加", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.sections, key = { it.id }) { section ->
                    SectionItem(
                        section = section,
                        onClick = { onSectionClick(section.id) },
                        onDelete = { viewModel.deleteSection(section) }
                    )
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddSectionDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { name, orderIndex ->
                viewModel.addSection(name, orderIndex)
                viewModel.hideAddDialog()
            }
        )
    }
}

@Composable
private fun SectionItem(
    section: SectionEntity,
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
                Text(text = section.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "顺序: ${section.orderIndex}",
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
private fun AddSectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, orderIndex: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var orderIndex by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加小节") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("小节名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = orderIndex,
                    onValueChange = { orderIndex = it.filter { c -> c.isDigit() } },
                    label = { Text("顺序号") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val order = orderIndex.toIntOrNull() ?: 1
                    if (name.isNotBlank()) {
                        onConfirm(name, order)
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
