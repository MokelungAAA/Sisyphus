package com.mokelab.sisyphus.feature.home

import com.mokelab.sisyphus.core.di.KoinModules
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class HomeModule : KoinModules {
    override fun modules() = listOf(
        module {
            viewModel { HomeViewModel(get(), get(), get()) }
        }
    )
}
