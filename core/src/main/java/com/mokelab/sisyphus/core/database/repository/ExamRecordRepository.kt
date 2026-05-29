package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.entity.ExamRecordEntity
import kotlinx.coroutines.flow.Flow

interface ExamRecordRepository {
    fun getBySubjectId(subjectId: Long): Flow<List<ExamRecordEntity>>
    fun getAll(): Flow<List<ExamRecordEntity>>
    suspend fun getById(id: Long): ExamRecordEntity?
    suspend fun insert(record: ExamRecordEntity): Long
    suspend fun update(record: ExamRecordEntity)
    suspend fun delete(record: ExamRecordEntity)
    suspend fun deleteById(id: Long)
}
