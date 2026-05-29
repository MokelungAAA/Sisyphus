package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.dao.ChapterDao
import com.mokelab.sisyphus.core.database.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

class ChapterRepositoryImpl(
    private val dao: ChapterDao
) : ChapterRepository {
    override fun getByTextbookId(textbookId: Long): Flow<List<ChapterEntity>> = dao.getByTextbookId(textbookId)

    override suspend fun getById(id: Long): ChapterEntity? = dao.getById(id)

    override suspend fun insert(chapter: ChapterEntity): Long = dao.insert(chapter)

    override suspend fun update(chapter: ChapterEntity) = dao.update(chapter)

    override suspend fun delete(chapter: ChapterEntity) = dao.delete(chapter)

    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
