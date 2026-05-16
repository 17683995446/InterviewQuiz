package com.interviewquiz.app.ui.screens

import androidx.lifecycle.ViewModel
import com.interviewquiz.app.data.Question
import com.interviewquiz.app.data.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class HomeUiState(
    val allQuestions: List<Question> = emptyList(),
    val filteredQuestions: List<Question> = emptyList(),
    val selectedCategory: String? = null,
    val selectedDifficulty: String? = null,
    val showFilters: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val questionRepository: QuestionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadQuestions()
    }
    
    private fun loadQuestions() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        try {
            val questions = questionRepository.getQuestions()
            _uiState.value = _uiState.value.copy(
                allQuestions = questions,
                filteredQuestions = questions,
                isLoading = false
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = e.message,
                isLoading = false
            )
        }
    }
    
    fun toggleFilters() {
        _uiState.value = _uiState.value.copy(
            showFilters = !_uiState.value.showFilters
        )
    }
    
    fun selectCategory(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        applyFilters()
    }
    
    fun selectDifficulty(difficulty: String?) {
        _uiState.value = _uiState.value.copy(selectedDifficulty = difficulty)
        applyFilters()
    }
    
    fun resetFilters() {
        _uiState.value = _uiState.value.copy(
            selectedCategory = null,
            selectedDifficulty = null,
            filteredQuestions = _uiState.value.allQuestions
        )
    }
    
    private fun applyFilters() {
        val category = _uiState.value.selectedCategory
        val difficulty = _uiState.value.selectedDifficulty
        
        val filtered = _uiState.value.allQuestions.filter { question ->
            (category == null || question.category == category) &&
            (difficulty == null || question.difficulty == difficulty)
        }
        
        _uiState.value = _uiState.value.copy(filteredQuestions = filtered)
    }
    
    fun getCategories(): List<String> = questionRepository.getCategories()
    
    fun getDifficulties(): List<String> = questionRepository.getDifficulties()
}
