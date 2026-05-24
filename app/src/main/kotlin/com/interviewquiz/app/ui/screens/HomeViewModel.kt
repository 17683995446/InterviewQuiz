package com.interviewquiz.app.ui.screens

import androidx.lifecycle.viewModelScope
import com.interviewquiz.app.data.Question
import com.interviewquiz.app.domain.model.QuizProgress
import com.interviewquiz.app.domain.model.QuizStatistics
import com.interviewquiz.app.domain.usecase.*
import com.interviewquiz.app.ui.base.BaseViewModel
import com.interviewquiz.app.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import kotlin.math.min

data class HomeUiState(
    val allQuestions: List<Question> = emptyList(),
    val filteredQuestions: List<Question> = emptyList(),
    val selectedCategory: String? = null,
    val selectedDifficulty: String? = null,
    val showFilters: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val categories: List<String> = emptyList(),
    val difficulties: List<String> = emptyList(),
    val totalQuestionCount: Int = 0,
    val statistics: QuizStatistics = QuizStatistics(),
    val progressMap: Map<Int, QuizProgress> = emptyMap(),
    val showStatistics: Boolean = false,
    val lastStateVersion: Long = 0L
)

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val filterQuestionsUseCase: FilterQuestionsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getDifficultiesUseCase: GetDifficultiesUseCase,
    private val getQuestionCountUseCase: GetQuestionCountUseCase,
    private val getStatisticsUseCase: GetStatisticsUseCase,
    private val getAllProgressUseCase: GetAllProgressUseCase,
    private val recordAnswerUseCase: RecordAnswerUseCase,
    private val clearProgressUseCase: ClearProgressUseCase,
    private val resetQuestionProgressUseCase: ResetQuestionProgressUseCase
) : BaseViewModel() {
    
    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
        private const val TAG = "HomeViewModel"
        private const val MAX_PROGRESS_ENTRIES = 1000
    }
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    // 第一性原理：状态更新必须是原子的，防止竞态条件
    private val stateMutex = Mutex()
    
    init {
        loadQuestions() // 新的统一加载序列，包含 progress 和 stats
        observeFiltersAndApply()
    }
    
    /**
     * 第一性原理：原子状态更新，防止竞态条件
     * 使用 Mutex 确保所有状态修改都是原子操作
     * 包含状态验证和自动修复机制
     */
    private suspend fun updateStateAtomically(
        description: String,
        update: (HomeUiState) -> HomeUiState
    ) {
        stateMutex.withLock {
            val oldState = _uiState.value
            var newState = update(oldState)
                .copy(lastStateVersion = oldState.lastStateVersion + 1)
            
            // 验证状态一致性并自动修复
            newState = validateStateTransition(oldState, newState, description)
            
            _uiState.value = newState
            
            AppLogger.d(TAG, "State updated: $description (version: ${newState.lastStateVersion})")
        }
    }
    
    /**
     * 第一性原理：状态转换验证，防止无效状态
     * 包含极端情况下的状态修复和恢复能力
     */
    private fun validateStateTransition(
        oldState: HomeUiState,
        newState: HomeUiState,
        description: String
    ): HomeUiState {
        var repairedState = newState
        var needsRepair = false
        
        // 验证 progressMap 不会无限增长
        if (newState.progressMap.size > MAX_PROGRESS_ENTRIES) {
            AppLogger.w(TAG, "progressMap size exceeds limit: ${newState.progressMap.size}, repairing...")
            repairedState = repairedState.copy(progressMap = pruneProgressMapIfNeeded(repairedState.progressMap))
            needsRepair = true
        }
        
        // 验证 question 列表不为空时 count 一致
        if (newState.allQuestions.isNotEmpty()) {
            if (newState.totalQuestionCount != 0 && 
                newState.allQuestions.size != newState.totalQuestionCount) {
                AppLogger.w(TAG, "Question count mismatch: ${newState.allQuestions.size} vs ${newState.totalQuestionCount}, repairing...")
                repairedState = repairedState.copy(totalQuestionCount = newState.allQuestions.size)
                needsRepair = true
            }
        }
        
        // 验证 lastStateVersion 严格递增
        if (newState.lastStateVersion <= oldState.lastStateVersion) {
            AppLogger.w(TAG, "State version not increasing: ${oldState.lastStateVersion} -> ${newState.lastStateVersion}, repairing...")
            repairedState = repairedState.copy(lastStateVersion = oldState.lastStateVersion + 1)
            needsRepair = true
        }
        
        // 验证 filteredQuestions 是 allQuestions 的子集
        if (newState.allQuestions.isNotEmpty() && newState.filteredQuestions.isNotEmpty()) {
            val allIds = newState.allQuestions.map { it.id }.toSet()
            val invalidFiltered = newState.filteredQuestions.any { !allIds.contains(it.id) }
            if (invalidFiltered) {
                AppLogger.w(TAG, "filteredQuestions contains invalid items, repairing...")
                repairedState = repairedState.copy(filteredQuestions = newState.allQuestions)
                needsRepair = true
            }
        }
        
        // 验证 statistics 合理性
        if (newState.statistics.completedQuestions > newState.allQuestions.size) {
            AppLogger.w(TAG, "completedQuestions exceeds totalQuestions, repairing...")
            val safeStats = newState.statistics.copy(
                completedQuestions = newState.allQuestions.size,
                correctAnswers = newState.statistics.correctAnswers.coerceAtMost(newState.allQuestions.size)
            )
            repairedState = repairedState.copy(statistics = safeStats)
            needsRepair = true
        }
        
        if (needsRepair) {
            AppLogger.w(TAG, "State repaired for: $description")
        }
        
        return repairedState
    }
    
    /**
     * 第一性原理：限制 progressMap 大小，防止内存泄漏
     */
    private fun pruneProgressMapIfNeeded(map: Map<Int, QuizProgress>): Map<Int, QuizProgress> {
        if (map.size <= MAX_PROGRESS_ENTRIES) return map
        
        AppLogger.w(TAG, "Pruning progressMap from ${map.size} to $MAX_PROGRESS_ENTRIES")
        
        // 优先保留已完成的条目，然后按 questionId 排序
        val completedEntries = map.entries.filter { it.value.isCompleted }
        val uncompletedEntries = map.entries.filterNot { it.value.isCompleted }
        
        // 合并并保留前 MAX_PROGRESS_ENTRIES 个
        val combined = (completedEntries + uncompletedEntries).take(MAX_PROGRESS_ENTRIES)
        return combined.associate { it.key to it.value }
    }
    
    private fun loadQuestions() {
        launchSafe(
            block = {
                AppLogger.d(TAG, "[Flow] Starting initial data load sequence")
                
                updateStateAtomically("start_loading") { state ->
                    state.copy(isLoading = true)
                }
                
                AppLogger.d(TAG, "[Data] Loading categories and questions...")
                val categories = getCategoriesUseCase()
                val difficulties = getDifficultiesUseCase()
                val questions = getQuestionsUseCase()
                val count = getQuestionCountUseCase()
                AppLogger.d(TAG, "[Data] Loaded ${questions.size} questions")
                
                // 第一步：先加载进度数据
                AppLogger.d(TAG, "[Data] Loading progress map...")
                val progressMap = getAllProgressUseCase()
                val prunedMap = pruneProgressMapIfNeeded(progressMap)
                AppLogger.d(TAG, "[Data] Loaded ${prunedMap.size} progress entries")
                
                // 第二步：再计算统计数据（需要progressMap）
                val stats = getStatisticsUseCase(questions)
                AppLogger.d(TAG, "[Stats] Calculated statistics: ${stats.completedQuestions} completed, ${stats.correctAnswers} correct")
                
                // 第三步：原子性地更新所有状态
                updateStateAtomically("initial_load_complete") { state ->
                    state.copy(
                        allQuestions = questions,
                        filteredQuestions = questions,
                        isLoading = false,
                        categories = categories,
                        difficulties = difficulties,
                        totalQuestionCount = count,
                        progressMap = prunedMap,
                        statistics = stats
                    )
                }
                
                AppLogger.d(TAG, "[Flow] Initial data load completed successfully")
            },
            onError = { exception ->
                AppLogger.e(TAG, "[Error] Failed initial data load", exception)
                updateStateAtomically("load_error") { state ->
                    state.copy(
                        error = exception.message,
                        isLoading = false
                    )
                }
            },
            jobKey = "initial_load"
        )
    }
    
    private fun loadProgress() {
        AppLogger.w(TAG, "[Legacy] loadProgress() is deprecated, progress is now loaded in initial sequence")
        launchSafe(
            block = {
                val progressMap = getAllProgressUseCase()
                val prunedMap = pruneProgressMapIfNeeded(progressMap)
                updateStateAtomically("load_progress") { state ->
                    state.copy(progressMap = prunedMap)
                }
            },
            onError = { e ->
                AppLogger.e(TAG, "Error loading progress", e)
            },
            jobKey = "load_progress"
        )
    }
    
    private fun loadStatistics() {
        AppLogger.w(TAG, "[Legacy] loadStatistics() is deprecated, stats are now calculated in initial sequence")
        launchSafe(
            block = {
                updateStateAtomically("load_statistics_prepare") { state ->
                    if (state.allQuestions.isNotEmpty()) {
                        val stats = getStatisticsUseCase(state.allQuestions)
                        state.copy(statistics = stats)
                    } else {
                        state
                    }
                }
            },
            onError = { e ->
                AppLogger.e(TAG, "Error loading statistics", e)
            },
            jobKey = "load_statistics"
        )
    }
    
    private fun observeFiltersAndApply() {
        launchSafe(
            block = {
                AppLogger.d(TAG, "[Filter] Starting filter observation stream")
                
                _uiState
                    .distinctUntilChanged { old, new ->
                        old.selectedCategory == new.selectedCategory &&
                        old.selectedDifficulty == new.selectedDifficulty &&
                        old.searchQuery == new.searchQuery
                    }
                    .debounce(SEARCH_DEBOUNCE_MS)
                    .collect { state ->
                        try {
                            if (state.allQuestions.isNotEmpty()) {
                                AppLogger.d(TAG, "[Filter] Applying - category:${state.selectedCategory}, difficulty:${state.selectedDifficulty}, query:'${state.searchQuery.take(30)}...'")
                                
                                // 安全限制搜索查询长度
                                val safeQuery = if (state.searchQuery.length > 500) {
                                    AppLogger.w(TAG, "[Filter] Search query too long (${state.searchQuery.length} chars), truncating")
                                    state.searchQuery.take(500)
                                } else {
                                    state.searchQuery
                                }
                                
                                applyFilters(state.selectedCategory, state.selectedDifficulty, safeQuery)
                            }
                        } catch (e: Exception) {
                            AppLogger.e(TAG, "[Filter] Error applying filters", e)
                        }
                    }
            },
            onError = { e ->
                AppLogger.e(TAG, "[Error] Filter observation failed", e)
            },
            jobKey = "filter_observer"
        )
    }
    
    fun toggleFilters() {
        viewModelScope.launch {
            updateStateAtomically("toggle_filters") { state ->
                state.copy(showFilters = !state.showFilters)
            }
        }
    }
    
    fun toggleStatistics() {
        viewModelScope.launch {
            var shouldLoadStats = false
            
            updateStateAtomically("toggle_statistics") { state ->
                val newShow = !state.showStatistics
                shouldLoadStats = newShow
                state.copy(showStatistics = newShow)
            }
            
            if (shouldLoadStats) {
                loadStatistics()
            }
        }
    }
    
    fun selectCategory(category: String?) {
        viewModelScope.launch {
            updateStateAtomically("select_category") { state ->
                if (state.selectedCategory != category) {
                    state.copy(selectedCategory = category)
                } else {
                    state
                }
            }
        }
    }
    
    fun selectDifficulty(difficulty: String?) {
        viewModelScope.launch {
            updateStateAtomically("select_difficulty") { state ->
                if (state.selectedDifficulty != difficulty) {
                    state.copy(selectedDifficulty = difficulty)
                } else {
                    state
                }
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        viewModelScope.launch {
            updateStateAtomically("update_search_query") { state ->
                if (state.searchQuery != query) {
                    state.copy(searchQuery = query)
                } else {
                    state
                }
            }
        }
    }
    
    fun resetFilters() {
        viewModelScope.launch {
            updateStateAtomically("reset_filters") { state ->
                state.copy(
                    selectedCategory = null,
                    selectedDifficulty = null,
                    searchQuery = "",
                    filteredQuestions = state.allQuestions
                )
            }
        }
    }
    
    fun recordAnswer(question: Question, answerIndex: Int) {
        AppLogger.d(TAG, "[Answer] Recording answer for Q${question.id}: answerIndex=$answerIndex")
        
        launchSafe(
            block = {
                val result = recordAnswerUseCase(question, answerIndex)
                AppLogger.d(TAG, "[Answer] Result: isCorrect=${result.isCorrect}")
                
                updateStateAtomically("record_answer_q${question.id}") { state ->
                    // 同时更新 progressMap 和 statistics，确保数据一致性
                    val newProgressMap = state.progressMap.toMutableMap()
                    newProgressMap[question.id] = result.progress
                    val prunedMap = pruneProgressMapIfNeeded(newProgressMap)
                    
                    val newStats = if (state.allQuestions.isNotEmpty()) {
                        getStatisticsUseCase(state.allQuestions)
                    } else {
                        state.statistics
                    }
                    
                    AppLogger.d(TAG, "[Stats] Updated: ${newStats.completedQuestions} completed, ${newStats.correctAnswers} correct")
                    
                    state.copy(
                        progressMap = prunedMap,
                        statistics = newStats
                    )
                }
                
                AppLogger.d(TAG, "[Answer] Successfully recorded for Q${question.id}")
            },
            onError = { e ->
                AppLogger.e(TAG, "[Error] Failed to record answer for Q${question.id}", e)
            },
            jobKey = "record_answer_${question.id}"
        )
    }
    
    fun clearAllProgress() {
        launchSafe(
            block = {
                clearProgressUseCase()
                updateStateAtomically("clear_all_progress") { state ->
                    // 同时清空 progressMap 和 重置 statistics，确保数据一致性
                    state.copy(
                        progressMap = emptyMap(),
                        statistics = QuizStatistics()
                    )
                }
                AppLogger.d(TAG, "Cleared all progress")
            },
            onError = { e ->
                AppLogger.e(TAG, "Error clearing progress", e)
            },
            jobKey = "clear_progress"
        )
    }
    
    fun resetQuestionProgress(questionId: Int) {
        launchSafe(
            block = {
                resetQuestionProgressUseCase(questionId)
                updateStateAtomically("reset_progress_q$questionId") { state ->
                    // 同时更新 progressMap 和 statistics，确保数据一致性
                    val newProgressMap = state.progressMap.toMutableMap()
                    newProgressMap.remove(questionId)
                    
                    val newStats = if (state.allQuestions.isNotEmpty()) {
                        getStatisticsUseCase(state.allQuestions)
                    } else {
                        state.statistics
                    }
                    
                    state.copy(
                        progressMap = newProgressMap,
                        statistics = newStats
                    )
                }
            },
            onError = { e ->
                AppLogger.e(TAG, "Error resetting question progress", e)
            },
            jobKey = "reset_progress_$questionId"
        )
    }
    
    fun getProgress(questionId: Int): QuizProgress? {
        // 第一性原理：读取操作也应该是一致的
        // 虽然不修改状态，但确保读取到的是一致的版本
        return _uiState.value.progressMap[questionId]
    }
    
    fun getCategories(): List<String> = _uiState.value.categories
    
    fun getDifficulties(): List<String> = _uiState.value.difficulties
    
    private fun applyFilters(category: String?, difficulty: String?, query: String) {
        launchSafe(
            block = {
                updateStateAtomically("apply_filters") { state ->
                    if (state.allQuestions.isEmpty()) {
                        return@updateStateAtomically state
                    }
                    
                    val filtered = filterQuestionsUseCase(
                        category = category,
                        difficulty = difficulty,
                        searchQuery = query
                    )
                    
                    state.copy(filteredQuestions = filtered)
                }
            },
            jobKey = "apply_filters"
        )
    }
    
    fun clearError() {
        viewModelScope.launch {
            updateStateAtomically("clear_error") { state ->
                if (state.error != null) {
                    state.copy(error = null)
                } else {
                    state
                }
            }
        }
    }
    
    override fun handleException(exception: Throwable) {
        AppLogger.e(TAG, "Unhandled exception", exception)
        viewModelScope.launch {
            updateStateAtomically("handle_exception") { state ->
                state.copy(
                    error = exception.message,
                    isLoading = false
                )
            }
        }
    }
}
