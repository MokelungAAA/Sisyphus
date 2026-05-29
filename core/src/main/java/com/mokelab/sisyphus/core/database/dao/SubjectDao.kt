package com.mokelab.sisyphus.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mokelab.sisyphus.core.database.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/**
 * 学科DAO
 */
@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAll(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getById(id: Long): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: SubjectEntity): Long

    @Update
    suspend fun update(subject: SubjectEntity)

    @Delete
    suspend fun delete(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM subjects WHERE updatedAt > :since")
    suspend fun getModifiedSince(since: Instant): List<SubjectEntity>

    @Query("SELECT * FROM subjects WHERE name LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<SubjectEntity>
}
