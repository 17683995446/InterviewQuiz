package com.interviewquiz.app.stress

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
 * 第22轮 - 超超极限混沌测试
 * 挑战系统真正的不可能边界
 */
@RunWith(MockitoJUnitRunner::class)
class HyperChaosTest {

    companion object {
        private const val TAG = "HyperChaosTest"
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

        // Mock SharedPreferences
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
     * 测试 1: 1000线程超级混沌 - 突破系统绝对极限
     */
    @Test
    fun `1000 threads super chaos`() = runTest {
        println("\n=== [Test 1] 1000 Threads Super Chaos ===")

        val questions = questionRepository.getQuestions()
        val errorCount = AtomicInteger(0)
        val operationCount = AtomicInteger(0)
        val latch = CountDownLatch(1000)

        val startTime = System.currentTimeMillis()

        repeat(1000) { threadId ->
            launch(Dispatchers.Default) {
                try {
                    repeat(2000) { opId ->
                        when (Random.nextInt(0, 100)) {
                            in 0..40 -> { // 40% - 搜索
                                val query = listOf(
                                    "", "算法", "Java", "数据库", "网络", "Android", "测试", "性能",
                                    "安全", "架构", "并发", "线程", "锁", "缓存", "性能优化", "设计模式",
                                    "重构", "单元测试", "集成测试", "系统测试"
                                ).random()
                                questionRepository.searchQuestions(query)
                            }
                            in 41..60 -> { // 20% - 分类筛选
                                questionRepository.getQuestionsByCategory(questions.random().category)
                            }
                            in 61..80 -> { // 20% - 难度筛选
                                questionRepository.getQuestionsByDifficulty(questions.random().difficulty)
                            }
                            in 81..95 -> { // 15% - 分页
                                questionRepository.getQuestionsPaginated(Random.nextInt(0, 100), Random.nextInt(1, 50))
                            }
                            in 96..99 -> { // 4% - 保存进度
                                val q = questions.random()
                                progressRepository.saveProgress(
                                    QuizProgress(
                                        q.id,
                                        Random.nextInt(0, 4),
                                        true,
                                        isCorrect = Random.nextBoolean(),
                                        completedAt = System.currentTimeMillis(),
                                        attempts = Random.nextInt(0, 100)
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
        println("✅ Time: ${elapsed}ms")
        println("✅ Throughput: %.1f ops/sec".format(opsPerSec))
        println("❌ Errors: ${errorCount.get()}")

        assertEquals(0, errorCount.get(), "Should have 0 errors even with 1000 threads")
        assertTrue(operationCount.get() > 1000000, "Should have more than 1000000 operations")
        println("✅ [Test 1] Passed - System survived 1000 threads super chaos!\n")
    }

    /**
     * 测试 2: 10分钟永恒测试 - 挑战系统极限运行时间
     */
    @Test
    fun `10 minute eternal test`() = runTest {
        println("\n=== [Test 2] 10 Minute Eternal Test ===")

        val questions = questionRepository.getQuestions()
        val errorCount = AtomicInteger(0)
        val operationCount = AtomicInteger(0)
        val startTime = System.currentTimeMillis()
        val duration = 600000L // 10 minutes

        coroutineScope {
            repeat(200) { _ ->
                launch(Dispatchers.Default) {
                    while (System.currentTimeMillis() - startTime < duration) {
                        try {
                            when (Random.nextInt(0, 10)) {
                                0, 1, 2, 3, 4 -> questionRepository.searchQuestions(
                                    listOf(
                                        "算法", "Java", "数据库", "网络", "Android", "Kotlin",
                                        "性能", "安全", "并发", "架构"
                                    ).random()
                                )
                                5, 6 -> questionRepository.getQuestionsPaginated(Random.nextInt(0, 100), Random.nextInt(1, 50))
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
                                9 -> progressRepository.getAllProgress()
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
        println("✅ Duration: ${elapsed/1000}s (10 minutes)")
        println("✅ Throughput: %.1f ops/sec".format(opsPerSec))
        println("❌ Errors: ${errorCount.get()}")

        assertEquals(0, errorCount.get(), "Should have 0 errors")
        assertTrue(operationCount.get() > 200000, "Should have more than 200000 operations")
        println("✅ [Test 2] Passed - System survived 10 minutes eternal test!\n")
    }

    /**
     * 测试 3: 无限混沌压力 - 持续直到失败
     */
    @Test
    fun `infinite chaos pressure test`() = runTest {
        println("\n=== [Test 3] Infinite Chaos Pressure Test ===")

        val questions = questionRepository.getQuestions()
        val errorCount = AtomicInteger(0)
        val operationCount = AtomicInteger(0)
        val startTime = System.currentTimeMillis()
        val maxDuration = 60000L // 1 minute max but tests resilience
        val maxOperations = 500000 // or 500k operations

        coroutineScope {
            repeat(300) { _ ->
                launch(Dispatchers.Default) {
                    while (operationCount.get() < maxOperations && System.currentTimeMillis() - startTime < maxDuration) {
                        try {
                            when (Random.nextInt(0, 20)) {
                                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 -> {
                                    questionRepository.searchQuestions(
                                        listOf(
                                            "算法", "Java", "Kotlin", "Android", "数据库", "网络",
                                            "操作系统", "安全", "架构", "测试", "性能", "并发",
                                            "线程", "锁", "缓存", "优化", "设计", "模式"
                                        ).random()
                                    )
                                }
                                18 -> {
                                    val q = questions.random()
                                    progressRepository.saveProgress(
                                        QuizProgress(
                                            q.id,
                                            Random.nextInt(0, 4),
                                            true,
                                            isCorrect = Random.nextBoolean(),
                                            completedAt = System.currentTimeMillis(),
                                            attempts = Random.nextInt(0, 100)
                                        )
                                    )
                                }
                                19 -> progressRepository.getStatistics(questions)
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
        println("✅ Duration: ${elapsed}ms")
        println("✅ Throughput: %.1f ops/sec".format(opsPerSec))
        println("❌ Errors: ${errorCount.get()}")

        assertEquals(0, errorCount.get(), "Should have 0 errors")
        println("✅ [Test 3] Passed - System survived infinite chaos pressure!\n")
    }

    /**
     * 测试 4: 超超边界测试 - 突破所有不可能边界
     */
    @Test
    fun `hyper boundary impossibility test`() = runTest {
        println("\n=== [Test 4] Hyper Boundary Impossibility Test ===")

        // 突破所有数学极限
        questionRepository.getQuestionsPaginated(Int.MAX_VALUE, Int.MAX_VALUE)
        questionRepository.getQuestionsPaginated(Int.MIN_VALUE, Int.MIN_VALUE)
        questionRepository.getQuestionsPaginated(Int.MAX_VALUE / 2, Int.MAX_VALUE / 2)
        questionRepository.getQuestionsPaginated(Int.MIN_VALUE / 2 + 1, Int.MIN_VALUE / 2 + 1)

        // 突破所有字符串极限
        questionRepository.searchQuestions("a".repeat(1000000)) // 1MB
        questionRepository.searchQuestions("中文字符串测试 你好世界 Hello World 🌍🎉🚀") // Emoji + Unicode
        questionRepository.searchQuestions("SQL_INJECTION'; DROP TABLE users; --") // SQL注入
        questionRepository.searchQuestions("<script>alert('XSS');</script>") // XSS
        questionRepository.searchQuestions("\u0000\u0001\u0002\u0003\u0004\u0005\u0006") // 控制字符
        questionRepository.searchQuestions("null\u0000null\u0001null") // 混合NULL
        questionRepository.searchQuestions("   \t\n\r\f\b   \u0000   ") // 混合空白

        // 突破所有数据极限
        repeat(10000) { i ->
            progressRepository.saveProgress(
                QuizProgress(
                    questionId = i,
                    lastAnswer = Random.nextInt(0, 10),
                    isCompleted = Random.nextBoolean(),
                    isCorrect = if (Random.nextBoolean()) Random.nextBoolean() else null,
                    completedAt = if (Random.nextBoolean()) System.currentTimeMillis() + Random.nextLong(-1000000, 1000000) else null,
                    attempts = Random.nextInt(0, 1000000)
                )
            )
        }

        // 验证系统仍然活着
        val count = questionRepository.getQuestionCount()
        assertTrue(count > 0, "Should have questions")

        val searchResult = questionRepository.searchQuestions("Java")
        assertTrue(searchResult.isNotEmpty(), "Should have search results")

        val stats = progressRepository.getStatistics(questionRepository.getQuestions())
        assertTrue(stats.totalQuestions >= 0, "Stats should be valid")

        println("✅ All hyper boundary impossibility tests handled safely")
        println("✅ [Test 4] Passed - System survived all impossibilities!\n")
    }

    /**
     * 测试 5: 随机混沌末日 - 完全不可预测的场景
     */
    @Test
    fun `random chaos armageddon`() = runTest {
        println("\n=== [Test 5] Random Chaos Armageddon ===")

        val questions = questionRepository.getQuestions()
        val errorCount = AtomicInteger(0)
        val operationCount = AtomicInteger(0)
        val latch = CountDownLatch(500)

        repeat(500) { threadId ->
            launch(Dispatchers.Default) {
                try {
                    repeat(3000) {
                        // 完全随机的操作组合
                        val operation = Random.nextInt(0, 100)

                        when {
                            operation < 30 -> questionRepository.getQuestions()
                            operation < 45 -> questionRepository.getCategories()
                            operation < 60 -> questionRepository.getDifficulties()
                            operation < 75 -> questionRepository.getQuestionCount()
                            operation < 85 -> questionRepository.searchQuestions(
                                (1..Random.nextInt(1, 100)).map { ('a'..'z').random() }.joinToString("")
                            )
                            operation < 92 -> questionRepository.getQuestionsByCategory(
                                questions.getOrNull(Random.nextInt(0, questions.size))?.category ?: ""
                            )
                            operation < 97 -> {
                                val q = questions.getOrNull(Random.nextInt(0, questions.size)) ?: continue
                                progressRepository.saveProgress(
                                    QuizProgress(
                                        q.id,
                                        Random.nextInt(0, 4),
                                        Random.nextBoolean(),
                                        isCorrect = Random.nextBoolean(),
                                        completedAt = System.currentTimeMillis(),
                                        attempts = Random.nextInt(0, 100)
                                    )
                                )
                            }
                            operation < 99 -> progressRepository.getAllProgress()
                            else -> progressRepository.getStatistics(questions)
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
        assertTrue(operationCount.get() > 1000000, "Should have more than 1000000 operations")
        println("✅ [Test 5] Passed - System survived random chaos armageddon!\n")
    }

    /**
     * 测试 6: 终极稳定性测试 - 混合所有极端场景
     */
    @Test
    fun `ultimate stability test`() = runTest {
        println("\n=== [Test 6] Ultimate Stability Test ===")

        val questions = questionRepository.getQuestions()
        val errorCount = AtomicInteger(0)
        val operationCount = AtomicInteger(0)
        val startTime = System.currentTimeMillis()
        val duration = 120000L // 2 minutes

        // 阶段1: 纯搜索 (0-20秒)
        // 阶段2: 纯保存 (20-40秒)
        // 阶段3: 混合操作 (40-60秒)
        // 阶段4: 极限边界 (60-80秒)
        // 阶段5: 混沌操作 (80-100秒)
        // 阶段6: 全面混合 (100-120秒)

        coroutineScope {
            repeat(400) { threadId ->
                launch(Dispatchers.Default) {
                    var phase = 0
                    while (System.currentTimeMillis() - startTime < duration) {
                        val elapsed = System.currentTimeMillis() - startTime
                        phase = when {
                            elapsed < 20000 -> 1 // 纯搜索
                            elapsed < 40000 -> 2 // 纯保存
                            elapsed < 60000 -> 3 // 混合
                            elapsed < 80000 -> 4 // 极限
                            elapsed < 100000 -> 5 // 混沌
                            else -> 6 // 全面
                        }

                        try {
                            when (phase) {
                                1 -> questionRepository.searchQuestions("算法")
                                2 -> {
                                    val q = questions.random()
                                    progressRepository.saveProgress(
                                        QuizProgress(
                                            q.id,
                                            Random.nextInt(0, 4),
                                            true,
                                            isCorrect = Random.nextBoolean(),
                                            completedAt = System.currentTimeMillis(),
                                            attempts = 1
                                        )
                                    )
                                }
                                3 -> {
                                    when (Random.nextInt(0, 3)) {
                                        0 -> questionRepository.searchQuestions("Java")
                                        1 -> {
                                            val q = questions.random()
                                            progressRepository.saveProgress(
                                                QuizProgress(
                                                    q.id,
                                                    Random.nextInt(0, 4),
                                                    true,
                                                    isCorrect = Random.nextBoolean(),
                                                    completedAt = System.currentTimeMillis(),
                                                    attempts = 1
                                                )
                                            )
                                        }
                                        else -> questionRepository.getQuestionsPaginated(0, 20)
                                    }
                                }
                                4 -> questionRepository.getQuestionsPaginated(Random.nextInt(0, 100), Random.nextInt(1, 1000))
                                5 -> questionRepository.searchQuestions("a".repeat(Random.nextInt(0, 10000)))
                                else -> {
                                    when (Random.nextInt(0, 10)) {
                                        0, 1, 2, 3 -> questionRepository.searchQuestions("测试")
                                        4, 5 -> questionRepository.getQuestionsPaginated(0, 20)
                                        6, 7, 8 -> {
                                            val q = questions.random()
                                            progressRepository.saveProgress(
                                                QuizProgress(
                                                    q.id,
                                                    Random.nextInt(0, 4),
                                                    true,
                                                    isCorrect = Random.nextBoolean(),
                                                    completedAt = System.currentTimeMillis(),
                                                    attempts = 1
                                                )
                                            )
                                        }
                                        else -> progressRepository.getStatistics(questions)
                                    }
                                }
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
        println("✅ Duration: ${elapsed/1000}s (2 minutes)")
        println("✅ Throughput: %.1f ops/sec".format(opsPerSec))
        println("❌ Errors: ${errorCount.get()}")

        assertEquals(0, errorCount.get(), "Should have 0 errors")
        assertTrue(operationCount.get() > 300000, "Should have more than 300000 operations")
        println("✅ [Test 6] Passed - Ultimate stability verified!\n")
    }
}
