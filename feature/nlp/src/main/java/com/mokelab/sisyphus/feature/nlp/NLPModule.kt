package com.mokelab.sisyphus.feature.nlp

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val nlpModule = module {
    single { NLPAnalyzer() }
    single { JiebaAnalyzer(androidContext()) }
    single { LLMAnalyzer() }
    single { NLPManager(androidContext()) }
    viewModel { NLPViewModel(androidContext()) }
}
