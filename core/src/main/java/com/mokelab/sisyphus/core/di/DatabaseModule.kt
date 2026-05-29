package com.mokelab.sisyphus.core.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mokelab.sisyphus.core.database.SisyphusDatabase
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            get(),
            SisyphusDatabase::class.java,
            "sisyphus.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    single { get<SisyphusDatabase>().subjectDao() }
    single { get<SisyphusDatabase>().textbookDao() }
    single { get<SisyphusDatabase>().chapterDao() }
    single { get<SisyphusDatabase>().sectionDao() }
    single { get<SisyphusDatabase>().knowledgePointDao() }
    single { get<SisyphusDatabase>().studyRecordDao() }
    single { get<SisyphusDatabase>().reviewCardDao() }
    single { get<SisyphusDatabase>().reviewHistoryDao() }
    single { get<SisyphusDatabase>().pomodoroSessionDao() }
    single { get<SisyphusDatabase>().examRecordDao() }
    single { get<SisyphusDatabase>().readingRecordDao() }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `review_history` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `cardId` INTEGER NOT NULL,
                `rating` INTEGER NOT NULL,
                `stabilityBefore` REAL NOT NULL,
                `stabilityAfter` REAL NOT NULL,
                `difficultyBefore` REAL NOT NULL,
                `difficultyAfter` REAL NOT NULL,
                `intervalBefore` INTEGER NOT NULL,
                `intervalAfter` INTEGER NOT NULL,
                `reviewTime` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                FOREIGN KEY(`cardId`) REFERENCES `review_cards`(`id`) ON DELETE CASCADE
            )
        """)
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_review_history_cardId` ON `review_history` (`cardId`)")
    }
}
