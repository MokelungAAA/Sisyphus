package com.mokelab.sisyphus.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mokelab.sisyphus.core.database.entity.SectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionDao {
    @Query("SELECT * FROM sections WHERE chapterId = :chapterId ORDER BY orderIndex ASC")
    fun getByChapterId(chapterId: Long): Flow<List<SectionEntity>>

    @Query("SELECT * FROM sections WHERE id = :id")
    suspend fun getById(id: Long): SectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(section: SectionEntity): Long

    @Update
    suspend fun update(section: SectionEntity)

    @Delete
    suspend fun delete(section: SectionEntity)

    @Query("DELETE FROM sections WHERE id = :id")
    suspend fun deleteById(id: Long)
}
