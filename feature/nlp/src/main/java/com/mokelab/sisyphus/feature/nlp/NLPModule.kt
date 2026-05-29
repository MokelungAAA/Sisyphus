package com.mokelab.sisyphus.feature.nlp

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val nlpModule = module {
    single { NLPAnalyzer() }
    viewModel { NLPViewModel() }
}
