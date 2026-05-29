package com.mokelab.sisyphus.feature.nlp

/**
 * NLP正则表达式规则库（第一层）
 * 覆盖约70%的常见模式
 */
object NLPRegexPatterns {

    // 学习时长匹配：学了2小时、学习30分钟、看了1.5h
    val DURATION_PATTERN = Regex(
        """(?:学了|学习|看了|刷了|用了|花了|用了)?\s*(\d+(?:\.\d+)?)\s*(?:小时|个小时|分钟|min|h|小时)"""
    )

    // 章节匹配：第3章、第5节、ch2、chapter3
    val CHAPTER_PATTERN = Regex(
        """(?:第|chapter|ch|Chapter|CH)?\s*(\d+)\s*(?:章|节|课|单元|篇)"""
    )

    // 页码匹配：第10页、p15、page20、10-20页
    val PAGE_PATTERN = Regex(
        """(?:第|p|page|P|Page)?\s*(\d+)(?:\s*[-~到至]\s*(\d+))?\s*(?:页|P|p)"""
    )

    // 学习类型匹配
    val STUDY_TYPE_PATTERNS: Map<StudyType, Regex> = mapOf(
        StudyType.EXERCISE to Regex("""(?:刷题|做题|练习|题目|试卷|真题|模拟题)"""),
        StudyType.MEMORIZATION to Regex("""(?:背诵|背|记忆|默写|记住了)"""),
        StudyType.REVIEW to Regex("""(?:复习|回顾|巩固|重看)"""),
        StudyType.NOTE to Regex("""(?:笔记|记笔记|整理|摘抄|摘录)"""),
        StudyType.READING to Regex("""(?:读书|阅读|看书|读完|读了)"""),
        StudyType.COURSE to Regex("""(?:上课|听课|网课|课程|听讲)"""),
        StudyType.EXPERIMENT to Regex("""(?:实验|操作|实践)""")
    )

    // 考试成绩匹配：考了95分、得分88、120/150
    val EXAM_SCORE_PATTERN = Regex(
        """(?:考了|得分|成绩|分数|拿了)?\s*(\d+(?:\.\d+)?)\s*(?:分)?\s*(?:/\s*(\d+))?"""
    )

    // 阅读记录匹配：读了《三体》、看了百年孤独
    val READING_PATTERN = Regex(
        """(?:读了|听了|看了|读完|阅读了?)\s*(?:《)?(.+?)(?:》)?\s*(?:\d+)?\s*(?:分钟|小时|页)?"""
    )

    // 听书匹配：听了xxx、听书xxx
    val AUDIOBOOK_PATTERN = Regex(
        """(?:听书|听了|听完|有声书|播客)\s*(?:《)?(.+?)(?:》)?"""
    )

    // 学科名称匹配（常见学科）
    val SUBJECT_PATTERN = Regex(
        """(?:数学|语文|英语|物理|化学|生物|历史|地理|政治|信息技术|通用技术|体育|音乐|美术)"""
    )

    // 考试类型匹配：月考、期中、期末、模拟、联考、统考
    val EXAM_TYPE_PATTERN = Regex(
        """(?:月考|期中|期末|模拟|联考|统考|一模|二模|三模|周测|周考|单元测|单元考|阶段考)"""
    )

    // 全真模拟/高考标记：全真模拟、高考真题、真题卷
    val FULL_MOCK_PATTERN = Regex(
        """(?:全真模拟|高考真题|真题卷|高考卷|全国卷|新高考|全国甲卷|全国乙卷)"""
    )

    /**
     * 考试类型关键词到 ExamType 的映射
     */
    fun matchExamType(text: String): String? {
        val match = EXAM_TYPE_PATTERN.find(text) ?: return null
        return when (match.value) {
            "月考" -> "MONTHLY"
            "期中" -> "MIDTERM"
            "期末" -> "FINAL"
            "模拟", "一模", "二模", "三模", "联考", "统考" -> "MOCK"
            "周测", "周考", "单元测", "单元考", "阶段考" -> "SIMULATION"
            else -> "MOCK"
        }
    }

    /**
     * 检测是否为全真模拟
     */
    fun isFullMock(text: String): Boolean = FULL_MOCK_PATTERN.containsMatchIn(text)

    /**
     * 获取所有模式
     */
    fun getAllPatterns(): List<Pair<PatternType, Regex>> = listOf(
        PatternType.DURATION to DURATION_PATTERN,
        PatternType.CHAPTER to CHAPTER_PATTERN,
        PatternType.PAGE to PAGE_PATTERN,
        PatternType.EXAM_SCORE to EXAM_SCORE_PATTERN,
        PatternType.READING to READING_PATTERN
    )

    /**
     * 获取学习类型模式
     */
    fun getStudyTypePatterns(): List<Pair<StudyType, Regex>> =
        STUDY_TYPE_PATTERNS.entries.map { it.key to it.value }
}
