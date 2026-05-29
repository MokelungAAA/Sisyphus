package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

/**
 * 学科仓库接口
 */
interface SubjectRepository {
    fun getAll(): Flow<List<SubjectEntity>>
    suspend fun getById(id: Long): SubjectEntity?
    suspend fun insert(subject: SubjectEntity): Long
    suspend fun update(subject: SubjectEntity)
    suspend fun delete(subject: SubjectEntity)
    suspend fun deleteById(id: Long)
}
