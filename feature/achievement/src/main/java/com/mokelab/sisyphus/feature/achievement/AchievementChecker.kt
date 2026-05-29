package com.mokelab.sisyphus.feature.achievement

import com.mokelab.sisyphus.core.database.dao.AchievementDao
import com.mokelab.sisyphus.core.database.dao.ExamRecordDao
import com.mokelab.sisyphus.core.database.dao.PomodoroSessionDao
import com.mokelab.sisyphus.core.database.dao.ReadingRecordDao
import com.mokelab.sisyphus.core.database.dao.StudyRecordDao
import com.mokelab.sisyphus.core.database.dao.SubjectDao
import com.mokelab.sisyphus.core.database.entity.ExamRecordEntity
import java.util.Calendar

/**
 * 成就检查器
 * 在关键事件后调用检查是否解锁新成就
 */
class AchievementChecker(
    private val achievementDao: AchievementDao,
    private val studyRecordDao: StudyRecordDao,
    private val subjectDao: SubjectDao,
    private val pomodoroSessionDao: PomodoroSessionDao,
    private val examRecordDao: ExamRecordDao,
    private val readingRecordDao: ReadingRecordDao
) {
    /**
     * 初始化成就表
     * 首次启动时将所有成就定义插入数据库
     */
    suspend fun initializeAchievements() {
        val existing = achievementDao.getAll()
        if (existing.isEmpty()) {
            achievementDao.insertAll(AchievementDefinitions.all)
        }
    }

    /**
     * 学习记录创建后检查
     */
    suspend fun onStudyRecordCreated(): List<String> {
        val unlocked = mutableListOf<String>()

        // 进度类 - 累计时长
        val totalMinutes = studyRecordDao.getTotalMinutes() ?: 0
        val totalHours = totalMinutes / 60.0

        if (totalHours >= 1) unlocked.addAll(tryUnlock("progress_1h"))
        if (totalHours >= 10) unlocked.addAll(tryUnlock("progress_10h"))
        if (totalHours >= 50) unlocked.addAll(tryUnlock("progress_50h"))
        if (totalHours >= 100) unlocked.addAll(tryUnlock("progress_100h"))

        // 进度类 - 累计XP
        val totalXp = studyRecordDao.getTotalXp() ?: 0
        if (totalXp >= 10) unlocked.addAll(tryUnlock("progress_10xp"))
        if (totalXp >= 100) unlocked.addAll(tryUnlock("progress_100xp"))
        if (totalXp >= 1000) unlocked.addAll(tryUnlock("progress_1000xp"))

        // 进度类 - 连续天数
        val streak = calculateStreak()
        if (streak >= 3) unlocked.addAll(tryUnlock("progress_streak3"))
        if (streak >= 7) unlocked.addAll(tryUnlock("progress_streak7"))
        if (streak >= 30) unlocked.addAll(tryUnlock("progress_streak30"))
        if (streak >= 100) unlocked.addAll(tryUnlock("progress_streak100"))

        // 探索类 - 第一条记录
        unlocked.addAll(tryUnlock("explore_first_record"))

        // 彩蛋类 - 时间检查
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hour in 0..5) {
            unlocked.addAll(tryUnlock("easter_night_owl"))
        }

        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            unlocked.addAll(tryUnlock("easter_weekend"))
        }

        // 完美一周检查
        if (checkPerfectWeek()) {
            unlocked.addAll(tryUnlock("easter_perfect_week"))
        }

        return unlocked
    }

    /**
     * 学科创建后检查
     */
    suspend fun onSubjectCreated(): List<String> {
        val unlocked = mutableListOf<String>()
        unlocked.addAll(tryUnlock("explore_first_subject"))

        val subjectCount = subjectDao.getCount()
        if (subjectCount >= 5) {
            unlocked.addAll(tryUnlock("explore_5_subjects"))
        }
        return unlocked
    }

    /**
     * 教材创建后检查
     */
    suspend fun onTextbookCreated(): List<String> {
        return tryUnlock("explore_first_textbook")
    }

    /**
     * 番茄钟完成后检查
     */
    suspend fun onPomodoroCompleted(): List<String> {
        val unlocked = mutableListOf<String>()
        unlocked.addAll(tryUnlock("explore_first_pomodoro"))

        val pomodoroCount = pomodoroSessionDao.getCount()
        if (pomodoroCount >= 10) {
            unlocked.addAll(tryUnlock("explore_10_pomodoros"))
        }
        return unlocked
    }

    /**
     * 考试记录创建后检查
     */
    suspend fun onExamRecordCreated(exam: ExamRecordEntity): List<String> {
        val unlocked = mutableListOf<String>()
        unlocked.addAll(tryUnlock("explore_first_exam"))

        val examCount = examRecordDao.getCount()
        if (examCount >= 5) unlocked.addAll(tryUnlock("score_5_exams"))
        if (examCount >= 20) unlocked.addAll(tryUnlock("score_20_exams"))

        // 成绩类
        if (exam.scoreRate >= 0.9) {
            unlocked.addAll(tryUnlock("score_first_90"))
        }
        if (exam.scoreRate >= 1.0) {
            unlocked.addAll(tryUnlock("score_first_100"))
        }

        return unlocked
    }

    /**
     * 阅读记录创建后检查
     */
    suspend fun onReadingRecordCreated(): List<String> {
        return tryUnlock("explore_first_reading")
    }

    /**
     * 搜索后检查
     */
    suspend fun onSearchPerformed(): List<String> {
        return tryUnlock("explore_search")
    }

    /**
     * 同步完成后检查
     */
    suspend fun onSyncCompleted(): List<String> {
        return tryUnlock("explore_sync")
    }

    /**
     * 尝试解锁成就，返回新解锁的成就ID列表
     */
    private suspend fun tryUnlock(achievementId: String): List<String> {
        val achievement = achievementDao.getById(achievementId) ?: return emptyList()
        if (achievement.unlockedAt != null) return emptyList() // 已解锁

        achievementDao.unlock(achievementId, System.currentTimeMillis())
        return listOf(achievementId)
    }

    /**
     * 计算连续学习天数
     */
    private suspend fun calculateStreak(): Int {
        val records = studyRecordDao.getAllOrderedByDate()
        if (records.isEmpty()) return 0

        val calendar = Calendar.getInstance()
        val today = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        var streak = 0
        var currentDate = today

        val dates = records.map { record ->
            calendar.apply {
                timeInMillis = record.createdAt.toEpochMilliseconds()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            calendar.timeInMillis
        }.distinct().sortedDescending()

        for (date in dates) {
            if (date == currentDate) {
                streak++
                currentDate -= 24 * 60 * 60 * 1000 // 前一天
            } else if (date < currentDate) {
                break
            }
        }

        return streak
    }

    /**
     * 检查完美一周（7天内每天都有学习）
     */
    private suspend fun checkPerfectWeek(): Boolean {
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis

        // 获取7天前的时间戳
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val weekAgo = calendar.timeInMillis

        val records = studyRecordDao.getRecordsBetween(weekAgo, today)
        if (records.isEmpty()) return false

        // 检查每天是否有记录
        val dates = records.map { record ->
            calendar.apply {
                timeInMillis = record.createdAt.toEpochMilliseconds()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            calendar.timeInMillis
        }.distinct()

        return dates.size >= 7
    }
}
