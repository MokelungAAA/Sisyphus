package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.entity.ReviewHistoryEntity
import kotlinx.coroutines.flow.Flow

interface ReviewHistoryRepository {
    fun getAll(): Flow<List<ReviewHistoryEntity>>
    fun getByCardId(cardId: Long): Flow<List<ReviewHistoryEntity>>
    fun getByTimeRange(startTime: Long, endTime: Long): Flow<List<ReviewHistoryEntity>>
    suspend fun getById(id: Long): ReviewHistoryEntity?
    suspend fun getTotalReviewCount(): Int
    suspend fun getLapseCount(): Int
    suspend fun getReviewCountSince(startTime: Long): Int
    suspend fun insert(history: ReviewHistoryEntity): Long
    suspend fun delete(history: ReviewHistoryEntity)
    suspend fun deleteById(id: Long)
}
