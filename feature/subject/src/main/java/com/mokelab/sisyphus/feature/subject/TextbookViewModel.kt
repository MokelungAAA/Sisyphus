package com.mokelab.sisyphus.feature.subject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.TextbookEntity
import com.mokelab.sisyphus.core.database.entity.TextbookSource
import com.mokelab.sisyphus.core.database.entity.TextbookType
import com.mokelab.sisyphus.core.database.repository.TextbookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class TextbookUiState(
    val textbooks: List<TextbookEntity> = emptyList(),
    val showAddDialog: Boolean = false
)

class TextbookViewModel(
    private val repository: TextbookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TextbookUiState())
    val uiState: StateFlow<TextbookUiState> = _uiState.asStateFlow()

    private var currentSubjectId: Long = 0

    fun loadTextbooks(subjectId: Long) {
        currentSubjectId = subjectId
        viewModelScope.launch {
            repository.getBySubjectId(subjectId).collect { textbooks ->
                _uiState.value = _uiState.value.copy(textbooks = textbooks)
            }
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }

    fun addTextbook(name: String, type: TextbookType) {
        viewModelScope.launch {
            val now = Clock.System.now()
            repository.insert(
                TextbookEntity(
                    subjectId = currentSubjectId,
                    name = name,
                    type = type,
                    source = TextbookSource.MANUAL,
                    createdAt = now
                )
            )
        }
    }

    fun deleteTextbook(textbook: TextbookEntity) {
        viewModelScope.launch {
            repository.delete(textbook)
        }
    }
}
