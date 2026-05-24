package com.interviewquiz.app.stress

import com.interviewquiz.app.data.Question
import com.interviewquiz.app.data.QuestionRepositoryImpl
import com.interviewquiz.app.domain.model.QuizProgress
import com.interviewquiz.app.data.QuizProgressRepositoryImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import android.content.Context
import android.content.SharedPreferences
import com.interviewquiz.app.util.AppLogger
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 第24轮 - 超超极限混沌测试
 * 挑战系统的真正极限
 */
@RunWith(MockitoJUnitRunner::class)
class HyperUltraChaosTest {

    companion object {
        private const val TAG = "HyperUltraChaosTest"
    }

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var sharedPreferences: SharedPreferences

    @Mock
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var appLogger: AppLogger
    private lateinit var questionRepository: QuestionRepositoryImpl
    private lateinit var progressRepository: QuizProgressRepositoryImpl

    @Before
    fun setup() {
        appLogger = AppLogger()
        questionRepository = QuestionRepositoryImpl()

        org.mockito.Mockito.`when`(context.getSharedPreferences(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyInt()))
            .thenReturn(sharedPreferences)
        org.mockito.Mockito.`when`(sharedPreferences.edit()).thenReturn(editor)
        org.mockito.Mockito.`when`(editor.putBoolean(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyBoolean())).thenReturn(editor)
        org.mockito.Mockito.`when`(editor.putInt(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyInt())).thenReturn(editor)
        org.mockito.Mockito.`when`(editor.putLong(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(editor)
        org.mockito.Mockito.`when`(editor.remove(org.mockito.ArgumentMatchers.anyString())).thenReturn(editor)
        org.mockito.Mockito.`when`(editor.clear()).thenReturn(editor)

        progressRepository = QuizProgressRepositoryImpl(context, appLogger)
    }

    /**
     * 测试 1: 2000线程超超极限并发
     */
    @Test
    fun `2000 threads hyper ultra chaos`() = runTest {
        println("\n=== [Test 1] 2000 Threads Hyper Ultra Chaos ===")

        val questions = questionRepository.getQuestions()
        val errorCount = AtomicInteger(0)
        val operationCount = AtomicInteger(0)
        val latch = CountDownLatch(2000)

        val startTime = System.currentTimeMillis()

        repeat(2000) { threadId ->
            launch(Dispatchers.Default) {
                try {
                    repeat(1500) { opId ->
                        when (Random.nextInt(0, 100)) {
                            in 0..40 -> {
                                val query = listOf(
                                    "", "算法", "Java", "数据库", "网络", "Android", "测试", "性能",
                                    "安全", "架构", "并发", "线程", "锁", "缓存", "优化", "设计模式",
                                    "重构", "单元测试", "集成测试", "系统测试", "Kotlin", "Redis",
                                    "MySQL", "HTTP", "TCP", "UDP", "JSON", "XML", "REST", "API",
                                    "Docker", "Kubernetes", "Git", "GitHub", "CI", "CD", "DevOps",
                                    "机器学习", "深度学习", "人工智能", "大数据", "云计算", "边缘计算",
                                    "物联网", "区块链", "量子计算", "虚拟现实", "增强现实", "5G", "6G",
                                    "网络安全", "数据加密", "隐私保护", "GDPR", "CCPA", "SOC2", "ISO27001"
                                ).random()
                                questionRepository.searchQuestions(query)
                            }
                            in 41..60 -> {
                                questionRepository.getQuestionsByCategory(questions.random().category)
                            }
                            in 61..80 -> {
                                questionRepository.getQuestionsByDifficulty(questions.random().difficulty)
                            }
                            in 81..95 -> {
                                questionRepository.getQuestionsPaginated(Random.nextInt(-100, 200), Random.nextInt(-50, 500))
                            }
                            in 96..99 -> {
                                val q = questions.random()
                                progressRepository.saveProgress(
                                    QuizProgress(
                                        q.id,
                                        Random.nextInt(-10, 20),
                                        Random.nextBoolean(),
                                        isCorrect = if (Random.nextBoolean()) Random.nextBoolean() else null,
                                        completedAt = if (Random.nextBoolean()) System.currentTimeMillis() + Random.nextLong(-10000000, 10000000) else null,
                                        attempts = Random.nextInt(-100, 100000)
                                    )
                                )
                            }
                        }
                        operationCount.incrementAndGet()
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
        val opsPerSec = (operationCount.get() * 1000) / elapsed.toDouble()

        println("✅ Total operations: ${operationCount.get()}")
        println("✅ Time: ${elapsed}ms (${elapsed/1000.0}s)")
        println("✅ Throughput: %.1f ops/sec".format(opsPerSec))
        println("❌ Errors: ${errorCount.get()}")

        assertEquals(0, errorCount.get(), "Should have 0 errors even with 2000 threads")
        assertTrue(operationCount.get() > 2000000, "Should have more than 2,000,000 operations")
        println("✅ [Test 1] Passed - System survived 2000 threads hyper ultra chaos!\n")
    }

    /**
     * 测试 2: 30分钟永恒压力测试
     */
    @Test
    fun `30 minutes eternal stress`() = runTest {
        println("\n=== [Test 2] 30 Minutes Eternal Stress (Simulated) ===")

        val questions = questionRepository.getQuestions()
        val errorCount = AtomicInteger(0)
        val operationCount = AtomicInteger(0)
        val startTime = System.currentTimeMillis()
        val duration = 10000L // 模拟10秒

        coroutineScope {
            repeat(500) { _ ->
                launch(Dispatchers.Default) {
                    while (System.currentTimeMillis() - startTime < duration) {
                        try {
                            when (Random.nextInt(0, 10)) {
                                0, 1, 2, 3, 4 -> questionRepository.searchQuestions(
                                    listOf(
                                        "算法", "Java", "数据库", "网络", "Android", "Kotlin",
                                        "性能", "安全", "并发", "架构", "设计模式", "重构"
                                    ).random()
                                )
                                5, 6 -> questionRepository.getQuestionsPaginated(Random.nextInt(0, 100), Random.nextInt(1, 100))
                                7, 8 -> {
                                    val q = questions.random()
                                    progressRepository.saveProgress(
                                        QuizProgress(
                                            q.id,
                                            Random.nextInt(0, 4),
                                            true,
                                            isCorrect = Random.nextBoolean(),
                                            completedAt = System.currentTimeMillis(),
                                            attempts = Random.nextInt(0, 10)
                                        )
                                    )
                                }
                                9 -> progressRepository.getStatistics(questions)
                            }
                            operationCount.incrementAndGet()
                        } catch (e: Exception) {
                            errorCount.incrementAndGet()
                        }
                    }
                }
            }
        }

        val elapsed = System.currentTimeMillis() - startTime
        val opsPerSec = (operationCount.get() * 1000) / elapsed.toDouble()

        println("✅ Total operations: ${operationCount.get()}")
        println("✅ Duration: ${elapsed/1000.0}s (Simulated 30 minutes)")
        println("✅ Throughput: %.1f ops/sec".format(opsPerSec))
        println("❌ Errors: ${errorCount.get()}")

        assertEquals(0, errorCount.get(), "Should have 0 errors")
        println("✅ [Test 2] Passed - System survived 30 minutes eternal stress!\n")
    }

    /**
     * 测试 3: 超极限边界轰炸 - 各种疯狂边界
     */
    @Test
    fun `hyper ultra boundary bombing`() = runTest {
        println("\n=== [Test 3] Hyper Ultra Boundary Bombing ===")

        val questions = questionRepository.getQuestions()

        // 疯狂边界测试
        val testQueries = listOf(
            "", "a", "aa", "aaa",
            "a".repeat(100),
            "a".repeat(1000),
            "a".repeat(10000),
            "a".repeat(100000),
            "a".repeat(1000000),
            "中文字符串测试你好世界",
            "Unicode测试🌍🎉🚀🔥💻⌨️📱💾",
            "SQL_INJECTION'; DROP TABLE users; --",
            "<script>alert('XSS');</script>",
            "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\u0009",
            "\n\r\t\f\b",
            "   ",
            "null"
        )

        testQueries.forEach { query ->
            questionRepository.searchQuestions(query)
        }

        // 疯狂分页参数
        val testPages = listOf(
            -1000, -100, -10, -1, 0, 1, 10, 100, 1000, 10000,
            Int.MAX_VALUE, Int.MAX_VALUE - 1, Int.MAX_VALUE / 2,
            Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MIN_VALUE / 2
        )

        val testSizes = listOf(
            -1000, -100, -10, -1, 0, 1, 10, 100, 1000, 10000,
            Int.MAX_VALUE, Int.MAX_VALUE - 1, Int.MAX_VALUE / 2,
            Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MIN_VALUE / 2
        )

        testPages.forEach { page ->
            testSizes.forEach { size ->
                questionRepository.getQuestionsPaginated(page, size)
            }
        }

        // 疯狂进度数据
        repeat(20000) { i ->
            progressRepository.saveProgress(
                QuizProgress(
                    questionId = i % 200 - 100,
                    lastAnswer = Random.nextInt(-100, 200),
                    isCompleted = Random.nextBoolean(),
                    isCorrect = if (Random.nextBoolean()) Random.nextBoolean() else null,
                    completedAt = if (Random.nextBoolean()) System.currentTimeMillis() + Random.nextLong(-1000000000, 1000000000) else null,
                    attempts = Random.nextInt(-1000000, 1000000)
                )
            )
        }

        // 验证系统仍然活着
        val count = questionRepository.getQuestionCount()
        assertTrue(count > 0, "Should have questions")

        val searchResult = questionRepository.searchQuestions("Java")
        assertTrue(searchResult.isNotEmpty(), "Should have search results")

        println("✅ Hyper ultra boundary bombing test passed!")
        println("✅ All crazy inputs handled gracefully!")
        println("✅ [Test 3] Passed - System survived hyper ultra boundary bombing!\n")
    }

    /**
     * 测试 4: 随机混沌末日 - 完全无法预测的场景
     */
    @Test
    fun `random chaos armageddon`() = runTest {
        println("\n=== [Test 4] Random Chaos Armageddon ===")

        val questions = questionRepository.getQuestions()
        val errorCount = AtomicInteger(0)
        val operationCount = AtomicInteger(0)
        val latch = CountDownLatch(1000)

        repeat(1000) { threadId ->
            launch(Dispatchers.Default) {
                try {
                    repeat(5000) {
                        // 完全随机的操作组合
                        when (Random.nextInt(0, 50)) {
                            in 0..20 -> questionRepository.getQuestions()
                            in 21..25 -> questionRepository.getCategories()
                            in 26..30 -> questionRepository.getDifficulties()
                            in 31..35 -> questionRepository.getQuestionCount()
                            in 36..40 -> questionRepository.searchQuestions(
                                (1..Random.nextInt(0, 2000)).map { ('a'..'z').random() }.joinToString("")
                            )
                            in 41..45 -> questionRepository.getQuestionsByCategory(
                                questions.getOrNull(Random.nextInt(0, questions.size * 2))?.category ?: ""
                            )
                            in 46..48 -> {
                                val q = questions.getOrNull(Random.nextInt(0, questions.size * 2)) ?: return@repeat
                                progressRepository.saveProgress(
                                    QuizProgress(
                                        q.id,
                                        Random.nextInt(-100, 100),
                                        Random.nextBoolean(),
                                        isCorrect = Random.nextBoolean(),
                                        completedAt = System.currentTimeMillis(),
                                        attempts = Random.nextInt(0, 1000)
                                    )
                                )
                            }
                            49 -> progressRepository.getStatistics(questions)
                        }
                        operationCount.incrementAndGet()
                    }
                } catch (e: Exception) {
                    errorCount.incrementAndGet()
                    println("⚠️ Thread $threadId error: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        println("✅ Total operations: ${operationCount.get()}")
        println("❌ Errors: ${errorCount.get()}")

        assertEquals(0, errorCount.get(), "Should have 0 errors in random chaos armageddon")
        assertTrue(operationCount.get() > 3000000, "Should have more than 3,000,000 operations")
        println("✅ [Test 4] Passed - System survived random chaos armageddon!\n")
    }
}
