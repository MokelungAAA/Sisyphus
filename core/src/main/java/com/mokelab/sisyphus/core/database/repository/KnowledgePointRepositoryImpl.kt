package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.dao.KnowledgePointDao
import com.mokelab.sisyphus.core.database.entity.KnowledgePointEntity
import kotlinx.coroutines.flow.Flow

class KnowledgePointRepositoryImpl(
    private val dao: KnowledgePointDao
) : KnowledgePointRepository {
    override fun getBySectionId(sectionId: Long): Flow<List<KnowledgePointEntity>> = dao.getBySectionId(sectionId)

    override suspend fun getById(id: Long): KnowledgePointEntity? = dao.getById(id)

    override suspend fun insert(knowledgePoint: KnowledgePointEntity): Long = dao.insert(knowledgePoint)

    override suspend fun update(knowledgePoint: KnowledgePointEntity) = dao.update(knowledgePoint)

    override suspend fun delete(knowledgePoint: KnowledgePointEntity) = dao.delete(knowledgePoint)

    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
