package com.interviewquiz.app.domain.usecase

import com.interviewquiz.app.data.Question
import com.interviewquiz.app.data.QuestionRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when`

class QuestionUseCasesTest {
    
    @Mock
    private lateinit var repository: QuestionRepository
    
    private lateinit var getQuestionsUseCase: GetQuestionsUseCase
    private lateinit var filterQuestionsUseCase: FilterQuestionsUseCase
    private lateinit var getCategoriesUseCase: GetCategoriesUseCase
    private lateinit var getDifficultiesUseCase: GetDifficultiesUseCase
    private lateinit var getQuestionCountUseCase: GetQuestionCountUseCase
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        getQuestionsUseCase = GetQuestionsUseCase(repository)
        filterQuestionsUseCase = FilterQuestionsUseCase(repository)
        getCategoriesUseCase = GetCategoriesUseCase(repository)
        getDifficultiesUseCase = GetDifficultiesUseCase(repository)
        getQuestionCountUseCase = GetQuestionCountUseCase(repository)
    }
    
    @Test
    fun `GetQuestionsUseCase should delegate to repository`() {
        val mockQuestions = listOf(
            createQuestion(1, "算法", "EASY"),
            createQuestion(2, "Java", "MEDIUM")
        )
        `when`(repository.getQuestions()).thenReturn(mockQuestions)
        
        val result = getQuestionsUseCase()
        
        assertEquals(2, result.size)
        assertEquals("算法", result[0].category)
    }
    
    @Test
    fun `FilterQuestionsUseCase should filter by category`() {
        val mockQuestions = listOf(
            createQuestion(1, "算法", "EASY"),
            createQuestion(2, "Java", "MEDIUM"),
            createQuestion(3, "算法", "HARD")
        )
        `when`(repository.getQuestions()).thenReturn(mockQuestions)
        `when`(repository.getQuestionsByCategory("算法")).thenReturn(
            mockQuestions.filter { it.category == "算法" }
        )
        
        val result = filterQuestionsUseCase(category = "算法")
        
        assertEquals(2, result.size)
        assertTrue(result.all { it.category == "算法" })
    }
    
    @Test
    fun `FilterQuestionsUseCase should filter by difficulty`() {
        val mockQuestions = listOf(
            createQuestion(1, "算法", "EASY"),
            createQuestion(2, "Java", "MEDIUM"),
            createQuestion(3, "DB", "EASY")
        )
        `when`(repository.getQuestionsByDifficulty("EASY")).thenReturn(
            mockQuestions.filter { it.difficulty == "EASY" }
        )
        
        val result = filterQuestionsUseCase(difficulty = "EASY")
        
        assertEquals(2, result.size)
        assertTrue(result.all { it.difficulty == "EASY" })
    }
    
    @Test
    fun `FilterQuestionsUseCase should filter by search query`() {
        val mockQuestions = listOf(
            createQuestion(1, "算法", "EASY", "二叉树遍历"),
            createQuestion(2, "Java", "MEDIUM", "Spring框架"),
            createQuestion(3, "算法", "HARD", "动态规划")
        )
        `when`(repository.getQuestions()).thenReturn(mockQuestions)
        
        val result = filterQuestionsUseCase(searchQuery = "二叉树")
        
        assertEquals(1, result.size)
        assertTrue(result[0].title.contains("二叉树"))
    }
    
    @Test
    fun `FilterQuestionsUseCase should combine multiple filters`() {
        val mockQuestions = listOf(
            createQuestion(1, "算法", "EASY", "二叉树遍历"),
            createQuestion(2, "算法", "MEDIUM", "二叉树平衡"),
            createQuestion(3, "算法", "HARD", "动态规划")
        )
        `when`(repository.getQuestionsByCategory("算法")).thenReturn(
            mockQuestions.filter { it.category == "算法" }
        )
        
        val result = filterQuestionsUseCase(
            category = "算法",
            searchQuery = "二叉树"
        )
        
        assertEquals(2, result.size)
        assertTrue(result.all { it.category == "算法" })
        assertTrue(result.all { it.title.contains("二叉树") })
    }
    
    @Test
    fun `FilterQuestionsUseCase should return all when no filters`() {
        val mockQuestions = listOf(
            createQuestion(1, "算法", "EASY"),
            createQuestion(2, "Java", "MEDIUM")
        )
        `when`(repository.getQuestions()).thenReturn(mockQuestions)
        
        val result = filterQuestionsUseCase()
        
        assertEquals(2, result.size)
    }
    
    @Test
    fun `GetCategoriesUseCase should return unique categories`() {
        val categories = listOf("算法", "Java", "算法", "数据库")
        `when`(repository.getCategories()).thenReturn(categories)
        
        val result = getCategoriesUseCase()
        
        assertEquals(3, result.size)
    }
    
    @Test
    fun `GetDifficultiesUseCase should return all difficulties`() {
        val difficulties = listOf("EASY", "MEDIUM", "HARD")
        `when`(repository.getDifficulties()).thenReturn(difficulties)
        
        val result = getDifficultiesUseCase()
        
        assertEquals(3, result.size)
    }
    
    @Test
    fun `GetQuestionCountUseCase should return correct count`() {
        `when`(repository.getQuestionCount()).thenReturn(100)
        
        val result = getQuestionCountUseCase()
        
        assertEquals(100, result)
    }
    
    private fun createQuestion(
        id: Int,
        category: String,
        difficulty: String,
        title: String = "Test Question $id"
    ): Question {
        return Question(
            id = id,
            title = title,
            options = listOf("A", "B", "C", "D"),
            correctAnswer = 0,
            explanation = "Test explanation",
            category = category,
            difficulty = difficulty
        )
    }
}
