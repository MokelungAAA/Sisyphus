package com.mokelab.sisyphus.feature.skilltree

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.CardState
import com.mokelab.sisyphus.core.database.repository.ChapterRepository
import com.mokelab.sisyphus.core.database.repository.KnowledgePointRepository
import com.mokelab.sisyphus.core.database.repository.ReviewCardRepository
import com.mokelab.sisyphus.core.database.repository.SectionRepository
import com.mokelab.sisyphus.core.database.repository.SubjectRepository
import com.mokelab.sisyphus.core.database.repository.TextbookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SkillTreeViewModel(
    private val subjectRepository: SubjectRepository,
    private val textbookRepository: TextbookRepository,
    private val chapterRepository: ChapterRepository,
    private val sectionRepository: SectionRepository,
    private val knowledgePointRepository: KnowledgePointRepository,
    private val reviewCardRepository: ReviewCardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SkillTreeUiState())
    val uiState: StateFlow<SkillTreeUiState> = _uiState.asStateFlow()

    init {
        loadSkillTree()
    }

    fun loadSkillTree() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val subjects = subjectRepository.getAll().first()
                val subjectNodes = mutableListOf<SubjectNode>()
                var totalKP = 0
                var mastered = 0
                var learning = 0
                var new = 0

                for (subject in subjects) {
                    val textbooks = textbookRepository.getBySubjectId(subject.id).first()
                    val textbookNodes = mutableListOf<TextbookNode>()

                    for (textbook in textbooks) {
                        val chapters = chapterRepository.getByTextbookId(textbook.id).first()
                        val chapterNodes = mutableListOf<ChapterNode>()

                        for (chapter in chapters) {
                            val sections = sectionRepository.getByChapterId(chapter.id).first()
                            val sectionNodes = mutableListOf<SectionNode>()

                            for (section in sections) {
                                val kps = knowledgePointRepository.getBySectionId(section.id).first()
                                // 批量查询所有知识点的复习卡片，避免 N+1 查询问题
                                val allKpIds = kps.map { it.id }
                                val allCards = if (allKpIds.isNotEmpty()) {
                                    reviewCardRepository.getByKnowledgePointIds(allKpIds).first()
                                } else {
                                    emptyList()
                                }
                                val cardMap = allCards.groupBy { it.knowledgePointId }
                                val knowledgeNodes = kps.map { kp ->
                                    val card = cardMap[kp.id]?.firstOrNull()
                                    val masteryLevel = classifyMastery(card?.state, card?.stability)
                                    when (masteryLevel) {
                                        MasteryLevel.MASTERED -> mastered++
                                        MasteryLevel.LEARNING, MasteryLevel.REVIEWING -> learning++
                                        MasteryLevel.NEW -> new++
                                    }
                                    totalKP++
                                    KnowledgeNode(
                                        id = kp.id,
                                        name = kp.name,
                                        masteryLevel = masteryLevel,
                                        stability = card?.stability ?: 0f,
                                        difficulty = card?.difficulty ?: 0f,
                                        reps = card?.reps ?: 0
                                    )
                                }
                                val sectionRate = if (knowledgeNodes.isEmpty()) 0f
                                else knowledgeNodes.count { it.masteryLevel == MasteryLevel.MASTERED }.toFloat() / knowledgeNodes.size
                                sectionNodes.add(SectionNode(section.id, section.name, knowledgeNodes, sectionRate))
                            }

                            val chapterRate = if (sectionNodes.isEmpty()) 0f
                            else sectionNodes.map { it.masteryRate }.average().toFloat()
                            chapterNodes.add(ChapterNode(chapter.id, chapter.name, sectionNodes, chapterRate))
                        }

                        val textbookRate = if (chapterNodes.isEmpty()) 0f
                        else chapterNodes.map { it.masteryRate }.average().toFloat()
                        textbookNodes.add(TextbookNode(textbook.id, textbook.name, chapterNodes, textbookRate))
                    }

                    val subjectRate = if (textbookNodes.isEmpty()) 0f
                    else textbookNodes.map { it.masteryRate }.average().toFloat()
                    subjectNodes.add(SubjectNode(subject.id, subject.name, textbookNodes, subjectRate))
                }

                _uiState.value = SkillTreeUiState(
                    isLoading = false,
                    subjects = subjectNodes,
                    totalKnowledgePoints = totalKP,
                    masteredCount = mastered,
                    learningCount = learning,
                    newCount = new
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun toggleSubject(id: Long) {
        val current = _uiState.value.expandedSubjectIds
        _uiState.value = _uiState.value.copy(
            expandedSubjectIds = if (id in current) current - id else current + id
        )
    }

    fun toggleTextbook(id: Long) {
        val current = _uiState.value.expandedTextbookIds
        _uiState.value = _uiState.value.copy(
            expandedTextbookIds = if (id in current) current - id else current + id
        )
    }

    fun toggleChapter(id: Long) {
        val current = _uiState.value.expandedChapterIds
        _uiState.value = _uiState.value.copy(
            expandedChapterIds = if (id in current) current - id else current + id
        )
    }

    fun toggleSection(id: Long) {
        val current = _uiState.value.expandedSectionIds
        _uiState.value = _uiState.value.copy(
            expandedSectionIds = if (id in current) current - id else current + id
        )
    }

    private fun classifyMastery(state: CardState?, stability: Float?): MasteryLevel {
        if (state == null) return MasteryLevel.NEW
        return when (state) {
            CardState.NEW -> MasteryLevel.NEW
            CardState.LEARNING -> MasteryLevel.LEARNING
            CardState.RELEARNING -> MasteryLevel.LEARNING
            CardState.REVIEW -> {
                if ((stability ?: 0f) >= 21f) MasteryLevel.MASTERED
                else MasteryLevel.REVIEWING
            }
        }
    }
}
