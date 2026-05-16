package com.interviewquiz.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.interviewquiz.app.data.Question
import com.interviewquiz.app.data.QuestionRepository
import com.interviewquiz.app.ui.screens.HomeScreen
import com.interviewquiz.app.ui.theme.InterviewQuizTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class HomeScreenTest {
    
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()
    
    private val testQuestions = listOf(
        Question(1, "测试题目1", listOf("选项A", "选项B", "选项C", "选项D"), 0, "解析1", "算法与数据结构", "EASY"),
        Question(2, "测试题目2", listOf("选项A", "选项B", "选项C", "选项D"), 1, "解析2", "Java与Kotlin", "MEDIUM")
    )
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun `home screen should display title`() {
        composeTestRule.setContent {
            InterviewQuizTheme {
                HomeScreen()
            }
        }
        
        composeTestRule.onNodeWithText("InterviewQuiz").assertIsDisplayed()
    }
    
    @Test
    fun `home screen should display welcome card`() {
        composeTestRule.setContent {
            InterviewQuizTheme {
                HomeScreen()
            }
        }
        
        composeTestRule.onNodeWithText("欢迎使用 InterviewQuiz！").assertIsDisplayed()
    }
    
    @Test
    fun `home screen should display questions list`() {
        composeTestRule.setContent {
            InterviewQuizTheme {
                HomeScreen()
            }
        }
        
        composeTestRule.onNodeWithText("题目列表").assertIsDisplayed()
    }
    
    @Test
    fun `home screen should display filter button`() {
        composeTestRule.setContent {
            InterviewQuizTheme {
                HomeScreen()
            }
        }
        
        composeTestRule.onNodeWithContentDescription("筛选").assertIsDisplayed()
    }
    
    @Test
    fun `clicking question card should expand it`() {
        composeTestRule.setContent {
            InterviewQuizTheme {
                HomeScreen()
            }
        }
        
        val firstQuestionCard = composeTestRule.onNodeWithText(containsText("什么是时间复杂度"))
        firstQuestionCard.performClick()
    }
}
