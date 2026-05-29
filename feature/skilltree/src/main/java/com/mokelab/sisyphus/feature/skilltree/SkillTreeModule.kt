package com.mokelab.sisyphus.feature.skilltree

import com.mokelab.sisyphus.core.di.KoinModules
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class SkillTreeModule : KoinModules {
    override fun modules(): List<Module> = listOf(
        module {
            viewModel {
                SkillTreeViewModel(
                    subjectRepository = get(),
                    textbookRepository = get(),
                    chapterRepository = get(),
                    sectionRepository = get(),
                    knowledgePointRepository = get(),
                    reviewCardRepository = get()
                )
            }
        }
    )
}
