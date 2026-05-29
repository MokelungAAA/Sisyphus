package com.mokelab.sisyphus.feature.subject

import com.mokelab.sisyphus.core.di.KoinModules
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class SubjectModule : KoinModules {
    override fun modules() = listOf(
        module {
            viewModel { SubjectViewModel(get(), get()) }
            viewModel { TextbookViewModel(get()) }
            viewModel { ChapterViewModel(get()) }
            viewModel { SectionViewModel(get()) }
            viewModel { KnowledgePointViewModel(get()) }
            viewModel { SubjectDetailViewModel(get(), get(), get(), get(), get(), get(), get()) }
        }
    )
}
