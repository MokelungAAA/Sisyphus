package com.mokelab.sisyphus.feature.recommendation

/**
 * 推荐类型
 */
enum class RecommendationType(val displayName: String, val icon: String) {
    FSRS_REVIEW("FSRS复习", "🔄"),      // FSRS到期卡片，最高优先级
    WEAK_SUBJECT("薄弱学科", "📊"),      // 薄弱学科补充
    INPUT_ACTIVITY("输入型学习", "📥"),   // 阅读、听课等（30%）
    OUTPUT_ACTIVITY("输出型学习", "📤"),  // 刷题、背诵等（70%）
    NEW_CONTENT("新内容", "📚")           // 新课程、新章节
}

/**
 * 学习活动分类
 */
enum class ActivityCategory {
    INPUT,   // 输入型：阅读、听课、看视频
    OUTPUT   // 输出型：刷题、背诵、默写、笔记
}

/**
 * 推荐项目
 */
data class RecommendationItem(
    val id: String,
    val type: RecommendationType,
    val subjectId: Long,
    val subjectName: String,
    val title: String,
    val description: String,
    val estimatedMinutes: Int,      // 预估时长（分钟）
    val urgencyScore: Float,        // 紧迫度分数 0-1
    val priorityScore: Float,       // 综合优先级分数
    val category: ActivityCategory, // 输入/输出分类
    val metadata: Map<String, String> = emptyMap() // 附加数据
)

/**
 * 推荐结果
 */
data class RecommendationResult(
    val items: List<RecommendationItem>,
    val totalEstimatedMinutes: Int,
    val inputPercentage: Float,     // 输入型占比
    val outputPercentage: Float,    // 输出型占比
    val timeBudgetMinutes: Int      // 时间预算
)

/**
 * 学科权重
 */
data class SubjectWeight(
    val subjectId: Long,
    val subjectName: String,
    val weight: Float,              // 权重 0-10
    val isManual: Boolean,          // 是否手动设置
    val studyMinutes: Long          // 累计学习时长
)

/**
 * 时间预算配置
 */
data class TimeBudgetConfig(
    val dailyMinutes: Int,          // 每日可用时间
    val fsrsPercentage: Float = 0.3f, // FSRS复习占比
    val inputPercentage: Float = 0.3f, // 输入型占比
    val outputPercentage: Float = 0.7f // 输出型占比
)
