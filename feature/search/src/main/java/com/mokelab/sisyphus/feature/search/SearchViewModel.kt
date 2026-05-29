package com.mokelab.sisyphus.feature.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mokelab.sisyphus.core.database.dao.*
import com.mokelab.sisyphus.core.database.entity.*
import com.mokelab.sisyphus.feature.achievement.AchievementChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 搜索结果类型
 */
enum class SearchResultType(val label: String) {
    SUBJECT("学科"),
    STUDY_RECORD("学习记录"),
    TEXTBOOK("教材"),
    CHAPTER("章"),
    SECTION("节"),
    KNOWLEDGE_POINT("知识点"),
    EXAM_RECORD("考试记录"),
    READING_RECORD("阅读记录")
}

/**
 * 搜索结果项
 */
data class SearchResultItem(
    val id: Long,
    val type: SearchResultType,
    val title: String,
    val subtitle: String? = null,
    val subjectId: Long? = null,
    val textbookId: Long? = null,
    val chapterId: Long? = null,
    val sectionId: Long? = null,
)

/**
 * 按类型分组的搜索结果
 */
data class SearchResultGroup(
    val type: SearchResultType,
    val items: List<SearchResultItem>
)

/**
 * 搜索UI状态
 */
data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val resultGroups: List<SearchResultGroup> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val hasSearched: Boolean = false,
)

/**
 * 搜索ViewModel
 */
class SearchViewModel(
    private val context: Context,
    private val subjectDao: SubjectDao,
    private val studyRecordDao: StudyRecordDao,
    private val textbookDao: TextbookDao,
    private val chapterDao: ChapterDao,
    private val sectionDao: SectionDao,
    private val knowledgePointDao: KnowledgePointDao,
    private val examRecordDao: ExamRecordDao,
    private val readingRecordDao: ReadingRecordDao,
    private val achievementChecker: AchievementChecker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    companion object {
        private const val PREFS_NAME = "search_history"
        private const val KEY_HISTORY = "history"
        private const val MAX_HISTORY = 20
    }

    init {
        loadSearchHistory()
    }

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun search(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        _uiState.value = _uiState.value.copy(isLoading = true, hasSearched = true)
        addToHistory(trimmed)

        viewModelScope.launch {
            try {
                val groups = mutableListOf<SearchResultGroup>()

                // 搜索学科
                val subjects = subjectDao.search(trimmed)
                    .filter { PinyinUtils.matches(it.name, trimmed) }
                if (subjects.isNotEmpty()) {
                    groups.add(
                        SearchResultGroup(
                            type = SearchResultType.SUBJECT,
                            items = subjects.map {
                                SearchResultItem(
                                    id = it.id,
                                    type = SearchResultType.SUBJECT,
                                    title = it.name,
                                    subtitle = if (it.isElective) "选修" else "必修",
                                    subjectId = it.id
                                )
                            }
                        )
                    )
                }

                // 搜索教材
                val textbooks = textbookDao.search(trimmed)
                    .filter { PinyinUtils.matches(it.name, trimmed) }
                if (textbooks.isNotEmpty()) {
                    groups.add(
                        SearchResultGroup(
                            type = SearchResultType.TEXTBOOK,
                            items = textbooks.map {
                                SearchResultItem(
                                    id = it.id,
                                    type = SearchResultType.TEXTBOOK,
                                    title = it.name,
                                    subtitle = if (it.type == TextbookType.TUTORIAL) "教辅" else "网课",
                                    subjectId = it.subjectId,
                                    textbookId = it.id
                                )
                            }
                        )
                    )
                }

                // 搜索章
                val chapters = chapterDao.search(trimmed)
                    .filter { PinyinUtils.matches(it.name, trimmed) }
                if (chapters.isNotEmpty()) {
                    groups.add(
                        SearchResultGroup(
                            type = SearchResultType.CHAPTER,
                            items = chapters.map {
                                SearchResultItem(
                                    id = it.id,
                                    type = SearchResultType.CHAPTER,
                                    title = it.name,
                                    subjectId = null,
                                    textbookId = it.textbookId,
                                    chapterId = it.id
                                )
                            }
                        )
                    )
                }

                // 搜索节
                val sections = sectionDao.search(trimmed)
                    .filter { PinyinUtils.matches(it.name, trimmed) }
                if (sections.isNotEmpty()) {
                    groups.add(
                        SearchResultGroup(
                            type = SearchResultType.SECTION,
                            items = sections.map {
                                SearchResultItem(
                                    id = it.id,
                                    type = SearchResultType.SECTION,
                                    title = it.name,
                                    chapterId = it.chapterId,
                                    sectionId = it.id
                                )
                            }
                        )
                    )
                }

                // 搜索知识点
                val knowledgePoints = knowledgePointDao.search(trimmed)
                    .filter { PinyinUtils.matches(it.name, trimmed) || (it.content?.let { c -> PinyinUtils.matches(c, trimmed) } == true) }
                if (knowledgePoints.isNotEmpty()) {
                    groups.add(
                        SearchResultGroup(
                            type = SearchResultType.KNOWLEDGE_POINT,
                            items = knowledgePoints.map {
                                SearchResultItem(
                                    id = it.id,
                                    type = SearchResultType.KNOWLEDGE_POINT,
                                    title = it.name,
                                    subtitle = it.content?.take(50),
                                    sectionId = it.sectionId,
                                    subjectId = null
                                )
                            }
                        )
                    )
                }

                // 搜索学习记录
                val studyRecords = studyRecordDao.search(trimmed)
                    .filter { it.note?.let { n -> PinyinUtils.matches(n, trimmed) } == true }
                if (studyRecords.isNotEmpty()) {
                    groups.add(
                        SearchResultGroup(
                            type = SearchResultType.STUDY_RECORD,
                            items = studyRecords.map {
                                SearchResultItem(
                                    id = it.id,
                                    type = SearchResultType.STUDY_RECORD,
                                    title = it.note?.take(50) ?: "学习记录",
                                    subtitle = "${it.durationMinutes}分钟",
                                    subjectId = it.subjectId
                                )
                            }
                        )
                    )
                }

                // 搜索考试记录
                val examRecords = examRecordDao.search(trimmed)
                    .filter { PinyinUtils.matches(it.examName, trimmed) }
                if (examRecords.isNotEmpty()) {
                    groups.add(
                        SearchResultGroup(
                            type = SearchResultType.EXAM_RECORD,
                            items = examRecords.map {
                                SearchResultItem(
                                    id = it.id,
                                    type = SearchResultType.EXAM_RECORD,
                                    title = it.examName,
                                    subtitle = "${it.score}/${it.totalScore}",
                                    subjectId = it.subjectId
                                )
                            }
                        )
                    )
                }

                // 搜索阅读记录
                val readingRecords = readingRecordDao.search(trimmed)
                    .filter {
                        PinyinUtils.matches(it.bookName, trimmed) ||
                            (it.author?.let { a -> PinyinUtils.matches(a, trimmed) } == true) ||
                            (it.note?.let { n -> PinyinUtils.matches(n, trimmed) } == true)
                    }
                if (readingRecords.isNotEmpty()) {
                    groups.add(
                        SearchResultGroup(
                            type = SearchResultType.READING_RECORD,
                            items = readingRecords.map {
                                SearchResultItem(
                                    id = it.id,
                                    type = SearchResultType.READING_RECORD,
                                    title = it.bookName,
                                    subtitle = it.author ?: it.note?.take(30)
                                )
                            }
                        )
                    )
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    resultGroups = groups
                )
                // 成就检查
                if (groups.isNotEmpty()) {
                    achievementChecker.onSearchPerformed()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    resultGroups = emptyList()
                )
            }
        }
    }

    private fun loadSearchHistory() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, null)
        if (json != null) {
            try {
                val history = Json.decodeFromString<List<String>>(json)
                _uiState.value = _uiState.value.copy(searchHistory = history)
            } catch (_: Exception) {
            }
        }
    }

    private fun addToHistory(query: String) {
        val current = _uiState.value.searchHistory.toMutableList()
        current.remove(query)
        current.add(0, query)
        if (current.size > MAX_HISTORY) {
            current.removeAt(current.lastIndex)
        }
        _uiState.value = _uiState.value.copy(searchHistory = current)
        saveHistory(current)
    }

    fun removeFromHistory(query: String) {
        val current = _uiState.value.searchHistory.toMutableList()
        current.remove(query)
        _uiState.value = _uiState.value.copy(searchHistory = current)
        saveHistory(current)
    }

    fun clearHistory() {
        _uiState.value = _uiState.value.copy(searchHistory = emptyList())
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun saveHistory(history: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_HISTORY, Json.encodeToString(history)).apply()
    }
}
