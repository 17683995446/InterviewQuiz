package com.interviewquiz.app.domain.model

import org.junit.Assert.*
import org.junit.Test
import org.junit.Before

class QuizProgressTest {
    
    @Test(expected = IllegalArgumentException::class)
    fun `QuizProgress should reject negative questionId`() {
        QuizProgress(questionId = -1)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `QuizProgress should reject negative attempts`() {
        QuizProgress(questionId = 1, attempts = -1)
    }
    
    @Test
    fun `QuizProgress should create valid default progress`() {
        val progress = QuizProgress(questionId = 1)
        assertEquals(1, progress.questionId)
        assertFalse(progress.isCompleted)
        assertNull(progress.lastAnswer)
        assertNull(progress.isCorrect)
        assertNull(progress.completedAt)
        assertEquals(0, progress.attempts)
    }
    
    @Test
    fun `QuizProgress should record answer correctly`() {
        val progress = QuizProgress(questionId = 1)
        val updated = progress.recordAnswer(answerIndex = 2, correct = true)
        
        assertEquals(2, updated.lastAnswer)
        assertTrue(updated.isCorrect)
        assertTrue(updated.isCompleted)
        assertNotNull(updated.completedAt)
        assertEquals(1, updated.attempts)
    }
    
    @Test
    fun `QuizProgress should increment attempts on each record`() {
        val progress = QuizProgress(questionId = 1)
        val step1 = progress.recordAnswer(answerIndex = 0, correct = false)
        val step2 = step1.recordAnswer(answerIndex = 1, correct = false)
        val step3 = step2.recordAnswer(answerIndex = 2, correct = true)
        
        assertEquals(1, step1.attempts)
        assertEquals(2, step2.attempts)
        assertEquals(3, step3.attempts)
    }
    
    @Test
    fun `QuizProgress should preserve last answer after multiple attempts`() {
        val progress = QuizProgress(questionId = 1)
        val updated = progress
            .recordAnswer(answerIndex = 0, correct = false)
            .recordAnswer(answerIndex = 1, correct = true)
        
        assertEquals(1, updated.lastAnswer)
        assertTrue(updated.isCorrect)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `QuizProgress recordAnswer should reject negative index`() {
        val progress = QuizProgress(questionId = 1)
        progress.recordAnswer(answerIndex = -1, correct = false)
    }
}

class QuizStatisticsTest {
    
    @Test
    fun `QuizStatistics should calculate completion rate correctly`() {
        val stats = QuizStatistics(
            totalQuestions = 100,
            completedQuestions = 50
        )
        assertEquals(0.5f, stats.completionRate, 0.001f)
    }
    
    @Test
    fun `QuizStatistics should calculate accuracy rate correctly`() {
        val stats = QuizStatistics(
            totalQuestions = 100,
            completedQuestions = 60,
            correctAnswers = 45
        )
        assertEquals(0.75f, stats.accuracyRate, 0.001f)
    }
    
    @Test
    fun `QuizStatistics should return zero rate when no questions`() {
        val stats = QuizStatistics(totalQuestions = 0)
        assertEquals(0f, stats.completionRate, 0.001f)
        assertEquals(0f, stats.accuracyRate, 0.001f)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `QuizStatistics should reject negative totalQuestions`() {
        QuizStatistics(totalQuestions = -1)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `QuizStatistics should reject negative completedQuestions`() {
        QuizStatistics(completedQuestions = -1)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `QuizStatistics should reject completed > total`() {
        QuizStatistics(totalQuestions = 10, completedQuestions = 20)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `QuizStatistics should reject correct > completed`() {
        QuizStatistics(
            totalQuestions = 10,
            completedQuestions = 5,
            correctAnswers = 10
        )
    }
    
    @Test
    fun `QuizStatistics completion rate should be clamped to 0-1`() {
        val stats = QuizStatistics(
            totalQuestions = 100,
            completedQuestions = 50
        )
        assertTrue(stats.completionRate >= 0f)
        assertTrue(stats.completionRate <= 1f)
    }
}

class CategoryStatsTest {
    
    @Test
    fun `CategoryStats should calculate accuracy rate correctly`() {
        val stats = CategoryStats(
            category = "算法",
            totalQuestions = 20,
            completedQuestions = 15,
            correctAnswers = 12
        )
        assertEquals(0.8f, stats.accuracyRate, 0.001f)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `CategoryStats should reject blank category`() {
        CategoryStats(category = "")
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `CategoryStats should reject negative total`() {
        CategoryStats(category = "Test", totalQuestions = -1)
    }
}

class DifficultyStatsTest {
    
    @Test
    fun `DifficultyStats should calculate accuracy rate correctly`() {
        val stats = DifficultyStats(
            difficulty = "HARD",
            totalQuestions = 30,
            completedQuestions = 20,
            correctAnswers = 14
        )
        assertEquals(0.7f, stats.accuracyRate, 0.001f)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `DifficultyStats should reject blank difficulty`() {
        DifficultyStats(difficulty = "  ")
    }
}
