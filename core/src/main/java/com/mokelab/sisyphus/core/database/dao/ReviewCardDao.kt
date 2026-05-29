package com.mokelab.sisyphus.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mokelab.sisyphus.core.database.entity.ReviewCardEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface ReviewCardDao {
    @Query("SELECT * FROM review_cards ORDER BY due ASC")
    fun getAll(): Flow<List<ReviewCardEntity>>

    @Query("SELECT * FROM review_cards WHERE knowledgePointId = :knowledgePointId")
    fun getByKnowledgePointId(knowledgePointId: Long): Flow<List<ReviewCardEntity>>

    @Query("SELECT * FROM review_cards WHERE due <= :now ORDER BY due ASC")
    fun getDueCards(now: Long = System.currentTimeMillis()): Flow<List<ReviewCardEntity>>

    @Query("SELECT * FROM review_cards WHERE id = :id")
    suspend fun getById(id: Long): ReviewCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: ReviewCardEntity): Long

    @Update
    suspend fun update(card: ReviewCardEntity)

    @Delete
    suspend fun delete(card: ReviewCardEntity)

    @Query("DELETE FROM review_cards WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM review_cards")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM review_cards WHERE state = :state")
    suspend fun getCountByState(state: String): Int

    @Query("SELECT COUNT(*) FROM review_cards WHERE due <= :now")
    suspend fun getDueCount(now: Long = System.currentTimeMillis()): Int

    @Query("SELECT * FROM review_cards WHERE updatedAt > :since")
    suspend fun getModifiedSince(since: Instant): List<ReviewCardEntity>
}
