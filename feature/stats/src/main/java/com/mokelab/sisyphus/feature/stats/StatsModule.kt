package com.mokelab.sisyphus.feature.stats

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val statsModule = module {
    viewModel { ExamStatsViewModel(get(), get()) }
    viewModel { StudyStatsViewModel(get(), get(), get()) }
    viewModel { LogViewModel(get(), get()) }
}
