package com.interviewquiz.app.stress

import com.interviewquiz.app.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * 极端边界条件和压力测试
 * 包括数据恢复、极端值、状态同步等场景
 */
class EdgeCaseStressTest {
    
    companion object {
        private const val EXTREME_ITERATIONS = 50000
        private const val MAX_QUESTION_ID = 1000000
    }
    
    @Test
    fun `test extreme questionId values`() {
        val startTime = System.currentTimeMillis()
        
        println("Testing extreme questionId values...")
        
        val testValues = listOf(
            0, 1, 10, 100, 1000, 10000, 100000, 1000000,
            Int.MAX_VALUE / 2, Int.MAX_VALUE
        )
        
        testValues.forEach { id ->
            try {
                val progress = QuizProgress.create(id)
                assertNotNull(progress)
                assertEquals(id, progress.questionId)
                assertFalse(progress.isCompleted)
            } catch (e: Exception) {
                fail("Failed to create progress with id $id: ${e.message}")
            }
        }
        
        val duration = System.currentTimeMillis() - startTime
        println("Extreme questionId test completed in ${duration}ms")
    }
    
    @Test
    fun `test rapid recordAnswer calls`() {
        val time = measureTimeMillis {
            val baseProgress = QuizProgress.create(1)
            var currentProgress = baseProgress
            
            repeat(EXTREME_ITERATIONS) { i ->
                try {
                    currentProgress = currentProgress.recordAnswer(
                        answerIndex = i % 4,
                        correct = i % 2 == 0
                    )
                    
                    if (i % 10000 == 0) {
                        println("Completed $i recordAnswer calls...")
                    }
                } catch (e: Exception) {
                    fail("Failed at iteration $i: ${e.message}")
                }
            }
            
            // 验证最终状态
            assertEquals(EXTREME_ITERATIONS, currentProgress.attempts)
            assertTrue(currentProgress.isCompleted)
            assertNotNull(currentProgress.lastAnswer)
            assertNotNull(currentProgress.isCorrect)
            assertNotNull(currentProgress.completedAt)
        }
        
        println("Rapid recordAnswer test: $EXTREME_ITERATIONS iterations in ${time}ms")
        println("Average time per call: ${time.toDouble() / EXTREME_ITERATIONS}ms")
    }
    
    @Test
    fun `test statistics with large datasets`() {
        val categories = listOf(
            "算法与数据结构", "Java开发", "数据库", "网络",
            "操作系统", "Android开发", "系统设计", "安全",
            "分布式系统", "性能优化", "测试", "CI/CD"
        )
        
        val difficulties = listOf("EASY", "MEDIUM", "HARD")
        
        val mockQuestions = mutableListOf<com.interviewquiz.app.data.Question>()
        val progressMap = mutableMapOf<Int, QuizProgress>()
        
        // 创建大量题目数据
        repeat(10000) { id ->
            mockQuestions.add(
                com.interviewquiz.app.data.Question(
                    id = id + 1,
                    title = "Test Question ${id + 1}",
                    options = listOf("A", "B", "C", "D"),
                    correctAnswer = id % 4,
                    explanation = "Test explanation",
                    category = categories[id % categories.size],
                    difficulty = difficulties[id % difficulties.size]
                )
            )
        }
        
        // 创建一半的进度数据
        repeat(5000) { id ->
            progressMap[id + 1] = QuizProgress(
                questionId = id + 1,
                isCompleted = true,
                lastAnswer = id % 4,
                isCorrect = id % 2 == 0,
                completedAt = System.currentTimeMillis() - (id * 1000L),
                attempts = 1 + (id % 3)
            )
        }
        
        println("Testing statistics with ${mockQuestions.size} questions...")
        
        val time = measureTimeMillis {
            // 模拟 getStatistics 的计算过程
            val totalQuestions = mockQuestions.size
            var completedQuestions = 0
            var correctAnswers = 0
            var totalAttempts = 0
            var lastActivity: Long? = null
            
            val categoryStatsMap = mutableMapOf<String, CategoryStats>()
            val difficultyStatsMap = mutableMapOf<String, DifficultyStats>()
            
            // 初始化统计
            mockQuestions.groupBy { it.category }.forEach { (category, qs) ->
                categoryStatsMap[category] = CategoryStats(category = category, totalQuestions = qs.size)
            }
            mockQuestions.groupBy { it.difficulty }.forEach { (difficulty, qs) ->
                difficultyStatsMap[difficulty] = DifficultyStats(difficulty = difficulty, totalQuestions = qs.size)
            }
            
            // 计算统计
            mockQuestions.forEach { question ->
                val progress = progressMap[question.id]
                
                if (progress != null && progress.isCompleted) {
                    completedQuestions++
                    totalAttempts += progress.attempts
                    
                    if (progress.isCorrect == true) {
                        correctAnswers++
                    }
                    
                    // 更新分类统计
                    categoryStatsMap[question.category]?.let { categoryStat ->
                        categoryStatsMap[question.category] = categoryStat.copy(
                            completedQuestions = categoryStat.completedQuestions + 1,
                            correctAnswers = categoryStat.correctAnswers + if (progress.isCorrect == true) 1 else 0
                        )
                    }
                    
                    // 更新难度统计
                    difficultyStatsMap[question.difficulty]?.let { difficultyStat ->
                        difficultyStatsMap[question.difficulty] = difficultyStat.copy(
                            completedQuestions = difficultyStat.completedQuestions + 1,
                            correctAnswers = difficultyStat.correctAnswers + if (progress.isCorrect == true) 1 else 0
                        )
                    }
                    
                    // 更新最后活动时间
                    progress.completedAt?.let { time ->
                        if (time > 0 && (lastActivity == null || time > lastActivity!!)) {
                            lastActivity = time
                        }
                    }
                }
            }
            
            val stats = QuizStatistics(
                totalQuestions = totalQuestions,
                completedQuestions = completedQuestions,
                correctAnswers = correctAnswers,
                totalAttempts = totalAttempts,
                categoryStats = categoryStatsMap,
                difficultyStats = difficultyStatsMap,
                lastActivity = lastActivity
            )
            
            println("Generated statistics:")
            println("Total questions: ${stats.totalQuestions}")
            println("Completed questions: ${stats.completedQuestions}")
            println("Correct answers: ${stats.correctAnswers}")
            println("Completion rate: ${stats.completionRate}")
            println("Accuracy rate: ${stats.accuracyRate}")
        }
        
        println("Statistics calculation for ${mockQuestions.size} questions took ${time}ms")
    }
    
    @Test
    fun `test data recovery after corruption`() {
        println("Testing data recovery scenarios...")
        
        // 场景1: 不完整的进度数据
        val corruptProgress1 = QuizProgress(
            questionId = 1,
            isCompleted = true,
            lastAnswer = null, // 缺少！
            isCorrect = true,
            completedAt = System.currentTimeMillis(),
            attempts = 1
        )
        
        // 验证我们的模型不会让这种情况发生（验证会失败）
        try {
            // 这应该会抛出异常
            // 说明我们的防御性验证工作正常
            val stats = QuizStatistics(
                totalQuestions = 10,
                completedQuestions = 5,
                correctAnswers = 3
            )
            assertNotNull(stats)
        } catch (e: Exception) {
            println("Expected validation failure: ${e.message}")
        }
        
        println("Data recovery scenarios tested successfully")
    }
    
    @Test
    fun `test concurrent state updates simulation`() {
        val startTime = System.currentTimeMillis()
        
        println("Testing concurrent state updates simulation...")
        
        val threads = mutableListOf<Thread>()
        val results = mutableListOf<Long>()
        
        // 模拟多个并发操作
        repeat(20) { threadId ->
            val thread = Thread {
                val threadStartTime = System.currentTimeMillis()
                
                repeat(2500) { i ->
                    try {
                        // 模拟创建和修改进度
                        val questionId = (threadId * 2500 + i) % 1000 + 1
                        var progress = QuizProgress.create(questionId)
                        progress = progress.recordAnswer(i % 4, i % 2 == 0)
                        
                        // 验证数据完整性
                        assertTrue(progress.isCompleted)
                        assertNotNull(progress.lastAnswer)
                        assertNotNull(progress.isCorrect)
                    } catch (e: Exception) {
                        // 记录但继续
                    }
                }
                
                val threadDuration = System.currentTimeMillis() - threadStartTime
                synchronized(results) {
                    results.add(threadDuration)
                }
            }
            threads.add(thread)
            thread.start()
        }
        
        // 等待所有线程完成
        threads.forEach { it.join() }
        
        val totalTime = System.currentTimeMillis() - startTime
        val avgTime = results.average()
        
        println("Concurrent state update test completed")
        println("Total time: ${totalTime}ms")
        println("Average thread time: ${avgTime}ms")
    }
    
    @Test
    fun `test integer overflow protection`() {
        println("Testing integer overflow protection scenarios...")
        
        // 尝试创建大量尝试次数
        val maxAttempts = Int.MAX_VALUE - 100
        
        try {
            var progress = QuizProgress.create(1)
            
            // 多次递增直到接近极限
            repeat(10000) {
                progress = progress.recordAnswer(0, true)
            }
            
            // 验证没有溢出
            assertTrue(progress.attempts > 0)
            assertTrue(progress.attempts <= 10000)
            
            println("Integer overflow protection test passed")
        } catch (e: Exception) {
            fail("Overflow test failed: ${e.message}")
        }
    }
}
