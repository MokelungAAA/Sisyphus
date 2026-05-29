package com.mokelab.sisyphus.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,           // 如 "progress_1", "explore_3"
    val category: String,     // PROGRESS, EXPLORE, SCORE, EASTER_EGG
    val name: String,         // 显示名称
    val description: String,  // 解锁后显示的描述
    val iconRes: String,      // 图标资源名
    val rarity: String,       // COMMON, RARE, EPIC, LEGENDARY
    val unlockedAt: Long? = null  // 解锁时间戳，null表示未解锁
)
