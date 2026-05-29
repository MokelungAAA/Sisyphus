package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.entity.KnowledgePointEntity
import kotlinx.coroutines.flow.Flow

interface KnowledgePointRepository {
    fun getBySectionId(sectionId: Long): Flow<List<KnowledgePointEntity>>
    suspend fun getById(id: Long): KnowledgePointEntity?
    suspend fun insert(knowledgePoint: KnowledgePointEntity): Long
    suspend fun update(knowledgePoint: KnowledgePointEntity)
    suspend fun delete(knowledgePoint: KnowledgePointEntity)
    suspend fun deleteById(id: Long)
}
