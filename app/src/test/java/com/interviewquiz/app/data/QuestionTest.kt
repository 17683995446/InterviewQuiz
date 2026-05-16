package com.interviewquiz.app.data

import org.junit.Assert.*
import org.junit.Test

class QuestionTest {
    
    @Test
    fun `question creation should work`() {
        val question = Question(
            id = 1,
            title = "测试题目",
            options = listOf("选项A", "选项B", "选项C", "选项D"),
            correctAnswer = 0,
            explanation = "测试解析",
            category = "测试分类",
            difficulty = "EASY"
        )
        
        assertEquals(1, question.id)
        assertEquals("测试题目", question.title)
        assertEquals(4, question.options.size)
        assertEquals(0, question.correctAnswer)
        assertEquals("测试解析", question.explanation)
        assertEquals("测试分类", question.category)
        assertEquals("EASY", question.difficulty)
    }
    
    @Test
    fun `correct answer should be valid index`() {
        val question = Question(
            id = 1,
            title = "测试题目",
            options = listOf("选项A", "选项B", "选项C", "选项D"),
            correctAnswer = 2,
            explanation = "测试解析",
            category = "测试分类",
            difficulty = "MEDIUM"
        )
        
        assertTrue("Correct answer should be valid index", 
            question.correctAnswer in 0 until question.options.size)
    }
    
    @Test
    fun `options should have exactly 4 items`() {
        val question = Question(
            id = 1,
            title = "测试题目",
            options = listOf("选项A", "选项B", "选项C", "选项D"),
            correctAnswer = 0,
            explanation = "测试解析",
            category = "测试分类",
            difficulty = "EASY"
        )
        
        assertEquals("Should have exactly 4 options", 4, question.options.size)
    }
    
    @Test
    fun `difficulty should be one of valid values`() {
        val validDifficulties = listOf("EASY", "MEDIUM", "HARD")
        val question = Question(
            id = 1,
            title = "测试题目",
            options = listOf("选项A", "选项B", "选项C", "选项D"),
            correctAnswer = 0,
            explanation = "测试解析",
            category = "测试分类",
            difficulty = "EASY"
        )
        
        assertTrue("Difficulty should be valid", validDifficulties.contains(question.difficulty))
    }
}
