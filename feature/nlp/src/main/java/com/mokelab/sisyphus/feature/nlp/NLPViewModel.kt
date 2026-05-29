package com.mokelab.sisyphus.feature.nlp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NLPInputUiState(
    val text: String = "",
    val nlpResult: NLPResult? = null,
    val isAnalyzing: Boolean = false,
    val layer: NLPManager.Layer = NLPManager.Layer.REGEX,
    val needsConfirmation: Boolean = false,
    val showConfirmationSheet: Boolean = false,
    val editedEntities: Map<String, String> = emptyMap()
)

class NLPViewModel(
    private val context: Context
) : ViewModel() {

    private val nlpManager = NLPManager(context)

    private val _uiState = MutableStateFlow(NLPInputUiState())
    val uiState: StateFlow<NLPInputUiState> = _uiState.asStateFlow()

    /**
     * 更新输入文本并进行分析
     */
    fun updateText(text: String) {
        _uiState.update { it.copy(text = text, isAnalyzing = true) }

        viewModelScope.launch {
            if (text.isNotBlank()) {
                val result = nlpManager.analyze(text)
                _uiState.update {
                    it.copy(
                        nlpResult = result.nlpResult,
                        layer = result.layer,
                        needsConfirmation = result.needsConfirmation,
                        editedEntities = result.nlpResult.entities,
                        isAnalyzing = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        nlpResult = null,
                        isAnalyzing = false,
                        editedEntities = emptyMap()
                    )
                }
            }
        }
    }

    /**
     * 清空输入
     */
    fun clearText() {
        _uiState.update { NLPInputUiState() }
    }

    /**
     * 显示确认面板
     */
    fun showConfirmation() {
        _uiState.update { it.copy(showConfirmationSheet = true) }
    }

    /**
     * 隐藏确认面板
     */
    fun hideConfirmation() {
        _uiState.update { it.copy(showConfirmationSheet = false) }
    }

    /**
     * 更新编辑的实体
     */
    fun updateEntity(key: String, value: String) {
        _uiState.update {
            it.copy(editedEntities = it.editedEntities + (key to value))
        }
    }

    /**
     * 删除实体
     */
    fun removeEntity(key: String) {
        _uiState.update {
            it.copy(editedEntities = it.editedEntities - key)
        }
    }

    /**
     * 确认并提交
     * 返回编辑后的实体数据
     */
    fun confirmAndSubmit(): Map<String, String> {
        val entities = _uiState.value.editedEntities
        hideConfirmation()
        return entities
    }

    /**
     * 获取格式化的结果摘要
     */
    fun getResultSummary(): String {
        val result = _uiState.value.nlpResult ?: return ""

        val parts = mutableListOf<String>()

        result.entities["duration"]?.let {
            parts.add("时长: ${it}分钟")
        }
        result.entities["subject"]?.let {
            parts.add("学科: $it")
        }
        result.entities["chapter"]?.let {
            parts.add("章节: 第${it}章")
        }
        result.entities["page"]?.let {
            val end = result.entities["pageEnd"]
            if (end != null) {
                parts.add("页码: ${it}-${end}页")
            } else {
                parts.add("页码: 第${it}页")
            }
        }
        result.entities["score"]?.let {
            val total = result.entities["totalScore"]
            if (total != null) {
                parts.add("成绩: ${it}/${total}分")
            } else {
                parts.add("成绩: ${it}分")
            }
        }
        result.entities["bookName"]?.let {
            parts.add("书名: $it")
        }

        return parts.joinToString("\n")
    }

    /**
     * 获取层级显示名
     */
    fun getLayerDisplayName(): String = when (_uiState.value.layer) {
        NLPManager.Layer.REGEX -> "正则匹配"
        NLPManager.Layer.JIEBA -> "智能分词"
        NLPManager.Layer.LLM -> "AI分析"
    }

    /**
     * 配置LLM API
     */
    fun configureLLM(provider: LLMAnalyzer.LLMProvider, apiKey: String) {
        nlpManager.configureLLM(provider, apiKey)
    }
}
