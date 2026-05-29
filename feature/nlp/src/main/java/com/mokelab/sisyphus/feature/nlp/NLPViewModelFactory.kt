package com.mokelab.sisyphus.feature.nlp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * NLPViewModel工厂
 * 用于注入Context依赖
 */
class NLPViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NLPViewModel::class.java)) {
            return NLPViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
