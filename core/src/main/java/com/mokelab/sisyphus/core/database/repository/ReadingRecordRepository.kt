package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.entity.ReadingRecordEntity
import kotlinx.coroutines.flow.Flow

interface ReadingRecordRepository {
    fun getAll(): Flow<List<ReadingRecordEntity>>
    suspend fun getById(id: Long): ReadingRecordEntity?
    suspend fun insert(record: ReadingRecordEntity): Long
    suspend fun update(record: ReadingRecordEntity)
    suspend fun delete(record: ReadingRecordEntity)
    suspend fun deleteById(id: Long)
}
