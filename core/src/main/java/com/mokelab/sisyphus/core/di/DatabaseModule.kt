package com.mokelab.sisyphus.core.di

import androidx.room.Room
import com.mokelab.sisyphus.core.database.SisyphusDatabase
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            get(),
            SisyphusDatabase::class.java,
            "sisyphus.db"
        ).build()
    }

    single { get<SisyphusDatabase>().subjectDao() }
    single { get<SisyphusDatabase>().textbookDao() }
    single { get<SisyphusDatabase>().chapterDao() }
    single { get<SisyphusDatabase>().sectionDao() }
    single { get<SisyphusDatabase>().knowledgePointDao() }
    single { get<SisyphusDatabase>().studyRecordDao() }
    single { get<SisyphusDatabase>().reviewCardDao() }
    single { get<SisyphusDatabase>().pomodoroSessionDao() }
    single { get<SisyphusDatabase>().examRecordDao() }
    single { get<SisyphusDatabase>().readingRecordDao() }
}
