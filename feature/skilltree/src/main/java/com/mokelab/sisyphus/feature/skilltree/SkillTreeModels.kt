package com.mokelab.sisyphus.feature.skilltree

import androidx.compose.ui.graphics.Color

/**
 * 知识点掌握等级
 */
enum class MasteryLevel(val label: String, val color: Color) {
    NEW("未学习", Color(0xFF9E9E9E)),
    LEARNING("学习中", Color(0xFFFF9800)),
    REVIEWING("复习中", Color(0xFF42A5F5)),
    MASTERED("已掌握", Color(0xFF2196F3))
}

/**
 * 知识点节点（叶子）
 */
data class KnowledgeNode(
    val id: Long,
    val name: String,
    val masteryLevel: MasteryLevel,
    val stability: Float = 0f,
    val difficulty: Float = 0f,
    val reps: Int = 0
)

/**
 * 节节点
 */
data class SectionNode(
    val id: Long,
    val name: String,
    val knowledgePoints: List<KnowledgeNode>,
    val masteryRate: Float
)

/**
 * 章节点
 */
data class ChapterNode(
    val id: Long,
    val name: String,
    val sections: List<SectionNode>,
    val masteryRate: Float
)

/**
 * 教材节点
 */
data class TextbookNode(
    val id: Long,
    val name: String,
    val chapters: List<ChapterNode>,
    val masteryRate: Float
)

/**
 * 学科节点（根）
 */
data class SubjectNode(
    val id: Long,
    val name: String,
    val textbooks: List<TextbookNode>,
    val masteryRate: Float
)

/**
 * 技能树UI状态
 */
data class SkillTreeUiState(
    val isLoading: Boolean = true,
    val subjects: List<SubjectNode> = emptyList(),
    val expandedSubjectIds: Set<Long> = emptySet(),
    val expandedTextbookIds: Set<Long> = emptySet(),
    val expandedChapterIds: Set<Long> = emptySet(),
    val expandedSectionIds: Set<Long> = emptySet(),
    val totalKnowledgePoints: Int = 0,
    val masteredCount: Int = 0,
    val learningCount: Int = 0,
    val newCount: Int = 0
)
