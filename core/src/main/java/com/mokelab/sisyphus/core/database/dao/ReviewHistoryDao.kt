package com.mokelab.sisyphus.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mokelab.sisyphus.core.database.entity.ReviewHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface ReviewHistoryDao {
    @Query("SELECT * FROM review_history ORDER BY reviewTime DESC")
    fun getAll(): Flow<List<ReviewHistoryEntity>>

    @Query("SELECT * FROM review_history WHERE cardId = :cardId ORDER BY reviewTime DESC")
    fun getByCardId(cardId: Long): Flow<List<ReviewHistoryEntity>>

    @Query("SELECT * FROM review_history WHERE reviewTime >= :startTime AND reviewTime <= :endTime ORDER BY reviewTime DESC")
    fun getByTimeRange(startTime: Long, endTime: Long): Flow<List<ReviewHistoryEntity>>

    @Query("SELECT * FROM review_history WHERE id = :id")
    suspend fun getById(id: Long): ReviewHistoryEntity?

    @Query("SELECT COUNT(*) FROM review_history")
    suspend fun getTotalReviewCount(): Int

    @Query("SELECT COUNT(*) FROM review_history WHERE rating = 0")
    suspend fun getLapseCount(): Int

    @Query("SELECT COUNT(*) FROM review_history WHERE reviewTime >= :startTime")
    suspend fun getReviewCountSince(startTime: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: ReviewHistoryEntity): Long

    @Delete
    suspend fun delete(history: ReviewHistoryEntity)

    @Query("DELETE FROM review_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM review_history WHERE updatedAt > :since")
    suspend fun getModifiedSince(since: Instant): List<ReviewHistoryEntity>
}
