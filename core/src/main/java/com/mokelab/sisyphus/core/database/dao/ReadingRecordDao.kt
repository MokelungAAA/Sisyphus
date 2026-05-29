package com.mokelab.sisyphus.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mokelab.sisyphus.core.database.entity.ReadingRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface ReadingRecordDao {
    @Query("SELECT * FROM reading_records ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ReadingRecordEntity>>

    @Query("SELECT * FROM reading_records WHERE id = :id")
    suspend fun getById(id: Long): ReadingRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ReadingRecordEntity): Long

    @Update
    suspend fun update(record: ReadingRecordEntity)

    @Delete
    suspend fun delete(record: ReadingRecordEntity)

    @Query("DELETE FROM reading_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM reading_records WHERE updatedAt > :since")
    suspend fun getModifiedSince(since: Instant): List<ReadingRecordEntity>

    @Query("SELECT * FROM reading_records WHERE bookName LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    suspend fun search(query: String): List<ReadingRecordEntity>
}
