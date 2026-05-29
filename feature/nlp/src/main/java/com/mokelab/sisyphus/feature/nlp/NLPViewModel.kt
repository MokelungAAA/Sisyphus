package com.mokelab.sisyphus.feature.nlp

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NLPInputUiState(
    val text: String = "",
    val nlpResult: NLPResult? = null,
    val isAnalyzing: Boolean = false
)

class NLPViewModel : ViewModel() {

    private val analyzer = NLPAnalyzer()

    private val _uiState = MutableStateFlow(NLPInputUiState())
    val uiState: StateFlow<NLPInputUiState> = _uiState.asStateFlow()

    /**
     * 更新输入文本并进行分析
     */
    fun updateText(text: String) {
        _uiState.update { it.copy(text = text) }

        // 实时分析
        if (text.isNotBlank()) {
            val result = analyzer.analyze(text)
            _uiState.update { it.copy(nlpResult = result) }
        } else {
            _uiState.update { it.copy(nlpResult = null) }
        }
    }

    /**
     * 清空输入
     */
    fun clearText() {
        _uiState.update { NLPInputUiState() }
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
}
