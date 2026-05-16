package com.interviewquiz.app.data

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class QuestionRepositoryTest {
    
    private lateinit var repository: QuestionRepositoryImpl
    
    @Before
    fun setup() {
        repository = QuestionRepositoryImpl()
    }
    
    @Test
    fun `getQuestions should return all questions`() {
        val questions = repository.getQuestions()
        assertTrue("Should return questions", questions.isNotEmpty())
        assertEquals("Should have 120 questions", 120, questions.size)
    }
    
    @Test
    fun `getQuestionsByCategory should filter correctly`() {
        val algorithmQuestions = repository.getQuestionsByCategory("算法与数据结构")
        assertTrue("Should have algorithm questions", algorithmQuestions.isNotEmpty())
        algorithmQuestions.forEach { question ->
            assertEquals("Should be algorithm category", "算法与数据结构", question.category)
        }
    }
    
    @Test
    fun `getQuestionsByDifficulty should filter correctly`() {
        val easyQuestions = repository.getQuestionsByDifficulty("EASY")
        assertTrue("Should have easy questions", easyQuestions.isNotEmpty())
        easyQuestions.forEach { question ->
            assertEquals("Should be easy difficulty", "EASY", question.difficulty)
        }
    }
    
    @Test
    fun `getCategories should return unique categories`() {
        val categories = repository.getCategories()
        assertTrue("Should have categories", categories.isNotEmpty())
        assertTrue("Should contain 算法与数据结构", categories.contains("算法与数据结构"))
        assertTrue("Should contain Java与Kotlin", categories.contains("Java与Kotlin"))
        assertTrue("Should contain 数据库", categories.contains("数据库"))
        assertTrue("Should contain 网络基础", categories.contains("网络基础"))
        assertTrue("Should contain 操作系统", categories.contains("操作系统"))
        assertTrue("Should contain Android开发", categories.contains("Android开发"))
    }
    
    @Test
    fun `getDifficulties should return correct difficulties`() {
        val difficulties = repository.getDifficulties()
        assertEquals("Should have 3 difficulties", 3, difficulties.size)
        assertTrue("Should contain EASY", difficulties.contains("EASY"))
        assertTrue("Should contain MEDIUM", difficulties.contains("MEDIUM"))
        assertTrue("Should contain HARD", difficulties.contains("HARD"))
    }
    
    @Test
    fun `all questions should have valid data`() {
        val questions = repository.getQuestions()
        questions.forEach { question ->
            assertNotNull("Should have id", question.id)
            assertTrue("Should have title", question.title.isNotEmpty())
            assertEquals("Should have 4 options", 4, question.options.size)
            assertTrue("Correct answer should be 0-3", question.correctAnswer in 0..3)
            assertTrue("Should have explanation", question.explanation.isNotEmpty())
            assertTrue("Should have category", question.category.isNotEmpty())
            assertTrue("Should have difficulty", question.difficulty.isNotEmpty())
        }
    }
    
    @Test
    fun `all question IDs should be unique`() {
        val questions = repository.getQuestions()
        val ids = questions.map { it.id }
        val uniqueIds = ids.distinct()
        assertEquals("All IDs should be unique", ids.size, uniqueIds.size)
    }
    
    @Test
    fun `difficulty distribution should be reasonable`() {
        val questions = repository.getQuestions()
        val easyCount = questions.count { it.difficulty == "EASY" }
        val mediumCount = questions.count { it.difficulty == "MEDIUM" }
        val hardCount = questions.count { it.difficulty == "HARD" }
        
        assertTrue("Should have at least 20 easy questions", easyCount >= 20)
        assertTrue("Should have at least 40 medium questions", mediumCount >= 40)
        assertTrue("Should have at least 20 hard questions", hardCount >= 20)
    }
}
