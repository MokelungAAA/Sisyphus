package com.mokelab.sisyphus.feature.review

import com.mokelab.sisyphus.core.di.KoinModules
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class ReviewModule : KoinModules {
    override fun modules() = listOf(
        module {
            viewModel { ReviewCardViewModel(get()) }
        }
    )
}
