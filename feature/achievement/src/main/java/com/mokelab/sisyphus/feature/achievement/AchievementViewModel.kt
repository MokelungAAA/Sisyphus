package com.mokelab.sisyphus.feature.achievement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.dao.AchievementDao
import com.mokelab.sisyphus.core.database.entity.AchievementEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AchievementUiState(
    val selectedCategory: AchievementDefinitions.Category = AchievementDefinitions.Category.PROGRESS,
    val achievements: List<AchievementEntity> = emptyList(),
    val totalCount: Int = 0,
    val unlockedCount: Int = 0,
    val isLoading: Boolean = true
)

class AchievementViewModel(
    private val achievementDao: AchievementDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementUiState())
    val uiState: StateFlow<AchievementUiState> = _uiState.asStateFlow()

    init {
        loadAchievements()
    }

    fun selectCategory(category: AchievementDefinitions.Category) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadAchievements()
    }

    fun loadAchievements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val category = _uiState.value.selectedCategory
            val achievements = achievementDao.getByCategory(category.name)
            val totalCount = achievementDao.getTotalCount()
            val unlockedCount = achievementDao.getUnlockedCount()

            _uiState.update {
                it.copy(
                    achievements = achievements,
                    totalCount = totalCount,
                    unlockedCount = unlockedCount,
                    isLoading = false
                )
            }
        }
    }

    fun getUnlockedAchievements(): List<AchievementEntity> {
        return _uiState.value.achievements.filter { it.unlockedAt != null }
    }

    fun getLockedAchievements(): List<AchievementEntity> {
        return _uiState.value.achievements.filter { it.unlockedAt == null }
    }
}
