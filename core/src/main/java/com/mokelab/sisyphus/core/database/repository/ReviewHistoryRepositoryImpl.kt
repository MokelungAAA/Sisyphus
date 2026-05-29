package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.dao.ReviewHistoryDao
import com.mokelab.sisyphus.core.database.entity.ReviewHistoryEntity
import kotlinx.coroutines.flow.Flow

class ReviewHistoryRepositoryImpl(
    private val dao: ReviewHistoryDao
) : ReviewHistoryRepository {
    override fun getAll(): Flow<List<ReviewHistoryEntity>> = dao.getAll()

    override fun getByCardId(cardId: Long): Flow<List<ReviewHistoryEntity>> = dao.getByCardId(cardId)

    override fun getByTimeRange(startTime: Long, endTime: Long): Flow<List<ReviewHistoryEntity>> =
        dao.getByTimeRange(startTime, endTime)

    override suspend fun getById(id: Long): ReviewHistoryEntity? = dao.getById(id)

    override suspend fun getTotalReviewCount(): Int = dao.getTotalReviewCount()

    override suspend fun getLapseCount(): Int = dao.getLapseCount()

    override suspend fun getReviewCountSince(startTime: Long): Int = dao.getReviewCountSince(startTime)

    override suspend fun insert(history: ReviewHistoryEntity): Long = dao.insert(history)

    override suspend fun delete(history: ReviewHistoryEntity) = dao.delete(history)

    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
