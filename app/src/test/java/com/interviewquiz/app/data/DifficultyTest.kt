package com.interviewquiz.app.data

import org.junit.Assert.*
import org.junit.Test

class DifficultyTest {
    
    @Test
    fun `Difficulty enum has correct values`() {
        assertEquals(3, Difficulty.entries.size)
        assertEquals("简单", Difficulty.EASY.displayName)
        assertEquals("中等", Difficulty.MEDIUM.displayName)
        assertEquals("困难", Difficulty.HARD.displayName)
    }
    
    @Test
    fun `fromString returns correct Difficulty`() {
        assertEquals(Difficulty.EASY, Difficulty.fromString("EASY"))
        assertEquals(Difficulty.MEDIUM, Difficulty.fromString("MEDIUM"))
        assertEquals(Difficulty.HARD, Difficulty.fromString("HARD"))
        assertNull(Difficulty.fromString("INVALID"))
    }
    
    @Test
    fun `getDisplayName returns correct display name`() {
        assertEquals("简单", Difficulty.getDisplayName("EASY"))
        assertEquals("中等", Difficulty.getDisplayName("MEDIUM"))
        assertEquals("困难", Difficulty.getDisplayName("HARD"))
        assertEquals("UNKNOWN", Difficulty.getDisplayName("UNKNOWN"))
    }
    
    @Test
    fun `Question getDifficultyLevel returns correct level`() {
        val question = Question(
            id = 1,
            title = "Test",
            options = listOf("A", "B", "C", "D"),
            correctAnswer = 0,
            explanation = "Test",
            category = "Test",
            difficulty = "EASY"
        )
        
        assertEquals(Difficulty.EASY, question.getDifficultyLevel())
    }
    
    @Test
    fun `Question isCorrectAnswer returns correct boolean`() {
        val question = Question(
            id = 1,
            title = "Test",
            options = listOf("A", "B", "C", "D"),
            correctAnswer = 2,
            explanation = "Test",
            category = "Test",
            difficulty = "EASY"
        )
        
        assertFalse(question.isCorrectAnswer(0))
        assertFalse(question.isCorrectAnswer(1))
        assertTrue(question.isCorrectAnswer(2))
        assertFalse(question.isCorrectAnswer(3))
    }
    
    @Test
    fun `Question getOptionLetter returns correct letter`() {
        val question = Question(
            id = 1,
            title = "Test",
            options = listOf("A", "B", "C", "D"),
            correctAnswer = 0,
            explanation = "Test",
            category = "Test",
            difficulty = "EASY"
        )
        
        assertEquals('A', question.getOptionLetter(0))
        assertEquals('B', question.getOptionLetter(1))
        assertEquals('C', question.getOptionLetter(2))
        assertEquals('D', question.getOptionLetter(3))
    }
}
