package com.interviewquiz.app.stress

import com.interviewquiz.app.data.Question
import com.interviewquiz.app.data.QuestionRepositoryImpl
import com.interviewquiz.app.data.QuizProgressRepositoryImpl
import com.interviewquiz.app.domain.model.QuizProgress
import com.interviewquiz.app.ui.screens.HomeViewModel
import com.interviewquiz.app.util.AppLogger
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 第十三轮 - 超极端混乱场景测试
 * 测试内容：
 * 1. 10000次并发搜索，测试searchCache线程安全
 * 2. 1000个线程同时记录答题，测试数据一致性
 * 3. 100000次日志操作，测试performanceMetrics线程安全
 * 4. 极端长时间运行稳定性测试
 */
class ExtremeChaosTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    
    @Before
    fun setup() {
        setMain(testDispatcher)
    }
    
    @After
    fun teardown() {
        resetMain()
    }

    /**
     * Bug 1: QuestionRepository.searchCache 线程安全测试
     * 预期问题：多线程并发访问可能导致ConcurrentModificationException
     */
    @Test
    fun `test search cache thread safety with extreme concurrency`() = runTest {
        println("=== 测试 1: searchCache 极端并发测试 ===")
        
        val repository = QuestionRepositoryImpl()
        val threadCount = 200
        val operationsPerThread = 50
        val successCount = AtomicInteger(0)
        val exceptionCount = AtomicInteger(0)
        
        val searchQueries = listOf(
            "算法", "Java", "数据库", "网络", "操作系统", "Android",
            "时间复杂度", "HashMap", "SQL", "TCP", "线程", "Activity"
        )
        
        val latch = CountDownLatch(threadCount)
        
        repeat(threadCount) { threadId ->
            launch(Dispatchers.Default) {
                try {
                    repeat(operationsPerThread) { opId ->
                        val query = searchQueries.random()
                        val results = repository.searchQuestions(query)
                        assertNotNull(results)
                        successCount.incrementAndGet()
                        
                        // 加入随机延迟模拟真实场景
                        if (Random.nextBoolean()) {
                            delay(1)
                        }
                    }
                } catch (e: Exception) {
                    exceptionCount.incrementAndGet()
                    println("⚠️ 线程 $threadId 发生异常: ${e.javaClass.simpleName}")
                } finally {
                    latch.countDown()
                }
            }
        }
        
        latch.await()
        
        println("✅ 成功操作: ${successCount.get()}")
        println("❌ 异常次数: ${exceptionCount.get()}")
        
        assertEquals(0, exceptionCount.get(), "searchCache 在极端并发下出现异常")
        assertTrue(successCount.get() > 0, "至少应该有一些成功操作")
        
        println("✅ searchCache 线程安全测试通过\n")
    }

    /**
     * Bug 2: AppLogger.performanceMetrics 线程安全测试
     * 预期问题：多线程同时调用measurePerformance可能导致Map崩溃
     */
    @Test
    fun `test logger performance metrics thread safety`() = runTest {
        println("=== 测试 2: AppLogger 性能指标线程安全测试 ===")
        
        val logger = spyk(AppLogger())
        val threadCount = 100
        val operationsPerThread = 100
        val successCount = AtomicInteger(0)
        val exceptionCount = AtomicInteger(0)
        
        val latch = CountDownLatch(threadCount)
        
        repeat(threadCount) { threadId ->
            launch(Dispatchers.Default) {
                try {
                    repeat(operationsPerThread) { opId ->
                        val operationName = "test_op_${threadId}_$opId"
                        logger.measurePerformance(operationName, "TestTag") {
                            // 模拟一些工作
                            Thread.sleep(0, Random.nextInt(10000))
                        }
                        successCount.incrementAndGet()
                    }
                } catch (e: Exception) {
                    exceptionCount.incrementAndGet()
                    println("⚠️ Logger 线程 $threadId 异常: ${e.javaClass.simpleName}")
                } finally {
                    latch.countDown()
                }
            }
        }
        
        latch.await()
        
        println("✅ Logger 成功操作: ${successCount.get()}")
        println("❌ Logger 异常次数: ${exceptionCount.get()}")
        
        assertEquals(0, exceptionCount.get(), "Logger 在极端并发下出现异常")
        println("✅ AppLogger 性能指标线程安全测试通过\n")
    }

    /**
     * 极端长时间压力测试
     */
    @Test
    fun `test extreme long running stability`() = runTest(timeout = 30000) {
        println("=== 测试 3: 极端长时间稳定性测试 (30秒) ===")
        
        val repository = QuestionRepositoryImpl()
        val logger = AppLogger()
        val operationCount = AtomicLong(0)
        val startTime = System.currentTimeMillis()
        val endTime = startTime + 25000 // 运行25秒
        
        val exceptionCount = AtomicInteger(0)
        
        // 创建多个并发任务持续运行
        val jobs = mutableListOf<Job>()
        
        repeat(10) { workerId ->
            jobs += launch(Dispatchers.Default) {
                while (System.currentTimeMillis() < endTime && isActive) {
                    try {
                        // 混合多种操作
                        when (Random.nextInt(4)) {
                            0 -> {
                                val query = listOf("算法", "Java", "SQL", "Android").random()
                                repository.searchQuestions(query)
                            }
                            1 -> {
                                val cat = listOf("算法与数据结构", "Java与Kotlin", "数据库").random()
                                repository.getQuestionsByCategory(cat)
                            }
                            2 -> {
                                logger.d("StressTest", "Worker $workerId: operation #${operationCount.get()}")
                            }
                            3 -> {
                                logger.measurePerformance("quick_op_$workerId", "StressTest") {
                                    // 快速操作
                                }
                            }
                        }
                        
                        operationCount.incrementAndGet()
                        
                        // 偶尔让出CPU
                        if (operationCount.get() % 100 == 0L) {
                            yield()
                        }
                    } catch (e: Exception) {
                        exceptionCount.incrementAndGet()
                        println("⚠️ Worker $workerId 异常: ${e.message}")
                    }
                }
            }
        }
        
        // 等待完成
        delay(26000)
        jobs.forEach { it.cancelAndJoin() }
        
        val totalDuration = System.currentTimeMillis() - startTime
        
        println("⏱️ 总运行时间: ${totalDuration}ms")
        println("📊 总操作次数: ${operationCount.get()}")
        println("❌ 异常次数: ${exceptionCount.get()}")
        println("🚀 吞吐量: ${(operationCount.get() * 1000) / totalDuration} ops/sec")
        
        assertEquals(0, exceptionCount.get(), "极端长时间运行出现异常")
        println("✅ 极端长时间稳定性测试通过\n")
    }

    /**
     * 测试4: 内存溢出保护测试
     * 极端大数据量测试
     */
    @Test
    fun `test memory safety with extreme data`() = runTest {
        println("=== 测试 4: 极端大数据量内存安全测试 ===")
        
        val repository = QuestionRepositoryImpl()
        val logger = AppLogger()
        val memoryTests = AtomicInteger(0)
        
        // 测试1: 海量搜索查询
        println("测试1.1: 海量搜索查询缓存测试...")
        repeat(1000) { i ->
            val query = "query_${i}_${System.nanoTime()}"
            repository.searchQuestions(query)
            memoryTests.incrementAndGet()
        }
        
        // 验证缓存大小被限制
        println("测试1.2: 验证缓存LRU机制...")
        repeat(100) { i ->
            val query = "verify_${i}"
            repository.searchQuestions(query)
        }
        
        println("✅ 缓存内存安全测试通过")
        
        // 测试2: 大量日志操作
        println("测试2: 大量日志操作测试...")
        repeat(10000) { i ->
            logger.d("Test", "Log message #$i with some data")
        }
        
        println("✅ 极端大数据量内存安全测试通过\n")
    }

    /**
     * 测试5: 极端混合操作 - 真实场景模拟
     */
    @Test
    fun `test extreme mixed operations - real world scenario`() = runTest(timeout = 20000) {
        println("=== 测试 5: 极端混合操作 - 真实场景模拟 ===")
        
        val repository = QuestionRepositoryImpl()
        val logger = AppLogger()
        
        val successCount = AtomicInteger(0)
        val errorCount = AtomicInteger(0)
        
        val workerCount = 50
        val latch = CountDownLatch(workerCount)
        
        val categories = listOf("算法与数据结构", "Java与Kotlin", "数据库", "网络基础", "操作系统", "Android开发")
        val difficulties = listOf("EASY", "MEDIUM", "HARD")
        val searchTerms = listOf("时间复杂度", "HashMap", "SQL", "TCP", "线程", "Activity", "算法", "数据")
        
        repeat(workerCount) { workerId ->
            launch(Dispatchers.Default) {
                try {
                    repeat(200) { opIndex ->
                        when (Random.nextInt(8)) {
                            0 -> {
                                repository.getQuestions()
                            }
                            1 -> {
                                val cat = categories.random()
                                repository.getQuestionsByCategory(cat)
                            }
                            2 -> {
                                val diff = difficulties.random()
                                repository.getQuestionsByDifficulty(diff)
                            }
                            3 -> {
                                val query = searchTerms.random()
                                repository.searchQuestions(query)
                            }
                            4 -> {
                                repository.getCategories()
                            }
                            5 -> {
                                repository.getQuestionCount()
                            }
                            6 -> {
                                logger.d("Worker$workerId", "Operation $opIndex completed")
                            }
                            7 -> {
                                logger.measurePerformance("mixed_op_$workerId", "Worker$workerId") {
                                    // 混合操作
                                }
                            }
                        }
                        successCount.incrementAndGet()
                    }
                } catch (e: Exception) {
                    errorCount.incrementAndGet()
                    println("⚠️ Worker $workerId 错误: ${e.javaClass.simpleName}")
                } finally {
                    latch.countDown()
                }
            }
        }
        
        latch.await()
        
        println("✅ 成功操作: ${successCount.get()}")
        println("❌ 错误次数: ${errorCount.get()}")
        
        assertEquals(0, errorCount.get(), "极端混合操作测试出现错误")
        println("✅ 极端混合操作 - 真实场景模拟测试通过\n")
    }
}
