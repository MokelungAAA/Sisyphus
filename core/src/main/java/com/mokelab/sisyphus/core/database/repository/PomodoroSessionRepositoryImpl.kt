package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.dao.PomodoroSessionDao
import com.mokelab.sisyphus.core.database.entity.PomodoroSessionEntity
import kotlinx.coroutines.flow.Flow

class PomodoroSessionRepositoryImpl(
    private val dao: PomodoroSessionDao
) : PomodoroSessionRepository {
    override fun getBySubjectId(subjectId: Long): Flow<List<PomodoroSessionEntity>> =
        dao.getBySubjectId(subjectId)

    override fun getAll(): Flow<List<PomodoroSessionEntity>> = dao.getAll()

    override suspend fun getById(id: Long): PomodoroSessionEntity? = dao.getById(id)

    override suspend fun insert(session: PomodoroSessionEntity): Long = dao.insert(session)

    override suspend fun update(session: PomodoroSessionEntity) = dao.update(session)

    override suspend fun delete(session: PomodoroSessionEntity) = dao.delete(session)

    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
