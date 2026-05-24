package com.interviewquiz.app.data

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class QuestionRepositoryEdgeCaseTest {
    
    private lateinit var repository: QuestionRepositoryImpl
    
    @Before
    fun setup() {
        repository = QuestionRepositoryImpl()
    }
    
    @Test
    fun `searchQuestions should return all for blank query`() {
        val allQuestions = repository.getQuestions()
        val result = repository.searchQuestions("")
        assertEquals(allQuestions.size, result.size)
    }
    
    @Test
    fun `searchQuestions should be case insensitive`() {
        val lowerResult = repository.searchQuestions("算法")
        val upperResult = repository.searchQuestions("算法".uppercase())
        val mixedResult = repository.searchQuestions("算 法")
        
        assertEquals(lowerResult.size, upperResult.size)
        assertEquals(lowerResult.size, mixedResult.size)
    }
    
    @Test
    fun `searchQuestions should search in explanation`() {
        val allQuestions = repository.getQuestions()
        val firstQuestion = allQuestions.first()
        
        val result = repository.searchQuestions(firstQuestion.explanation.take(5))
        
        assertTrue(result.any { it.id == firstQuestion.id })
    }
    
    @Test
    fun `searchQuestions should search in options`() {
        val allQuestions = repository.getQuestions()
        val firstQuestion = allQuestions.first()
        val firstOption = firstQuestion.options.first()
        
        val result = repository.searchQuestions(firstOption.take(2))
        
        assertTrue(result.any { it.id == firstQuestion.id })
    }
    
    @Test
    fun `searchQuestions should cache results`() {
        val query = "test"
        val first = repository.searchQuestions(query)
        val second = repository.searchQuestions(query)
        
        assertSame(first, second)
    }
    
    @Test
    fun `searchQuestions should not cache blank query`() {
        val result1 = repository.searchQuestions("")
        val result2 = repository.searchQuestions("")
        
        assertNotSame(result1, result2)
    }
    
    @Test
    fun `getQuestionsPaginated should return correct page`() {
        val page0 = repository.getQuestionsPaginated(0, 10)
        val page1 = repository.getQuestionsPaginated(1, 10)
        
        assertEquals(10, page0.size)
        assertEquals(10, page1.size)
        assertNotEquals(page0, page1)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `getQuestionsPaginated should reject negative page`() {
        repository.getQuestionsPaginated(-1, 10)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `getQuestionsPaginated should reject zero pageSize`() {
        repository.getQuestionsPaginated(0, 0)
    }
    
    @Test
    fun `getQuestionsPaginated should return empty for out of range`() {
        val result = repository.getQuestionsPaginated(100, 10)
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `getQuestionsPaginated should handle partial last page`() {
        val totalSize = repository.getQuestionCount()
        val pageSize = 10
        val lastPageIndex = totalSize / pageSize
        
        val result = repository.getQuestionsPaginated(lastPageIndex, pageSize)
        
        assertTrue(result.size <= pageSize)
    }
    
    @Test
    fun `all questions should have unique IDs`() {
        val questions = repository.getQuestions()
        val ids = questions.map { it.id }
        
        assertEquals(ids.size, ids.distinct().size)
    }
    
    @Test
    fun `all question IDs should be sequential`() {
        val questions = repository.getQuestions().sortedBy { it.id }
        val ids = questions.map { it.id }
        
        for (i in ids.indices) {
            assertEquals(i + 1, ids[i])
        }
    }
    
    @Test
    fun `correct answer index should always be valid`() {
        val questions = repository.getQuestions()
        
        questions.forEach { question ->
            assertTrue(
                "Question ${question.id} has invalid correctAnswer",
                question.correctAnswer in question.options.indices
            )
        }
    }
    
    @Test
    fun `no question should have empty fields`() {
        val questions = repository.getQuestions()
        
        questions.forEach { question ->
            assertNotNull("ID should not be null", question.id)
            assertTrue("Title should not be empty", question.title.isNotBlank())
            assertTrue("Category should not be empty", question.category.isNotBlank())
            assertTrue("Difficulty should not be empty", question.difficulty.isNotBlank())
            assertTrue("Options should not be empty", question.options.isNotEmpty())
            assertTrue("Explanation should not be empty", question.explanation.isNotBlank())
            
            question.options.forEach { option ->
                assertTrue("Option should not be empty", option.isNotBlank())
            }
        }
    }
    
    @Test
    fun `difficulty distribution should be reasonable`() {
        val questions = repository.getQuestions()
        val easy = questions.count { it.difficulty == "EASY" }
        val medium = questions.count { it.difficulty == "MEDIUM" }
        val hard = questions.count { it.difficulty == "HARD" }
        
        assertTrue("Should have easy questions", easy > 0)
        assertTrue("Should have medium questions", medium > 0)
        assertTrue("Should have hard questions", hard > 0)
        
        assertEquals(questions.size, easy + medium + hard)
    }
    
    @Test
    fun `category distribution should be reasonable`() {
        val questions = repository.getQuestions()
        val categories = questions.map { it.category }.distinct()
        
        assertTrue("Should have multiple categories", categories.size >= 3)
        assertTrue("Should have 6 categories", categories.size <= 10)
    }
}

class RepositoryConcurrencyTest {
    
    private lateinit var repository: QuestionRepositoryImpl
    
    @Before
    fun setup() {
        repository = QuestionRepositoryImpl()
    }
    
    @Test
    fun `concurrent searches should not throw`() {
        val queries = listOf("算法", "Java", "数据库", "网络", "操作系统")
        
        queries.forEach { query ->
            val result = repository.searchQuestions(query)
            assertNotNull(result)
        }
    }
    
    @Test
    fun `repeated searches should return cached results`() {
        val query = "常见"
        
        val result1 = repository.searchQuestions(query)
        val result2 = repository.searchQuestions(query)
        
        assertSame(result1, result2)
    }
    
    @Test
    fun `search with different cases should have separate cache`() {
        val query1 = "算法"
        val query2 = "算 法"
        
        repository.searchQuestions(query1)
        val result2 = repository.searchQuestions(query2)
        
        assertNotSame(repository.searchQuestions(query1), result2)
    }
}
