package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.dao.StudyRecordDao
import com.mokelab.sisyphus.core.database.entity.StudyRecordEntity
import kotlinx.coroutines.flow.Flow

class StudyRecordRepositoryImpl(
    private val dao: StudyRecordDao
) : StudyRecordRepository {
    override fun getBySubjectId(subjectId: Long): Flow<List<StudyRecordEntity>> = dao.getBySubjectId(subjectId)

    override fun getAll(): Flow<List<StudyRecordEntity>> = dao.getAll()

    override suspend fun getById(id: Long): StudyRecordEntity? = dao.getById(id)

    override suspend fun insert(record: StudyRecordEntity): Long = dao.insert(record)

    override suspend fun update(record: StudyRecordEntity) = dao.update(record)

    override suspend fun delete(record: StudyRecordEntity) = dao.delete(record)

    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
