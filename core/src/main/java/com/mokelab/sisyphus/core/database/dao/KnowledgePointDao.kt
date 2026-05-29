package com.mokelab.sisyphus.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mokelab.sisyphus.core.database.entity.KnowledgePointEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface KnowledgePointDao {
    @Query("SELECT * FROM knowledge_points WHERE sectionId = :sectionId ORDER BY name ASC")
    fun getBySectionId(sectionId: Long): Flow<List<KnowledgePointEntity>>

    @Query("SELECT * FROM knowledge_points WHERE id = :id")
    suspend fun getById(id: Long): KnowledgePointEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(knowledgePoint: KnowledgePointEntity): Long

    @Update
    suspend fun update(knowledgePoint: KnowledgePointEntity)

    @Delete
    suspend fun delete(knowledgePoint: KnowledgePointEntity)

    @Query("DELETE FROM knowledge_points WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM knowledge_points WHERE updatedAt > :since")
    suspend fun getModifiedSince(since: Instant): List<KnowledgePointEntity>

    @Query("SELECT * FROM knowledge_points WHERE name LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<KnowledgePointEntity>

    @Query("SELECT * FROM knowledge_points")
    suspend fun getAllList(): List<KnowledgePointEntity>
}
