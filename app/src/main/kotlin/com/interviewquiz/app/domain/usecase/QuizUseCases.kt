package com.interviewquiz.app.domain.usecase

import com.interviewquiz.app.data.Question
import com.interviewquiz.app.data.QuizProgressRepository
import com.interviewquiz.app.domain.model.*
import com.interviewquiz.app.util.AppLogger
import javax.inject.Inject

/**
 * 记录答题的 UseCase
 */
class RecordAnswerUseCase @Inject constructor(
    private val progressRepository: QuizProgressRepository,
    private val appLogger: AppLogger
) {
    companion object {
        private const val TAG = "RecordAnswerUC"
    }
    
    operator fun invoke(question: Question, answerIndex: Int): RecordAnswerResult {
        val isCorrect = question.isCorrectAnswer(answerIndex)
        val oldProgress = progressRepository.getProgress(question.id)
        val newProgress = oldProgress.recordAnswer(answerIndex, isCorrect)
        
        progressRepository.saveProgress(newProgress)
        
        appLogger.d(TAG, "Recorded answer for Q${question.id}: isCorrect=$isCorrect")
        
        return RecordAnswerResult(
            questionId = question.id,
            isCorrect = isCorrect,
            progress = newProgress
        )
    }
}

data class RecordAnswerResult(
    val questionId: Int,
    val isCorrect: Boolean,
    val progress: QuizProgress
)

/**
 * 获取答题进度的 UseCase
 */
class GetQuizProgressUseCase @Inject constructor(
    private val progressRepository: QuizProgressRepository
) {
    operator fun invoke(questionId: Int): QuizProgress =
        progressRepository.getProgress(questionId)
}

/**
 * 获取统计数据的 UseCase
 */
class GetStatisticsUseCase @Inject constructor(
    private val progressRepository: QuizProgressRepository
) {
    operator fun invoke(questions: List<Question>): QuizStatistics =
        progressRepository.getStatistics(questions)
}

/**
 * 清除进度的 UseCase
 */
class ClearProgressUseCase @Inject constructor(
    private val progressRepository: QuizProgressRepository
) {
    operator fun invoke() {
        progressRepository.clearProgress()
    }
}

/**
 * 重置单个题目进度的 UseCase
 */
class ResetQuestionProgressUseCase @Inject constructor(
    private val progressRepository: QuizProgressRepository
) {
    operator fun invoke(questionId: Int) {
        progressRepository.resetQuestionProgress(questionId)
    }
}

/**
 * 获取所有进度的 UseCase
 */
class GetAllProgressUseCase @Inject constructor(
    private val progressRepository: QuizProgressRepository
) {
    operator fun invoke(): Map<Int, QuizProgress> =
        progressRepository.getAllProgress()
}
