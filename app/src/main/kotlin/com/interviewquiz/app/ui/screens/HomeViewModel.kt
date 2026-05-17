package com.interviewquiz.app.ui.screens

import androidx.lifecycle.viewModelScope
import com.interviewquiz.app.data.Question
import com.interviewquiz.app.domain.usecase.FilterQuestionsUseCase
import com.interviewquiz.app.domain.usecase.GetCategoriesUseCase
import com.interviewquiz.app.domain.usecase.GetDifficultiesUseCase
import com.interviewquiz.app.domain.usecase.GetQuestionCountUseCase
import com.interviewquiz.app.domain.usecase.GetQuestionsUseCase
import com.interviewquiz.app.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val allQuestions: List<Question> = emptyList(),
    val filteredQuestions: List<Question> = emptyList(),
    val selectedCategory: String? = null,
    val selectedDifficulty: String? = null,
    val showFilters: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val categories: List<String> = emptyList(),
    val difficulties: List<String> = emptyList(),
    val totalQuestionCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val filterQuestionsUseCase: FilterQuestionsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getDifficultiesUseCase: GetDifficultiesUseCase,
    private val getQuestionCountUseCase: GetQuestionCountUseCase
) : BaseViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadQuestions()
    }
    
    private fun loadQuestions() {
        launchSafe(
            block = {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val categories = getCategoriesUseCase()
                val difficulties = getDifficultiesUseCase()
                val questions = getQuestionsUseCase()
                val count = getQuestionCountUseCase()
                
                _uiState.value = _uiState.value.copy(
                    allQuestions = questions,
                    filteredQuestions = questions,
                    isLoading = false,
                    categories = categories,
                    difficulties = difficulties,
                    totalQuestionCount = count
                )
            },
            onError = { exception ->
                _uiState.value = _uiState.value.copy(
                    error = exception.message,
                    isLoading = false
                )
            }
        )
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
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }
    
    fun resetFilters() {
        _uiState.value = _uiState.value.copy(
            selectedCategory = null,
            selectedDifficulty = null,
            searchQuery = "",
            filteredQuestions = _uiState.value.allQuestions
        )
    }
    
    private fun applyFilters() {
        viewModelScope.launch {
            val category = _uiState.value.selectedCategory
            val difficulty = _uiState.value.selectedDifficulty
            val query = _uiState.value.searchQuery
            
            val filtered = filterQuestionsUseCase(
                category = category,
                difficulty = difficulty,
                searchQuery = query
            )
            
            _uiState.value = _uiState.value.copy(filteredQuestions = filtered)
        }
    }
    
    fun getCategories(): List<String> = _uiState.value.categories
    
    fun getDifficulties(): List<String> = _uiState.value.difficulties
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    override fun handleException(exception: Throwable) {
        _uiState.value = _uiState.value.copy(
            error = exception.message,
            isLoading = false
        )
    }
}
