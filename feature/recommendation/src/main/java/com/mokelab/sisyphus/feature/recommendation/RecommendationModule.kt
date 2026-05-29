package com.mokelab.sisyphus.feature.recommendation

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val recommendationModule = module {
    single { RecommendationEngine() }
    viewModel {
        RecommendationViewModel(
            reviewCardRepository = get(),
            studyRecordRepository = get(),
            knowledgePointRepository = get(),
            subjectRepository = get(),
            engine = get()
        )
    }
}
