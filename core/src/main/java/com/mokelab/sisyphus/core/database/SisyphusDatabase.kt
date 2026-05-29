package com.mokelab.sisyphus.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mokelab.sisyphus.core.database.converter.Converters
import com.mokelab.sisyphus.core.database.dao.ChapterDao
import com.mokelab.sisyphus.core.database.dao.ExamRecordDao
import com.mokelab.sisyphus.core.database.dao.KnowledgePointDao
import com.mokelab.sisyphus.core.database.dao.PomodoroSessionDao
import com.mokelab.sisyphus.core.database.dao.ReadingRecordDao
import com.mokelab.sisyphus.core.database.dao.ReviewCardDao
import com.mokelab.sisyphus.core.database.dao.ReviewHistoryDao
import com.mokelab.sisyphus.core.database.dao.SectionDao
import com.mokelab.sisyphus.core.database.dao.StudyRecordDao
import com.mokelab.sisyphus.core.database.dao.SubjectDao
import com.mokelab.sisyphus.core.database.dao.TextbookDao
import com.mokelab.sisyphus.core.database.entity.ChapterEntity
import com.mokelab.sisyphus.core.database.entity.ExamRecordEntity
import com.mokelab.sisyphus.core.database.entity.KnowledgePointEntity
import com.mokelab.sisyphus.core.database.entity.PomodoroSessionEntity
import com.mokelab.sisyphus.core.database.entity.ReadingRecordEntity
import com.mokelab.sisyphus.core.database.entity.ReviewCardEntity
import com.mokelab.sisyphus.core.database.entity.ReviewHistoryEntity
import com.mokelab.sisyphus.core.database.entity.SectionEntity
import com.mokelab.sisyphus.core.database.entity.StudyRecordEntity
import com.mokelab.sisyphus.core.database.entity.SubjectEntity
import com.mokelab.sisyphus.core.database.entity.TextbookEntity

@Database(
    entities = [
        SubjectEntity::class,
        TextbookEntity::class,
        ChapterEntity::class,
        SectionEntity::class,
        KnowledgePointEntity::class,
        StudyRecordEntity::class,
        ReviewCardEntity::class,
        ReviewHistoryEntity::class,
        PomodoroSessionEntity::class,
        ExamRecordEntity::class,
        ReadingRecordEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class SisyphusDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun textbookDao(): TextbookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun sectionDao(): SectionDao
    abstract fun knowledgePointDao(): KnowledgePointDao
    abstract fun studyRecordDao(): StudyRecordDao
    abstract fun reviewCardDao(): ReviewCardDao
    abstract fun reviewHistoryDao(): ReviewHistoryDao
    abstract fun pomodoroSessionDao(): PomodoroSessionDao
    abstract fun examRecordDao(): ExamRecordDao
    abstract fun readingRecordDao(): ReadingRecordDao
}
