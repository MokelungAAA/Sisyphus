package com.mokelab.sisyphus.feature.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.CardState
import com.mokelab.sisyphus.core.database.entity.ReviewCardEntity
import com.mokelab.sisyphus.core.database.repository.ReviewCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class ReviewCardUiState(
    val cards: List<ReviewCardEntity> = emptyList(),
    val dueCards: List<ReviewCardEntity> = emptyList(),
    val showAddDialog: Boolean = false
)

class ReviewCardViewModel(
    private val repository: ReviewCardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewCardUiState())
    val uiState: StateFlow<ReviewCardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAll().collect { cards ->
                _uiState.value = _uiState.value.copy(cards = cards)
            }
        }
        viewModelScope.launch {
            repository.getDueCards().collect { dueCards ->
                _uiState.value = _uiState.value.copy(dueCards = dueCards)
            }
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }

    fun addCard(knowledgePointId: Long) {
        viewModelScope.launch {
            val now = Clock.System.now()
            repository.insert(
                ReviewCardEntity(
                    knowledgePointId = knowledgePointId,
                    studyRecordId = null,
                    stability = 1.0f,
                    difficulty = 0.3f,
                    elapsedDays = 0,
                    scheduledDays = 1,
                    reps = 0,
                    lapses = 0,
                    state = CardState.NEW,
                    due = now,
                    lastReview = null,
                    createdAt = now
                )
            )
        }
    }

    fun deleteCard(card: ReviewCardEntity) {
        viewModelScope.launch {
            repository.delete(card)
        }
    }
}
