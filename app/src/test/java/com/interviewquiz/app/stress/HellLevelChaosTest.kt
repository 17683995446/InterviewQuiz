package com.interviewquiz.app.stress

import com.interviewquiz.app.data.QuestionRepositoryImpl
import com.interviewquiz.app.domain.model.Difficulty
import com.interviewquiz.app.domain.model.QuizProgress
import com.interviewquiz.app.ui.screens.HomeUiState
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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 第十四轮 - 地狱级混乱场景测试
 * 测试内容：
 * 1. 极限API调用验证 - 重复API调用测试
 * 2. 边界值地狱测试 - 各种极端参数
 * 3. 内存压力测试 - 大量对象创建
 * 4. 最终回归测试 - 全链路验证
 */
class HellLevelChaosTest {

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
     * 测试 1: 极限API调用 - 方法重复调用测试
     */
    @Test
    fun `test extreme API method repetition`() = runTest {
        println("=== 地狱测试 1: 极限API调用验证 ===")
        
        val repository = QuestionRepositoryImpl()
        val successCount = AtomicInteger(0)
        val exceptionCount = AtomicInteger(0)
        
        // 对同一方法进行10,000次重复调用
        repeat(10000) {
            try {
                repository.getQuestions()
                repository.getCategories()
                repository.getDifficulties()
                repository.getQuestionCount()
                successCount.incrementAndGet()
            } catch (e: Exception) {
                exceptionCount.incrementAndGet()
            }
        }
        
        println("✅ 成功循环: ${successCount.get()}")
        println("❌ 异常次数: ${exceptionCount.get()}")
        
        assertEquals(0, exceptionCount.get(), "API调用不应该有异常")
        println("✅ 极限API调用验证通过\n")
    }

    /**
     * 测试 2: 边界值地狱测试 - 极端参数
     */
    @Test
    fun `test boundary values hell test`() = runTest {
        println("=== 地狱测试 2: 边界值地狱测试 ===")
        
        val repository = QuestionRepositoryImpl()
        val successCount = AtomicInteger(0)
        val exceptionCount = AtomicInteger(0)
        
        // 测试各种极端搜索查询
        val extremeQueries = listOf(
            "", " ", "a", "A",
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", // 50个A
            "算法", "Java", "数据库", "网络",
            "\n", "\t", "\r", // 空白字符
            "1234567890", "!@#$%^&*()_+",
            "测试", "测"
        )
        
        extremeQueries.forEach { query ->
            try {
                val results = repository.searchQuestions(query)
                assertNotNull(results)
                successCount.incrementAndGet()
            } catch (e: Exception) {
                exceptionCount.incrementAndGet()
                println("⚠️ 查询 '$query' 出现异常: ${e.message}")
            }
        }
        
        // 测试分页边界值
        val pageValues = listOf(-100, -1, 0, 1, 100, 1000, 99999)
        val pageSizeValues = listOf(-100, -1, 0, 1, 10, 100, 1000, 10000)
        
        pageValues.forEach { page ->
            pageSizeValues.forEach { pageSize ->
                try {
                    val results = repository.getQuestionsPaginated(page, pageSize)
                    assertNotNull(results)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    exceptionCount.incrementAndGet()
                }
            }
        }
        
        println("✅ 成功边界测试: ${successCount.get()}")
        println("❌ 异常次数: ${exceptionCount.get()}")
        
        assertEquals(0, exceptionCount.get(), "边界值测试不应该有异常")
        println("✅ 边界值地狱测试通过\n")
    }

    /**
     * 测试 3: 方法幂等性测试 - 重复调用同一方法
     */
    @Test
    fun `test method idempotency`() = runTest {
        println("=== 地狱测试 3: 方法幂等性测试 ===")
        
        val repository = QuestionRepositoryImpl()
        
        // 多次调用同一方法，验证结果一致
        val questions1 = repository.getQuestions()
        val questions2 = repository.getQuestions()
        val questions3 = repository.getQuestions()
        
        assertEquals(questions1.size, questions2.size)
        assertEquals(questions2.size, questions3.size)
        
        val categories1 = repository.getCategories()
        val categories2 = repository.getCategories()
        assertEquals(categories1, categories2)
        
        println("✅ 多次调用返回一致结果")
        
        // 测试搜索幂等性
        val query = "算法"
        val result1 = repository.searchQuestions(query)
        val result2 = repository.searchQuestions(query)
        val result3 = repository.searchQuestions(query)
        
        assertEquals(result1.size, result2.size)
        assertEquals(result2.size, result3.size)
        
        println("✅ 搜索幂等性验证通过\n")
    }

    /**
     * 测试 4: 内存压力测试 - 大量对象创建
     */
    @Test
    fun `test memory pressure test`() = runTest {
        println("=== 地狱测试 4: 内存压力测试 ===")
        
        val repository = QuestionRepositoryImpl()
        val successCount = AtomicInteger(0)
        val allResults = mutableListOf<List<*>>()
        
        try {
            // 创建大量临时对象
            repeat(1000) { iteration ->
                // 反复获取问题列表
                val questions = repository.getQuestions()
                allResults.add(questions)
                
                // 反复获取分类
                val categories = repository.getCategories()
                allResults.add(categories)
                
                // 进行一些搜索
                val searchResults = repository.searchQuestions("${iteration % 10}")
                allResults.add(searchResults)
                
                successCount.incrementAndGet()
                
                if (iteration % 100 == 0) {
                    println("🔄 已完成 $iteration 次循环")
                }
            }
            
            println("✅ 成功创建 ${allResults.size} 个对象列表")
            println("✅ 内存压力测试通过\n")
            
        } finally {
            // 清理
            allResults.clear()
        }
    }

    /**
     * 测试 5: 最终回归测试 - 全链路验证
     */
    @Test
    fun `test final regression - full pipeline`() = runTest {
        println("=== 地狱测试 5: 最终回归测试 - 全链路验证 ===")
        
        val repository = QuestionRepositoryImpl()
        var allPassed = true
        
        try {
            // 1. 获取问题列表
            println("📍 步骤 1/5: 获取问题列表")
            val questions = repository.getQuestions()
            assertTrue(questions.isNotEmpty())
            
            // 2. 获取分类
            println("📍 步骤 2/5: 获取分类")
            val categories = repository.getCategories()
            assertTrue(categories.isNotEmpty())
            
            // 3. 按分类获取问题
            println("📍 步骤 3/5: 按分类获取问题")
            val firstCategory = categories.first()
            val categoryQuestions = repository.getQuestionsByCategory(firstCategory)
            assertTrue(categoryQuestions.isNotEmpty())
            
            // 4. 按难度获取问题
            println("📍 步骤 4/5: 按难度获取问题")
            val difficultyQuestions = repository.getQuestionsByDifficulty("EASY")
            assertTrue(difficultyQuestions.isNotEmpty())
            
            // 5. 搜索问题
            println("📍 步骤 5/5: 搜索问题")
            val searchResults = repository.searchQuestions("算法")
            assertNotNull(searchResults)
            
            println("✅ 全链路回归测试通过\n")
            
        } catch (e: Exception) {
            allPassed = false
            println("❌ 全链路回归测试失败: ${e.message}")
            throw e
        }
        
        assertTrue(allPassed, "全链路回归测试应该完全通过")
    }
}
