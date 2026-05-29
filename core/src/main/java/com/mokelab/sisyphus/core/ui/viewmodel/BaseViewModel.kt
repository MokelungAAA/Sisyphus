package com.mokelab.sisyphus.core.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel基类
 * 提供通用的协程异常处理和状态管理
 */
abstract class BaseViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * 协程异常处理器
     * 子类可覆盖自定义错误处理
     */
    protected open val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _errorMessage.value = throwable.message ?: "Unknown error"
        _isLoading.value = false
    }

    /**
     * 在viewModelScope中启动协程
     * 自动处理加载状态和异常
     */
    protected fun launchWithLoading(block: suspend () -> Unit) {
        viewModelScope.launch(coroutineExceptionHandler) {
            _isLoading.value = true
            try {
                block()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
