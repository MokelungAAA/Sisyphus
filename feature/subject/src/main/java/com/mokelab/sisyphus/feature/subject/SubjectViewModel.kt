package com.mokelab.sisyphus.feature.subject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.SubjectEntity
import com.mokelab.sisyphus.core.database.repository.SubjectRepository
import com.mokelab.sisyphus.feature.achievement.AchievementChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SubjectUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val showAddDialog: Boolean = false,
    val editingSubject: SubjectEntity? = null
)

class SubjectViewModel(
    private val repository: SubjectRepository,
    private val achievementChecker: AchievementChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubjectUiState())
    val uiState: StateFlow<SubjectUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAll().collect { subjects ->
                _uiState.value = _uiState.value.copy(subjects = subjects)
            }
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }

    fun addSubject(name: String, weight: Float, isElective: Boolean) {
        viewModelScope.launch {
            val now = kotlinx.datetime.Clock.System.now()
            repository.insert(
                SubjectEntity(
                    name = name,
                    weight = weight,
                    isElective = isElective,
                    examScoreRatio = 1.0f,
                    createdAt = now,
                    updatedAt = now
                )
            )
            achievementChecker.onSubjectCreated()
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.delete(subject)
        }
    }
}
