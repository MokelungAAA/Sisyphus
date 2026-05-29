package com.mokelab.sisyphus.feature.subject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.SectionEntity
import com.mokelab.sisyphus.core.database.repository.SectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class SectionUiState(
    val sections: List<SectionEntity> = emptyList(),
    val showAddDialog: Boolean = false
)

class SectionViewModel(
    private val repository: SectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SectionUiState())
    val uiState: StateFlow<SectionUiState> = _uiState.asStateFlow()

    private var currentChapterId: Long = 0

    fun loadSections(chapterId: Long) {
        currentChapterId = chapterId
        viewModelScope.launch {
            repository.getByChapterId(chapterId).collect { sections ->
                _uiState.value = _uiState.value.copy(sections = sections)
            }
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }

    fun addSection(name: String, orderIndex: Int) {
        viewModelScope.launch {
            val now = Clock.System.now()
            repository.insert(
                SectionEntity(
                    chapterId = currentChapterId,
                    name = name,
                    orderIndex = orderIndex,
                    createdAt = now
                )
            )
        }
    }

    fun deleteSection(section: SectionEntity) {
        viewModelScope.launch {
            repository.delete(section)
        }
    }
}
