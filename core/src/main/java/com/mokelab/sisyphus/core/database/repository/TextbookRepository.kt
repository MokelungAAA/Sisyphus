package com.mokelab.sisyphus.core.database.repository

import com.mokelab.sisyphus.core.database.entity.TextbookEntity
import kotlinx.coroutines.flow.Flow

interface TextbookRepository {
    fun getBySubjectId(subjectId: Long): Flow<List<TextbookEntity>>
    suspend fun getById(id: Long): TextbookEntity?
    suspend fun insert(textbook: TextbookEntity): Long
    suspend fun update(textbook: TextbookEntity)
    suspend fun delete(textbook: TextbookEntity)
    suspend fun deleteById(id: Long)
}
