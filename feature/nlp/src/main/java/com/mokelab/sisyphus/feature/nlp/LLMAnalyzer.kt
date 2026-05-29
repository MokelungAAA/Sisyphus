package com.mokelab.sisyphus.feature.nlp

/**
 * LLM API分析器（第三层）
 * 在线处理，依赖网络
 * 当前两层置信度低于阈值时触发
 */
class LLMAnalyzer {

    // LLM提供商配置
    enum class LLMProvider(val displayName: String, val endpoint: String) {
        GLM_4_FLASH("GLM-4-Flash", "https://open.bigmodel.cn/api/paas/v4/chat/completions"),
        DEEPSEEK_V3("DeepSeek-V3", "https://api.deepseek.com/v1/chat/completions"),
        QWEN_TURBO("Qwen-Turbo", "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions")
    }

    // 当前使用的提供商
    private var currentProvider: LLMProvider = LLMProvider.GLM_4_FLASH
    private var apiKey: String = ""

    /**
     * 设置API配置
     */
    fun configure(provider: LLMProvider, apiKey: String) {
        this.currentProvider = provider
        this.apiKey = apiKey
    }

    /**
     * 分析文本（需要网络）
     * 返回null表示无法分析或API未配置
     */
    suspend fun analyze(text: String): NLPResult? {
        if (apiKey.isEmpty()) {
            return null // API未配置，跳过
        }

        return try {
            // 构建提示词
            val prompt = buildPrompt(text)

            // 调用API（此处为接口定义，实际实现需要网络库）
            val response = callAPI(prompt)

            // 解析响应
            parseResponse(response, text)
        } catch (e: Exception) {
            // API调用失败，返回null让上层使用低置信度结果
            null
        }
    }

    /**
     * 构建提示词
     */
    private fun buildPrompt(text: String): String {
        return """
你是一个学习数据解析助手。请从用户输入中提取以下信息：

1. 学科（subject）：数学、语文、英语、物理、化学、生物、历史、地理、政治等
2. 学习类型（type）：exercise(刷题)、memorization(背诵)、review(复习)、note(笔记)、reading(阅读)、course(上课)、experiment(实验)、other(其他)
3. 教材（textbook）：必修/选修 + 数字
4. 章节（chapter）：章号
5. 页码（page）：页码
6. 时长（duration）：分钟数

请以JSON格式返回：
{
  "subject": "学科名或null",
  "type": "学习类型",
  "textbook": "教材或null",
  "chapter": "章号或null",
  "page": "页码或null",
  "duration": "时长或null",
  "confidence": 0.0-1.0
}

用户输入：$text
""".trimIndent()
    }

    /**
     * 调用LLM API
     * 实际项目中需要使用OkHttp/Retrofit等网络库
     */
    private suspend fun callAPI(prompt: String): String {
        // TODO: 实际API调用实现
        // 需要添加网络依赖并实现HTTP请求
        throw NotImplementedError("LLM API调用需要网络库支持")
    }

    /**
     * 解析API响应
     */
    private fun parseResponse(response: String, originalText: String): NLPResult {
        // TODO: 解析JSON响应
        // 简化实现：返回空结果
        return NLPResult(
            entities = emptyMap(),
            intent = StudyType.OTHER,
            confidence = 0f,
            matchedPatterns = emptyList()
        )
    }

    /**
     * 检查API是否已配置
     */
    fun isConfigured(): Boolean = apiKey.isNotEmpty()

    /**
     * 获取当前提供商信息
     */
    fun getProviderInfo(): String {
        return "${currentProvider.displayName} (${if (isConfigured()) "已配置" else "未配置"})"
    }
}
