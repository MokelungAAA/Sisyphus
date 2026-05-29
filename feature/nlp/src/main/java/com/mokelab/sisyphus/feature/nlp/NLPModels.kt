package com.mokelab.sisyphus.feature.nlp

/**
 * NLP分析结果
 */
data class NLPResult(
    val entities: Map<String, String>,
    val intent: StudyType,
    val confidence: Float,
    val matchedPatterns: List<MatchedPattern>
)

/**
 * 匹配到的模式信息
 */
data class MatchedPattern(
    val type: PatternType,
    val value: String,
    val startIndex: Int,
    val endIndex: Int
)

/**
 * 模式类型
 */
enum class PatternType {
    DURATION,      // 学习时长
    CHAPTER,       // 章节
    PAGE,          // 页码
    STUDY_TYPE,    // 学习类型
    EXAM_SCORE,    // 考试成绩
    READING,       // 阅读记录
    SUBJECT,       // 学科
    TEXTBOOK       // 教材
}

/**
 * 学习类型
 */
enum class StudyType(val displayName: String, val keywords: List<String>) {
    COURSE("上课", listOf("上课", "听课", "网课", "课程")),
    EXERCISE("刷题", listOf("刷题", "做题", "练习", "题目")),
    MEMORIZATION("背诵", listOf("背诵", "背", "记忆", "默写")),
    REVIEW("复习", listOf("复习", "回顾", "巩固")),
    NOTE("笔记", listOf("笔记", "记笔记", "整理", "摘抄")),
    READING("阅读", listOf("读书", "阅读", "看书", "读完")),
    EXPERIMENT("实验", listOf("实验", "操作", "实践")),
    OTHER("其他", emptyList())
}
