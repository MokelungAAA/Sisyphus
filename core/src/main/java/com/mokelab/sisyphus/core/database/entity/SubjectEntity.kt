package com.mokelab.sisyphus.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

/**
 * 学科实体
 */
@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val weight: Float = 5.0f,
    val isElective: Boolean = false,
    val examScoreRatio: Float = 0f,
    val createdAt: Instant,
    val updatedAt: Instant,
)
