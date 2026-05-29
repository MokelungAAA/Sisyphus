package com.mokelab.sisyphus.feature.nlp

import android.content.Context

/**
 * NLP管理器 - 三层架构整合
 * 第一层：正则表达式（70%，即时）
 * 第二层：jieba分词（20%，离线）
 * 第三层：LLM API（10%，在线）
 */
class NLPManager(private val context: Context) {

    // 三层分析器
    private val regexAnalyzer = NLPAnalyzer()
    private val jiebaAnalyzer = JiebaAnalyzer(context)
    private val llmAnalyzer = LLMAnalyzer()

    // 置信度阈值
    companion object {
        const val HIGH_CONFIDENCE_THRESHOLD = 0.7f  // 高于此值直接使用
        const val MEDIUM_CONFIDENCE_THRESHOLD = 0.4f // 低于此值尝试LLM
    }

    /**
     * 分析文本 - 三层降级策略
     */
    suspend fun analyze(text: String): NLPManagerResult {
        if (text.isBlank()) {
            return NLPManagerResult(
                nlpResult = NLPResult(
                    entities = emptyMap(),
                    intent = StudyType.OTHER,
                    confidence = 0f,
                    matchedPatterns = emptyList()
                ),
                layer = Layer.REGEX,
                needsConfirmation = false
            )
        }

        // 第一层：正则表达式分析
        val regexResult = regexAnalyzer.analyze(text)

        // 高置信度，直接返回
        if (regexResult.confidence >= HIGH_CONFIDENCE_THRESHOLD) {
            return NLPManagerResult(
                nlpResult = regexResult,
                layer = Layer.REGEX,
                needsConfirmation = false
            )
        }

        // 第二层：jieba分词分析
        val jiebaResult = jiebaAnalyzer.analyze(text)
        val mergedResult = mergeResults(regexResult, jiebaResult)

        // 合并后高置信度，返回
        if (mergedResult.confidence >= HIGH_CONFIDENCE_THRESHOLD) {
            return NLPManagerResult(
                nlpResult = mergedResult,
                layer = Layer.JIEBA,
                needsConfirmation = false
            )
        }

        // 第三层：LLM API（仅当置信度低于阈值且API已配置）
        if (mergedResult.confidence < MEDIUM_CONFIDENCE_THRESHOLD && llmAnalyzer.isConfigured()) {
            val llmResult = llmAnalyzer.analyze(text)
            if (llmResult != null) {
                val finalResult = mergeResults(mergedResult, llmResult)
                return NLPManagerResult(
                    nlpResult = finalResult,
                    layer = Layer.LLM,
                    needsConfirmation = true // LLM结果需要确认
                )
            }
        }

        // 中等置信度，需要用户确认
        return NLPManagerResult(
            nlpResult = mergedResult,
            layer = Layer.JIEBA,
            needsConfirmation = mergedResult.confidence < HIGH_CONFIDENCE_THRESHOLD
        )
    }

    /**
     * 合并两层分析结果
     */
    private fun mergeResults(result1: NLPResult, result2: NLPResult): NLPResult {
        // 合并实体（第二层补充第一层缺失的）
        val mergedEntities = result1.entities.toMutableMap()
        for ((key, value) in result2.entities) {
            if (!mergedEntities.containsKey(key)) {
                mergedEntities[key] = value
            }
        }

        // 合并匹配模式
        val mergedPatterns = (result1.matchedPatterns + result2.matchedPatterns)
            .distinctBy { "${it.type}_${it.startIndex}" }

        // 使用更高置信度的结果
        val maxConfidence = maxOf(result1.confidence, result2.confidence)

        // 优先使用非OTHER的意图
        val intent = if (result1.intent != StudyType.OTHER) result1.intent else result2.intent

        return NLPResult(
            entities = mergedEntities,
            intent = intent,
            confidence = maxConfidence,
            matchedPatterns = mergedPatterns
        )
    }

    /**
     * 配置LLM API
     */
    fun configureLLM(provider: LLMAnalyzer.LLMProvider, apiKey: String) {
        llmAnalyzer.configure(provider, apiKey)
    }

    /**
     * 获取分析器状态
     */
    fun getStatus(): String {
        return buildString {
            append("第一层(Regex): 在线\n")
            append("第二层(jieba): 在线\n")
            append("第三层(LLM): ${llmAnalyzer.getProviderInfo()}")
        }
    }

    /**
     * 分析层级
     */
    enum class Layer {
        REGEX,  // 第一层
        JIEBA,  // 第二层
        LLM     // 第三层
    }

    /**
     * 管理器返回结果
     */
    data class NLPManagerResult(
        val nlpResult: NLPResult,
        val layer: Layer,
        val needsConfirmation: Boolean
    )
}
