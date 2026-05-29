package com.mokelab.sisyphus.feature.subject

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.ChapterEntity
import com.mokelab.sisyphus.core.database.entity.KnowledgePointEntity
import com.mokelab.sisyphus.core.database.entity.SectionEntity
import com.mokelab.sisyphus.core.database.entity.SubjectEntity
import com.mokelab.sisyphus.core.database.entity.TextbookEntity
import com.mokelab.sisyphus.core.database.repository.ChapterRepository
import com.mokelab.sisyphus.core.database.repository.KnowledgePointRepository
import com.mokelab.sisyphus.core.database.repository.SectionRepository
import com.mokelab.sisyphus.core.database.repository.StudyRecordRepository
import com.mokelab.sisyphus.core.database.repository.SubjectRepository
import com.mokelab.sisyphus.core.database.repository.TextbookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SubjectDetailUiState(
    val subject: SubjectEntity? = null,
    val textbooks: List<TextbookEntity> = emptyList(),
    val chapters: Map<Long, List<ChapterEntity>> = emptyMap(),
    val sections: Map<Long, List<SectionEntity>> = emptyMap(),
    val knowledgePoints: Map<Long, List<KnowledgePointEntity>> = emptyMap(),
    val totalStudyMinutes: Int = 0,
    val totalRecords: Int = 0,
    val showWeightDialog: Boolean = false,
    val showAddTextbookDialog: Boolean = false,
    val expandedTextbookId: Long? = null,
    val expandedChapterId: Long? = null,
    val expandedSectionId: Long? = null,
    val isLoading: Boolean = true
)

class SubjectDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val subjectRepository: SubjectRepository,
    private val textbookRepository: TextbookRepository,
    private val chapterRepository: ChapterRepository,
    private val sectionRepository: SectionRepository,
    private val knowledgePointRepository: KnowledgePointRepository,
    private val studyRecordRepository: StudyRecordRepository
) : ViewModel() {

    private val subjectId: Long = savedStateHandle.get<Long>("subjectId") ?: 0L

    private val _uiState = MutableStateFlow(SubjectDetailUiState())
    val uiState: StateFlow<SubjectDetailUiState> = _uiState.asStateFlow()

    init {
        loadSubject()
        loadTextbooks()
        loadStudyStats()
    }

    private fun loadSubject() {
        viewModelScope.launch {
            val subject = subjectRepository.getById(subjectId)
            _uiState.value = _uiState.value.copy(subject = subject, isLoading = false)
        }
    }

    private fun loadTextbooks() {
        viewModelScope.launch {
            textbookRepository.getBySubjectId(subjectId).collect { textbooks ->
                _uiState.value = _uiState.value.copy(textbooks = textbooks)
            }
        }
    }

    private fun loadStudyStats() {
        viewModelScope.launch {
            studyRecordRepository.getBySubjectId(subjectId).collect { records ->
                _uiState.value = _uiState.value.copy(
                    totalStudyMinutes = records.sumOf { it.durationMinutes },
                    totalRecords = records.size
                )
            }
        }
    }

    fun loadChapters(textbookId: Long) {
        viewModelScope.launch {
            chapterRepository.getByTextbookId(textbookId).collect { chapters ->
                _uiState.value = _uiState.value.copy(
                    chapters = _uiState.value.chapters + (textbookId to chapters)
                )
            }
        }
    }

    fun loadSections(chapterId: Long) {
        viewModelScope.launch {
            sectionRepository.getByChapterId(chapterId).collect { sections ->
                _uiState.value = _uiState.value.copy(
                    sections = _uiState.value.sections + (chapterId to sections)
                )
            }
        }
    }

    fun loadKnowledgePoints(sectionId: Long) {
        viewModelScope.launch {
            knowledgePointRepository.getBySectionId(sectionId).collect { points ->
                _uiState.value = _uiState.value.copy(
                    knowledgePoints = _uiState.value.knowledgePoints + (sectionId to points)
                )
            }
        }
    }

    fun toggleTextbook(textbookId: Long) {
        val current = _uiState.value.expandedTextbookId
        _uiState.value = _uiState.value.copy(
            expandedTextbookId = if (current == textbookId) null else textbookId
        )
        if (current != textbookId) {
            loadChapters(textbookId)
        }
    }

    fun toggleChapter(chapterId: Long) {
        val current = _uiState.value.expandedChapterId
        _uiState.value = _uiState.value.copy(
            expandedChapterId = if (current == chapterId) null else chapterId
        )
        if (current != chapterId) {
            loadSections(chapterId)
        }
    }

    fun toggleSection(sectionId: Long) {
        val current = _uiState.value.expandedSectionId
        _uiState.value = _uiState.value.copy(
            expandedSectionId = if (current == sectionId) null else sectionId
        )
        if (current != sectionId) {
            loadKnowledgePoints(sectionId)
        }
    }

    fun showWeightDialog() {
        _uiState.value = _uiState.value.copy(showWeightDialog = true)
    }

    fun hideWeightDialog() {
        _uiState.value = _uiState.value.copy(showWeightDialog = false)
    }

    fun updateWeight(weight: Float) {
        viewModelScope.launch {
            val subject = _uiState.value.subject ?: return@launch
            val updated = subject.copy(weight = weight, updatedAt = kotlinx.datetime.Clock.System.now())
            subjectRepository.update(updated)
            _uiState.value = _uiState.value.copy(subject = updated)
        }
    }

    fun toggleElective() {
        viewModelScope.launch {
            val subject = _uiState.value.subject ?: return@launch
            val updated = subject.copy(
                isElective = !subject.isElective,
                updatedAt = kotlinx.datetime.Clock.System.now()
            )
            subjectRepository.update(updated)
            _uiState.value = _uiState.value.copy(subject = updated)
        }
    }

    fun showAddTextbookDialog() {
        _uiState.value = _uiState.value.copy(showAddTextbookDialog = true)
    }

    fun hideAddTextbookDialog() {
        _uiState.value = _uiState.value.copy(showAddTextbookDialog = false)
    }

    fun addTextbook(
        name: String,
        type: com.mokelab.sisyphus.core.database.entity.TextbookType,
        source: com.mokelab.sisyphus.core.database.entity.TextbookSource
    ) {
        viewModelScope.launch {
            val now = kotlinx.datetime.Clock.System.now()
            textbookRepository.insert(
                TextbookEntity(
                    subjectId = subjectId,
                    name = name,
                    type = type,
                    source = source,
                    createdAt = now
                )
            )
        }
    }
}
