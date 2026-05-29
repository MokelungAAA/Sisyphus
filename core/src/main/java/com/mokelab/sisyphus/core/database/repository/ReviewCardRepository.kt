package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.entity.ReviewCardEntity
import kotlinx.coroutines.flow.Flow

interface ReviewCardRepository {
    fun getByKnowledgePointId(knowledgePointId: Long): Flow<List<ReviewCardEntity>>
    fun getDueCards(): Flow<List<ReviewCardEntity>>
    fun getAll(): Flow<List<ReviewCardEntity>>
    suspend fun getById(id: Long): ReviewCardEntity?
    suspend fun insert(card: ReviewCardEntity): Long
    suspend fun update(card: ReviewCardEntity)
    suspend fun delete(card: ReviewCardEntity)
    suspend fun deleteById(id: Long)
}
