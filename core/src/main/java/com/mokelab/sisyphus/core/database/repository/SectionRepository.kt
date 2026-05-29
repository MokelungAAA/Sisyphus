package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.entity.SectionEntity
import kotlinx.coroutines.flow.Flow

interface SectionRepository {
    fun getByChapterId(chapterId: Long): Flow<List<SectionEntity>>
    suspend fun getById(id: Long): SectionEntity?
    suspend fun insert(section: SectionEntity): Long
    suspend fun update(section: SectionEntity)
    suspend fun delete(section: SectionEntity)
    suspend fun deleteById(id: Long)
}
