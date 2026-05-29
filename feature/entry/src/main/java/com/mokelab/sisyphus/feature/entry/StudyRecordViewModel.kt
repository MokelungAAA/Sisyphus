package com.mokelab.sisyphus.feature.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.InputOutputType
import com.mokelab.sisyphus.core.database.entity.StudyRecordEntity
import com.mokelab.sisyphus.core.database.entity.StudyType
import com.mokelab.sisyphus.core.database.repository.StudyRecordRepository
import com.mokelab.sisyphus.feature.achievement.AchievementChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class StudyRecordUiState(
    val records: List<StudyRecordEntity> = emptyList(),
    val showAddDialog: Boolean = false,
    val unlockedAchievement: String? = null
)

class StudyRecordViewModel(
    private val repository: StudyRecordRepository,
    private val achievementChecker: AchievementChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudyRecordUiState())
    val uiState: StateFlow<StudyRecordUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAll().collect { records ->
                _uiState.value = _uiState.value.copy(records = records)
            }
        }
    }

    fun loadBySubject(subjectId: Long) {
        viewModelScope.launch {
            repository.getBySubjectId(subjectId).collect { records ->
                _uiState.value = _uiState.value.copy(records = records)
            }
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }

    fun addRecord(
        subjectId: Long,
        studyType: StudyType,
        durationMinutes: Int,
        inputType: InputOutputType,
        note: String
    ) {
        viewModelScope.launch {
            val now = Clock.System.now()
            repository.insert(
                StudyRecordEntity(
                    subjectId = subjectId,
                    textbookId = null,
                    chapterId = null,
                    sectionId = null,
                    studyType = studyType,
                    durationMinutes = durationMinutes,
                    startTime = now,
                    endTime = now,
                    inputType = inputType,
                    xpEarned = durationMinutes.toFloat(),
                    note = note,
                    createdAt = now
                )
            )
            // 成就检查
            achievementChecker.onStudyRecordCreated()
        }
    }

    fun deleteRecord(record: StudyRecordEntity) {
        viewModelScope.launch {
            repository.delete(record)
        }
    }
}
