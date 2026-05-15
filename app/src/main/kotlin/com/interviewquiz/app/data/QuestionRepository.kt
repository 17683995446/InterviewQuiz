package com.interviewquiz.app.data

import javax.inject.Inject

interface QuestionRepository {
    fun getQuestions(): List<Question>
}

class QuestionRepositoryImpl @Inject constructor() : QuestionRepository {
    override fun getQuestions(): List<Question> {
        return listOf(
            Question(
                id = 1,
                title = "什么是时间复杂度？",
                options = listOf(
                    "衡量算法执行时间与输入规模关系的指标",
                    "算法代码的长度",
                    "算法消耗的内存大小",
                    "算法执行的实际时间"
                ),
                correctAnswer = 0,
                explanation = "时间复杂度是衡量算法效率的重要方法，通常用大O表示法来描述。",
                category = "算法与数据结构",
                difficulty = "EASY"
            ),
            Question(
                id = 2,
                title = "Java中ArrayList和LinkedList的主要区别是什么？",
                options = listOf(
                    "ArrayList基于动态数组，LinkedList基于双向链表",
                    "LinkedList访问更快",
                    "ArrayList插入删除更快",
                    "没有区别"
                ),
                correctAnswer = 0,
                explanation = "ArrayList支持O(1)随机访问，LinkedList支持O(1)插入删除。",
                category = "Java与Kotlin",
                difficulty = "MEDIUM"
            )
        )
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
)
