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
import com.mokelab.sisyphus.core.database.entity.ChapterEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterListScreen(
    textbookId: Long,
    textbookName: String,
    viewModel: ChapterViewModel,
    onBack: () -> Unit = {},
    onChapterClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(textbookId) {
        viewModel.loadChapters(textbookId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(textbookName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "添加章节")
            }
        }
    ) { padding ->
        if (uiState.chapters.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无章节，点击右下角添加", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.chapters, key = { it.id }) { chapter ->
                    ChapterItem(
                        chapter = chapter,
                        onClick = { onChapterClick(chapter.id) },
                        onDelete = { viewModel.deleteChapter(chapter) }
                    )
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddChapterDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { name, orderIndex ->
                viewModel.addChapter(name, orderIndex)
                viewModel.hideAddDialog()
            }
        )
    }
}

@Composable
private fun ChapterItem(
    chapter: ChapterEntity,
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
                Text(text = chapter.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "顺序: ${chapter.orderIndex}",
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
private fun AddChapterDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, orderIndex: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var orderIndex by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加章节") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("章节名称") },
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
