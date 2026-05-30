package com.mokelab.sisyphus.feature.sync

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 同步生命周期观察者
 * 应用进入后台时自动同步
 * 添加防抖机制，避免频繁切换应用时频繁触发同步
 */
class SyncLifecycleObserver(
    private val syncManager: SyncManager
) : DefaultLifecycleObserver {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** 上次同步尝试的时间戳，用于防抖 */
    private var lastSyncAttempt = 0L

    companion object {
        /** 最小同步间隔：5 分钟 */
        private const val MIN_SYNC_INTERVAL_MS = 5 * 60 * 1000L
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)

        // 防抖：距离上次同步不足 5 分钟则跳过
        val now = System.currentTimeMillis()
        if (now - lastSyncAttempt < MIN_SYNC_INTERVAL_MS) return
        lastSyncAttempt = now

        coroutineScope.launch {
            try {
                syncManager.sync()
            } catch (e: Exception) {
                // 静默失败，不影响用户体验
            }
        }
    }
}
