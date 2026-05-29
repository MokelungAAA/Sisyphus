package com.mokelab.sisyphus.feature.sync

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 同步生命周期观察者
 * 应用进入后台时自动同步
 */
class SyncLifecycleObserver(
    private val syncManager: SyncManager,
    private val coroutineScope: CoroutineScope
) : DefaultLifecycleObserver {

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        coroutineScope.launch {
            try {
                syncManager.sync()
            } catch (e: Exception) {
                // 静默失败，不影响用户体验
            }
        }
    }
}
