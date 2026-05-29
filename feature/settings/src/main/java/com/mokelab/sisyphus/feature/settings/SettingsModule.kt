package com.mokelab.sisyphus.feature.settings

import com.mokelab.sisyphus.core.di.KoinModules
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class SettingsModule : KoinModules {
    override fun modules() = listOf(
        module {
            viewModel { DataExportImportViewModel(get(), get()) }
        }
    )
}
