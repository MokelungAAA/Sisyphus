package com.mokelab.sisyphus.core.ui.state

/**
 * UI状态密封接口
 * 统一管理加载/成功/错误三种状态
 */
sealed interface UiState<out T> {
    /** 加载中 */
    data object Loading : UiState<Nothing>

    /** 成功 */
    data class Success<T>(val data: T) : UiState<T>

    /** 错误 */
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>
}

/**
 * 扩展函数：获取成功状态的数据，否则返回null
 */
fun <T> UiState<T>.getOrNull(): T? = when (this) {
    is UiState.Success -> data
    else -> null
}

/**
 * 扩展函数：是否是加载中
 */
fun <T> UiState<T>.isLoading(): Boolean = this is UiState.Loading

/**
 * 扩展函数：是否是错误
 */
fun <T> UiState<T>.isError(): Boolean = this is UiState.Error

/**
 * 扩展函数：是否是成功
 */
fun <T> UiState<T>.isSuccess(): Boolean = this is UiState.Success
