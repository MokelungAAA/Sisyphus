package com.mokelab.sisyphus.feature.nlp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * NLPViewModel工厂
 * 使用 Koin 获取 NLPManager 单例
 */
class NLPViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NLPViewModel::class.java)) {
            // 通过 Koin 获取 NLPManager 单例
            val nlpManager = org.koin.java.KoinJavaComponent.get<NLPManager>(NLPManager::class.java)
            return NLPViewModel(nlpManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
