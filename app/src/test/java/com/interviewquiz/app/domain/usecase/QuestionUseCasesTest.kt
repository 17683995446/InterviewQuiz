package com.interviewquiz.app.domain.usecase

import com.interviewquiz.app.data.Question
import com.interviewquiz.app.data.QuestionRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class QuestionUseCasesTest {
    
    private lateinit var mockRepository: QuestionRepository
    private val testQuestions = listOf(
        Question(1, "测试题目1", listOf("A", "B", "C", "D"), 0, "解析1", "算法与数据结构", "EASY"),
        Question(2, "测试题目2", listOf("A", "B", "C", "D"), 1, "解析2", "Java与Kotlin", "MEDIUM"),
        Question(3, "测试题目3", listOf("A", "B", "C", "D"), 2, "解析3", "数据库", "HARD"),
        Question(4, "网络问题", listOf("A", "B", "C", "D"), 0, "解析4", "网络基础", "EASY"),
        Question(5, "OS问题", listOf("A", "B", "C", "D"), 1, "解析5", "操作系统", "MEDIUM")
    )
    
    @Before
    fun setup() {
        mockRepository = mockk()
        every { mockRepository.getQuestions() } returns testQuestions
        every { mockRepository.getQuestionsByCategory("算法与数据结构") } returns testQuestions.filter { it.category == "算法与数据结构" }
        every { mockRepository.getQuestionsByDifficulty("EASY") } returns testQuestions.filter { it.difficulty == "EASY" }
        every { mockRepository.getCategories() } returns testQuestions.map { it.category }.distinct()
        every { mockRepository.getDifficulties() } returns listOf("EASY", "MEDIUM", "HARD")
        every { mockRepository.getQuestionCount() } returns testQuestions.size
        every { mockRepository.searchQuestions(any()) } answers { 
            val query = firstArg<String>()
            if (query.isBlank()) testQuestions
            else testQuestions.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.category.contains(query, ignoreCase = true) ||
                it.explanation.contains(query, ignoreCase = true)
            }
        }
    }
    
    @Test
    fun `GetQuestionsUseCase returns all questions`() {
        val useCase = GetQuestionsUseCase(mockRepository)
        val result = useCase()
        
        assertEquals(5, result.size)
    }
    
    @Test
    fun `GetQuestionsByCategoryUseCase filters correctly`() {
        val useCase = GetQuestionsByCategoryUseCase(mockRepository)
        val result = useCase("算法与数据结构")
        
        assertEquals(1, result.size)
        assertEquals("算法与数据结构", result.first().category)
    }
    
    @Test
    fun `GetQuestionsByDifficultyUseCase filters correctly`() {
        val useCase = GetQuestionsByDifficultyUseCase(mockRepository)
        val result = useCase("EASY")
        
        assertEquals(2, result.size)
        result.forEach { assertEquals("EASY", it.difficulty) }
    }
    
    @Test
    fun `GetCategoriesUseCase returns unique categories`() {
        val useCase = GetCategoriesUseCase(mockRepository)
        val result = useCase()
        
        assertEquals(5, result.size)
        assertTrue(result.contains("算法与数据结构"))
    }
    
    @Test
    fun `GetDifficultiesUseCase returns all difficulties`() {
        val useCase = GetDifficultiesUseCase(mockRepository)
        val result = useCase()
        
        assertEquals(3, result.size)
        assertTrue(result.containsAll(listOf("EASY", "MEDIUM", "HARD")))
    }
    
    @Test
    fun `GetQuestionCountUseCase returns correct count`() {
        val useCase = GetQuestionCountUseCase(mockRepository)
        val result = useCase()
        
        assertEquals(5, result)
    }
    
    @Test
    fun `SearchQuestionsUseCase searches in title`() {
        val useCase = SearchQuestionsUseCase(mockRepository)
        val result = useCase("测试")
        
        assertEquals(3, result.size)
    }
    
    @Test
    fun `SearchQuestionsUseCase searches in category`() {
        val useCase = SearchQuestionsUseCase(mockRepository)
        val result = useCase("网络")
        
        assertEquals(1, result.size)
        assertEquals("网络基础", result.first().category)
    }
    
    @Test
    fun `SearchQuestionsUseCase returns all for blank query`() {
        val useCase = SearchQuestionsUseCase(mockRepository)
        val result = useCase("")
        
        assertEquals(5, result.size)
    }
    
    @Test
    fun `FilterQuestionsUseCase filters by category`() {
        val useCase = FilterQuestionsUseCase(mockRepository)
        val result = useCase(category = "算法与数据结构")
        
        assertEquals(1, result.size)
    }
    
    @Test
    fun `FilterQuestionsUseCase filters by difficulty`() {
        val useCase = FilterQuestionsUseCase(mockRepository)
        val result = useCase(difficulty = "EASY")
        
        assertEquals(2, result.size)
    }
    
    @Test
    fun `FilterQuestionsUseCase filters by category and difficulty`() {
        val useCase = FilterQuestionsUseCase(mockRepository)
        val result = useCase(category = "算法与数据结构", difficulty = "EASY")
        
        assertEquals(1, result.size)
        assertEquals("算法与数据结构", result.first().category)
        assertEquals("EASY", result.first().difficulty)
    }
    
    @Test
    fun `FilterQuestionsUseCase filters by search query`() {
        val useCase = FilterQuestionsUseCase(mockRepository)
        val result = useCase(searchQuery = "网络")
        
        assertEquals(1, result.size)
    }
    
    @Test
    fun `FilterQuestionsUseCase combines all filters`() {
        val useCase = FilterQuestionsUseCase(mockRepository)
        val result = useCase(
            category = "算法与数据结构",
            difficulty = "EASY",
            searchQuery = "测试"
        )
        
        assertEquals(1, result.size)
    }
}
