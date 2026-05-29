package com.mokelab.sisyphus.feature.sync

/**
 * 冲突解决器
 * 按字段合并，使用时间戳判断哪个版本更新
 */
class ConflictResolver {

    /**
     * 合并本地和远程实体数据
     */
    fun merge(entityId: String, local: EntityData, remote: EntityData): MergeResult {
        val merged = mutableMapOf<String, String?>()
        val mergedTimestamps = mutableMapOf<String, Long>()
        var hasConflict = false

        val allFields = (local.fields.keys + remote.fields.keys).distinct()

        for (field in allFields) {
            val localValue = local.fields[field]
            val remoteValue = remote.fields[field]
            val localTime = local.fieldTimestamps[field] ?: 0
            val remoteTime = remote.fieldTimestamps[field] ?: 0

            when {
                localValue == remoteValue -> {
                    // 值相同，无冲突
                    merged[field] = localValue
                    mergedTimestamps[field] = maxOf(localTime, remoteTime)
                }
                localTime > remoteTime -> {
                    // 本地更新，使用本地值
                    merged[field] = localValue
                    mergedTimestamps[field] = localTime
                }
                remoteTime > localTime -> {
                    // 远程更新，使用远程值
                    merged[field] = remoteValue
                    mergedTimestamps[field] = remoteTime
                }
                else -> {
                    // 同时修改，标记冲突，默认使用本地值
                    merged[field] = localValue
                    mergedTimestamps[field] = localTime
                    hasConflict = true
                }
            }
        }

        val mergedData = EntityData(
            fields = merged,
            fieldTimestamps = mergedTimestamps,
            version = maxOf(local.version, remote.version) + 1
        )

        return MergeResult(
            mergedData = mergedData,
            hasConflict = hasConflict,
            conflict = if (hasConflict) SyncConflict(entityId, local, remote) else null
        )
    }
}
