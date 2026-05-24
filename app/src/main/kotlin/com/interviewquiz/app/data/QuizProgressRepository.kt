package com.interviewquiz.app.data

import android.content.Context
import android.content.SharedPreferences
import com.interviewquiz.app.domain.model.*
import com.interviewquiz.app.util.AppLogger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 答题进度存储仓库
 */
interface QuizProgressRepository {
    fun getProgress(questionId: Int): QuizProgress
    fun saveProgress(progress: QuizProgress)
    fun getAllProgress(): Map<Int, QuizProgress>
    fun getStatistics(questions: List<Question>): QuizStatistics
    fun clearProgress()
    fun resetQuestionProgress(questionId: Int)
}

@Singleton
class QuizProgressRepositoryImpl @Inject constructor(
    private val context: Context,
    private val appLogger: AppLogger
) : QuizProgressRepository {
    
    companion object {
        private const val PREFS_NAME = "quiz_progress"
        private const val KEY_PROGRESS_PREFIX = "progress_"
        private const val KEY_IS_COMPLETED = "_isCompleted"
        private const val KEY_COMPLETED_AT = "_completedAt"
        private const val KEY_ATTEMPTS = "_attempts"
        private const val KEY_LAST_ANSWER = "_answer"
        private const val KEY_IS_CORRECT = "_correct"
        
        private const val TAG = "QuizProgressRepo"
    }
    
    // 第一性原理：使用 Mutex 确保并发安全
    private val repositoryMutex = Mutex()
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    override fun getProgress(questionId: Int): QuizProgress {
        return repositoryMutex.withLock {
            appLogger.d(TAG, "[Mutex] getProgress for Q$questionId")
            val keyPrefix = KEY_PROGRESS_PREFIX + questionId
            val isCompleted = prefs.getBoolean(keyPrefix + KEY_IS_COMPLETED, false)
            val attempts = prefs.getInt(keyPrefix + KEY_ATTEMPTS, 0)
            val lastAnswer = if (prefs.contains(keyPrefix + KEY_LAST_ANSWER)) {
                prefs.getInt(keyPrefix + KEY_LAST_ANSWER, -1).takeIf { it != -1 }
            } else null
            val isCorrect = if (prefs.contains(keyPrefix + KEY_IS_CORRECT)) {
                prefs.getBoolean(keyPrefix + KEY_IS_CORRECT, false)
            } else null
            val completedAt = if (isCompleted && prefs.contains(keyPrefix + KEY_COMPLETED_AT)) {
                prefs.getLong(keyPrefix + KEY_COMPLETED_AT, 0L).takeIf { it > 0 }
            } else null
            
            // 使用安全创建方法，自动修复可能的无效数据
            return@withLock com.interviewquiz.app.domain.model.QuizProgress.safeCreate(
                questionId = questionId,
                isCompleted = isCompleted,
                lastAnswer = lastAnswer,
                isCorrect = isCorrect,
                completedAt = completedAt,
                attempts = attempts
            )
        }
    }
    
    override fun saveProgress(progress: QuizProgress) {
        repositoryMutex.withLock {
            appLogger.d(TAG, "[Mutex] saveProgress for Q${progress.questionId}")
            try {
                // 使用安全创建方法验证和修复数据
                val validatedProgress = com.interviewquiz.app.domain.model.QuizProgress.safeCreate(
                    questionId = progress.questionId,
                    isCompleted = progress.isCompleted,
                    lastAnswer = progress.lastAnswer,
                    isCorrect = progress.isCorrect,
                    completedAt = progress.completedAt,
                    attempts = progress.attempts
                )
                
                val editor = prefs.edit()
                val keyPrefix = KEY_PROGRESS_PREFIX + validatedProgress.questionId
                
                editor.putBoolean(keyPrefix + KEY_IS_COMPLETED, validatedProgress.isCompleted)
                editor.putInt(keyPrefix + KEY_ATTEMPTS, validatedProgress.attempts)
                
                if (validatedProgress.lastAnswer != null) {
                    editor.putInt(keyPrefix + KEY_LAST_ANSWER, validatedProgress.lastAnswer)
                } else {
                    editor.remove(keyPrefix + KEY_LAST_ANSWER)
                }
                
                if (validatedProgress.isCorrect != null) {
                    editor.putBoolean(keyPrefix + KEY_IS_CORRECT, validatedProgress.isCorrect)
                } else {
                    editor.remove(keyPrefix + KEY_IS_CORRECT)
                }
                
                if (validatedProgress.isCompleted && validatedProgress.completedAt != null) {
                    editor.putLong(keyPrefix + KEY_COMPLETED_AT, validatedProgress.completedAt)
                } else {
                    editor.remove(keyPrefix + KEY_COMPLETED_AT)
                }
                
                editor.apply()
                appLogger.d(TAG, "Saved progress for question ${validatedProgress.questionId}")
            } catch (e: Exception) {
                appLogger.e(TAG, "Error saving progress for question ${progress.questionId}", e)
                // 即使保存失败，也尝试保存最基本的数据
                try {
                    val editor = prefs.edit()
                    val keyPrefix = KEY_PROGRESS_PREFIX + progress.questionId
                    editor.putInt(keyPrefix + KEY_ATTEMPTS, progress.attempts.coerceAtLeast(0))
                    editor.apply()
                } catch (e2: Exception) {
                    appLogger.e(TAG, "Critical error: cannot save any progress", e2)
                }
            }
        }
    }
    
    override fun getAllProgress(): Map<Int, QuizProgress> {
        return repositoryMutex.withLock {
            appLogger.d(TAG, "[Mutex] getAllProgress")
            return@withLock try {
                val allEntries = prefs.all
                val progressMap = mutableMapOf<Int, QuizProgress>()
                
                // 提取所有 questionId，带安全检查
                val questionIds = allEntries.keys
                    .mapNotNull { key ->
                        if (key.startsWith(KEY_PROGRESS_PREFIX)) {
                            try {
                                key.removePrefix(KEY_PROGRESS_PREFIX).split('_').firstOrNull()?.toIntOrNull()
                            } catch (e: NumberFormatException) {
                                null
                            }
                        } else null
                    }
                    .filterNotNull()
                    .distinct()
                
                // 限制处理数量，防止极端大的数据集
                val safeIds = if (questionIds.size > 1000) {
                    appLogger.w(TAG, "Too many progress entries (${questionIds.size}), limiting to 1000")
                    questionIds.take(1000)
                } else {
                    questionIds
                }
                
                // 注意：这里 getProgress 已经加锁了，所以要小心！
                // 避免嵌套锁，直接读取数据
                safeIds.forEach { questionId ->
                    try {
                        // 直接读取数据，避免嵌套锁
                        val keyPrefix = KEY_PROGRESS_PREFIX + questionId
                        val isCompleted = prefs.getBoolean(keyPrefix + KEY_IS_COMPLETED, false)
                        val attempts = prefs.getInt(keyPrefix + KEY_ATTEMPTS, 0)
                        val lastAnswer = if (prefs.contains(keyPrefix + KEY_LAST_ANSWER)) {
                            prefs.getInt(keyPrefix + KEY_LAST_ANSWER, -1).takeIf { it != -1 }
                        } else null
                        val isCorrect = if (prefs.contains(keyPrefix + KEY_IS_CORRECT)) {
                            prefs.getBoolean(keyPrefix + KEY_IS_CORRECT, false)
                        } else null
                        val completedAt = if (isCompleted && prefs.contains(keyPrefix + KEY_COMPLETED_AT)) {
                            prefs.getLong(keyPrefix + KEY_COMPLETED_AT, 0L).takeIf { it > 0 }
                        } else null
                        
                        // 使用安全创建方法
                        progressMap[questionId] = com.interviewquiz.app.domain.model.QuizProgress.safeCreate(
                            questionId = questionId,
                            isCompleted = isCompleted,
                            lastAnswer = lastAnswer,
                            isCorrect = isCorrect,
                            completedAt = completedAt,
                            attempts = attempts
                        )
                    } catch (e: Exception) {
                        appLogger.w(TAG, "Skipping invalid progress for question $questionId", e)
                    }
                }
                
                progressMap
            } catch (e: Exception) {
                appLogger.e(TAG, "Error loading all progress", e)
                emptyMap()
            }
        }
    }
    
    override fun getStatistics(questions: List<Question>): QuizStatistics {
        return repositoryMutex.withLock {
            appLogger.d(TAG, "[Mutex] getStatistics for ${questions.size} questions")
            val allProgress = try {
                // 直接读取，避免调用 getAllProgress（嵌套锁）
                val allEntries = prefs.all
                val progressMap = mutableMapOf<Int, QuizProgress>()
                
                val questionIds = allEntries.keys
                    .mapNotNull { key ->
                        if (key.startsWith(KEY_PROGRESS_PREFIX)) {
                            try {
                                key.removePrefix(KEY_PROGRESS_PREFIX).split('_').firstOrNull()?.toIntOrNull()
                            } catch (e: NumberFormatException) {
                                null
                            }
                        } else null
                    }
                    .filterNotNull()
                    .distinct()
                    .take(1000)
                
                questionIds.forEach { questionId ->
                    try {
                        val keyPrefix = KEY_PROGRESS_PREFIX + questionId
                        val isCompleted = prefs.getBoolean(keyPrefix + KEY_IS_COMPLETED, false)
                        val attempts = prefs.getInt(keyPrefix + KEY_ATTEMPTS, 0)
                        val lastAnswer = if (prefs.contains(keyPrefix + KEY_LAST_ANSWER)) {
                            prefs.getInt(keyPrefix + KEY_LAST_ANSWER, -1).takeIf { it != -1 }
                        } else null
                        val isCorrect = if (prefs.contains(keyPrefix + KEY_IS_CORRECT)) {
                            prefs.getBoolean(keyPrefix + KEY_IS_CORRECT, false)
                        } else null
                        val completedAt = if (isCompleted && prefs.contains(keyPrefix + KEY_COMPLETED_AT)) {
                            prefs.getLong(keyPrefix + KEY_COMPLETED_AT, 0L).takeIf { it > 0 }
                        } else null
                        
                        // 使用安全创建方法
                        progressMap[questionId] = com.interviewquiz.app.domain.model.QuizProgress.safeCreate(
                            questionId = questionId,
                            isCompleted = isCompleted,
                            lastAnswer = lastAnswer,
                            isCorrect = isCorrect,
                            completedAt = completedAt,
                            attempts = attempts
                        )
                    } catch (e: Exception) {
                        appLogger.w(TAG, "Skipping invalid progress for question $questionId", e)
                    }
                }
                
                progressMap
            } catch (e: Exception) {
                appLogger.e(TAG, "Error loading progress for statistics", e)
                emptyMap()
            }
            
            var totalCompleted = 0
            var totalCorrect = 0
            var totalAttempts = 0
            var lastActivity: Long? = null
            
            val categoryStatsMap = mutableMapOf<String, CategoryStats>()
            val difficultyStatsMap = mutableMapOf<String, DifficultyStats>()
            
            // 初始化分类和难度统计
            try {
                questions.groupBy { it.category }.forEach { (category, qs) ->
                    categoryStatsMap[category] = CategoryStats(
                        category = category,
                        totalQuestions = qs.size
                    )
                }
                questions.groupBy { it.difficulty }.forEach { (difficulty, qs) ->
                    difficultyStatsMap[difficulty] = DifficultyStats(
                        difficulty = difficulty,
                        totalQuestions = qs.size
                    )
                }
            } catch (e: Exception) {
                appLogger.e(TAG, "Error initializing statistics", e)
            }
            
            // 计算统计数据
            questions.forEach { question ->
                val progress = allProgress[question.id]
                
                if (progress != null && progress.isCompleted) {
                    // 安全更新统计，防止异常
                    try {
                        totalCompleted++
                        totalAttempts += progress.attempts.coerceAtLeast(0)
                        
                        if (progress.isCorrect == true) {
                            totalCorrect++
                        }
                        
                        // 更新分类统计（安全更新）
                        categoryStatsMap[question.category]?.let { categoryStat ->
                            try {
                                val newCompleted = (categoryStat.completedQuestions + 1).coerceAtMost(categoryStat.totalQuestions)
                                val newCorrect = categoryStat.correctAnswers + if (progress.isCorrect == true) 1 else 0
                                val finalCorrect = newCorrect.coerceAtMost(newCompleted)
                                
                                categoryStatsMap[question.category] = categoryStat.copy(
                                    completedQuestions = newCompleted,
                                    correctAnswers = finalCorrect
                                )
                            } catch (e: Exception) {
                                appLogger.e(TAG, "Error updating category stats for ${question.category}", e)
                            }
                        }
                        
                        // 更新难度统计（安全更新）
                        difficultyStatsMap[question.difficulty]?.let { difficultyStat ->
                            try {
                                val newCompleted = (difficultyStat.completedQuestions + 1).coerceAtMost(difficultyStat.totalQuestions)
                                val newCorrect = difficultyStat.correctAnswers + if (progress.isCorrect == true) 1 else 0
                                val finalCorrect = newCorrect.coerceAtMost(newCompleted)
                                
                                difficultyStatsMap[question.difficulty] = difficultyStat.copy(
                                    completedQuestions = newCompleted,
                                    correctAnswers = finalCorrect
                                )
                            } catch (e: Exception) {
                                appLogger.e(TAG, "Error updating difficulty stats for ${question.difficulty}", e)
                            }
                        }
                        
                        // 更新最后活动时间
                        progress.completedAt?.let { time ->
                            if (time > 0 && (lastActivity == null || time > lastActivity)) {
                                lastActivity = time
                            }
                        }
                    } catch (e: Exception) {
                        appLogger.e(TAG, "Error processing progress for question ${question.id}", e)
                    }
                }
            }
            
            // 安全创建统计数据
            return@withLock try {
                QuizStatistics(
                    totalQuestions = questions.size,
                    completedQuestions = totalCompleted.coerceAtMost(questions.size),
                    correctAnswers = totalCorrect.coerceAtMost(totalCompleted),
                    totalAttempts = totalAttempts.coerceAtLeast(0),
                    categoryStats = categoryStatsMap,
                    difficultyStats = difficultyStatsMap,
                    lastActivity = lastActivity
                )
            } catch (e: Exception) {
                appLogger.e(TAG, "Error creating statistics object", e)
                QuizStatistics(totalQuestions = questions.size)
            }
        }
    }
    
    override fun clearProgress() {
        repositoryMutex.withLock {
            appLogger.d(TAG, "[Mutex] clearProgress")
            prefs.edit().clear().apply()
            appLogger.d(TAG, "Cleared all progress")
        }
    }
    
    override fun resetQuestionProgress(questionId: Int) {
        repositoryMutex.withLock {
            appLogger.d(TAG, "[Mutex] resetQuestionProgress for Q$questionId")
            val keyPrefix = KEY_PROGRESS_PREFIX + questionId
            val editor = prefs.edit()
            
            // 移除所有相关的键
            editor.remove(keyPrefix + KEY_IS_COMPLETED)
            editor.remove(keyPrefix + KEY_COMPLETED_AT)
            editor.remove(keyPrefix + KEY_ATTEMPTS)
            editor.remove(keyPrefix + KEY_LAST_ANSWER)
            editor.remove(keyPrefix + KEY_IS_CORRECT)
            
            editor.apply()
            appLogger.d(TAG, "Reset progress for question $questionId")
        }
    }
}
