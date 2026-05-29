package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.entity.PomodoroSessionEntity
import kotlinx.coroutines.flow.Flow

interface PomodoroSessionRepository {
    fun getBySubjectId(subjectId: Long): Flow<List<PomodoroSessionEntity>>
    fun getAll(): Flow<List<PomodoroSessionEntity>>
    suspend fun getById(id: Long): PomodoroSessionEntity?
    suspend fun insert(session: PomodoroSessionEntity): Long
    suspend fun update(session: PomodoroSessionEntity)
    suspend fun delete(session: PomodoroSessionEntity)
    suspend fun deleteById(id: Long)
}
