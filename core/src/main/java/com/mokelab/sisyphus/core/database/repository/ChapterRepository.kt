package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

interface ChapterRepository {
    fun getByTextbookId(textbookId: Long): Flow<List<ChapterEntity>>
    suspend fun getById(id: Long): ChapterEntity?
    suspend fun insert(chapter: ChapterEntity): Long
    suspend fun update(chapter: ChapterEntity)
    suspend fun delete(chapter: ChapterEntity)
    suspend fun deleteById(id: Long)
}
