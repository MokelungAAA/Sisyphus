package com.mokelab.sisyphus.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mokelab.sisyphus.core.database.entity.ExamRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface ExamRecordDao {
    @Query("SELECT * FROM exam_records ORDER BY examDate DESC")
    fun getAll(): Flow<List<ExamRecordEntity>>

    @Query("SELECT * FROM exam_records WHERE subjectId = :subjectId ORDER BY examDate DESC")
    fun getBySubjectId(subjectId: Long): Flow<List<ExamRecordEntity>>

    @Query("SELECT * FROM exam_records WHERE id = :id")
    suspend fun getById(id: Long): ExamRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ExamRecordEntity): Long

    @Update
    suspend fun update(record: ExamRecordEntity)

    @Delete
    suspend fun delete(record: ExamRecordEntity)

    @Query("DELETE FROM exam_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM exam_records WHERE updatedAt > :since")
    suspend fun getModifiedSince(since: Instant): List<ExamRecordEntity>

    @Query("SELECT * FROM exam_records WHERE examName LIKE '%' || :query || '%' ORDER BY examDate DESC")
    suspend fun search(query: String): List<ExamRecordEntity>

    @Query("SELECT COUNT(*) FROM exam_records")
    suspend fun getCount(): Int

    @Query("SELECT * FROM exam_records")
    suspend fun getAllList(): List<ExamRecordEntity>
}
