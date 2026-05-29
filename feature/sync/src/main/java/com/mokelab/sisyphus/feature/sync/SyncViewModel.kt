package com.mokelab.sisyphus.feature.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class SyncUiState(
    val isAuthenticated: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncTime: String = "从未同步",
    val message: String? = null,
    val error: String? = null,
    val uploaded: Int = 0,
    val downloaded: Int = 0,
    val conflicts: List<SyncConflict> = emptyList()
)

class SyncViewModel(
    private val authManager: AuthManager,
    private val syncService: SyncService,
    private val tokenStorage: TokenStorage,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val isAuth = authManager.isAuthenticated()
        val lastSync = tokenStorage.getLastSyncTime()
        val lastSyncStr = if (lastSync > 0) {
            val dt = Instant.fromEpochMilliseconds(lastSync)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            "${dt.date} ${dt.hour}:${dt.minute.toString().padStart(2, '0')}"
        } else {
            "从未同步"
        }

        _uiState.value = _uiState.value.copy(
            isAuthenticated = isAuth,
            lastSyncTime = lastSyncStr
        )
    }

    /**
     * 处理 OAuth 回调
     */
    fun handleAuthCallback(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, error = null)
            val result = authManager.handleAuthCallback(code)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = true,
                        isSyncing = false,
                        message = "认证成功"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        error = "认证失败: ${e.message}"
                    )
                }
            )
        }
    }

    /**
     * 上传数据
     */
    fun uploadData(data: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, error = null, message = null)
            val result = syncService.uploadData(data)
            result.fold(
                onSuccess = {
                    checkAuthState()
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        message = "同步成功"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        error = "同步失败: ${e.message}"
                    )
                }
            )
        }
    }

    /**
     * 下载数据
     */
    fun downloadData(onDataReceived: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, error = null, message = null)
            val result = syncService.downloadData()
            result.fold(
                onSuccess = { data ->
                    checkAuthState()
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        message = "下载成功"
                    )
                    onDataReceived(data)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        error = "下载失败: ${e.message}"
                    )
                }
            )
        }
    }

    /**
     * 手动同步
     */
    fun manualSync() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, error = null, message = null)
            val result = syncManager.sync()
            if (result.success) {
                checkAuthState()
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    message = "同步完成: 上传 ${result.uploaded}, 下载 ${result.downloaded}",
                    uploaded = result.uploaded,
                    downloaded = result.downloaded,
                    conflicts = result.conflicts
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    error = "同步失败: ${result.error}"
                )
            }
        }
    }

    /**
     * 清除冲突
     */
    fun clearConflicts() {
        syncManager.clearConflicts()
        _uiState.value = _uiState.value.copy(conflicts = emptyList())
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        authManager.disconnect()
        _uiState.value = _uiState.value.copy(
            isAuthenticated = false,
            message = "已断开连接"
        )
    }

    /**
     * 清除消息
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
}
