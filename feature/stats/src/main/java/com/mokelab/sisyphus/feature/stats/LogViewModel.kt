package com.mokelab.sisyphus.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.StudyRecordEntity
import com.mokelab.sisyphus.core.database.entity.SubjectEntity
import com.mokelab.sisyphus.core.database.repository.StudyRecordRepository
import com.mokelab.sisyphus.core.database.repository.SubjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * 日志页面UI状态
 */
data class LogUiState(
    val records: List<StudyRecordEntity> = emptyList(),
    val subjects: Map<Long, SubjectEntity> = emptyMap(),
    val isLoading: Boolean = true
)

/**
 * 日志页面ViewModel
 */
class LogViewModel(
    private val studyRecordRepository: StudyRecordRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                studyRecordRepository.getAll(),
                subjectRepository.getAll()
            ) { records, subjects ->
                LogUiState(
                    records = records.sortedByDescending { it.startTime },
                    subjects = subjects.associateBy { it.id },
                    isLoading = false
                )
            }.collectLatest { state ->
                _uiState.value = state
            }
        }
    }
}
