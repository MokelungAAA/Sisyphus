package com.mokelab.sisyphus.feature.subject

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mokelab.sisyphus.core.database.entity.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    viewModel: SubjectDetailViewModel,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.subject?.name ?: "学科详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
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
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. 学科概览
                item {
                    SubjectOverviewCard(
                        subject = uiState.subject!!,
                        totalMinutes = uiState.totalStudyMinutes,
                        totalRecords = uiState.totalRecords,
                        onWeightClick = { viewModel.showWeightDialog() },
                        onElectiveToggle = { viewModel.toggleElective() }
                    )
                }

                // 2. 教材目录
                item {
                    TextbookSection(
                        textbooks = uiState.textbooks,
                        chapters = uiState.chapters,
                        sections = uiState.sections,
                        knowledgePoints = uiState.knowledgePoints,
                        expandedTextbookId = uiState.expandedTextbookId,
                        expandedChapterId = uiState.expandedChapterId,
                        expandedSectionId = uiState.expandedSectionId,
                        onTextbookToggle = { viewModel.toggleTextbook(it) },
                        onChapterToggle = { viewModel.toggleChapter(it) },
                        onSectionToggle = { viewModel.toggleSection(it) },
                        onAddClick = { viewModel.showAddTextbookDialog() }
                    )
                }
            }
        }
    }

    // 权重调整弹窗
    if (uiState.showWeightDialog) {
        WeightDialog(
            currentWeight = uiState.subject?.weight ?: 5f,
            onDismiss = { viewModel.hideWeightDialog() },
            onConfirm = { viewModel.updateWeight(it) }
        )
    }

    // 添加教材弹窗
    if (uiState.showAddTextbookDialog) {
        AddTextbookDialog(
            onDismiss = { viewModel.hideAddTextbookDialog() },
            onConfirm = { name, type, source ->
                viewModel.addTextbook(name, type, source)
                viewModel.hideAddTextbookDialog()
            }
        )
    }
}

@Composable
private fun SubjectOverviewCard(
    subject: SubjectEntity,
    totalMinutes: Int,
    totalRecords: Int,
    onWeightClick: () -> Unit,
    onElectiveToggle: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = subject.name,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 统计信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("学习时长", "${totalMinutes}分钟")
                StatItem("学习次数", "${totalRecords}次")
                StatItem("权重", "${subject.weight}")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onWeightClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("调整权重")
                }
                OutlinedButton(
                    onClick = onElectiveToggle,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        if (subject.isElective) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (subject.isElective) "已选修" else "设为选修")
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
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
private fun TextbookSection(
    textbooks: List<TextbookEntity>,
    chapters: Map<Long, List<ChapterEntity>>,
    sections: Map<Long, List<SectionEntity>>,
    knowledgePoints: Map<Long, List<KnowledgePointEntity>>,
    expandedTextbookId: Long?,
    expandedChapterId: Long?,
    expandedSectionId: Long?,
    onTextbookToggle: (Long) -> Unit,
    onChapterToggle: (Long) -> Unit,
    onSectionToggle: (Long) -> Unit,
    onAddClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "教材目录",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = "添加教材")
                }
            }

            if (textbooks.isEmpty()) {
                Text(
                    text = "暂无教材",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                textbooks.forEach { textbook ->
                    TextbookItem(
                        textbook = textbook,
                        isExpanded = expandedTextbookId == textbook.id,
                        chapters = chapters[textbook.id] ?: emptyList(),
                        sections = sections,
                        knowledgePoints = knowledgePoints,
                        expandedChapterId = expandedChapterId,
                        expandedSectionId = expandedSectionId,
                        onToggle = { onTextbookToggle(textbook.id) },
                        onChapterToggle = onChapterToggle,
                        onSectionToggle = onSectionToggle
                    )
                }
            }
        }
    }
}

@Composable
private fun TextbookItem(
    textbook: TextbookEntity,
    isExpanded: Boolean,
    chapters: List<ChapterEntity>,
    sections: Map<Long, List<SectionEntity>>,
    knowledgePoints: Map<Long, List<KnowledgePointEntity>>,
    expandedChapterId: Long?,
    expandedSectionId: Long?,
    onToggle: () -> Unit,
    onChapterToggle: (Long) -> Unit,
    onSectionToggle: (Long) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = textbook.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = when (textbook.type) {
                        TextbookType.TUTORIAL -> "教辅"
                        TextbookType.COURSE -> "网课"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onToggle) {
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "收起" else "展开"
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                if (chapters.isEmpty()) {
                    Text(
                        text = "暂无章节",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    chapters.forEach { chapter ->
                        ChapterItem(
                            chapter = chapter,
                            isExpanded = expandedChapterId == chapter.id,
                            sections = sections[chapter.id] ?: emptyList(),
                            knowledgePoints = knowledgePoints,
                            expandedSectionId = expandedSectionId,
                            onToggle = { onChapterToggle(chapter.id) },
                            onSectionToggle = onSectionToggle
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterItem(
    chapter: ChapterEntity,
    isExpanded: Boolean,
    sections: List<SectionEntity>,
    knowledgePoints: Map<Long, List<KnowledgePointEntity>>,
    expandedSectionId: Long?,
    onToggle: () -> Unit,
    onSectionToggle: (Long) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = chapter.name, style = MaterialTheme.typography.bodyMedium)
            IconButton(onClick = onToggle, modifier = Modifier.size(24.dp)) {
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                sections.forEach { section ->
                    SectionItem(
                        section = section,
                        isExpanded = expandedSectionId == section.id,
                        knowledgePoints = knowledgePoints[section.id] ?: emptyList(),
                        onToggle = { onSectionToggle(section.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionItem(
    section: SectionEntity,
    isExpanded: Boolean,
    knowledgePoints: List<KnowledgePointEntity>,
    onToggle: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = section.name, style = MaterialTheme.typography.bodySmall)
            IconButton(onClick = onToggle, modifier = Modifier.size(20.dp)) {
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                knowledgePoints.forEach { point ->
                    Text(
                        text = "• ${point.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeightDialog(
    currentWeight: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var weight by remember { mutableStateOf(currentWeight) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("调整权重") },
        text = {
            Column {
                Text("当前权重: ${String.format("%.1f", weight)}")
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = weight,
                    onValueChange = { weight = it },
                    valueRange = 0f..10f,
                    steps = 9
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(weight) }) {
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

@Composable
private fun AddTextbookDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, TextbookType, TextbookSource) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TextbookType.TUTORIAL) }
    var source by remember { mutableStateOf(TextbookSource.MANUAL) }

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
                // 类型选择
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = type == TextbookType.TUTORIAL,
                        onClick = { type = TextbookType.TUTORIAL }
                    )
                    Text("教辅")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = type == TextbookType.COURSE,
                        onClick = { type = TextbookType.COURSE }
                    )
                    Text("网课")
                }
                Spacer(modifier = Modifier.height(8.dp))
                // 来源选择
                Text("来源", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextbookSource.entries.forEach { src ->
                        val label = when (src) {
                            TextbookSource.PHOTO_OCR -> "拍照OCR"
                            TextbookSource.AI_GENERATED -> "AI生成"
                            TextbookSource.MANUAL -> "手动录入"
                        }
                        FilterChip(
                            selected = source == src,
                            onClick = { source = src },
                            label = { Text(label, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, type, source) },
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
