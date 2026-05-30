package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.entity.ReviewCardEntity
import kotlinx.coroutines.flow.Flow

interface ReviewCardRepository {
    fun getByKnowledgePointId(knowledgePointId: Long): Flow<List<ReviewCardEntity>>
    /**
     * 批量查询多个知识点的复习卡片
     * 用于技能树等需要批量加载的场景，避免 N+1 查询
     */
    fun getByKnowledgePointIds(knowledgePointIds: List<Long>): Flow<List<ReviewCardEntity>>
    fun getDueCards(): Flow<List<ReviewCardEntity>>
    fun getAll(): Flow<List<ReviewCardEntity>>
    suspend fun getById(id: Long): ReviewCardEntity?
    suspend fun insert(card: ReviewCardEntity): Long
    suspend fun update(card: ReviewCardEntity)
    suspend fun delete(card: ReviewCardEntity)
    suspend fun deleteById(id: Long)
    suspend fun getTotalCount(): Int
    suspend fun getCountByState(state: String): Int
    suspend fun getDueCount(): Int
}
