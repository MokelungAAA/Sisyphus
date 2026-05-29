package com.mokelab.sisyphus.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mokelab.sisyphus.core.database.entity.StudyRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface StudyRecordDao {
    @Query("SELECT * FROM study_records ORDER BY createdAt DESC")
    fun getAll(): Flow<List<StudyRecordEntity>>

    @Query("SELECT * FROM study_records WHERE subjectId = :subjectId ORDER BY createdAt DESC")
    fun getBySubjectId(subjectId: Long): Flow<List<StudyRecordEntity>>

    @Query("SELECT * FROM study_records WHERE id = :id")
    suspend fun getById(id: Long): StudyRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: StudyRecordEntity): Long

    @Update
    suspend fun update(record: StudyRecordEntity)

    @Delete
    suspend fun delete(record: StudyRecordEntity)

    @Query("DELETE FROM study_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM study_records WHERE updatedAt > :since")
    suspend fun getModifiedSince(since: Instant): List<StudyRecordEntity>

    @Query("SELECT * FROM study_records WHERE note LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    suspend fun search(query: String): List<StudyRecordEntity>
}
