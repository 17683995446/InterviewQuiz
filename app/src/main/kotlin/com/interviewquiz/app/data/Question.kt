package com.interviewquiz.app.data

enum class Difficulty(val displayName: String) {
    EASY("简单"),
    MEDIUM("中等"),
    HARD("困难");

    companion object {
        fun fromString(value: String): Difficulty? {
            return entries.find { it.name == value }
        }

        fun getDisplayName(value: String): String {
            return fromString(value)?.displayName ?: value
        }
    }
}

data class Question(
    val id: Int,
    val title: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String,
    val category: String,
    val difficulty: String
) {
    fun getDifficultyLevel(): Difficulty {
        return Difficulty.fromString(difficulty) ?: Difficulty.MEDIUM
    }
    
    fun isCorrectAnswer(index: Int): Boolean {
        return index == correctAnswer
    }
    
    fun getOptionLetter(index: Int): Char {
        // 边界验证，防止超出字母范围
        val safeIndex = index.coerceIn(0, 25)
        return ('A'.code + safeIndex).toChar()
    }
}
