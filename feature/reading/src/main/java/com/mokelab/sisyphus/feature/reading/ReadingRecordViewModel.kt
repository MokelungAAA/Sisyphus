package com.mokelab.sisyphus.feature.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.entity.ReadingRecordEntity
import com.mokelab.sisyphus.core.database.repository.ReadingRecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class ReadingRecordUiState(
    val records: List<ReadingRecordEntity> = emptyList(),
    val showAddDialog: Boolean = false
)

class ReadingRecordViewModel(
    private val repository: ReadingRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadingRecordUiState())
    val uiState: StateFlow<ReadingRecordUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAll().collect { records ->
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
        bookName: String,
        author: String,
        durationMinutes: Int,
        note: String
    ) {
        viewModelScope.launch {
            val now = Clock.System.now()
            repository.insert(
                ReadingRecordEntity(
                    bookName = bookName,
                    author = author,
                    durationMinutes = durationMinutes,
                    startTime = now,
                    endTime = now,
                    note = note,
                    createdAt = now
                )
            )
        }
    }

    fun deleteRecord(record: ReadingRecordEntity) {
        viewModelScope.launch {
            repository.delete(record)
        }
    }
}
