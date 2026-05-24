package com.interviewquiz.app.stress

import android.content.Context
import android.content.SharedPreferences
import com.interviewquiz.app.data.Question
import com.interviewquiz.app.data.QuestionRepositoryImpl
import com.interviewquiz.app.data.QuizProgressRepositoryImpl
import com.interviewquiz.app.domain.model.QuizProgress
import com.interviewquiz.app.util.AppLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * QuizProgressRepository 并发安全测试
 * 验证第18轮修复是否解决了并发安全问题
 */
@RunWith(MockitoJUnitRunner::class)
class QuizProgressConcurrencyTest {
    
    @Mock
    private lateinit var context: Context
    
    @Mock
    private lateinit var sharedPreferences: SharedPreferences
    
    @Mock
    private lateinit var editor: SharedPreferences.Editor
    
    private lateinit var appLogger: AppLogger
    
    private lateinit var questionRepository: QuestionRepositoryImpl
    
    @Before
    fun setup() {
        appLogger = AppLogger()
        `when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences)
        `when`(sharedPreferences.edit()).thenReturn(editor)
        `when`(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor)
        `when`(editor.putInt(anyString(), anyInt())).thenReturn(editor)
        `when`(editor.putLong(anyString(), anyLong())).thenReturn(editor)
        `when`(editor.remove(anyString())).thenReturn(editor)
        `when`(editor.clear()).thenReturn(editor)
        
        questionRepository = QuestionRepositoryImpl()
    }
    
    @Test
    fun `concurrent saveProgress - should be thread safe`() = runTest {
        println("\n=== [Test 1] Concurrent saveProgress Thread Safety ===")
        
        val repository = QuizProgressRepositoryImpl(context, appLogger)
        val questions = questionRepository.getQuestions()
        
        val threadCount = 50
        val operationsPerThread = 100
        val errorCount = AtomicInteger(0)
        val latch = CountDownLatch(threadCount)
        
        val startTime = System.currentTimeMillis()
        
        repeat(threadCount) { threadId ->
            launch(Dispatchers.Default) {
                try {
                    repeat(operationsPerThread) { opId ->
                        val question = questions.random()
                        val answer = (0 until question.options.size).random()
                        
                        val progress = QuizProgress(
                            questionId = question.id,
                            isCompleted = true,
                            lastAnswer = answer,
                            isCorrect = question.isCorrectAnswer(answer),
                            completedAt = System.currentTimeMillis(),
                            attempts = 1
                        )
                        
                        repository.saveProgress(progress)
                    }
                } catch (e: Exception) {
                    errorCount.incrementAndGet()
                    println("⚠️ Thread $threadId error: ${e.javaClass.simpleName}")
                } finally {
                    latch.countDown()
                }
            }
        }
        
        latch.await()
        
        val elapsed = System.currentTimeMillis() - startTime
        val totalOps = threadCount * operationsPerThread
        val opsPerSec = (totalOps * 1000) / elapsed.toDouble()
        
        println("✅ Total operations: $totalOps")
        println("✅ Time elapsed: ${elapsed}ms")
        println("✅ Throughput: %.1f ops/sec".format(opsPerSec))
        println("❌ Errors: ${errorCount.get()}")
        
        assertEquals(0, errorCount.get(), "Should have 0 errors")
        println("✅ [Test 1] Passed\n")
    }
    
    @Test
    fun `concurrent mixed operations - should maintain consistency`() = runTest {
        println("\n=== [Test 2] Concurrent Mixed Operations ===")
        
        val repository = QuizProgressRepositoryImpl(context, appLogger)
        val questions = questionRepository.getQuestions()
        
        val threadCount = 30
        val operationsPerThread = 200
        val errorCount = AtomicInteger(0)
        val latch = CountDownLatch(threadCount)
        
        repeat(threadCount) { threadId ->
            launch(Dispatchers.Default) {
                try {
                    repeat(operationsPerThread) { opId ->
                        val question = questions.random()
                        when ((0..99).random()) {
                            in 0..60 -> { // saveProgress
                                val answer = (0 until question.options.size).random()
                                val progress = QuizProgress(
                                    questionId = question.id,
                                    isCompleted = true,
                                    lastAnswer = answer,
                                    isCorrect = question.isCorrectAnswer(answer),
                                    completedAt = System.currentTimeMillis(),
                                    attempts = 1
                                )
                                repository.saveProgress(progress)
                            }
                            in 61..80 -> { // getProgress
                                repository.getProgress(question.id)
                            }
                            in 81..90 -> { // getAllProgress
                                repository.getAllProgress()
                            }
                            in 91..99 -> { // getStatistics
                                repository.getStatistics(questions)
                            }
                        }
                    }
                } catch (e: Exception) {
                    errorCount.incrementAndGet()
                    println("⚠️ Thread $threadId error: ${e.javaClass.simpleName}")
                } finally {
                    latch.countDown()
                }
            }
        }
        
        latch.await()
        
        println("✅ Total operations: ${threadCount * operationsPerThread}")
        println("❌ Errors: ${errorCount.get()}")
        
        assertEquals(0, errorCount.get(), "Should have 0 errors")
        println("✅ [Test 2] Passed\n")
    }
    
    @Test
    fun `long running stability test - 10 seconds`() = runTest {
        println("\n=== [Test 3] Long Running Stability Test (10s) ===")
        
        val repository = QuizProgressRepositoryImpl(context, appLogger)
        val questions = questionRepository.getQuestions()
        val errorCount = AtomicInteger(0)
        val operations = AtomicInteger(0)
        val startTime = System.currentTimeMillis()
        val duration = 10000L // 10 seconds
        
        coroutineScope {
            repeat(20) { _ ->
                launch(Dispatchers.Default) {
                    while (System.currentTimeMillis() - startTime < duration) {
                        try {
                            val question = questions.random()
                            val answer = (0 until question.options.size).random()
                            val progress = QuizProgress(
                                questionId = question.id,
                                isCompleted = true,
                                lastAnswer = answer,
                                isCorrect = question.isCorrectAnswer(answer),
                                completedAt = System.currentTimeMillis(),
                                attempts = 1
                            )
                            repository.saveProgress(progress)
                            operations.incrementAndGet()
                        } catch (e: Exception) {
                            errorCount.incrementAndGet()
                        }
                    }
                }
            }
        }
        
        val elapsed = System.currentTimeMillis() - startTime
        val opsPerSec = (operations.get() * 1000) / elapsed.toDouble()
        
        println("✅ Operations: ${operations.get()}")
        println("✅ Time: ${elapsed}ms")
        println("✅ Throughput: %.1f ops/sec".format(opsPerSec))
        println("❌ Errors: ${errorCount.get()}")
        
        assertEquals(0, errorCount.get(), "Should have 0 errors")
        assertTrue(operations.get() > 1000, "Should have at least 1000 operations")
        println("✅ [Test 3] Passed\n")
    }
}
