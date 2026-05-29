package com.mokelab.sisyphus.feature.reading

import com.mokelab.sisyphus.core.di.KoinModules
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class ReadingModule : KoinModules {
    override fun modules() = listOf(
        module {
            viewModel { ReadingRecordViewModel(get()) }
        }
    )
}
