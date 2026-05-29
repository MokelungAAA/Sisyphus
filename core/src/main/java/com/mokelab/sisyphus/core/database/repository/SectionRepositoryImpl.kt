package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.dao.SectionDao
import com.mokelab.sisyphus.core.database.entity.SectionEntity
import kotlinx.coroutines.flow.Flow

class SectionRepositoryImpl(
    private val dao: SectionDao
) : SectionRepository {
    override fun getByChapterId(chapterId: Long): Flow<List<SectionEntity>> = dao.getByChapterId(chapterId)

    override suspend fun getById(id: Long): SectionEntity? = dao.getById(id)

    override suspend fun insert(section: SectionEntity): Long = dao.insert(section)

    override suspend fun update(section: SectionEntity) = dao.update(section)

    override suspend fun delete(section: SectionEntity) = dao.delete(section)

    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
