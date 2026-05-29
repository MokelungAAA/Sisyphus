package com.mokelab.sisyphus.feature.skilltree

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillTreeScreen(
    onBack: () -> Unit,
    viewModel: SkillTreeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("技能树") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("加载中...", style = MaterialTheme.typography.bodyLarge)
            }
        } else if (uiState.subjects.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无学科数据", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    SummaryCard(uiState)
                }
                items(uiState.subjects, key = { it.id }) { subject ->
                    SubjectCard(
                        subject = subject,
                        isExpanded = subject.id in uiState.expandedSubjectIds,
                        expandedTextbookIds = uiState.expandedTextbookIds,
                        expandedChapterIds = uiState.expandedChapterIds,
                        expandedSectionIds = uiState.expandedSectionIds,
                        onToggleSubject = { viewModel.toggleSubject(subject.id) },
                        onToggleTextbook = { viewModel.toggleTextbook(it) },
                        onToggleChapter = { viewModel.toggleChapter(it) },
                        onToggleSection = { viewModel.toggleSection(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(uiState: SkillTreeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("知识总览", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatChip("${uiState.totalKnowledgePoints}", "总知识点")
                StatChip("${uiState.masteredCount}", "已掌握")
                StatChip("${uiState.learningCount}", "学习中")
                StatChip("${uiState.newCount}", "未开始")
            }
            if (uiState.totalKnowledgePoints > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { uiState.masteredCount.toFloat() / uiState.totalKnowledgePoints },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = MasteryLevel.MASTERED.color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatChip(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SubjectCard(
    subject: SubjectNode,
    isExpanded: Boolean,
    expandedTextbookIds: Set<Long>,
    expandedChapterIds: Set<Long>,
    expandedSectionIds: Set<Long>,
    onToggleSubject: () -> Unit,
    onToggleTextbook: (Long) -> Unit,
    onToggleChapter: (Long) -> Unit,
    onToggleSection: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column {
            TreeNodeRow(
                name = subject.name,
                masteryRate = subject.masteryRate,
                isExpanded = isExpanded,
                onToggle = onToggleSubject,
                level = 0
            )
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    subject.textbooks.forEach { textbook ->
                        TextbookCard(
                            textbook = textbook,
                            isExpanded = textbook.id in expandedTextbookIds,
                            expandedChapterIds = expandedChapterIds,
                            expandedSectionIds = expandedSectionIds,
                            onToggle = { onToggleTextbook(textbook.id) },
                            onToggleChapter = onToggleChapter,
                            onToggleSection = onToggleSection
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextbookCard(
    textbook: TextbookNode,
    isExpanded: Boolean,
    expandedChapterIds: Set<Long>,
    expandedSectionIds: Set<Long>,
    onToggle: () -> Unit,
    onToggleChapter: (Long) -> Unit,
    onToggleSection: (Long) -> Unit
) {
    Column {
        TreeNodeRow(
            name = textbook.name,
            masteryRate = textbook.masteryRate,
            isExpanded = isExpanded,
            onToggle = onToggle,
            level = 1
        )
        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                textbook.chapters.forEach { chapter ->
                    ChapterCard(
                        chapter = chapter,
                        isExpanded = chapter.id in expandedChapterIds,
                        expandedSectionIds = expandedSectionIds,
                        onToggle = { onToggleChapter(chapter.id) },
                        onToggleSection = onToggleSection
                    )
                }
            }
        }
    }
}

@Composable
private fun ChapterCard(
    chapter: ChapterNode,
    isExpanded: Boolean,
    expandedSectionIds: Set<Long>,
    onToggle: () -> Unit,
    onToggleSection: (Long) -> Unit
) {
    Column {
        TreeNodeRow(
            name = chapter.name,
            masteryRate = chapter.masteryRate,
            isExpanded = isExpanded,
            onToggle = onToggle,
            level = 2
        )
        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                chapter.sections.forEach { section ->
                    SectionCard(
                        section = section,
                        isExpanded = section.id in expandedSectionIds,
                        onToggle = { onToggleSection(section.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    section: SectionNode,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Column {
        TreeNodeRow(
            name = section.name,
            masteryRate = section.masteryRate,
            isExpanded = isExpanded,
            onToggle = onToggle,
            level = 3
        )
        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                section.knowledgePoints.forEach { kp ->
                    KnowledgePointRow(kp)
                }
            }
        }
    }
}

@Composable
private fun KnowledgePointRow(kp: KnowledgeNode) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(10.dp).background(kp.masteryLevel.color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(kp.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(kp.masteryLevel.label, style = MaterialTheme.typography.labelSmall, color = kp.masteryLevel.color)
    }
}

@Composable
private fun TreeNodeRow(
    name: String,
    masteryRate: Float,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    level: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = if (isExpanded) "折叠" else "展开",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = if (level == 0) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { masteryRate },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = MasteryLevel.MASTERED.color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text("${(masteryRate * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
    }
}
