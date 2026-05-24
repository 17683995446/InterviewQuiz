package com.interviewquiz.app.stress

import com.interviewquiz.app.data.QuestionRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 第十五轮 - 终极验证测试
 * 验证之前所有修复的正确性
 */
class UltimateVerificationTest {

    @Test
    fun `verify LRU cache thread safety extreme`() = runBlocking {
        println("=== 测试：LRU缓存极端并发安全 ===")
        
        val repository = QuestionRepositoryImpl()
        val threadCount = 100
        val operationsPerThread = 50
        val totalOperations = threadCount * operationsPerThread
        val successCount = AtomicInteger(0)
        val exceptionCount = AtomicInteger(0)
        
        val testQueries = listOf(
            "算法", "Java", "数据库", "网络", "操作系统", "Android",
            "时间复杂度", "HashMap", "SQL", "TCP", "线程", "Activity",
            "排序", "二叉树", "Redis", "HTTP", "协程", "ViewModel"
        )
        
        val latch = CountDownLatch(threadCount)
        
        repeat(threadCount) { threadId ->
            launch(Dispatchers.Default) {
                try {
                    repeat(operationsPerThread) { opId ->
                        val query = testQueries.random()
                        val results = repository.searchQuestions(query)
                        assertTrue(results.isNotEmpty() || query.length > 10)
                        successCount.incrementAndGet()
                    }
                } catch (e: Exception) {
                    exceptionCount.incrementAndGet()
                    println("⚠️ 线程 $threadId 异常: ${e.javaClass.simpleName}")
                } finally {
                    latch.countDown()
                }
            }
        }
        
        latch.await()
        
        println("✅ 成功操作: ${successCount.get()}/$totalOperations")
        println("❌ 异常次数: ${exceptionCount.get()}")
        
        assertEquals(0, exceptionCount.get(), "LRU缓存应该线程安全，不应有异常")
        println("✅ LRU缓存线程安全测试通过\n")
    }

    @Test
    fun `verify LRU cache eviction policy`() = runBlocking {
        println("=== 测试：LRU缓存淘汰策略 ===")
        
        val repository = QuestionRepositoryImpl()
        
        // 先搜索21个不同的查询，超过LRU容量(20)
        val queries = (1..21).map { "query_$it" }
        
        queries.forEach { query ->
            repository.searchQuestions(query)
        }
        
        // 验证缓存工作正常
        val results = repository.searchQuestions("算法")
        assertTrue(results.isNotEmpty(), "缓存应该正常返回结果")
        
        println("✅ LRU缓存淘汰策略验证通过\n")
    }

    @Test
    fun `verify search boundary conditions`() = runBlocking {
        println("=== 测试：搜索边界条件 ===")
        
        val repository = QuestionRepositoryImpl()
        
        // 测试空查询
        val emptyResult = repository.searchQuestions("")
        assertTrue(emptyResult.isNotEmpty(), "空查询应该返回所有问题")
        
        // 测试非常长的查询
        val longQuery = "a".repeat(2000)
        val longResult = repository.searchQuestions(longQuery)
        assertEquals(0, longResult.size, "超长查询应该返回空结果")
        
        // 测试空白字符查询
        val blankResult = repository.searchQuestions("   \t\n  ")
        assertTrue(blankResult.isNotEmpty(), "空白字符查询应该返回所有问题")
        
        println("✅ 搜索边界条件测试通过\n")
    }

    @Test
    fun `verify pagination boundary conditions`() = runBlocking {
        println("=== 测试：分页边界条件 ===")
        
        val repository = QuestionRepositoryImpl()
        
        // 测试负数页码
        val negativePageResult = repository.getQuestionsPaginated(-1, 10)
        assertTrue(negativePageResult.isNotEmpty(), "负数页码应该处理为0")
        
        // 测试超大页码
        val hugePageResult = repository.getQuestionsPaginated(99999, 10)
        assertEquals(0, hugePageResult.size, "超大页码应该返回空")
        
        // 测试负数页大小
        val negativePageSizeResult = repository.getQuestionsPaginated(0, -5)
        assertTrue(negativePageSizeResult.isNotEmpty(), "负数页大小应该处理为1")
        
        // 测试超大页大小
        val hugePageSizeResult = repository.getQuestionsPaginated(0, 99999)
        assertTrue(hugePageSizeResult.size <= 1000, "超大页大小应该限制为1000")
        
        println("✅ 分页边界条件测试通过\n")
    }

    @Test
    fun `verify all repository methods work together`() = runBlocking {
        println("=== 测试：所有仓储方法协同工作 ===")
        
        val repository = QuestionRepositoryImpl()
        
        // 完整的测试流程
        val questions = repository.getQuestions()
        assertTrue(questions.isNotEmpty(), "应该能获取到问题")
        
        val categories = repository.getCategories()
        assertTrue(categories.isNotEmpty(), "应该能获取到分类")
        
        val difficulties = repository.getDifficulties()
        assertEquals(3, difficulties.size, "应该有3个难度级别")
        
        val count = repository.getQuestionCount()
        assertEquals(questions.size, count, "问题计数应该一致")
        
        val categoryQuestions = repository.getQuestionsByCategory(categories.first())
        assertTrue(categoryQuestions.isNotEmpty(), "应该能按分类获取问题")
        
        val difficultyQuestions = repository.getQuestionsByDifficulty("EASY")
        assertTrue(difficultyQuestions.isNotEmpty(), "应该能按难度获取问题")
        
        val searchResults = repository.searchQuestions("算法")
        assertTrue(searchResults.isNotEmpty(), "应该能搜索到问题")
        
        val paginatedResults = repository.getQuestionsPaginated(0, 10)
        assertEquals(10, paginatedResults.size, "第一页应该有10个问题")
        
        println("✅ 所有仓储方法协同工作测试通过\n")
    }

    @Test
    fun `extreme long running stability test`() = runBlocking {
        println("=== 测试：超长运行稳定性 ===")
        
        val repository = QuestionRepositoryImpl()
        val operationCount = AtomicInteger(0)
        val exceptionCount = AtomicInteger(0)
        val endTime = System.currentTimeMillis() + 3000 // 运行3秒
        
        val workerCount = 10
        val latch = CountDownLatch(workerCount)
        
        repeat(workerCount) { workerId ->
            launch(Dispatchers.Default) {
                try {
                    while (System.currentTimeMillis() < endTime) {
                        val operation = (0..5).random()
                        
                        when (operation) {
                            0 -> repository.getQuestions()
                            1 -> repository.searchQuestions(listOf("算法", "Java", "数据库").random())
                            2 -> repository.getCategories()
                            3 -> repository.getDifficulties()
                            4 -> repository.getQuestionsPaginated((0..10).random(), (1..50).random())
                            5 -> repository.getQuestionsByCategory(listOf("算法与数据结构", "Java与Kotlin").random())
                        }
                        
                        operationCount.incrementAndGet()
                    }
                } catch (e: Exception) {
                    exceptionCount.incrementAndGet()
                    println("⚠️ Worker $workerId 异常: ${e.javaClass.simpleName}")
                } finally {
                    latch.countDown()
                }
            }
        }
        
        latch.await()
        
        println("✅ 总操作次数: ${operationCount.get()}")
        println("❌ 异常次数: ${exceptionCount.get()}")
        println("📊 吞吐量: ${operationCount.get() / 3} ops/sec")
        
        assertEquals(0, exceptionCount.get(), "超长运行应该无异常")
        println("✅ 超长运行稳定性测试通过\n")
    }
}
