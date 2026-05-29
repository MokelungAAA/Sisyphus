package com.mokelab.sisyphus.feature.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.ExamRecordEntity
import com.mokelab.sisyphus.core.database.entity.ExamType
import com.mokelab.sisyphus.core.database.repository.ExamRecordRepository
import com.mokelab.sisyphus.feature.achievement.AchievementChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class ExamRecordUiState(
    val records: List<ExamRecordEntity> = emptyList(),
    val showAddDialog: Boolean = false
)

class ExamRecordViewModel(
    private val repository: ExamRecordRepository,
    private val achievementChecker: AchievementChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamRecordUiState())
    val uiState: StateFlow<ExamRecordUiState> = _uiState.asStateFlow()

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
        examName: String,
        examType: ExamType,
        score: Float,
        totalScore: Float,
        isFullMock: Boolean
    ) {
        viewModelScope.launch {
            val now = Clock.System.now()
            val entity = ExamRecordEntity(
                subjectId = subjectId,
                examName = examName,
                examType = examType,
                score = score,
                totalScore = totalScore,
                scoreRate = score / totalScore,
                isFullMock = isFullMock,
                examDate = now,
                createdAt = now
            )
            repository.insert(entity)
            achievementChecker.onExamRecordCreated(entity)
        }
    }

    fun deleteRecord(record: ExamRecordEntity) {
        viewModelScope.launch {
            repository.delete(record)
        }
    }
}
