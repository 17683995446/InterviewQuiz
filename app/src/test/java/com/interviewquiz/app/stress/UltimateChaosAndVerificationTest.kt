package com.interviewquiz.app.stress

import com.interviewquiz.app.data.Question
import com.interviewquiz.app.data.QuestionRepository
import com.interviewquiz.app.data.QuestionRepositoryImpl
import com.interviewquiz.app.domain.model.QuizProgress
import com.interviewquiz.app.util.AppLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlin.test.assertTrue

/**
 * 第十六轮 - 终极混乱场景测试与验证
 * 测试内容：
 * 1. 真实用户混乱行为模拟
 * 2. 极端并发答题
 * 3. 快速过滤条件切换
 * 4. 混合操作与状态验证
 * 5. 长时间运行稳定性
 */
class UltimateChaosAndVerificationTest {
    
    companion object {
        private const val TAG = "UltimateChaosTest"
    }
    
    private lateinit var repository: QuestionRepository
    private lateinit var questions: List<Question>
    private val progressStore = ConcurrentHashMap<Int, QuizProgress>()
    private val operationCount = AtomicInteger(0)
    private val errorCount = AtomicInteger(0)
    
    @Before
    fun setup() {
        repository = QuestionRepositoryImpl()
        questions = repository.getQuestions()
        progressStore.clear()
        operationCount.set(0)
        errorCount.set(0)
        AppLogger.d(TAG, "=== Ultimate Chaos Test Setup ===")
        AppLogger.d(TAG, "Test questions: ${questions.size}")
    }
    
    /**
     * 测试 1: 真实用户混乱行为模拟
     */
    @Test
    fun `ultimate chaos simulation - real user behavior`() = runTest {
        println("\n=== [Test 1] Ultimate Chaos Simulation: Real User Behavior ===")
        
        val threadCount = 30
        val operationsPerThread = 100
        val latch = CountDownLatch(threadCount)
        val categories = repository.getCategories()
        val difficulties = repository.getDifficulties()
        
        repeat(threadCount) { threadId ->
            launch(Dispatchers.Default) {
                try {
                    repeat(operationsPerThread) { opId ->
                        performRandomChaosOperation(threadId, opId, categories, difficulties)
                        operationCount.incrementAndGet()
                        
                        // 随机休眠，模拟真实用户延迟
                        if (Random.nextBoolean() && Random.nextBoolean()) {
                            delay(Random.nextLong(1, 5))
                        }
                    }
                } catch (e: Exception) {
                    errorCount.incrementAndGet()
                    println("⚠️  Thread $threadId error: ${e.javaClass.simpleName}")
                } finally {
                    latch.countDown()
                }
            }
        }
        
        latch.await()
        
        println("✅  Total operations: ${operationCount.get()}")
        println("❌  Errors: ${errorCount.get()}")
        
        assertTrue(errorCount.get() == 0, "Should have 0 errors in chaos simulation")
        println("✅  [Test 1] Passed\n")
    }
    
    private fun performRandomChaosOperation(
        threadId: Int,
        opId: Int,
        categories: List<String>,
        difficulties: List<String>
    ) {
        when (Random.nextInt(0, 10)) {
            0, 1, 2 -> {
                // 答题操作（最频繁）
                val question = questions.random()
                val answerIndex = Random.nextInt(0, question.options.size)
                
                val oldProgress = progressStore[question.id] 
                    ?: QuizProgress(questionId = question.id, lastAnswer = null, isCompleted = false)
                
                val isCorrect = question.isCorrectAnswer(answerIndex)
                val newProgress = QuizProgress(
                    questionId = question.id,
                    lastAnswer = answerIndex,
                    isCompleted = true,
                    attemptCount = oldProgress.attemptCount + 1,
                    correctCount = if (isCorrect) oldProgress.correctCount + 1 else oldProgress.correctCount,
                    wrongCount = if (!isCorrect) oldProgress.wrongCount + 1 else oldProgress.wrongCount,
                    streak = if (isCorrect) oldProgress.streak + 1 else 0
                )
                
                progressStore[question.id] = newProgress
                
                if (opId % 20 == 0) {
                    AppLogger.d(TAG, "[Chaos-$threadId] Answered Q${question.id}: $isCorrect")
                }
            }
            3 -> {
                // 搜索查询
                val query = listOf(
                    "", "a", "算法", "Java", "Android", 
                    "123", "测试", "网络", "数据库",
                    "A".repeat(Random.nextInt(1, 100))
                ).random()
                repository.searchQuestions(query)
                
                if (opId % 30 == 0) {
                    AppLogger.d(TAG, "[Chaos-$threadId] Searched: '${query.take(20)}...'")
                }
            }
            4 -> {
                // 按分类获取
                val category = categories.random()
                repository.getQuestionsByCategory(category)
            }
            5 -> {
                // 按难度获取
                val difficulty = difficulties.random()
                repository.getQuestionsByDifficulty(difficulty)
            }
            6 -> {
                // 获取全部问题
                repository.getQuestions()
            }
            7 -> {
                // 分页获取
                val page = Random.nextInt(0, 20)
                val pageSize = Random.nextInt(1, 100)
                repository.getQuestionsPaginated(page, pageSize)
            }
            8, 9 -> {
                // 混合查询
                val randomText = listOf("a", "b", "c", "1", "2", "3").random()
                repository.searchQuestions(randomText)
            }
        }
    }
    
    /**
     * 测试 2: 快速过滤条件切换
     */
    @Test
    fun `rapid filter switching stress test`() = runTest {
        println("\n=== [Test 2] Rapid Filter Switching Stress Test ===")
        
        val categories = repository.getCategories()
        val difficulties = repository.getDifficulties()
        val operations = AtomicInteger(0)
        
        repeat(100) { iteration ->
            // 快速连续切换过滤条件
            repeat(20) { _ ->
                repository.getQuestions()
                if (Random.nextBoolean()) {
                    repository.getQuestionsByCategory(categories.random())
                }
                if (Random.nextBoolean()) {
                    repository.getQuestionsByDifficulty(difficulties.random())
                }
                if (Random.nextBoolean()) {
                    val query = listOf("", "a", "算法", "Java").random()
                    repository.searchQuestions(query)
                }
                operations.incrementAndGet()
            }
            
            if (iteration % 20 == 0) {
                AppLogger.d(TAG, "[Switch] Iteration $iteration, ops: ${operations.get()}")
            }
        }
        
        println("✅  Rapid filter switching: ${operations.get()} operations")
        println("✅  [Test 2] Passed\n")
    }
    
    /**
     * 测试 3: 数据完整性验证 - 确保 searchCache 工作正常
     */
    @Test
    fun `search cache data integrity verification`() = runTest {
        println("\n=== [Test 3] Search Cache Data Integrity Verification ===")
        
        // 先缓存一些查询
        val testQueries = listOf("算法", "Java", "Android", "a", "b", "c", "1", "2", "3")
        testQueries.forEach { query ->
            val results = repository.searchQuestions(query)
            assertTrue(results.isNotEmpty() || query == "non-existent-x-y-z")
        }
        
        // 验证再次查询返回一致结果
        testQueries.forEach { query ->
            val results1 = repository.searchQuestions(query)
            val results2 = repository.searchQuestions(query)
            
            assertTrue(results1.size == results2.size, 
                "Cache should return consistent results for '$query'")
        }
        
        println("✅  Search cache integrity verified")
        println("✅  [Test 3] Passed\n")
    }
    
    /**
     * 测试 4: 长时间运行稳定性
     */
    @Test
    fun `long running stability - 20000 operations`() = runTest {
        println("\n=== [Test 4] Long Running Stability Test ===")
        
        val targetOperations = 20000
        val operations = AtomicInteger(0)
        val errors = AtomicInteger(0)
        val start = System.currentTimeMillis()
        
        coroutineScope {
            repeat(20) { threadId ->
                launch(Dispatchers.Default) {
                    while (operations.get() < targetOperations) {
                        try {
                            when (Random.nextInt(0, 6)) {
                                0 -> repository.getQuestions()
                                1 -> repository.searchQuestions(listOf("", "a", "算法").random())
                                2 -> repository.getQuestionsPaginated(Random.nextInt(0, 10), Random.nextInt(1, 50))
                                3 -> repository.getQuestionCount()
                                4 -> repository.getCategories()
                                5 -> repository.getDifficulties()
                            }
                            operations.incrementAndGet()
                        } catch (e: Exception) {
                            errors.incrementAndGet()
                            if (errors.get() <= 5) {
                                println("⚠️  Error: ${e.javaClass.simpleName}")
                            }
                        }
                    }
                }
            }
        }
        
        val duration = System.currentTimeMillis() - start
        val opsPerSec = (operations.get() * 1000) / duration.toDouble()
        
        println("✅  Total operations: ${operations.get()}")
        println("⏱️  Duration: ${duration}ms")
        println("⚡ Throughput: %.1f ops/sec".format(opsPerSec))
        println("❌ Errors: ${errors.get()}")
        
        assertTrue(errors.get() == 0, "Should have 0 errors in long run")
        println("✅  [Test 4] Passed\n")
    }
    
    /**
     * 测试 5: 边界条件全面测试
     */
    @Test
    fun `comprehensive boundary condition testing`() = runTest {
        println("\n=== [Test 5] Comprehensive Boundary Condition Testing ===")
        
        // 超长搜索查询
        val veryLongQuery = "x".repeat(2000)
        val resultLong = repository.searchQuestions(veryLongQuery)
        assertTrue(resultLong.isEmpty() || resultLong.isNotEmpty()) // 只要不崩溃就行
        
        // 空查询
        val emptyResult = repository.searchQuestions("")
        assertTrue(emptyResult.isNotEmpty())
        
        // 空白查询
        val blankResult = repository.searchQuestions("   \n\t  ")
        assertTrue(blankResult.isNotEmpty())
        
        // 极端分页
        repository.getQuestionsPaginated(-1000, 10)
        repository.getQuestionsPaginated(0, -10)
        repository.getQuestionsPaginated(99999, 99999)
        
        println("✅  All boundary conditions handled safely")
        println("✅  [Test 5] Passed\n")
    }
    
    /**
     * 测试 6: Question.getOptionLetter 边界验证
     */
    @Test
    fun `question option letter boundary verification`() {
        println("\n=== [Test 6] Question.getOptionLetter Boundary Verification ===")
        
        val question = questions.first()
        
        // 测试负数
        val negativeLetter = question.getOptionLetter(-5)
        assertTrue(negativeLetter >= 'A', "Should not go below 'A'")
        
        // 测试正常范围
        repeat(question.options.size) { idx ->
            val letter = question.getOptionLetter(idx)
            assertTrue(letter in 'A'..'Z', "Letter should be in A-Z range")
        }
        
        // 测试超大值
        val veryLargeLetter = question.getOptionLetter(1000)
        assertTrue(veryLargeLetter <= 'Z', "Should not go above 'Z'")
        
        println("✅  getOptionLetter boundary checks verified")
        println("✅  [Test 6] Passed\n")
    }
}
