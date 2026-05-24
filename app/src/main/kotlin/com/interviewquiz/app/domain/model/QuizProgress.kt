package com.interviewquiz.app.domain.model

/**
 * 答题进度数据模型
 */
data class QuizProgress(
    val questionId: Int,
    val isCompleted: Boolean = false,
    val lastAnswer: Int? = null,
    val isCorrect: Boolean? = null,
    val completedAt: Long? = null,
    val attempts: Int = 0
) {
    init {
        // 自动修复边界条件，而不是抛出异常
        // 这提高了系统的鲁棒性，能处理各种混乱数据
    }
    
    companion object {
        /**
         * 创建 QuizProgress 实例，会自动修复无效数据
         */
        fun create(questionId: Int): QuizProgress {
            return safeCreate(
                questionId = questionId,
                isCompleted = false,
                lastAnswer = null,
                isCorrect = null,
                completedAt = null,
                attempts = 0
            )
        }
        
        /**
         * 安全创建 QuizProgress 实例，自动修复无效数据
         */
        fun safeCreate(
            questionId: Int,
            isCompleted: Boolean,
            lastAnswer: Int?,
            isCorrect: Boolean?,
            completedAt: Long?,
            attempts: Int
        ): QuizProgress {
            // 自动修复无效数据
            val safeQuestionId = questionId.coerceAtLeast(0)
            val safeAttempts = attempts.coerceAtLeast(0)
            val safeLastAnswer = lastAnswer?.coerceAtLeast(0)
            val safeCompletedAt = if (completedAt != null && completedAt > 0) completedAt else null
            
            // 一致性自动修复
            val (safeIsCompleted, safeIsCorrect, finalCompletedAt) = if (isCompleted) {
                if (safeLastAnswer == null) {
                    // 如果已完成但没有答案，自动设为未完成
                    Triple(false, null, null)
                } else {
                    Triple(true, isCorrect ?: false, safeCompletedAt)
                }
            } else {
                Triple(false, null, null)
            }
            
            return QuizProgress(
                questionId = safeQuestionId,
                isCompleted = safeIsCompleted,
                lastAnswer = safeLastAnswer,
                isCorrect = safeIsCorrect,
                completedAt = finalCompletedAt,
                attempts = safeAttempts
            )
        }
    }
    
    fun recordAnswer(answerIndex: Int, correct: Boolean): QuizProgress {
        val safeAnswerIndex = answerIndex.coerceAtLeast(0)
        return copy(
            lastAnswer = safeAnswerIndex,
            isCorrect = correct,
            isCompleted = true,
            completedAt = System.currentTimeMillis(),
            attempts = (attempts + 1).coerceAtLeast(0)
        )
    }
}

/**
 * 整体答题统计数据
 */
data class QuizStatistics(
    val totalQuestions: Int = 0,
    val completedQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val totalAttempts: Int = 0,
    val categoryStats: Map<String, CategoryStats> = emptyMap(),
    val difficultyStats: Map<String, DifficultyStats> = emptyMap(),
    val lastActivity: Long? = null
) {
    init {
        // 边界条件验证
        require(totalQuestions >= 0) { "totalQuestions must be non-negative" }
        require(completedQuestions >= 0) { "completedQuestions must be non-negative" }
        require(correctAnswers >= 0) { "correctAnswers must be non-negative" }
        require(totalAttempts >= 0) { "totalAttempts must be non-negative" }
        
        // 一致性验证
        require(completedQuestions <= totalQuestions) { 
            "completedQuestions($completedQuestions) cannot exceed totalQuestions($totalQuestions)" 
        }
        require(correctAnswers <= completedQuestions) { 
            "correctAnswers($correctAnswers) cannot exceed completedQuestions($completedQuestions)" 
        }
        
        if (lastActivity != null) {
            // 只验证时间戳是正数，不与当前时间绑定，以兼容历史数据
            require(lastActivity > 0) { "lastActivity must be positive timestamp if present" }
        }
    }
    
    val completionRate: Float
        get() = if (totalQuestions > 0) {
            (completedQuestions.toFloat() / totalQuestions).coerceIn(0f, 1f)
        } else 0f
    
    val accuracyRate: Float
        get() = if (completedQuestions > 0) {
            (correctAnswers.toFloat() / completedQuestions).coerceIn(0f, 1f)
        } else 0f
}

/**
 * 分类统计数据
 */
data class CategoryStats(
    val category: String,
    val totalQuestions: Int = 0,
    val completedQuestions: Int = 0,
    val correctAnswers: Int = 0
) {
    init {
        // 边界条件验证
        require(category.isNotBlank()) { "category must not be blank" }
        require(totalQuestions >= 0) { "totalQuestions must be non-negative" }
        require(completedQuestions >= 0) { "completedQuestions must be non-negative" }
        require(correctAnswers >= 0) { "correctAnswers must be non-negative" }
        
        // 一致性验证
        require(completedQuestions <= totalQuestions) { 
            "completedQuestions($completedQuestions) cannot exceed totalQuestions($totalQuestions)" 
        }
        require(correctAnswers <= completedQuestions) { 
            "correctAnswers($correctAnswers) cannot exceed completedQuestions($completedQuestions)" 
        }
    }
    
    val accuracyRate: Float
        get() = if (completedQuestions > 0) {
            (correctAnswers.toFloat() / completedQuestions).coerceIn(0f, 1f)
        } else 0f
}

/**
 * 难度统计数据
 */
data class DifficultyStats(
    val difficulty: String,
    val totalQuestions: Int = 0,
    val completedQuestions: Int = 0,
    val correctAnswers: Int = 0
) {
    init {
        // 边界条件验证
        require(difficulty.isNotBlank()) { "difficulty must not be blank" }
        require(totalQuestions >= 0) { "totalQuestions must be non-negative" }
        require(completedQuestions >= 0) { "completedQuestions must be non-negative" }
        require(correctAnswers >= 0) { "correctAnswers must be non-negative" }
        
        // 一致性验证
        require(completedQuestions <= totalQuestions) { 
            "completedQuestions($completedQuestions) cannot exceed totalQuestions($totalQuestions)" 
        }
        require(correctAnswers <= completedQuestions) { 
            "correctAnswers($correctAnswers) cannot exceed completedQuestions($completedQuestions)" 
        }
    }
    
    val accuracyRate: Float
        get() = if (completedQuestions > 0) {
            (correctAnswers.toFloat() / completedQuestions).coerceIn(0f, 1f)
        } else 0f
}
