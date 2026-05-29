package com.mokelab.sisyphus.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

/**
 * 阅读记录实体（独立模块，不计入XP）
 */
@Entity(tableName = "reading_records")
data class ReadingRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookName: String,
    val author: String? = null,
    val durationMinutes: Int,
    val startTime: Instant,
    val endTime: Instant,
    val note: String? = null,
    val createdAt: Instant,
)
