package com.mokelab.sisyphus.feature.nlp

import android.content.Context

/**
 * jieba分词分析器（第二层）
 * 离线处理，约20ms
 * 注意：jieba-android库需要单独集成，此处提供接口定义
 */
class JiebaAnalyzer(private val context: Context) {

    // 自定义词典
    private val customDict = setOf(
        // 学科名称
        "数学", "语文", "英语", "物理", "化学", "生物",
        "历史", "地理", "政治", "信息技术", "通用技术",
        "体育", "音乐", "美术",
        // 教材类型
        "必修", "选修", "选择性必修",
        // 学习动作
        "刷题", "做题", "背诵", "默写", "复习", "听课",
        "看书", "记笔记", "整理", "练习", "巩固",
        // 考试类型
        "月考", "期中", "期末", "模拟", "高考", "联考"
    )

    /**
     * 分析文本（模拟jieba分词）
     * 实际项目中应集成jieba-android库
     */
    fun analyze(text: String): NLPResult {
        val entities = mutableMapOf<String, String>()
        val matchedPatterns = mutableListOf<MatchedPattern>()

        // 1. 实体识别 - 学科
        for (subject in listOf("数学", "语文", "英语", "物理", "化学", "生物", "历史", "地理", "政治")) {
            if (text.contains(subject)) {
                entities["subject"] = subject
                matchedPatterns.add(
                    MatchedPattern(
                        type = PatternType.STUDY_TYPE,
                        value = subject,
                        startIndex = text.indexOf(subject),
                        endIndex = text.indexOf(subject) + subject.length
                    )
                )
                break
            }
        }

        // 2. 意图分类
        val intent = classifyIntent(text)

        // 3. 提取教材信息
        val textbookPatterns = listOf(
            Regex("""(必修|选修|选择性必修)\s*(\d+)"""),
            Regex("""(第\w+册)""")
        )
        for (pattern in textbookPatterns) {
            pattern.find(text)?.let { match ->
                entities["textbook"] = match.value
            }
        }

        // 4. 提取章节
        Regex("""第\s*(\d+)\s*章""").find(text)?.let { match ->
            entities["chapter"] = match.groupValues[1]
        }

        // 5. 计算置信度
        val confidence = calculateConfidence(entities, intent)

        return NLPResult(
            entities = entities,
            intent = intent,
            confidence = confidence,
            matchedPatterns = matchedPatterns
        )
    }

    /**
     * 意图分类
     */
    private fun classifyIntent(text: String): StudyType {
        val intentKeywords = mapOf(
            StudyType.EXERCISE to listOf("刷题", "做题", "练习", "题目", "试卷"),
            StudyType.MEMORIZATION to listOf("背诵", "背", "记忆", "默写"),
            StudyType.REVIEW to listOf("复习", "回顾", "巩固"),
            StudyType.NOTE to listOf("笔记", "记笔记", "整理", "摘抄"),
            StudyType.READING to listOf("读书", "阅读", "看书"),
            StudyType.COURSE to listOf("上课", "听课", "网课", "课程"),
            StudyType.EXPERIMENT to listOf("实验", "操作", "实践")
        )

        for ((studyType, keywords) in intentKeywords) {
            if (keywords.any { text.contains(it) }) {
                return studyType
            }
        }

        return StudyType.OTHER
    }

    /**
     * 计算置信度
     */
    private fun calculateConfidence(
        entities: Map<String, String>,
        intent: StudyType
    ): Float {
        var confidence = 0.3f // 基础置信度

        if (entities.containsKey("subject")) confidence += 0.2f
        if (entities.containsKey("textbook")) confidence += 0.15f
        if (entities.containsKey("chapter")) confidence += 0.15f
        if (intent != StudyType.OTHER) confidence += 0.2f

        return confidence.coerceIn(0f, 1f)
    }

    /**
     * 词性标注（简化版）
     */
    fun posTag(text: String): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()

        // 简化的词性标注
        val words = simpleSegment(text)
        for (word in words) {
            val pos = when {
                customDict.contains(word) -> "n" // 名词
                word.matches(Regex("""\d+""")) -> "m" // 数词
                word.length == 1 && "的了着过".contains(word) -> "u" // 助词
                else -> "v" // 默认动词
            }
            result.add(word to pos)
        }

        return result
    }

    /**
     * 简单分词
     */
    private fun simpleSegment(text: String): List<String> {
        val words = mutableListOf<String>()
        var i = 0

        while (i < text.length) {
            // 尝试匹配最长的词
            var matched = false
            for (len in 4 downTo 2) {
                if (i + len <= text.length) {
                    val word = text.substring(i, i + len)
                    if (customDict.contains(word)) {
                        words.add(word)
                        i += len
                        matched = true
                        break
                    }
                }
            }

            if (!matched) {
                // 单字分词
                words.add(text[i].toString())
                i++
            }
        }

        return words
    }
}
