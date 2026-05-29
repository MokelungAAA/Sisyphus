package com.mokelab.sisyphus.feature.achievement

import com.mokelab.sisyphus.core.di.KoinModules
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class AchievementModule : KoinModules {
    override fun modules() = listOf(
        module {
            single {
                AchievementChecker(
                    achievementDao = get(),
                    studyRecordDao = get(),
                    subjectDao = get(),
                    pomodoroSessionDao = get(),
                    examRecordDao = get(),
                    readingRecordDao = get()
                )
            }
            viewModel { AchievementViewModel(get()) }
        }
    )
}
