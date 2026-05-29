package com.mokelab.sisyphus.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mokelab.sisyphus.core.database.entity.AchievementEntity

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY category, id")
    suspend fun getAll(): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE category = :category ORDER BY id")
    suspend fun getByCategory(category: String): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getById(id: String): AchievementEntity?

    @Query("SELECT * FROM achievements WHERE unlockedAt IS NOT NULL ORDER BY unlockedAt DESC")
    suspend fun getUnlocked(): List<AchievementEntity>

    @Query("SELECT COUNT(*) FROM achievements WHERE unlockedAt IS NOT NULL")
    suspend fun getUnlockedCount(): Int

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getTotalCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<AchievementEntity>)

    @Query("UPDATE achievements SET unlockedAt = :timestamp WHERE id = :id AND unlockedAt IS NULL")
    suspend fun unlock(id: String, timestamp: Long)

    @Query("UPDATE achievements SET unlockedAt = NULL WHERE id = :id")
    suspend fun lock(id: String)

    @Query("DELETE FROM achievements")
    suspend fun deleteAll()
}
