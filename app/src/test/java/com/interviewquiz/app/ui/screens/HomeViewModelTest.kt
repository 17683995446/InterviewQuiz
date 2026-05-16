package com.interviewquiz.app.ui.screens

import com.interviewquiz.app.data.Question
import com.interviewquiz.app.data.QuestionRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    
    private lateinit var viewModel: HomeViewModel
    private lateinit var mockRepository: QuestionRepository
    
    private val testDispatcher = UnconfinedTestDispatcher()
    
    private val testQuestions = listOf(
        Question(1, "测试题目1", listOf("A", "B", "C", "D"), 0, "解析1", "算法与数据结构", "EASY"),
        Question(2, "测试题目2", listOf("A", "B", "C", "D"), 1, "解析2", "Java与Kotlin", "MEDIUM"),
        Question(3, "测试题目3", listOf("A", "B", "C", "D"), 2, "解析3", "数据库", "HARD")
    )
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk()
        every { mockRepository.getQuestions() } returns testQuestions
        every { mockRepository.getQuestionsByCategory(any()) } returns testQuestions.take(1)
        every { mockRepository.getQuestionsByDifficulty(any()) } returns testQuestions.take(1)
        every { mockRepository.getCategories() } returns listOf("算法与数据结构", "Java与Kotlin", "数据库")
        every { mockRepository.getDifficulties() } returns listOf("EASY", "MEDIUM", "HARD")
        
        viewModel = HomeViewModel(mockRepository)
    }
    
    @After
    fun teardown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `viewModel initializes with correct state`() {
        val state = viewModel.uiState.value
        
        assertFalse("Should not be loading", state.isLoading)
        assertEquals("Should have all test questions", 3, state.allQuestions.size)
        assertEquals("Should have all test questions filtered", 3, state.filteredQuestions.size)
        assertNull("Should have no category selected", state.selectedCategory)
        assertNull("Should have no difficulty selected", state.selectedDifficulty)
    }
    
    @Test
    fun `selectCategory should filter questions`() {
        viewModel.selectCategory("算法与数据结构")
        
        val state = viewModel.uiState.value
        assertEquals("Should have selected category", "算法与数据结构", state.selectedCategory)
        assertEquals("Should have filtered questions", 1, state.filteredQuestions.size)
    }
    
    @Test
    fun `selectDifficulty should filter questions`() {
        viewModel.selectDifficulty("EASY")
        
        val state = viewModel.uiState.value
        assertEquals("Should have selected difficulty", "EASY", state.selectedDifficulty)
    }
    
    @Test
    fun `resetFilters should clear all filters`() {
        viewModel.selectCategory("算法与数据结构")
        viewModel.selectDifficulty("EASY")
        
        viewModel.resetFilters()
        
        val state = viewModel.uiState.value
        assertNull("Category should be null", state.selectedCategory)
        assertNull("Difficulty should be null", state.selectedDifficulty)
        assertEquals("Should have all questions", 3, state.filteredQuestions.size)
    }
    
    @Test
    fun `toggleFilters should toggle showFilters`() {
        val initialState = viewModel.uiState.value.showFilters
        
        viewModel.toggleFilters()
        assertEquals("Should be toggled", !initialState, viewModel.uiState.value.showFilters)
        
        viewModel.toggleFilters()
        assertEquals("Should be toggled back", initialState, viewModel.uiState.value.showFilters)
    }
    
    @Test
    fun `getCategories should return categories from repository`() {
        val categories = viewModel.getCategories()
        
        assertEquals("Should have 3 categories", 3, categories.size)
        assertTrue("Should contain 算法与数据结构", categories.contains("算法与数据结构"))
    }
    
    @Test
    fun `getDifficulties should return difficulties from repository`() {
        val difficulties = viewModel.getDifficulties()
        
        assertEquals("Should have 3 difficulties", 3, difficulties.size)
        assertTrue("Should contain EASY", difficulties.contains("EASY"))
    }
    
    @Test
    fun `combined filter should work`() {
        val repo = object : QuestionRepository {
            override fun getQuestions(): List<Question> = testQuestions
            override fun getQuestionsByCategory(category: String): List<Question> = 
                testQuestions.filter { it.category == category }
            override fun getQuestionsByDifficulty(difficulty: String): List<Question> =
                testQuestions.filter { it.difficulty == difficulty }
            override fun getCategories(): List<String> = listOf("算法与数据结构", "Java与Kotlin", "数据库")
            override fun getDifficulties(): List<String> = listOf("EASY", "MEDIUM", "HARD")
        }
        
        val vm = HomeViewModel(repo)
        vm.selectCategory("算法与数据结构")
        vm.selectDifficulty("EASY")
        
        assertEquals("Should have 1 question", 1, vm.uiState.value.filteredQuestions.size)
    }
}
