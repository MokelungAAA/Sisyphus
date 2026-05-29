package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.dao.ExamRecordDao
import com.mokelab.sisyphus.core.database.entity.ExamRecordEntity
import kotlinx.coroutines.flow.Flow

class ExamRecordRepositoryImpl(
    private val dao: ExamRecordDao
) : ExamRecordRepository {
    override fun getBySubjectId(subjectId: Long): Flow<List<ExamRecordEntity>> = dao.getBySubjectId(subjectId)

    override fun getAll(): Flow<List<ExamRecordEntity>> = dao.getAll()

    override suspend fun getById(id: Long): ExamRecordEntity? = dao.getById(id)

    override suspend fun insert(record: ExamRecordEntity): Long = dao.insert(record)

    override suspend fun update(record: ExamRecordEntity) = dao.update(record)

    override suspend fun delete(record: ExamRecordEntity) = dao.delete(record)

    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
