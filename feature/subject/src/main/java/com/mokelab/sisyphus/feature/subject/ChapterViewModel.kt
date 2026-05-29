package com.mokelab.sisyphus.feature.subject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.ChapterEntity
import com.mokelab.sisyphus.core.database.repository.ChapterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class ChapterUiState(
    val chapters: List<ChapterEntity> = emptyList(),
    val showAddDialog: Boolean = false
)

class ChapterViewModel(
    private val repository: ChapterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChapterUiState())
    val uiState: StateFlow<ChapterUiState> = _uiState.asStateFlow()

    private var currentTextbookId: Long = 0

    fun loadChapters(textbookId: Long) {
        currentTextbookId = textbookId
        viewModelScope.launch {
            repository.getByTextbookId(textbookId).collect { chapters ->
                _uiState.value = _uiState.value.copy(chapters = chapters)
            }
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }

    fun addChapter(name: String, orderIndex: Int) {
        viewModelScope.launch {
            val now = Clock.System.now()
            repository.insert(
                ChapterEntity(
                    textbookId = currentTextbookId,
                    name = name,
                    orderIndex = orderIndex,
                    createdAt = now
                )
            )
        }
    }

    fun deleteChapter(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.delete(chapter)
        }
    }
}
