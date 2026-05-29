package com.mokelab.sisyphus.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mokelab.sisyphus.core.database.entity.PomodoroSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface PomodoroSessionDao {
    @Query("SELECT * FROM pomodoro_sessions ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM pomodoro_sessions WHERE subjectId = :subjectId ORDER BY createdAt DESC")
    fun getBySubjectId(subjectId: Long): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM pomodoro_sessions WHERE id = :id")
    suspend fun getById(id: Long): PomodoroSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: PomodoroSessionEntity): Long

    @Update
    suspend fun update(session: PomodoroSessionEntity)

    @Delete
    suspend fun delete(session: PomodoroSessionEntity)

    @Query("DELETE FROM pomodoro_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM pomodoro_sessions WHERE updatedAt > :since")
    suspend fun getModifiedSince(since: Instant): List<PomodoroSessionEntity>

    @Query("SELECT COUNT(*) FROM pomodoro_sessions")
    suspend fun getCount(): Int
}
