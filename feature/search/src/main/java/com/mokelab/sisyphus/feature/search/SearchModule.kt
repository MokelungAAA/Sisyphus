package com.mokelab.sisyphus.feature.search

import com.mokelab.sisyphus.core.di.KoinModules
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class SearchModule : KoinModules {
    override fun modules() = listOf(
        module {
            viewModel {
                SearchViewModel(
                    context = get(),
                    subjectDao = get(),
                    studyRecordDao = get(),
                    textbookDao = get(),
                    chapterDao = get(),
                    sectionDao = get(),
                    knowledgePointDao = get(),
                    examRecordDao = get(),
                    readingRecordDao = get()
                )
            }
        }
    )
}
