package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.entity.StudyRecordEntity
import kotlinx.coroutines.flow.Flow

interface StudyRecordRepository {
    fun getBySubjectId(subjectId: Long): Flow<List<StudyRecordEntity>>
    fun getAll(): Flow<List<StudyRecordEntity>>
    suspend fun getById(id: Long): StudyRecordEntity?
    suspend fun insert(record: StudyRecordEntity): Long
    suspend fun update(record: StudyRecordEntity)
    suspend fun delete(record: StudyRecordEntity)
    suspend fun deleteById(id: Long)
}
