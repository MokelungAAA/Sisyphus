package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.dao.ReadingRecordDao
import com.mokelab.sisyphus.core.database.entity.ReadingRecordEntity
import kotlinx.coroutines.flow.Flow

class ReadingRecordRepositoryImpl(
    private val dao: ReadingRecordDao
) : ReadingRecordRepository {
    override fun getAll(): Flow<List<ReadingRecordEntity>> = dao.getAll()

    override suspend fun getById(id: Long): ReadingRecordEntity? = dao.getById(id)

    override suspend fun insert(record: ReadingRecordEntity): Long = dao.insert(record)

    override suspend fun update(record: ReadingRecordEntity) = dao.update(record)

    override suspend fun delete(record: ReadingRecordEntity) = dao.delete(record)

    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
