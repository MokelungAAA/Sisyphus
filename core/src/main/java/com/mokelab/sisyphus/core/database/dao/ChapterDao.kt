package com.mokelab.sisyphus.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mokelab.sisyphus.core.database.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE textbookId = :textbookId ORDER BY orderIndex ASC")
    fun getByTextbookId(textbookId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getById(id: Long): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chapter: ChapterEntity): Long

    @Update
    suspend fun update(chapter: ChapterEntity)

    @Delete
    suspend fun delete(chapter: ChapterEntity)

    @Query("DELETE FROM chapters WHERE id = :id")
    suspend fun deleteById(id: Long)
}
