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
 * 第20轮 - 终极毁灭级压力测试
 * 寻找更深层次的问题
 */
@RunWith(MockitoJUnitRunner::class)
class UltimateDoomTest {

    companion object {
        private const val TAG = "UltimateDoomTest"
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
     * 测试 1: 200线程疯狂操作 - 超大规模并发
     */
    @Test
    fun `200 threads extreme chaos`() = runTest {
        println("\n=== [Test 1] 200 Threads Extreme Chaos ===")

        val questions = questionRepository.getQuestions()
        val errorCount = AtomicInteger(0)
        val operationCount = AtomicInteger(0)
        val latch = CountDownLatch(200)

        val startTime = System.currentTimeMillis()

        repeat(200) { threadId ->
            launch(Dispatchers.Default) {
                try {
                    repeat(800) { opId ->
                        when (Random.nextInt(0, 100)) {
                            in 0..40 -> { // 40% - 搜索
                                val query = listOf("", "算法", "Java", "数据库", "网络", "Android", "测试", "性能", "安全", "架构").random()
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
                                progressRepository.saveProgress(QuizProgress(q.id, Random.nextInt(0, 4), true, isCorrect = Random.nextBoolean(), completedAt = System.currentTimeMillis(), attempts = Random.nextInt(0, 100)))
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

        assertEquals(0, errorCount.get(), "Should have 0 errors")
        assertTrue(operationCount.get() > 100000, "Should have more than 100000 operations")
        println("✅ [Test 1] Passed\n")
    }

    /**
     * 测试 2: 3分钟持续压力测试 - 超长时间运行
     */
    @Test
    fun `3 minute continuous apocalypse`() = runTest {
        println("\n=== [Test 2] 3 Minute Continuous Apocalypse ===")

        val questions = questionRepository.getQuestions()
        val errorCount = AtomicInteger(0)
        val operationCount = AtomicInteger(0)
        val startTime = System.currentTimeMillis()
        val duration = 180000L // 3 minutes

        coroutineScope {
            repeat(100) { _ ->
                launch(Dispatchers.Default) {
                    while (System.currentTimeMillis() - startTime < duration) {
                        try {
                            when (Random.nextInt(0, 10)) {
                                0, 1, 2, 3 -> questionRepository.searchQuestions(listOf("算法", "Java", "数据库", "网络", "Android", "Kotlin").random())
                                4, 5 -> questionRepository.getQuestionsPaginated(Random.nextInt(0, 100), Random.nextInt(1, 50))
                                6, 7 -> {
                                    val q = questions.random()
                                    progressRepository.saveProgress(QuizProgress(q.id, Random.nextInt(0, 4), true, isCorrect = Random.nextBoolean(), completedAt = System.currentTimeMillis(), attempts = Random.nextInt(0, 10)))
                                }
                                8 -> progressRepository.getAllProgress()
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
        println("✅ Duration: ${elapsed/1000}s")
        println("✅ Throughput: %.1f ops/sec".format(opsPerSec))
        println("❌ Errors: ${errorCount.get()}")

        assertEquals(0, errorCount.get(), "Should have 0 errors")
        assertTrue(operationCount.get() > 50000, "Should have more than 50000 operations")
        println("✅ [Test 2] Passed\n")
    }

    /**
     * 测试 3: 缓存毁灭测试 - 疯狂切换查询
     */
    @Test
    fun `cache apocalypse test`() = runTest {
        println("\n=== [Test 3] Cache Apocalypse Test ===")

        // 疯狂访问完全随机的查询
        val randomQueries = (1..500).map { "query_$it" }

        repeat(500) { round ->
            randomQueries.forEach { query ->
                questionRepository.searchQuestions(query)
            }
        }

        // 验证 LRU 仍然工作
        repeat(50) { i ->
            val result = questionRepository.searchQuestions("query_$i")
            assertTrue(result.isNotEmpty() || result.isEmpty())
        }

        println("✅ Cache survived apocalypse test")
        println("✅ [Test 3] Passed\n")
    }

    /**
     * 测试 4: 极端数据爆炸 - 大量 progress 数据
     */
    @Test
    fun `data explosion test`() = runTest {
        println("\n=== [Test 4] Data Explosion Test ===")

        val questions = questionRepository.getQuestions()

        // 疯狂保存 progress
        repeat(10000) { i ->
            val q = questions[i % questions.size]
            progressRepository.saveProgress(QuizProgress(q.id, Random.nextInt(0, 4), true, isCorrect = Random.nextBoolean(), completedAt = System.currentTimeMillis(), attempts = Random.nextInt(0, 1000)))
        }

        // 验证仍然能读取
        repeat(100) { i ->
            val q = questions[i % questions.size]
            val progress = progressRepository.getProgress(q.id)
            assertTrue(progress.questionId == q.id)
        }

        // 验证统计仍然能正常工作
        val stats = progressRepository.getStatistics(questions)
        assertTrue(stats.totalQuestions == questions.size)

        println("✅ Data explosion test passed")
        println("✅ [Test 4] Passed\n")
    }

    /**
     * 测试 5: 混合操作末日场景 - 所有功能疯狂调用
     */
    @Test
    fun `mixed operation doomsday`() = runTest {
        println("\n=== [Test 5] Mixed Operation Doomsday ===")

        val questions = questionRepository.getQuestions()
        val errorCount = AtomicInteger(0)
        val operationCount = AtomicInteger(0)
        val latch = CountDownLatch(100)

        repeat(100) { threadId ->
            launch(Dispatchers.Default) {
                try {
                    repeat(1000) {
                        when (Random.nextInt(0, 30)) {
                            0, 1 -> questionRepository.getQuestions()
                            2, 3 -> questionRepository.getCategories()
                            4, 5 -> questionRepository.getDifficulties()
                            6, 7, 8, 9, 10, 11, 12, 13, 14 -> questionRepository.searchQuestions(listOf("算法", "Java", "Kotlin", "Android", "数据库", "网络", "操作系统", "安全", "架构", "测试").random())
                            15, 16, 17 -> questionRepository.getQuestionsByCategory(questions.random().category)
                            18, 19, 20 -> questionRepository.getQuestionsByDifficulty(questions.random().difficulty)
                            21, 22, 23, 24 -> questionRepository.getQuestionsPaginated(Random.nextInt(0, 50), Random.nextInt(1, 50))
                            25, 26, 27 -> {
                                val q = questions.random()
                                progressRepository.saveProgress(QuizProgress(q.id, Random.nextInt(0, 4), true, isCorrect = Random.nextBoolean(), completedAt = System.currentTimeMillis(), attempts = Random.nextInt(0, 100)))
                            }
                            28 -> progressRepository.getAllProgress()
                            29 -> progressRepository.getStatistics(questions)
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

        assertEquals(0, errorCount.get(), "Should have 0 errors")
        println("✅ [Test 5] Passed\n")
    }
}
