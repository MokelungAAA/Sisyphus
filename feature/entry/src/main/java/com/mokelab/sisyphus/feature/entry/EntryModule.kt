package com.mokelab.sisyphus.feature.entry

import com.mokelab.sisyphus.core.di.KoinModules
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class EntryModule : KoinModules {
    override fun modules() = listOf(
        module {
            viewModel { StudyRecordViewModel(get(), get()) }
        }
    )
}
