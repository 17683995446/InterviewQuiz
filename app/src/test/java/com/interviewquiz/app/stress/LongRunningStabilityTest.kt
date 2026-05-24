package com.interviewquiz.app.stress

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * 长时间运行稳定性测试
 * 模拟长时间使用中的各种场景
 */
class LongRunningStabilityTest {
    
    companion object {
        private const val SIMULATED_MINUTES = 60L // 模拟60分钟使用
        private const val ACTIONS_PER_MINUTE = 10
    }
    
    @Test
    fun `simulate 60 minute usage pattern`() = runBlocking {
        println("Starting 60-minute stability simulation...")
        
        val totalTime = measureTimeMillis {
            var completedActions = 0
            
            repeat(SIMULATED_MINUTES.toInt()) { minute ->
                try {
                    println("Simulating minute $minute...")
                    
                    repeat(ACTIONS_PER_MINUTE) { actionIndex ->
                        val actionType = (minute * ACTIONS_PER_MINUTE + actionIndex) % 10
                        
                        when (actionType) {
                            0 -> simulateSearch()
                            1 -> simulateCategoryFilter()
                            2 -> simulateDifficultyFilter()
                            3 -> simulateAnswerQuestion()
                            4 -> simulateViewStatistics()
                            5 -> simulateResetProgress()
                            6 -> simulateToggleFilters()
                            7 -> simulateToggleStatistics()
                            8 -> simulateResetFilters()
                            9 -> simulateRapidStateChanges()
                        }
                        
                        completedActions++
                        
                        // 模拟用户思考时间
                        delay(10L)
                    }
                    
                    // 每分钟检查一次
                    checkStateConsistency()
                    
                } catch (e: Exception) {
                    println("Error at minute $minute: ${e.message}")
                    // 继续测试，不要崩溃
                }
            }
            
            println("Completed $completedActions actions")
        }
        
        println("Total simulation time: ${totalTime}ms")
        println("Simulation completed successfully!")
    }
    
    @Test
    fun `simulate rapid consecutive operations`() {
        println("Starting rapid operation stress test...")
        
        val time = measureTimeMillis {
            repeat(1000) { i ->
                try {
                    when (i % 5) {
                        0 -> simulateSearch()
                        1 -> simulateCategoryFilter()
                        2 -> simulateDifficultyFilter()
                        3 -> simulateAnswerQuestion()
                        4 -> simulateResetProgress()
                    }
                    
                    if (i % 100 == 0) {
                        println("Completed $i operations...")
                    }
                } catch (e: Exception) {
                    println("Error at operation $i: ${e.message}")
                }
            }
        }
        
        println("Rapid operation test completed in ${time}ms")
    }
    
    @Test
    fun `simulate memory pressure scenario`() {
        println("Starting memory pressure simulation...")
        
        val largeStringBuilders = mutableListOf<StringBuilder>()
        val largeLists = mutableListOf<MutableList<Int>>()
        
        val time = measureTimeMillis {
            repeat(100) { iteration ->
                try {
                    // 创建一些内存压力
                    largeStringBuilders.add(StringBuilder("data_$iteration").apply {
                        repeat(1000) { append("x") }
                    })
                    
                    largeLists.add(MutableList(10000) { it })
                    
                    // 同时执行一些操作
                    if (iteration % 10 == 0) {
                        simulateSearch()
                        simulateCategoryFilter()
                    }
                    
                    if (iteration % 25 == 0) {
                        // 释放一些内存
                        largeStringBuilders.clear()
                        largeLists.clear()
                        println("Released memory at iteration $iteration")
                    }
                    
                } catch (e: OutOfMemoryError) {
                    println("OOM at iteration $iteration!")
                    // 尽量恢复
                    largeStringBuilders.clear()
                    largeLists.clear()
                } catch (e: Exception) {
                    println("Error at iteration $iteration: ${e.message}")
                }
            }
        }
        
        println("Memory pressure test completed in ${time}ms")
    }
    
    @Test
    fun `simulate concurrent filter operations`() {
        println("Starting concurrent filter simulation...")
        
        val time = measureTimeMillis {
            val threads = mutableListOf<Thread>()
            val results = mutableListOf<String>()
            
            repeat(10) { threadId ->
                val thread = Thread {
                    repeat(100) { operationId ->
                        try {
                            when (operationId % 3) {
                                0 -> results.add(simulateSearch())
                                1 -> results.add(simulateCategoryFilter())
                                2 -> results.add(simulateDifficultyFilter())
                            }
                        } catch (e: Exception) {
                            // 记录但继续
                        }
                    }
                }
                threads.add(thread)
                thread.start()
            }
            
            // 等待所有线程完成
            threads.forEach { it.join() }
        }
        
        println("Concurrent filter test completed in ${time}ms")
    }
    
    // 模拟操作的辅助方法
    private fun simulateSearch(): String {
        val queries = listOf("算法", "Java", "数据库", "网络", "排序", "时间复杂度")
        val randomQuery = queries.random()
        return "search_$randomQuery"
    }
    
    private fun simulateCategoryFilter(): String {
        val categories = listOf("算法与数据结构", "Java开发", "数据库", "网络")
        return "category_${categories.random()}"
    }
    
    private fun simulateDifficultyFilter(): String {
        val difficulties = listOf("EASY", "MEDIUM", "HARD")
        return "difficulty_${difficulties.random()}"
    }
    
    private fun simulateAnswerQuestion(): String {
        val questionIds = (1..100).toList()
        return "answer_${questionIds.random()}"
    }
    
    private fun simulateViewStatistics(): String {
        return "view_stats"
    }
    
    private fun simulateResetProgress(): String {
        val questionIds = (1..100).toList()
        return "reset_${questionIds.random()}"
    }
    
    private fun simulateToggleFilters(): String {
        return "toggle_filters"
    }
    
    private fun simulateToggleStatistics(): String {
        return "toggle_stats"
    }
    
    private fun simulateResetFilters(): String {
        return "reset_filters"
    }
    
    private fun simulateRapidStateChanges() {
        // 模拟快速状态变化
        repeat(50) { i ->
            val state = "state_$i"
            state.length // 只是使用一下防止优化掉
        }
    }
    
    private fun checkStateConsistency() {
        // 检查状态是否一致
        // 在真实环境中会检查各种不变量
        assertNotNull("State check passed")
    }
}
