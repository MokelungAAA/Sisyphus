package com.mokelab.sisyphus.feature.achievement

import com.mokelab.sisyphus.core.database.entity.AchievementEntity

/**
 * 成就定义
 * 4类：进度、探索、成绩、彩蛋
 */
object AchievementDefinitions {

    enum class Category(val label: String) {
        PROGRESS("进度"),
        EXPLORE("探索"),
        SCORE("成绩"),
        EASTER_EGG("彩蛋")
    }

    enum class Rarity(val label: String, val color: Long) {
        COMMON("普通", 0xFF9E9E9E),
        RARE("稀有", 0xFF2196F3),
        EPIC("史诗", 0xFF9C27B0),
        LEGENDARY("传说", 0xFFFF9800)
    }

    val all = listOf(
        // ========== 进度类 ==========
        AchievementEntity(
            id = "progress_1h",
            category = Category.PROGRESS.name,
            name = "初入学习之门",
            description = "累计学习时长达到 1 小时",
            iconRes = "ic_timer",
            rarity = Rarity.COMMON.name
        ),
        AchievementEntity(
            id = "progress_10h",
            category = Category.PROGRESS.name,
            name = "学习小能手",
            description = "累计学习时长达到 10 小时",
            iconRes = "ic_timer",
            rarity = Rarity.RARE.name
        ),
        AchievementEntity(
            id = "progress_50h",
            category = Category.PROGRESS.name,
            name = "学习大师",
            description = "累计学习时长达到 50 小时",
            iconRes = "ic_timer",
            rarity = Rarity.EPIC.name
        ),
        AchievementEntity(
            id = "progress_100h",
            category = Category.PROGRESS.name,
            name = "百时成金",
            description = "累计学习时长达到 100 小时",
            iconRes = "ic_timer",
            rarity = Rarity.LEGENDARY.name
        ),
        AchievementEntity(
            id = "progress_10xp",
            category = Category.PROGRESS.name,
            name = "经验新手",
            description = "累计获得 10 XP",
            iconRes = "ic_star",
            rarity = Rarity.COMMON.name
        ),
        AchievementEntity(
            id = "progress_100xp",
            category = Category.PROGRESS.name,
            name = "经验积累者",
            description = "累计获得 100 XP",
            iconRes = "ic_star",
            rarity = Rarity.RARE.name
        ),
        AchievementEntity(
            id = "progress_1000xp",
            category = Category.PROGRESS.name,
            name = "经验大师",
            description = "累计获得 1000 XP",
            iconRes = "ic_star",
            rarity = Rarity.EPIC.name
        ),
        AchievementEntity(
            id = "progress_streak3",
            category = Category.PROGRESS.name,
            name = "三日不辍",
            description = "连续学习 3 天",
            iconRes = "ic_fire",
            rarity = Rarity.COMMON.name
        ),
        AchievementEntity(
            id = "progress_streak7",
            category = Category.PROGRESS.name,
            name = "一周坚持",
            description = "连续学习 7 天",
            iconRes = "ic_fire",
            rarity = Rarity.RARE.name
        ),
        AchievementEntity(
            id = "progress_streak30",
            category = Category.PROGRESS.name,
            name = "月度达人",
            description = "连续学习 30 天",
            iconRes = "ic_fire",
            rarity = Rarity.EPIC.name
        ),
        AchievementEntity(
            id = "progress_streak100",
            category = Category.PROGRESS.name,
            name = "百日如一",
            description = "连续学习 100 天",
            iconRes = "ic_fire",
            rarity = Rarity.LEGENDARY.name
        ),

        // ========== 探索类 ==========
        AchievementEntity(
            id = "explore_first_subject",
            category = Category.EXPLORE.name,
            name = "学科启蒙",
            description = "创建第一个学科",
            iconRes = "ic_book",
            rarity = Rarity.COMMON.name
        ),
        AchievementEntity(
            id = "explore_first_textbook",
            category = Category.EXPLORE.name,
            name = "教材入手",
            description = "添加第一本教材",
            iconRes = "ic_book",
            rarity = Rarity.COMMON.name
        ),
        AchievementEntity(
            id = "explore_first_record",
            category = Category.EXPLORE.name,
            name = "学习启程",
            description = "记录第一条学习记录",
            iconRes = "ic_edit",
            rarity = Rarity.COMMON.name
        ),
        AchievementEntity(
            id = "explore_first_pomodoro",
            category = Category.EXPLORE.name,
            name = "专注开始",
            description = "完成第一个番茄钟",
            iconRes = "ic_timer",
            rarity = Rarity.COMMON.name
        ),
        AchievementEntity(
            id = "explore_first_exam",
            category = Category.EXPLORE.name,
            name = "考试首秀",
            description = "记录第一次考试成绩",
            iconRes = "ic_quiz",
            rarity = Rarity.COMMON.name
        ),
        AchievementEntity(
            id = "explore_first_reading",
            category = Category.EXPLORE.name,
            name = "阅读起步",
            description = "记录第一次阅读",
            iconRes = "ic_book",
            rarity = Rarity.COMMON.name
        ),
        AchievementEntity(
            id = "explore_5_subjects",
            category = Category.EXPLORE.name,
            name = "学科探索者",
            description = "创建 5 个学科",
            iconRes = "ic_book",
            rarity = Rarity.RARE.name
        ),
        AchievementEntity(
            id = "explore_10_pomodoros",
            category = Category.EXPLORE.name,
            name = "番茄达人",
            description = "完成 10 个番茄钟",
            iconRes = "ic_timer",
            rarity = Rarity.RARE.name
        ),
        AchievementEntity(
            id = "explore_search",
            category = Category.EXPLORE.name,
            name = "搜索达人",
            description = "使用搜索功能",
            iconRes = "ic_search",
            rarity = Rarity.COMMON.name
        ),
        AchievementEntity(
            id = "explore_sync",
            category = Category.EXPLORE.name,
            name = "云端同步",
            description = "首次使用云端同步",
            iconRes = "ic_cloud",
            rarity = Rarity.RARE.name
        ),

        // ========== 成绩类 ==========
        AchievementEntity(
            id = "score_first_90",
            category = Category.SCORE.name,
            name = "九十分万岁",
            description = "首次考试得分率达到 90%",
            iconRes = "ic_emoji",
            rarity = Rarity.RARE.name
        ),
        AchievementEntity(
            id = "score_first_100",
            category = Category.SCORE.name,
            name = "满分荣耀",
            description = "首次考试获得满分",
            iconRes = "ic_emoji",
            rarity = Rarity.EPIC.name
        ),
        AchievementEntity(
            id = "score_improve",
            category = Category.SCORE.name,
            name = "进步之星",
            description = "同一学科连续两次考试成绩提升",
            iconRes = "ic_trending_up",
            rarity = Rarity.RARE.name
        ),
        AchievementEntity(
            id = "score_5_exams",
            category = Category.SCORE.name,
            name = "考场常客",
            description = "累计记录 5 次考试",
            iconRes = "ic_quiz",
            rarity = Rarity.COMMON.name
        ),
        AchievementEntity(
            id = "score_20_exams",
            category = Category.SCORE.name,
            name = "身经百战",
            description = "累计记录 20 次考试",
            iconRes = "ic_quiz",
            rarity = Rarity.RARE.name
        ),

        // ========== 彩蛋类 ==========
        AchievementEntity(
            id = "easter_night_owl",
            category = Category.EASTER_EGG.name,
            name = "夜猫子",
            description = "在凌晨 0-5 点记录学习",
            iconRes = "ic_night",
            rarity = Rarity.RARE.name
        ),
        AchievementEntity(
            id = "easter_weekend",
            category = Category.EASTER_EGG.name,
            name = "周末战士",
            description = "在周末记录学习",
            iconRes = "ic_weekend",
            rarity = Rarity.COMMON.name
        ),
        AchievementEntity(
            id = "easter_new_year",
            category = Category.EASTER_EGG.name,
            name = "新年新气象",
            description = "在元旦当天记录学习",
            iconRes = "ic_celebration",
            rarity = Rarity.EPIC.name
        ),
        AchievementEntity(
            id = "easter_birthday",
            category = Category.EASTER_EGG.name,
            name = "生日快乐",
            description = "在生日当天记录学习",
            iconRes = "ic_celebration",
            rarity = Rarity.EPIC.name
        ),
        AchievementEntity(
            id = "easter_perfect_week",
            category = Category.EASTER_EGG.name,
            name = "完美一周",
            description = "一周内每天都学习",
            iconRes = "ic_fire",
            rarity = Rarity.LEGENDARY.name
        )
    )

    /** 获取指定类别的成就 */
    fun getByCategory(category: Category): List<AchievementEntity> {
        return all.filter { it.category == category.name }
    }

    /** 根据ID获取成就定义 */
    fun getById(id: String): AchievementEntity? {
        return all.find { it.id == id }
    }
}
