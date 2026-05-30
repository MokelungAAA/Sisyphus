package com.mokelab.sisyphus.feature.nlp

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * NLP 模块的 Koin 依赖注入配置
 * NLPViewModel 通过构造函数注入 NLPManager 单例，避免绕过 DI
 */
val nlpModule = module {
    single { NLPAnalyzer() }
    single { JiebaAnalyzer(androidContext()) }
    single { LLMAnalyzer() }
    single { NLPManager(androidContext()) }
    viewModel { NLPViewModel(get()) }
}
