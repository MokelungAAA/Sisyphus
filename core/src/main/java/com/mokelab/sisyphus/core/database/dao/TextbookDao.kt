package com.mokelab.sisyphus.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mokelab.sisyphus.core.database.entity.TextbookEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface TextbookDao {
    @Query("SELECT * FROM textbooks WHERE subjectId = :subjectId ORDER BY name ASC")
    fun getBySubjectId(subjectId: Long): Flow<List<TextbookEntity>>

    @Query("SELECT * FROM textbooks WHERE id = :id")
    suspend fun getById(id: Long): TextbookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(textbook: TextbookEntity): Long

    @Update
    suspend fun update(textbook: TextbookEntity)

    @Delete
    suspend fun delete(textbook: TextbookEntity)

    @Query("DELETE FROM textbooks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM textbooks WHERE updatedAt > :since")
    suspend fun getModifiedSince(since: Instant): List<TextbookEntity>
}
