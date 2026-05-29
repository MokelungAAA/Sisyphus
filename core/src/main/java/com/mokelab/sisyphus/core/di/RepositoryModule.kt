package com.mokelab.sisyphus.core.di

import com.mokelab.sisyphus.core.database.repository.*
import com.mokelab.sisyphus.core.preferences.PomodoroPreferences
import com.mokelab.sisyphus.core.preferences.ThemePreferences
import org.koin.dsl.module

val repositoryModule = module {
    single<SubjectRepository> { SubjectRepositoryImpl(get()) }
    single<TextbookRepository> { TextbookRepositoryImpl(get()) }
    single<ChapterRepository> { ChapterRepositoryImpl(get()) }
    single<SectionRepository> { SectionRepositoryImpl(get()) }
    single<KnowledgePointRepository> { KnowledgePointRepositoryImpl(get()) }
    single<StudyRecordRepository> { StudyRecordRepositoryImpl(get()) }
    single<ReviewCardRepository> { ReviewCardRepositoryImpl(get()) }
    single<ReviewHistoryRepository> { ReviewHistoryRepositoryImpl(get()) }
    single<PomodoroSessionRepository> { PomodoroSessionRepositoryImpl(get()) }
    single<ExamRecordRepository> { ExamRecordRepositoryImpl(get()) }
    single<ReadingRecordRepository> { ReadingRecordRepositoryImpl(get()) }
    single { ThemePreferences(get()) }
    single { PomodoroPreferences(get()) }
}
