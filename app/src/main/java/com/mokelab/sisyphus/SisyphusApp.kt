package com.mokelab.sisyphus

import android.app.Application
import com.mokelab.sisyphus.core.di.databaseModule
import com.mokelab.sisyphus.core.di.repositoryModule
import com.mokelab.sisyphus.feature.entry.EntryModule
import com.mokelab.sisyphus.feature.exam.ExamModule
import com.mokelab.sisyphus.feature.home.HomeModule
import com.mokelab.sisyphus.feature.pomodoro.PomodoroModule
import com.mokelab.sisyphus.feature.nlp.nlpModule
import com.mokelab.sisyphus.feature.recommendation.recommendationModule
import com.mokelab.sisyphus.feature.reading.ReadingModule
import com.mokelab.sisyphus.feature.review.ReviewModule
import com.mokelab.sisyphus.feature.achievement.AchievementModule
import com.mokelab.sisyphus.feature.settings.SettingsModule
import com.mokelab.sisyphus.feature.search.SearchModule
import com.mokelab.sisyphus.feature.stats.statsModule
import com.mokelab.sisyphus.feature.subject.SubjectModule
import com.mokelab.sisyphus.feature.sync.syncModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class SisyphusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@SisyphusApp)
            modules(
                databaseModule,
                repositoryModule,
                *SubjectModule().modules().toTypedArray(),
                *HomeModule().modules().toTypedArray(),
                *EntryModule().modules().toTypedArray(),
                *PomodoroModule().modules().toTypedArray(),
                *ExamModule().modules().toTypedArray(),
                *ReadingModule().modules().toTypedArray(),
                *ReviewModule().modules().toTypedArray(),
                *SearchModule().modules().toTypedArray(),
                *AchievementModule().modules().toTypedArray(),
                *SettingsModule().modules().toTypedArray(),
                nlpModule,
                recommendationModule,
                statsModule,
                syncModule
            )
        }
    }
}
