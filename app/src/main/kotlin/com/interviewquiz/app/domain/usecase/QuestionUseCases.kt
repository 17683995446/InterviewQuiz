package com.interviewquiz.app.domain.usecase

import com.interviewquiz.app.data.Question
import com.interviewquiz.app.data.QuestionRepository
import javax.inject.Inject

class GetQuestionsUseCase @Inject constructor(
    private val repository: QuestionRepository
) {
    operator fun invoke(): List<Question> = repository.getQuestions()
}

class GetQuestionsByCategoryUseCase @Inject constructor(
    private val repository: QuestionRepository
) {
    operator fun invoke(category: String): List<Question> = 
        repository.getQuestionsByCategory(category)
}

class GetQuestionsByDifficultyUseCase @Inject constructor(
    private val repository: QuestionRepository
) {
    operator fun invoke(difficulty: String): List<Question> = 
        repository.getQuestionsByDifficulty(difficulty)
}

class GetCategoriesUseCase @Inject constructor(
    private val repository: QuestionRepository
) {
    operator fun invoke(): List<String> = repository.getCategories()
}

class GetDifficultiesUseCase @Inject constructor(
    private val repository: QuestionRepository
) {
    operator fun invoke(): List<String> = repository.getDifficulties()
}

class SearchQuestionsUseCase @Inject constructor(
    private val repository: QuestionRepository
) {
    operator fun invoke(query: String): List<Question> = 
        repository.searchQuestions(query)
}

class FilterQuestionsUseCase @Inject constructor(
    private val repository: QuestionRepository
) {
    operator fun invoke(
        category: String? = null,
        difficulty: String? = null,
        searchQuery: String? = null
    ): List<Question> {
        // 使用 repository 的缓存优先，减少全量筛选
        var questions = when {
            category != null && difficulty != null -> {
                repository.getQuestionsByCategory(category)
                    .filter { it.difficulty == difficulty }
            }
            category != null -> repository.getQuestionsByCategory(category)
            difficulty != null -> repository.getQuestionsByDifficulty(difficulty)
            else -> repository.getQuestions()
        }
        
        searchQuery?.takeIf { it.isNotBlank() }?.let { query ->
            val lowerQuery = query.lowercase()
            questions = questions.filter { question ->
                repository.isQuestionMatch(question, lowerQuery)
            }
        }
        
        return questions
    }
}

class GetQuestionCountUseCase @Inject constructor(
    private val repository: QuestionRepository
) {
    operator fun invoke(): Int = repository.getQuestionCount()
}
