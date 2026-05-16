package com.interviewquiz.app.data

data class Question(
    val id: Int,
    val title: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String,
    val category: String,
    val difficulty: String
)
