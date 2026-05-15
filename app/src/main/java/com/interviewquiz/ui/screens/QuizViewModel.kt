package com.interviewquiz.ui.screens

import androidx.lifecycle.ViewModel
import com.interviewquiz.core.model.Question
import com.interviewquiz.core.model.QuestionDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor() : ViewModel() {
    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswer = MutableStateFlow<Int?>(null)
    val selectedAnswer: StateFlow<Int?> = _selectedAnswer.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _isAnswerChecked = MutableStateFlow(false)
    val isAnswerChecked: StateFlow<Boolean> = _isAnswerChecked.asStateFlow()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        _questions.value = QuestionDataSource.getRandomQuestions(10)
    }

    fun selectAnswer(answerIndex: Int) {
        if (!_isAnswerChecked.value) {
            _selectedAnswer.value = answerIndex
        }
    }

    fun checkAnswer() {
        if (_selectedAnswer.value == null) return
        
        _isAnswerChecked.value = true
        
        val currentQuestion = _questions.value[_currentQuestionIndex.value]
        if (currentQuestion.isCorrect(_selectedAnswer.value!!)) {
            _score.value = _score.value + 1
        }
    }

    fun nextQuestion() {
        if (_currentQuestionIndex.value < _questions.value.size - 1) {
            _currentQuestionIndex.value = _currentQuestionIndex.value + 1
            _selectedAnswer.value = null
            _isAnswerChecked.value = false
        }
    }

    fun resetQuiz() {
        _currentQuestionIndex.value = 0
        _selectedAnswer.value = null
        _score.value = 0
        _isAnswerChecked.value = false
        loadQuestions()
    }
}
