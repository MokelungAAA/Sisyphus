package com.mokelab.sisyphus.feature.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.CardState
import com.mokelab.sisyphus.core.database.entity.ReviewCardEntity
import com.mokelab.sisyphus.core.database.entity.ReviewHistoryEntity
import com.mokelab.sisyphus.core.database.repository.ReviewCardRepository
import com.mokelab.sisyphus.core.database.repository.ReviewHistoryRepository
import com.mokelab.sisyphus.feature.review.algorithm.FSRSAlgorithm
import com.mokelab.sisyphus.feature.review.algorithm.FSRSParams
import com.mokelab.sisyphus.feature.review.algorithm.Rating
import com.mokelab.sisyphus.feature.review.algorithm.ReviewEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class ReviewCardUiState(
    val cards: List<ReviewCardEntity> = emptyList(),
    val dueCards: List<ReviewCardEntity> = emptyList(),
    val showAddDialog: Boolean = false,
    val isReviewSessionActive: Boolean = false,
    val currentReviewCard: ReviewCardEntity? = null,
    val reviewHistory: List<ReviewHistoryEntity> = emptyList(),
    val totalReviewCount: Int = 0,
    val lapseCount: Int = 0,
    val todayReviewCount: Int = 0,
    val isLoading: Boolean = false
)

class ReviewCardViewModel(
    private val repository: ReviewCardRepository,
    private val historyRepository: ReviewHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewCardUiState())
    val uiState: StateFlow<ReviewCardUiState> = _uiState.asStateFlow()

    private val fsrsParams = FSRSParams()

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
        viewModelScope.launch {
            historyRepository.getAll().collect { history ->
                _uiState.value = _uiState.value.copy(reviewHistory = history)
            }
        }
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            val totalCount = historyRepository.getTotalReviewCount()
            val lapseCount = historyRepository.getLapseCount()
            val now = Clock.System.now()
            val todayStart = now.toEpochMilliseconds() - (now.toEpochMilliseconds() % 86400000L)
            val todayCount = historyRepository.getReviewCountSince(todayStart)
            _uiState.value = _uiState.value.copy(
                totalReviewCount = totalCount,
                lapseCount = lapseCount,
                todayReviewCount = todayCount
            )
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

    fun startReviewSession() {
        val dueCards = _uiState.value.dueCards
        if (dueCards.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                isReviewSessionActive = true,
                currentReviewCard = dueCards.first()
            )
        }
    }

    fun endReviewSession() {
        _uiState.value = _uiState.value.copy(
            isReviewSessionActive = false,
            currentReviewCard = null
        )
    }

    fun rateCard(rating: Rating) {
        val currentCard = _uiState.value.currentReviewCard ?: return
        viewModelScope.launch {
            val now = Clock.System.now()
            val nowMs = now.toEpochMilliseconds()
            val totalCards = repository.getTotalCount()

            val updatedCard = ReviewEngine.nextReview(
                card = currentCard,
                rating = rating,
                params = fsrsParams,
                totalCards = totalCards,
                now = nowMs
            )

            repository.update(updatedCard)

            val history = ReviewHistoryEntity(
                cardId = currentCard.id,
                rating = rating.value,
                stabilityBefore = currentCard.stability,
                stabilityAfter = updatedCard.stability,
                difficultyBefore = currentCard.difficulty,
                difficultyAfter = updatedCard.difficulty,
                intervalBefore = currentCard.scheduledDays,
                intervalAfter = updatedCard.scheduledDays,
                reviewTime = now,
                createdAt = now
            )
            historyRepository.insert(history)

            loadStatistics()

            val remainingDueCards = _uiState.value.dueCards.filter { it.id != currentCard.id }
            if (remainingDueCards.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(currentReviewCard = remainingDueCards.first())
            } else {
                endReviewSession()
            }
        }
    }

    fun deleteHistory(history: ReviewHistoryEntity) {
        viewModelScope.launch {
            historyRepository.delete(history)
            loadStatistics()
        }
    }
}
