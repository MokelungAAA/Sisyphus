package com.mokelab.sisyphus.feature.exam

import com.mokelab.sisyphus.core.di.KoinModules
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class ExamModule : KoinModules {
    override fun modules() = listOf(
        module {
            viewModel { ExamRecordViewModel(get(), get()) }
        }
    )
}
