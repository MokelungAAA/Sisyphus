package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.dao.SubjectDao
import com.mokelab.sisyphus.core.database.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

class SubjectRepositoryImpl(
    private val dao: SubjectDao
) : SubjectRepository {
    override fun getAll(): Flow<List<SubjectEntity>> = dao.getAll()

    override suspend fun getById(id: Long): SubjectEntity? = dao.getById(id)

    override suspend fun insert(subject: SubjectEntity): Long = dao.insert(subject)

    override suspend fun update(subject: SubjectEntity) = dao.update(subject)

    override suspend fun delete(subject: SubjectEntity) = dao.delete(subject)

    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
