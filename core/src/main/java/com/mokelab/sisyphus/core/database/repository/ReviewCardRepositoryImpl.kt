package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.dao.ReviewCardDao
import com.mokelab.sisyphus.core.database.entity.ReviewCardEntity
import kotlinx.coroutines.flow.Flow

class ReviewCardRepositoryImpl(
    private val dao: ReviewCardDao
) : ReviewCardRepository {
    override fun getByKnowledgePointId(knowledgePointId: Long): Flow<List<ReviewCardEntity>> =
        dao.getByKnowledgePointId(knowledgePointId)

    override fun getDueCards(): Flow<List<ReviewCardEntity>> = dao.getDueCards()

    override fun getAll(): Flow<List<ReviewCardEntity>> = dao.getAll()

    override suspend fun getById(id: Long): ReviewCardEntity? = dao.getById(id)

    override suspend fun insert(card: ReviewCardEntity): Long = dao.insert(card)

    override suspend fun update(card: ReviewCardEntity) = dao.update(card)

    override suspend fun delete(card: ReviewCardEntity) = dao.delete(card)

    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
