package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.dao.TextbookDao
import com.mokelab.sisyphus.core.database.entity.TextbookEntity
import kotlinx.coroutines.flow.Flow

class TextbookRepositoryImpl(
    private val dao: TextbookDao
) : TextbookRepository {
    override fun getBySubjectId(subjectId: Long): Flow<List<TextbookEntity>> = dao.getBySubjectId(subjectId)

    override suspend fun getById(id: Long): TextbookEntity? = dao.getById(id)

    override suspend fun insert(textbook: TextbookEntity): Long = dao.insert(textbook)

    override suspend fun update(textbook: TextbookEntity) = dao.update(textbook)

    override suspend fun delete(textbook: TextbookEntity) = dao.delete(textbook)

    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
