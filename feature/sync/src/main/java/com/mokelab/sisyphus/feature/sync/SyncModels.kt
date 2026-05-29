package com.mokelab.sisyphus.feature.sync

import kotlinx.serialization.Serializable

/**
 * 同步元数据
 */
@Serializable
data class SyncMetadata(
    val lastSyncTimestamp: Long,
    val deviceId: String,
    val entityVersions: Map<String, Long> = emptyMap()  // entityId -> version
)

/**
 * 实体数据（用于同步传输）
 */
@Serializable
data class EntityData(
    val fields: Map<String, String?> = emptyMap(),
    val fieldTimestamps: Map<String, Long> = emptyMap(),
    val version: Long = 0
)

/**
 * 同步冲突
 */
data class SyncConflict(
    val entityId: String,
    val localData: EntityData,
    val remoteData: EntityData
)

/**
 * 合并结果
 */
data class MergeResult(
    val mergedData: EntityData,
    val hasConflict: Boolean,
    val conflict: SyncConflict? = null
)

/**
 * 同步结果
 */
data class SyncResult(
    val uploaded: Int = 0,
    val downloaded: Int = 0,
    val conflicts: List<SyncConflict> = emptyList(),
    val success: Boolean = true,
    val error: String? = null
)
