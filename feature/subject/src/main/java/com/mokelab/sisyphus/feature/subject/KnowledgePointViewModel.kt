package com.mokelab.sisyphus.feature.subject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.KnowledgePointEntity
import com.mokelab.sisyphus.core.database.entity.KnowledgePointSource
import com.mokelab.sisyphus.core.database.repository.KnowledgePointRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class KnowledgePointUiState(
    val points: List<KnowledgePointEntity> = emptyList(),
    val showAddDialog: Boolean = false
)

class KnowledgePointViewModel(
    private val repository: KnowledgePointRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KnowledgePointUiState())
    val uiState: StateFlow<KnowledgePointUiState> = _uiState.asStateFlow()

    private var currentSectionId: Long = 0

    fun loadPoints(sectionId: Long) {
        currentSectionId = sectionId
        viewModelScope.launch {
            repository.getBySectionId(sectionId).collect { points ->
                _uiState.value = _uiState.value.copy(points = points)
            }
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }

    fun addPoint(name: String, content: String) {
        viewModelScope.launch {
            val now = Clock.System.now()
            repository.insert(
                KnowledgePointEntity(
                    sectionId = currentSectionId,
                    name = name,
                    content = content,
                    source = KnowledgePointSource.USER_ADDED,
                    createdAt = now
                )
            )
        }
    }

    fun deletePoint(point: KnowledgePointEntity) {
        viewModelScope.launch {
            repository.delete(point)
        }
    }
}
