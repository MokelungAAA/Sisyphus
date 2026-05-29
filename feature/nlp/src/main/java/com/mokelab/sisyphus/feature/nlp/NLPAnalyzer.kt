package com.mokelab.sisyphus.feature.nlp

/**
 * NLP分析器（第一层：正则表达式）
 * 离线即时处理，覆盖约70%的常见模式
 */
class NLPAnalyzer {

    /**
     * 分析文本，提取实体和意图
     */
    fun analyze(text: String): NLPResult {
        if (text.isBlank()) {
            return NLPResult(
                entities = emptyMap(),
                intent = StudyType.OTHER,
                confidence = 0f,
                matchedPatterns = emptyList()
            )
        }

        val entities = mutableMapOf<String, String>()
        val matchedPatterns = mutableListOf<MatchedPattern>()

        // 1. 提取时长
        NLPRegexPatterns.DURATION_PATTERN.find(text)?.let { match ->
            val value = match.groupValues[1]
            entities["duration"] = value
            matchedPatterns.add(
                MatchedPattern(
                    type = PatternType.DURATION,
                    value = match.value,
                    startIndex = match.range.first,
                    endIndex = match.range.last + 1
                )
            )
        }

        // 2. 提取章节
        NLPRegexPatterns.CHAPTER_PATTERN.find(text)?.let { match ->
            val value = match.groupValues[1]
            entities["chapter"] = value
            matchedPatterns.add(
                MatchedPattern(
                    type = PatternType.CHAPTER,
                    value = match.value,
                    startIndex = match.range.first,
                    endIndex = match.range.last + 1
                )
            )
        }

        // 3. 提取页码
        NLPRegexPatterns.PAGE_PATTERN.find(text)?.let { match ->
            val startPage = match.groupValues[1]
            val endPage = match.groupValues.getOrNull(2)?.takeIf { it.isNotEmpty() }
            entities["page"] = startPage
            if (endPage != null) {
                entities["pageEnd"] = endPage
            }
            matchedPatterns.add(
                MatchedPattern(
                    type = PatternType.PAGE,
                    value = match.value,
                    startIndex = match.range.first,
                    endIndex = match.range.last + 1
                )
            )
        }

        // 4. 提取考试成绩
        NLPRegexPatterns.EXAM_SCORE_PATTERN.find(text)?.let { match ->
            val score = match.groupValues[1]
            val totalScore = match.groupValues.getOrNull(2)?.takeIf { it.isNotEmpty() }
            entities["score"] = score
            if (totalScore != null) {
                entities["totalScore"] = totalScore
            }
            matchedPatterns.add(
                MatchedPattern(
                    type = PatternType.EXAM_SCORE,
                    value = match.value,
                    startIndex = match.range.first,
                    endIndex = match.range.last + 1
                )
            )
        }

        // 4b. 提取考试类型
        NLPRegexPatterns.matchExamType(text)?.let { examType ->
            entities["examType"] = examType
        }

        // 4c. 检测全真模拟标记
        if (NLPRegexPatterns.isFullMock(text)) {
            entities["isFullMock"] = "true"
        }

        // 5. 提取阅读记录
        NLPRegexPatterns.READING_PATTERN.find(text)?.let { match ->
            val bookName = match.groupValues[1].trim()
            if (bookName.isNotEmpty()) {
                entities["bookName"] = bookName
                matchedPatterns.add(
                    MatchedPattern(
                        type = PatternType.READING,
                        value = match.value,
                        startIndex = match.range.first,
                        endIndex = match.range.last + 1
                    )
                )
            }
        }

        // 5b. 检测听书类型
        NLPRegexPatterns.AUDIOBOOK_PATTERN.find(text)?.let { match ->
            entities["readingType"] = "AUDIOBOOK"
            val bookName = match.groupValues[1].trim()
            if (bookName.isNotEmpty() && !entities.containsKey("bookName")) {
                entities["bookName"] = bookName
            }
        }

        // 6. 识别学习意图
        val intent = classifyIntent(text)

        // 7. 提取学科名称
        NLPRegexPatterns.SUBJECT_PATTERN.find(text)?.let { match ->
            entities["subject"] = match.value
        }

        // 8. 计算置信度
        val confidence = calculateConfidence(matchedPatterns, intent)

        return NLPResult(
            entities = entities,
            intent = intent,
            confidence = confidence,
            matchedPatterns = matchedPatterns
        )
    }

    /**
     * 识别学习意图
     */
    private fun classifyIntent(text: String): StudyType {
        // 按优先级检查
        for ((studyType, pattern) in NLPRegexPatterns.getStudyTypePatterns()) {
            if (pattern.containsMatchIn(text)) {
                return studyType
            }
        }
        return StudyType.OTHER
    }

    /**
     * 计算置信度
     */
    private fun calculateConfidence(
        matchedPatterns: List<MatchedPattern>,
        intent: StudyType
    ): Float {
        var confidence = 0f

        // 匹配到的模式越多，置信度越高
        confidence += matchedPatterns.size * 0.15f

        // 识别到学习意图
        if (intent != StudyType.OTHER) {
            confidence += 0.3f
        }

        // 匹配到时长
        if (matchedPatterns.any { it.type == PatternType.DURATION }) {
            confidence += 0.2f
        }

        return confidence.coerceIn(0f, 1f)
    }

    /**
     * 获取实体显示值
     */
    fun getEntityDisplayName(key: String): String = when (key) {
        "duration" -> "时长"
        "chapter" -> "章节"
        "page" -> "页码"
        "pageEnd" -> "结束页"
        "score" -> "分数"
        "totalScore" -> "总分"
        "bookName" -> "书名"
        "subject" -> "学科"
        "examType" -> "考试类型"
        "isFullMock" -> "全真模拟"
        "readingType" -> "阅读类型"
        else -> key
    }

    /**
     * 格式化实体值
     */
    fun formatEntityValue(key: String, value: String): String = when (key) {
        "duration" -> "${value}分钟"
        "chapter" -> "第${value}章"
        "page" -> "第${value}页"
        "score" -> "${value}分"
        "examType" -> formatExamType(value)
        "isFullMock" -> "是"
        "readingType" -> formatReadingType(value)
        else -> value
    }

    private fun formatExamType(type: String): String = when (type) {
        "MONTHLY" -> "月考"
        "MIDTERM" -> "期中"
        "FINAL" -> "期末"
        "MOCK" -> "模拟考"
        "SIMULATION" -> "小测"
        else -> type
    }

    private fun formatReadingType(type: String): String = when (type) {
        "BOOK" -> "📖 阅读"
        "AUDIOBOOK" -> "🎧 听书"
        "NOTES" -> "📝 笔记"
        else -> type
    }
}
