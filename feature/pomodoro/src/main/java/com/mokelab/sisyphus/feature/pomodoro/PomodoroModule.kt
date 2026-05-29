package com.mokelab.sisyphus.feature.pomodoro

import com.mokelab.sisyphus.core.di.KoinModules
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class PomodoroModule : KoinModules {
    override fun modules() = listOf(
        module {
            viewModel { PomodoroViewModel(get(), androidContext(), get()) }
        }
    )
}